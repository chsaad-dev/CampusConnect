package com.example.campusconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.data.model.Chat
import com.example.campusconnect.data.model.Message
import com.example.campusconnect.data.repository.ChatRepository
import com.example.campusconnect.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository
) : ViewModel() {
    private val _chats = MutableLiveData<Resource<List<Chat>>>()
    val chats: LiveData<Resource<List<Chat>>> = _chats
    
    private val _messages = MutableLiveData<Resource<List<Message>>>()
    val messages: LiveData<Resource<List<Message>>> = _messages

    private val _sendStatus = MutableLiveData<Resource<String>>()
    val sendStatus: LiveData<Resource<String>> = _sendStatus

    fun fetchChats(userId: String) {
        repo.getChats(userId).onEach { _chats.value = it }.launchIn(viewModelScope)
    }

    fun fetchMessages(chatId: String) {
        repo.getMessages(chatId).onEach { _messages.value = it }.launchIn(viewModelScope)
    }

    fun sendMessage(chatId: String, message: Message) {
        repo.sendMessage(chatId, message).onEach { _sendStatus.value = it }.launchIn(viewModelScope)
    }
}
