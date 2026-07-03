package com.campusconnect.domain.usecase.auth

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Handles user registration (creates Firebase Auth account).
 * Profile completion is a separate step via CompleteProfileUseCase.
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<Resource<String>> {
        return authRepository.register(email, password)
    }
}
