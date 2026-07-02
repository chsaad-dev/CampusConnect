package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Society
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow

interface SocietyRepository {
    fun getSocieties(): Flow<Resource<List<Society>>>
    fun getSocietyDetails(societyId: String): Flow<Resource<Society>>
    fun joinSociety(societyId: String, userId: String): Flow<Resource<String>>
}
