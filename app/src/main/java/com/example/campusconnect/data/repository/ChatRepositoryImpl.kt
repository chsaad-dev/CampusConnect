package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Chat
import com.example.campusconnect.data.model.Message
import com.example.campusconnect.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override fun createChat(chat: Chat): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val id = firestore.collection("chats").document().id
            val newChat = chat.copy(id = id)
            firestore.collection("chats").document(id).set(newChat).await()
            trySend(Resource.Success(id))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to create chat"))
        }
        awaitClose()
    }

    override fun getChats(userId: String): Flow<Resource<List<Chat>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching chats"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val chats = snapshot.toObjects(Chat::class.java)
                    trySend(Resource.Success(chats))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getMessages(chatId: String): Flow<Resource<List<Message>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching messages"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                    trySend(Resource.Success(messages))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun sendMessage(chatId: String, message: Message): Flow<Resource<String>> = callbackFlow {
        try {
            val messageId = firestore.collection("chats").document(chatId)
                .collection("messages").document().id
            val newMessage = message.copy(id = messageId)
            
            firestore.collection("chats").document(chatId)
                .collection("messages").document(messageId).set(newMessage).await()
            
            firestore.collection("chats").document(chatId).update(
                "lastMessage", message.text,
                "lastMessageTimestamp", message.timestamp
            ).await()
            
            trySend(Resource.Success("Message sent"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to send message"))
        }
        awaitClose()
    }
}
