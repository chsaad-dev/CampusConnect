package com.example.campusconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.data.model.User
import com.example.campusconnect.data.repository.UserRepository
import com.example.campusconnect.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: UserRepository
) : ViewModel() {
    private val _userProfile = MutableLiveData<Resource<User>>()
    val userProfile: LiveData<Resource<User>> = _userProfile

    private val _updateStatus = MutableLiveData<Resource<String>>()
    val updateStatus: LiveData<Resource<String>> = _updateStatus

    fun fetchUserProfile(userId: String) {
        repo.getUserById(userId).onEach { _userProfile.value = it }.launchIn(viewModelScope)
    }

    fun updateProfile(user: User) {
        repo.updateUser(user).onEach { _updateStatus.value = it }.launchIn(viewModelScope)
    }
}
