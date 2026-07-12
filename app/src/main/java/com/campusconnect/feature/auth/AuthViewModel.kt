package com.campusconnect.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.User
import com.campusconnect.domain.usecase.auth.CheckEmailVerifiedUseCase
import com.campusconnect.domain.usecase.auth.ForgotPasswordUseCase
import com.campusconnect.domain.usecase.auth.LoginUseCase
import com.campusconnect.domain.usecase.auth.LoginWithGoogleUseCase
import com.campusconnect.domain.usecase.auth.RegisterUseCase
import com.campusconnect.domain.usecase.user.GetCurrentUserUseCase
import com.campusconnect.domain.repository.AuthRepository
import com.campusconnect.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val registerUseCase: RegisterUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val checkEmailVerifiedUseCase: CheckEmailVerifiedUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<User>?>(null)
    val loginState: StateFlow<Resource<User>?> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<String>?>(null)
    val registerState: StateFlow<Resource<String>?> = _registerState.asStateFlow()

    private val _resetState = MutableStateFlow<Resource<Unit>?>(null)
    val resetState: StateFlow<Resource<Unit>?> = _resetState.asStateFlow()

    private val _verificationState = MutableStateFlow<Resource<Unit>?>(null)
    val verificationState: StateFlow<Resource<Unit>?> = _verificationState.asStateFlow()

    private val _profileCheckState = MutableStateFlow<Resource<Boolean>?>(null)
    val profileCheckState: StateFlow<Resource<Boolean>?> = _profileCheckState.asStateFlow()

    fun login(email: String, password: String) {
        loginUseCase(email, password).onEach { result ->
            _loginState.value = result
        }.launchIn(viewModelScope)
    }

    fun loginWithGoogle(idToken: String) {
        loginWithGoogleUseCase(idToken).onEach { result ->
            _loginState.value = result
        }.launchIn(viewModelScope)
    }

    fun register(email: String, password: String) {
        registerUseCase(email, password).onEach { result ->
            _registerState.value = result
        }.launchIn(viewModelScope)
    }

    fun sendPasswordResetEmail(email: String) {
        forgotPasswordUseCase(email).onEach { result ->
            _resetState.value = result
        }.launchIn(viewModelScope)
    }

    fun sendVerificationEmail() {
        checkEmailVerifiedUseCase.sendVerification().onEach { result ->
            _verificationState.value = result
        }.launchIn(viewModelScope)
    }

    fun isEmailVerified(): Boolean = checkEmailVerifiedUseCase.isVerified()

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun getCurrentUid(): String? = authRepository.getCurrentUid()

    fun checkProfileComplete() {
        val uid = authRepository.getCurrentUid() ?: return
        userRepository.isProfileComplete(uid).onEach { result ->
            _profileCheckState.value = result
        }.launchIn(viewModelScope)
    }

    fun logout() {
        authRepository.logout()
        _loginState.value = null
        _registerState.value = null
    }

    fun resetLoginState() { _loginState.value = null }
    fun resetRegisterState() { _registerState.value = null }
    fun resetPasswordState() { _resetState.value = null }
}
