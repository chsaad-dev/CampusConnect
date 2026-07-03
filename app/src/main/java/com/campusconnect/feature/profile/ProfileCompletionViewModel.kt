package com.campusconnect.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.User
import com.campusconnect.domain.repository.AuthRepository
import com.campusconnect.domain.usecase.user.CheckUsernameUseCase
import com.campusconnect.domain.usecase.user.CompleteProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileCompletionViewModel @Inject constructor(
    private val checkUsernameUseCase: CheckUsernameUseCase,
    private val completeProfileUseCase: CompleteProfileUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _usernameState = MutableStateFlow<Resource<Boolean>?>(null)
    val usernameState: StateFlow<Resource<Boolean>?> = _usernameState.asStateFlow()

    private val _completionState = MutableStateFlow<Resource<Unit>?>(null)
    val completionState: StateFlow<Resource<Unit>?> = _completionState.asStateFlow()

    private var usernameCheckJob: Job? = null

    /**
     * Debounced username availability check. Triggers 500ms after last keystroke.
     */
    fun checkUsername(username: String) {
        usernameCheckJob?.cancel()
        if (username.length < 3) {
            _usernameState.value = null
            return
        }
        usernameCheckJob = viewModelScope.launch {
            delay(500) // debounce
            checkUsernameUseCase(username).onEach { result ->
                _usernameState.value = result
            }.launchIn(this)
        }
    }

    fun completeProfile(
        name: String,
        rollNumber: String,
        department: String,
        semester: Int,
        uniqueUsername: String,
        bloodGroup: String,
        skills: List<String>,
        interests: List<String>,
        imageUri: android.net.Uri? = null
    ) {
        val uid = authRepository.getCurrentUid() ?: return
        val email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: ""

        val user = User(
            uid = uid,
            uniqueUsername = uniqueUsername.lowercase(),
            name = name,
            rollNumber = rollNumber,
            department = department,
            semester = semester,
            email = email,
            bloodGroup = bloodGroup,
            skills = skills,
            interests = interests,
            createdAt = System.currentTimeMillis(),
            profileComplete = true
        )

        completeProfileUseCase(user, imageUri).onEach { result ->
            _completionState.value = result
        }.launchIn(viewModelScope)
    }

    fun resetCompletionState() { _completionState.value = null }
}
