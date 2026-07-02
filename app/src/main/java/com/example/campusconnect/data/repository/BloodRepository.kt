package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.BloodRequest
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow

interface BloodRepository {
    fun postBloodRequest(request: BloodRequest): Flow<Resource<String>>
    fun getBloodRequests(): Flow<Resource<List<BloodRequest>>>
    fun updateRequestStatus(requestId: String, status: String): Flow<Resource<String>>
    fun searchDonors(bloodGroup: String): Flow<Resource<List<BloodRequest>>>
    fun searchUsersByBloodGroup(bloodGroup: String): Flow<Resource<List<com.example.campusconnect.data.model.User>>>
}
