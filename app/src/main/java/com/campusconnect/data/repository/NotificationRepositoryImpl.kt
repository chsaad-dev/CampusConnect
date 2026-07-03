package com.campusconnect.data.repository

import com.campusconnect.core.common.Constants
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.NotificationItem
import com.campusconnect.domain.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    private val currentUid: String
        get() = auth.currentUser?.uid ?: throw Exception("User not logged in")

    override fun saveFcmToken(token: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                firestore.collection(Constants.COLLECTION_USERS)
                    .document(uid)
                    .update("fcmToken", token)
                    .await()
            }
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to save FCM token"))
        }
    }

    override fun subscribeToTopic(topic: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Topic subscription failed"))
        }
    }

    override fun unsubscribeFromTopic(topic: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Topic unsubscription failed"))
        }
    }

    override fun sendNotification(targetUid: String, notification: NotificationItem): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val itemsRef = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(targetUid)
                .collection("items")

            val notifId = itemsRef.document().id
            val finalNotification = notification.copy(
                notifId = notifId,
                createdAt = System.currentTimeMillis()
            )

            itemsRef.document(notifId).set(finalNotification).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to send notification log"))
        }
    }

    override fun getNotificationsStream(): Flow<Resource<List<NotificationItem>>> = callbackFlow {
        trySend(Resource.Loading)
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
            .document(uid)
            .collection("items")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to listen to notifications"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(NotificationItem::class.java)
                    trySend(Resource.Success(items))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun markAsRead(notifId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(currentUid)
                .collection("items")
                .document(notifId)
                .update("read", true)
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update notification"))
        }
    }
}
