package com.campusconnect.domain.repository

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.Complaint
import kotlinx.coroutines.flow.Flow

interface ComplaintRepository {
    fun submitComplaint(complaint: Complaint, imageUri: android.net.Uri?): Flow<Resource<Unit>>
    fun getStudentComplaints(): Flow<Resource<List<Complaint>>>
    fun getAllComplaints(): Flow<Resource<List<Complaint>>>
    fun updateComplaintStatus(complaintId: String, status: String, duplicateOfId: String): Flow<Resource<Unit>>
}
