package com.campusconnect.domain.usecase.auth

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Sends password reset email via Firebase Auth.
 */
class ForgotPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String): Flow<Resource<Unit>> {
        return authRepository.sendPasswordResetEmail(email)
    }
}
