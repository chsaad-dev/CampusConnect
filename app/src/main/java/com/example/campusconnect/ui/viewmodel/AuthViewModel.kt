package com.example.campusconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.data.model.User
import com.example.campusconnect.data.repository.AuthRepository
import com.example.campusconnect.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableLiveData<Resource<User>>()
    val authState: LiveData<Resource<User>> = _authState

    private val _resetState = MutableLiveData<Resource<String>>()
    val resetState: LiveData<Resource<String>> = _resetState

    fun login(email: String, password: String) {
        authRepository.login(email, password).onEach { _authState.value = it }.launchIn(viewModelScope)
    }

    fun register(user: User, password: String) {
        authRepository.register(user, password).onEach { _authState.value = it }.launchIn(viewModelScope)
    }

    fun logout() = authRepository.logout()

    fun getCurrentUser() = authRepository.getCurrentUser()

    fun isUserLoggedIn() = authRepository.isUserLoggedIn()

    fun sendPasswordResetEmail(email: String) {
        authRepository.sendPasswordResetEmail(email).onEach { _resetState.value = it }.launchIn(viewModelScope)
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.loadCurrentUser()
        }
    }
}
