package com.campusconnect.feature.splash

import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.campusconnect.MainActivity
import com.campusconnect.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Android Core SplashScreen library compatibility support
        installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        runRevealSequence()
    }

    private fun runRevealSequence() {
        binding.ivLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(100)
            .setDuration(600)
            .setInterpolator(OvershootInterpolator(1.2f))
            .start()

        binding.tvAppName.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(450)
            .setDuration(500)
            .start()

        binding.tvTagline.animate()
            .alpha(1f)
            .setStartDelay(650)
            .setDuration(500)
            .withEndAction { navigateNext() }
            .start()
    }

    private fun navigateNext() {
        lifecycleScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            val startDest = if (user == null) {
                "login"
            } else {
                try {
                    val doc = firestore.collection("users").document(user.uid).get().await()
                    val isComplete = doc.getBoolean("profileComplete") ?: false
                    if (isComplete) "home" else "profile_completion"
                } catch (e: Exception) {
                    e.printStackTrace()
                    "home"
                }
            }

            delay(300)

            val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                putExtra("EXTRA_START_DESTINATION", startDest)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}
