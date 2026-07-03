package com.campusconnect.domain.usecase.auth

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Sends email verification and checks whether user's email is verified.
 */
class CheckEmailVerifiedUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    fun isVerified(): Boolean = authRepository.isEmailVerified()

    fun sendVerification(): Flow<Resource<Unit>> = authRepository.sendVerificationEmail()
}
