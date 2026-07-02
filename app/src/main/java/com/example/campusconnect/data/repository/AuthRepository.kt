package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.User
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, password: String): Flow<Resource<User>>
    fun register(user: User, password: String): Flow<Resource<User>>
    fun logout()
    fun getCurrentUser(): User?
    fun isUserLoggedIn(): Boolean
    fun sendPasswordResetEmail(email: String): Flow<Resource<String>>
    suspend fun loadCurrentUser(): User?
}
