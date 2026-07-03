package com.campusconnect.domain.usecase.user

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.User
import com.campusconnect.domain.repository.AuthRepository
import com.campusconnect.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Fetches the currently logged-in user's profile from Firestore.
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Resource<User>> {
        val uid = authRepository.getCurrentUid()
            ?: return flowOf(Resource.Error("Not logged in"))
        return userRepository.getUserById(uid)
    }
}
