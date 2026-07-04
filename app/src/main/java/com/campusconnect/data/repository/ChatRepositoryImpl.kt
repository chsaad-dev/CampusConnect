package com.campusconnect.data.repository

import android.net.Uri
import com.campusconnect.core.common.Constants
import com.campusconnect.core.common.Resource
import com.campusconnect.data.remote.dto.UserDto
import com.campusconnect.domain.model.Chat
import com.campusconnect.domain.model.Message
import com.campusconnect.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ChatRepository {

    private val currentUid: String
        get() = auth.currentUser?.uid ?: throw Exception("User not logged in")

    override fun getChats(): Flow<Resource<List<Chat>>> = callbackFlow {
        trySend(Resource.Loading)
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Success(emptyList()))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(Constants.COLLECTION_CHATS)
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to listen to chats"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val chatsList = snapshot.toObjects(Chat::class.java)
                        .sortedByDescending { it.lastMessageAt }
                    trySend(Resource.Success(chatsList))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getMessages(chatId: String): Flow<Resource<List<Message>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection(Constants.COLLECTION_CHATS)
            .document(chatId)
            .collection(Constants.COLLECTION_MESSAGES)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to listen to messages"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                    trySend(Resource.Success(messages))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun sendMessage(chatId: String, text: String, mediaUri: Uri?, mediaType: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val uid = currentUid
            var uploadUrl = ""
            val messageId = firestore.collection(Constants.COLLECTION_CHATS)
                .document(chatId)
                .collection(Constants.COLLECTION_MESSAGES)
                .document().id

            // 1. Upload media if present
            if (mediaUri != null) {
                val extension = mediaUri.toString().substringAfterLast(".", "jpg")
                val filename = "${System.currentTimeMillis()}.$extension"
                val storageRef = storage.reference.child("chats/$chatId/media/$filename")
                storageRef.putFile(mediaUri).await()
                uploadUrl = storageRef.downloadUrl.await().toString()
            }

            val message = Message(
                messageId = messageId,
                senderId = uid,
                text = text,
                mediaUrl = uploadUrl,
                mediaType = if (uploadUrl.isNotEmpty()) mediaType else "none",
                seenBy = listOf(uid),
                createdAt = System.currentTimeMillis()
            )

            val batch = firestore.batch()

            // Write message doc
            val msgRef = firestore.collection(Constants.COLLECTION_CHATS)
                .document(chatId)
                .collection(Constants.COLLECTION_MESSAGES)
                .document(messageId)
            batch.set(msgRef, message)

            // Update parent chat doc
            val chatRef = firestore.collection(Constants.COLLECTION_CHATS).document(chatId)
            batch.update(
                chatRef,
                mapOf(
                    "lastMessage" to if (uploadUrl.isNotEmpty()) "Sent an attachment" else text,
                    "lastMessageAt" to message.createdAt,
                    "lastReadAt.$uid" to message.createdAt
                )
            )

            // Write notification log to recipient
            val recipientUid = chatId.split("_").firstOrNull { it != uid } ?: ""
            if (recipientUid.isNotEmpty()) {
                val notifRef = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                    .document(recipientUid)
                    .collection("items")
                    .document()

                val senderDoc = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
                val senderName = senderDoc.getString("name") ?: "CampusConnect User"

                val notifItem = com.campusconnect.domain.model.NotificationItem(
                    notifId = notifRef.id,
                    title = "New message from $senderName",
                    body = if (uploadUrl.isNotEmpty()) "Sent an attachment" else text,
                    type = "chat_message",
                    refId = chatId,
                    createdAt = message.createdAt
                )
                batch.set(notifRef, notifItem)
            }

            batch.commit().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to send message"))
        }
    }

    override fun getOrCreateChat(targetUid: String): Flow<Resource<Chat>> = flow {
        emit(Resource.Loading)
        try {
            val uid = currentUid
            val chatId = if (uid < targetUid) "${uid}_${targetUid}" else "${targetUid}_${uid}"
            val chatRef = firestore.collection(Constants.COLLECTION_CHATS).document(chatId)
            val doc = chatRef.get().await()

            if (doc.exists()) {
                val chat = doc.toObject(Chat::class.java)!!
                emit(Resource.Success(chat))
            } else {
                val senderDoc = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
                val receiverDoc = firestore.collection(Constants.COLLECTION_USERS).document(targetUid).get().await()

                val sender = senderDoc.toObject(UserDto::class.java) ?: throw Exception("Sender profile not found")
                val receiver = receiverDoc.toObject(UserDto::class.java) ?: throw Exception("Receiver profile not found")

                val newChat = Chat(
                    chatId = chatId,
                    participants = listOf(uid, targetUid),
                    participantNames = mapOf(uid to sender.name, targetUid to receiver.name),
                    participantUsernames = mapOf(uid to sender.uniqueUsername, targetUid to receiver.uniqueUsername),
                    participantPhotoUrls = mapOf(uid to sender.photoUrl, targetUid to receiver.photoUrl),
                    lastMessage = "",
                    lastMessageAt = System.currentTimeMillis()
                )

                chatRef.set(newChat).await()
                emit(Resource.Success(newChat))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to open or create chat"))
        }
    }

    override fun updateTypingStatus(chatId: String, isTyping: Boolean): Flow<Resource<Unit>> = flow {
        try {
            firestore.collection(Constants.COLLECTION_CHATS)
                .document(chatId)
                .collection("typing")
                .document(currentUid)
                .set(mapOf("isTyping" to isTyping))
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update typing status"))
        }
    }

    override fun listenToTypingStatus(chatId: String): Flow<Map<String, Boolean>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_CHATS)
            .document(chatId)
            .collection("typing")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val typingMap = mutableMapOf<String, Boolean>()
                    for (doc in snapshot.documents) {
                        val isTyping = doc.getBoolean("isTyping") ?: false
                        typingMap[doc.id] = isTyping
                    }
                    trySend(typingMap)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun markMessagesAsSeen(chatId: String): Flow<Resource<Unit>> = flow {
        try {
            val uid = currentUid
            val messagesRef = firestore.collection(Constants.COLLECTION_CHATS)
                .document(chatId)
                .collection(Constants.COLLECTION_MESSAGES)
            
            val snapshot = messagesRef
                .whereNotEqualTo("senderId", uid)
                .get()
                .await()

            val batch = firestore.batch()
            var updated = false
            for (doc in snapshot.documents) {
                val message = doc.toObject(Message::class.java)
                if (message != null && !message.seenBy.contains(uid)) {
                    batch.update(doc.reference, "seenBy", FieldValue.arrayUnion(uid))
                    updated = true
                }
            }
            if (updated) {
                batch.commit().await()
            }
            
            // Also update lastReadAt on parent chat doc
            firestore.collection(Constants.COLLECTION_CHATS)
                .document(chatId)
                .update("lastReadAt.$uid", System.currentTimeMillis())
                .await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to mark messages as seen"))
        }
    }

    override fun getTotalUnreadCount(): Flow<Int> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(Constants.COLLECTION_CHATS)
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(0)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    var totalUnread = 0
                    for (doc in snapshot.documents) {
                        val chat = doc.toObject(Chat::class.java)
                        if (chat != null) {
                            val lastMessageAt = chat.lastMessageAt
                            val lastReadAt = chat.lastReadAt[uid] ?: 0L
                            if (lastMessageAt > lastReadAt) {
                                totalUnread++
                            }
                        }
                    }
                    trySend(totalUnread)
                }
            }
        awaitClose { listener.remove() }
    }
}
