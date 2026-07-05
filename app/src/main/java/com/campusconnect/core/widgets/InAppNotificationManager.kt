package com.campusconnect.core.widgets

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import com.bumptech.glide.Glide
import com.campusconnect.R
import com.campusconnect.databinding.LayoutInAppNotificationBinding

object InAppNotificationManager {

    private val handler = Handler(Looper.getMainLooper())
    private var activeNotificationView: View? = null
    private var dismissRunnable: Runnable? = null

    fun showNotification(
        activity: Activity,
        title: String,
        body: String,
        avatarUrl: String,
        onClick: (() -> Unit)? = null,
        posActionText: String? = null,
        negActionText: String? = null,
        onPosAction: (() -> Unit)? = null,
        onNegAction: (() -> Unit)? = null
    ) {
        handler.post {
            // Dismiss current notification if any
            dismissActiveNotification()

            val decorView = activity.window?.decorView as? ViewGroup ?: return@post
            val inflater = LayoutInflater.from(activity)
            val binding = LayoutInAppNotificationBinding.inflate(inflater, decorView, false)
            val notificationView = binding.root

            binding.tvNotifTitle.text = title
            binding.tvNotifBody.text = body

            Glide.with(activity)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivSenderAvatar)

            // Setup buttons
            if (posActionText != null || negActionText != null) {
                binding.layoutActions.visibility = View.VISIBLE
                if (posActionText != null) {
                    binding.btnActionPos.text = posActionText
                    binding.btnActionPos.setOnClickListener {
                        onPosAction?.invoke()
                        dismissActiveNotification()
                    }
                    binding.btnActionPos.visibility = View.VISIBLE
                } else {
                    binding.btnActionPos.visibility = View.GONE
                }

                if (negActionText != null) {
                    binding.btnActionNeg.text = negActionText
                    binding.btnActionNeg.setOnClickListener {
                        onNegAction?.invoke()
                        dismissActiveNotification()
                    }
                    binding.btnActionNeg.visibility = View.VISIBLE
                } else {
                    binding.btnActionNeg.visibility = View.GONE
                }
            } else {
                binding.layoutActions.visibility = View.GONE
            }

            binding.btnClose.setOnClickListener {
                dismissActiveNotification()
            }

            if (onClick != null) {
                binding.cardNotification.setOnClickListener {
                    onClick.invoke()
                    dismissActiveNotification()
                }
            }

            // Slide down animation
            notificationView.visibility = View.INVISIBLE
            decorView.addView(notificationView)

            notificationView.post {
                val height = notificationView.height.toFloat()
                notificationView.translationY = -height - 100f
                notificationView.visibility = View.VISIBLE
                notificationView.animate()
                    .translationY(0f)
                    .setDuration(400)
                    .setInterpolator(OvershootInterpolator(1.0f))
                    .start()
            }

            activeNotificationView = notificationView

            // Auto dismiss after 5 seconds
            val runnable = Runnable {
                dismissActiveNotification()
            }
            dismissRunnable = runnable
            handler.postDelayed(runnable, 5000)
        }
    }

    private fun dismissActiveNotification() {
        activeNotificationView?.let { view ->
            val parent = view.parent as? ViewGroup
            dismissRunnable?.let { handler.removeCallbacks(it) }
            dismissRunnable = null

            view.animate()
                .translationY(-view.height.toFloat() - 100f)
                .setDuration(300)
                .withEndAction {
                    parent?.removeView(view)
                }
                .start()

            activeNotificationView = null
        }
    }
}
