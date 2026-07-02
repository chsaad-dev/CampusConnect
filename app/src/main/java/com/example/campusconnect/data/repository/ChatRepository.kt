package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Chat
import com.example.campusconnect.data.model.Message
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun createChat(chat: Chat): Flow<Resource<String>>
    fun getChats(userId: String): Flow<Resource<List<Chat>>>
    fun getMessages(chatId: String): Flow<Resource<List<Message>>>
    fun sendMessage(chatId: String, message: Message): Flow<Resource<String>>
}
