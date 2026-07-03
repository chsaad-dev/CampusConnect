package com.campusconnect.domain.repository

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * User repository interface for profile operations. Implemented in data layer.
 */
interface UserRepository {
    fun getUserById(uid: String): Flow<Resource<User>>
    fun getCurrentUserProfile(): Flow<Resource<User>>
    fun updateProfile(user: User): Flow<Resource<Unit>>
    fun checkUsernameAvailability(username: String): Flow<Resource<Boolean>>
    fun claimUsername(uid: String, username: String): Flow<Resource<Unit>>
    fun completeProfile(user: User, imageUri: android.net.Uri? = null): Flow<Resource<Unit>>
    fun isProfileComplete(uid: String): Flow<Resource<Boolean>>
    fun trackSubjectView(subject: String): Flow<Resource<Unit>>
}
