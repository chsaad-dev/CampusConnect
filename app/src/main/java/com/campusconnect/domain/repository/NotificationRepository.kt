package com.campusconnect.domain.repository

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.NotificationItem
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun saveFcmToken(token: String): Flow<Resource<Unit>>
    fun subscribeToTopic(topic: String): Flow<Resource<Unit>>
    fun unsubscribeFromTopic(topic: String): Flow<Resource<Unit>>
    fun sendNotification(targetUid: String, notification: NotificationItem): Flow<Resource<Unit>>
    fun getNotificationsStream(): Flow<Resource<List<NotificationItem>>>
    fun markAsRead(notifId: String): Flow<Resource<Unit>>
}
