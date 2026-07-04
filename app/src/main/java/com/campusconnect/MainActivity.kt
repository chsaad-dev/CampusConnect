package com.campusconnect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.campusconnect.core.common.NotificationHelper
import com.campusconnect.core.common.Resource
import com.campusconnect.core.theme.ThemeManager
import com.campusconnect.databinding.ActivityMainBinding
import com.campusconnect.domain.repository.NotificationRepository
import com.campusconnect.feature.auth.AuthViewModel
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val authViewModel: AuthViewModel by viewModels()

    private val sessionStartTime = System.currentTimeMillis()

    // Permission launcher for POST_NOTIFICATIONS (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, setup FCM
            setupFcmTokenRegistration()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        com.campusconnect.core.common.AnalyticsHelper.initialize(applicationContext)
        com.campusconnect.core.common.AnalyticsHelper.logEvent("app_session_start")

        setupNavigation()
        checkNotificationPermission()
        handleNotificationIntent(intent)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set start destination based on auth state
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        if (authViewModel.isLoggedIn()) {
            navGraph.setStartDestination(R.id.main_nav_graph)
            setupFcmTokenRegistration()
            startNotificationListener()
        } else {
            navGraph.setStartDestination(R.id.auth_nav_graph)
        }
        navController.graph = navGraph

        // Setup bottom navigation
        binding.bottomNavigation.setupWithNavController(navController)

        // Show/hide bottom nav based on current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.settingsFragment, R.id.friendsFragment, R.id.chatListFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.navShadow.visibility = View.VISIBLE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.navShadow.visibility = View.GONE
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                setupFcmTokenRegistration()
            }
        } else {
            setupFcmTokenRegistration()
        }
    }

    private fun setupFcmTokenRegistration() {
        if (authViewModel.isLoggedIn()) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    lifecycleScope.launch {
                        notificationRepository.saveFcmToken(token).collect {}
                    }
                }
            }
        }
    }

    private fun startNotificationListener() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationRepository.getNotificationsStream().collectLatest { resource ->
                    if (resource is Resource.Success) {
                        val unreadNotifs = resource.data.filter { !it.read && it.createdAt > sessionStartTime }
                        for (notif in unreadNotifs) {
                            // Show local push notification
                            NotificationHelper.showNotification(
                                context = this@MainActivity,
                                title = notif.title,
                                body = notif.body,
                                type = notif.type,
                                refId = notif.refId
                            )
                            // Mark as read in Firestore
                            launch {
                                notificationRepository.markAsRead(notif.notifId).collect {}
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val navigateTo = intent?.getStringExtra("navigate_to") ?: return
        val refId = intent.getStringExtra("refId") ?: ""

        lifecycleScope.launch {
            // Wait 500ms for navigation component graph setup if app was closed
            kotlinx.coroutines.delay(500)
            val bundle = Bundle().apply {
                putString("postId", refId)
                putString("complaintId", refId)
                putString("targetUid", refId)
                putString("targetName", intent.getStringExtra("title") ?: "Chat")
            }
            try {
                when (navigateTo) {
                    "chat_message" -> navController.navigate(R.id.chatFragment, bundle)
                    "friend_request" -> navController.navigate(R.id.friendsFragment)
                    "complaint_status" -> navController.navigate(R.id.complaintDetailFragment, bundle)
                    "note" -> navController.navigate(R.id.noteDetailFragment, bundle)
                    "blood_request" -> navController.navigate(R.id.bloodRequestDetailFragment, bundle)
                    "ride" -> navController.navigate(R.id.rideDetailFragment, bundle)
                }
            } catch (e: Exception) {
                // Navigation handling fallback
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
