package com.campusconnect.data.remote

import android.util.Log
import com.campusconnect.core.common.NotificationHelper
import com.campusconnect.domain.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        serviceScope.launch {
            notificationRepository.saveFcmToken(token).collect {}
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received: ${message.data}")

        val title = message.notification?.title ?: message.data["title"] ?: "CampusConnect"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = message.data["type"] ?: "general"
        val refId = message.data["refId"] ?: ""

        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            body = body,
            type = type,
            refId = refId
        )

        com.campusconnect.core.common.NotificationEventBus.postEvent(title, body, type, refId)
    }

    companion object {
        private const val TAG = "FCMService"
    }
}
