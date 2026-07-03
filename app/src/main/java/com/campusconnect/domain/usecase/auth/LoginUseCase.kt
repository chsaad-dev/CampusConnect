package com.campusconnect.domain.usecase.auth

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.User
import com.campusconnect.domain.repository.AuthRepository
import com.campusconnect.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import javax.inject.Inject

/**
 * Handles login and fetches user profile after successful authentication.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(email: String, password: String): Flow<Resource<User>> {
        return authRepository.login(email, password)
    }
}
