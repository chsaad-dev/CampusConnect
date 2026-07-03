package com.campusconnect.data.repository

import android.net.Uri
import com.campusconnect.core.common.Constants
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.Complaint
import com.campusconnect.domain.repository.ComplaintRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComplaintRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ComplaintRepository {

    private val currentUid: String
        get() = auth.currentUser?.uid ?: throw Exception("User not logged in")

    override fun submitComplaint(complaint: Complaint, imageUri: Uri?): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val uid = currentUid
            val complaintId = firestore.collection(Constants.COLLECTION_COMPLAINTS).document().id
            var uploadUrl = ""

            if (imageUri != null) {
                val extension = imageUri.toString().substringAfterLast(".", "jpg")
                val filename = "${System.currentTimeMillis()}.$extension"
                val storageRef = storage.reference.child("complaints/$complaintId/$filename")
                storageRef.putFile(imageUri).await()
                uploadUrl = storageRef.downloadUrl.await().toString()
            }

            val finalComplaint = complaint.copy(
                complaintId = complaintId,
                studentId = uid,
                mediaUrl = uploadUrl,
                createdAt = System.currentTimeMillis()
            )

            firestore.collection(Constants.COLLECTION_COMPLAINTS)
                .document(complaintId)
                .set(finalComplaint)
                .await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to submit complaint"))
        }
    }

    override fun getStudentComplaints(): Flow<Resource<List<Complaint>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = firestore.collection(Constants.COLLECTION_COMPLAINTS)
                .whereEqualTo("studentId", currentUid)
                .orderBy(Constants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .await()

            val complaints = snapshot.toObjects(Complaint::class.java)
            emit(Resource.Success(complaints))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch complaints"))
        }
    }

    override fun getAllComplaints(): Flow<Resource<List<Complaint>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = firestore.collection(Constants.COLLECTION_COMPLAINTS)
                .orderBy(Constants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .await()

            val complaints = snapshot.toObjects(Complaint::class.java)
            emit(Resource.Success(complaints))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch complaints"))
        }
    }

    override fun updateComplaintStatus(
        complaintId: String,
        status: String,
        duplicateOfId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val complaintRef = firestore.collection(Constants.COLLECTION_COMPLAINTS).document(complaintId)
            val snapshot = complaintRef.get().await()
            val studentId = snapshot.getString("studentId") ?: ""
            val category = snapshot.getString("category") ?: "Complaint"

            val batch = firestore.batch()
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "duplicateOfId" to duplicateOfId
            )
            batch.update(complaintRef, updates)

            if (studentId.isNotEmpty()) {
                val notifRef = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                    .document(studentId)
                    .collection("items")
                    .document()

                val statusString = when (status) {
                    "in_progress" -> "In Progress"
                    "resolved" -> "Resolved"
                    "duplicate" -> "Duplicate"
                    else -> status.replaceFirstChar { it.uppercase() }
                }

                val notifItem = com.campusconnect.domain.model.NotificationItem(
                    notifId = notifRef.id,
                    title = "Complaint Status Updated",
                    body = "Your complaint under '$category' has been updated to '$statusString'.",
                    type = "complaint_status",
                    refId = complaintId,
                    createdAt = System.currentTimeMillis()
                )
                batch.set(notifRef, notifItem)
            }
            batch.commit().await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update complaint status"))
        }
    }

    override fun getRecentComplaints(
        category: String,
        location: String,
        timeWindowMs: Long
    ): Flow<Resource<List<Complaint>>> = flow {
        emit(Resource.Loading)
        try {
            val cutoffTime = System.currentTimeMillis() - timeWindowMs
            val snapshot = firestore.collection(Constants.COLLECTION_COMPLAINTS)
                .whereEqualTo("category", category)
                .whereEqualTo("location", location)
                .whereGreaterThanOrEqualTo("createdAt", cutoffTime)
                .get()
                .await()

            val list = snapshot.toObjects(Complaint::class.java)
            emit(Resource.Success(list))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch recent complaints"))
        }
    }
}
