package com.campusconnect.domain.usecase.user

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.User
import com.campusconnect.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Completes user profile after registration. Writes to `users/{uid}` and claims
 * the unique username in `usernames/{username}` atomically.
 */
class CompleteProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(user: User): Flow<Resource<Unit>> {
        return userRepository.completeProfile(user)
    }
}
