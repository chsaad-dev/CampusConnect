package com.campusconnect.domain.usecase.user

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Checks if a username is available (not already claimed in `usernames/` collection).
 */
class CheckUsernameUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(username: String): Flow<Resource<Boolean>> {
        return userRepository.checkUsernameAvailability(username)
    }
}
