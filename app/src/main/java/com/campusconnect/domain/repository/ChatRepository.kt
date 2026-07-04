package com.campusconnect.domain.repository

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.Chat
import com.campusconnect.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(): Flow<Resource<List<Chat>>>
    fun getMessages(chatId: String): Flow<Resource<List<Message>>>
    fun sendMessage(chatId: String, text: String, mediaUri: android.net.Uri?, mediaType: String): Flow<Resource<Unit>>
    fun getOrCreateChat(targetUid: String): Flow<Resource<Chat>>
    fun updateTypingStatus(chatId: String, isTyping: Boolean): Flow<Resource<Unit>>
    fun listenToTypingStatus(chatId: String): Flow<Map<String, Boolean>>
    fun markMessagesAsSeen(chatId: String): Flow<Resource<Unit>>
    fun getTotalUnreadCount(): Flow<Int>
}
