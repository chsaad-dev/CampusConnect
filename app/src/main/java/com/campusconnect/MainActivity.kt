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

    @Inject
    lateinit var chatRepository: com.campusconnect.domain.repository.ChatRepository

    @Inject
    lateinit var firestore: com.google.firebase.firestore.FirebaseFirestore

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
        startUnreadCountListener()
        startInAppNotificationListener()
        checkNotificationPermission()
        handleNotificationIntent(intent)

        com.google.firebase.auth.FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                setupFcmTokenRegistration()
                startNotificationListener()
            }
        }
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
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.createPostFragment) {
                val bottomSheet = com.campusconnect.feature.post.CreatePostBottomSheetFragment()
                bottomSheet.show(supportFragmentManager, "create_post")
                false
            } else {
                androidx.navigation.ui.NavigationUI.onNavDestinationSelected(item, navController)
            }
        }

        // Show/hide bottom nav based on current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.searchFragment, R.id.friendsFragment, R.id.chatListFragment, R.id.settingsFragment -> {
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
                            val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                            val targetUid = if (currentUid != null && notif.refId.contains("_")) {
                                notif.refId.split("_").firstOrNull { it != currentUid } ?: notif.refId
                            } else {
                                notif.refId
                            }

                            if (notif.type == "chat_message") {
                                val currentDest = navController.currentDestination?.id
                                if (currentDest == R.id.chatFragment) {
                                    val openTargetUid = navController.currentBackStackEntry?.arguments?.getString("targetUid")
                                    if (openTargetUid != null && openTargetUid == targetUid) {
                                        launch {
                                            notificationRepository.markAsRead(notif.notifId).collect {}
                                        }
                                        continue
                                    }
                                }
                            }

                            NotificationHelper.showNotification(
                                context = this@MainActivity,
                                title = notif.title,
                                body = notif.body,
                                type = notif.type,
                                refId = notif.refId
                            )

                            com.campusconnect.core.common.NotificationEventBus.postEvent(
                                notif.title,
                                notif.body,
                                notif.type,
                                notif.refId
                            )

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
            kotlinx.coroutines.delay(500)
            val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            val targetUid = if (currentUid != null && refId.contains("_")) {
                refId.split("_").firstOrNull { it != currentUid } ?: refId
            } else {
                refId
            }
            val bundle = Bundle().apply {
                putString("postId", refId)
                putString("complaintId", refId)
                putString("targetUid", targetUid)
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

    private fun startUnreadCountListener() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatRepository.getTotalUnreadCount().collectLatest { count ->
                    val badge = binding.bottomNavigation.getOrCreateBadge(R.id.chatListFragment)
                    badge.isVisible = count > 0
                    badge.number = count
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateOnlineStatus(true)
    }

    override fun onPause() {
        super.onPause()
        updateOnlineStatus(false)
    }

    private fun updateOnlineStatus(isOnline: Boolean) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            firestore.collection(com.campusconnect.core.common.Constants.COLLECTION_USERS)
                .document(uid)
                .update("isOnline", isOnline, "lastActiveAt", System.currentTimeMillis())
        }
    }

    private fun startInAppNotificationListener() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                com.campusconnect.core.common.NotificationEventBus.events.collectLatest { event ->
                    com.campusconnect.core.widgets.InAppNotificationManager.showNotification(
                        activity = this@MainActivity,
                        title = event.title,
                        body = event.body,
                        avatarUrl = "",
                        onClick = {
                            if ((event.type == "chat" || event.type == "chat_message") && event.refId.isNotEmpty()) {
                                val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                val targetUid = if (currentUid != null && event.refId.contains("_")) {
                                    event.refId.split("_").firstOrNull { it != currentUid } ?: event.refId
                                } else {
                                    event.refId
                                }
                                val bundle = Bundle().apply {
                                    putString("targetUid", targetUid)
                                    putString("targetName", "Chat")
                                }
                                navController.navigate(R.id.chatFragment, bundle)
                            } else if (event.type == "friend_request") {
                                navController.navigate(R.id.friendsFragment)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
