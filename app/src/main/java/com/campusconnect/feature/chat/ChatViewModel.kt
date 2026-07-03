package com.campusconnect.feature.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.Chat
import com.campusconnect.domain.model.Message
import com.campusconnect.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val currentUid: String
        get() = auth.currentUser?.uid ?: ""

    private val _chats = MutableStateFlow<Resource<List<Chat>>>(Resource.Loading)
    val chats: StateFlow<Resource<List<Chat>>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<Resource<List<Message>>>(Resource.Loading)
    val messages: StateFlow<Resource<List<Message>>> = _messages.asStateFlow()

    private val _currentChat = MutableStateFlow<Resource<Chat>?>(null)
    val currentChat: StateFlow<Resource<Chat>?> = _currentChat.asStateFlow()

    private val _typingStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val typingStatus: StateFlow<Map<String, Boolean>> = _typingStatus.asStateFlow()

    fun loadChats() {
        chatRepository.getChats().onEach { result ->
            _chats.value = result
        }.launchIn(viewModelScope)
    }

    fun getOrCreateChat(targetUid: String) {
        chatRepository.getOrCreateChat(targetUid).onEach { result ->
            _currentChat.value = result
        }.launchIn(viewModelScope)
    }

    fun loadMessages(chatId: String) {
        chatRepository.getMessages(chatId).onEach { result ->
            _messages.value = result
            if (result is Resource.Success) {
                markMessagesAsSeen(chatId)
            }
        }.launchIn(viewModelScope)
    }

    fun sendMessage(chatId: String, text: String, mediaUri: Uri?, mediaType: String) {
        chatRepository.sendMessage(chatId, text, mediaUri, mediaType).onEach { result ->
            // Optionally handle message send result (e.g. error callback)
        }.launchIn(viewModelScope)
    }

    fun updateTypingStatus(chatId: String, isTyping: Boolean) {
        chatRepository.updateTypingStatus(chatId, isTyping).launchIn(viewModelScope)
    }

    fun listenToTypingStatus(chatId: String) {
        chatRepository.listenToTypingStatus(chatId).onEach { typingMap ->
            _typingStatus.value = typingMap
        }.launchIn(viewModelScope)
    }

    private fun markMessagesAsSeen(chatId: String) {
        chatRepository.markMessagesAsSeen(chatId).launchIn(viewModelScope)
    }

    fun resetChatState() {
        _currentChat.value = null
        _messages.value = Resource.Loading
        _typingStatus.value = emptyMap()
    }
}
