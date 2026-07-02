package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Complaint
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow
import android.net.Uri

interface ComplaintRepository {
    fun submitComplaint(complaint: Complaint, imageUri: Uri?): Flow<Resource<String>>
    fun getMyComplaints(studentId: String): Flow<Resource<List<Complaint>>>
    fun getAllComplaints(): Flow<Resource<List<Complaint>>>
    fun updateComplaintStatus(complaintId: String, status: String, feedback: String): Flow<Resource<String>>
}
