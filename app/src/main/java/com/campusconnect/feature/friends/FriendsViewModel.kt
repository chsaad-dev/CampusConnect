package com.campusconnect.feature.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.FriendRequest
import com.campusconnect.domain.model.User
import com.campusconnect.domain.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendRepository: FriendRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<Resource<List<User>>>(Resource.Success(emptyList()))
    val searchResults: StateFlow<Resource<List<User>>> = _searchResults.asStateFlow()

    private val _pendingRequests = MutableStateFlow<Resource<List<FriendRequest>>>(Resource.Loading)
    val pendingRequests: StateFlow<Resource<List<FriendRequest>>> = _pendingRequests.asStateFlow()

    private val _friendsList = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val friendsList: StateFlow<Resource<List<User>>> = _friendsList.asStateFlow()

    // Map to keep track of checked user friendship status
    private val _statuses = MutableStateFlow<Map<String, String>>(emptyMap())
    val statuses: StateFlow<Map<String, String>> = _statuses.asStateFlow()

    fun searchUsers(query: String) {
        friendRepository.searchUsers(query).onEach { result ->
            _searchResults.value = result
            if (result is Resource.Success) {
                // Check status for each found user
                result.data.forEach { user ->
                    checkStatus(user.uid)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun loadFriends() {
        friendRepository.getFriends().onEach { result ->
            _friendsList.value = result
        }.launchIn(viewModelScope)
    }

    fun loadPendingRequests() {
        friendRepository.getPendingRequests().onEach { result ->
            _pendingRequests.value = result
        }.launchIn(viewModelScope)
    }

    fun sendFriendRequest(user: User) {
        friendRepository.sendFriendRequest(user).onEach { result ->
            if (result is Resource.Success) {
                // Update local status map
                val updated = _statuses.value.toMutableMap()
                updated[user.uid] = "pending_sent"
                _statuses.value = updated
            }
        }.launchIn(viewModelScope)
    }

    fun acceptFriendRequest(request: FriendRequest) {
        friendRepository.acceptFriendRequest(request).onEach { result ->
            if (result is Resource.Success) {
                loadPendingRequests()
                loadFriends()
            }
        }.launchIn(viewModelScope)
    }

    fun rejectFriendRequest(request: FriendRequest) {
        friendRepository.rejectFriendRequest(request).onEach { result ->
            if (result is Resource.Success) {
                loadPendingRequests()
            }
        }.launchIn(viewModelScope)
    }

    private fun checkStatus(targetUid: String) {
        friendRepository.checkFriendshipStatus(targetUid).onEach { result ->
            if (result is Resource.Success) {
                val updated = _statuses.value.toMutableMap()
                updated[targetUid] = result.data
                _statuses.value = updated
            }
        }.launchIn(viewModelScope)
    }
}
