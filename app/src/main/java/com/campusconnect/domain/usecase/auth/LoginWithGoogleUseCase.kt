package com.campusconnect.domain.usecase.auth

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.User
import com.campusconnect.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Handles Google authentication sign-in.
 */
class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(idToken: String): Flow<Resource<User>> {
        return authRepository.loginWithGoogle(idToken)
    }
}
