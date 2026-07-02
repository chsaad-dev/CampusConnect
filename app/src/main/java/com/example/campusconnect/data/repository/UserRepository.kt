package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.User
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserById(userId: String): Flow<Resource<User>>
    fun updateUser(user: User): Flow<Resource<String>>
    fun updateReputationPoints(userId: String, points: Int): Flow<Resource<String>>
    fun verifyStudentId(userId: String, idCardImageUrl: String): Flow<Resource<String>>
    fun getUnverifiedUsers(): Flow<Resource<List<User>>>
}
