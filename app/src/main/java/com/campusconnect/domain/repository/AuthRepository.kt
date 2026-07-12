package com.campusconnect.domain.repository

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Auth repository interface. Implemented in data layer.
 */
interface AuthRepository {
    fun login(email: String, password: String): Flow<Resource<User>>
    fun register(email: String, password: String): Flow<Resource<String>>
    fun sendVerificationEmail(): Flow<Resource<Unit>>
    fun isEmailVerified(): Boolean
    fun sendPasswordResetEmail(email: String): Flow<Resource<Unit>>
    fun logout()
    fun isLoggedIn(): Boolean
    fun getCurrentUid(): String?
    fun loginWithGoogle(idToken: String): Flow<Resource<User>>
}
