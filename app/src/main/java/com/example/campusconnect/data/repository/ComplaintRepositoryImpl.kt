package com.example.campusconnect.data.repository

import android.net.Uri
import com.example.campusconnect.data.model.Complaint
import com.example.campusconnect.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ComplaintRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ComplaintRepository {

    override fun submitComplaint(complaint: Complaint, imageUri: Uri?): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            // AI Feature: Duplicate Complaint Detection
            // Check if a similar complaint exists in the same location/category recently
            val existing = firestore.collection("complaints")
                .whereEqualTo("location", complaint.location)
                .whereEqualTo("category", complaint.category)
                .whereEqualTo("status", "Pending")
                .get().await()

            if (!existing.isEmpty) {
                // If many people reported the same, we could merge or notify the user
                // For now, let's just allow it but mark it as a "Potential Duplicate" or handle it on admin side
            }

            var imageUrl = ""
            if (imageUri != null) {
                val fileName = "complaints/${System.currentTimeMillis()}_${complaint.studentId}"
                val storageRef = storage.reference.child(fileName)
                storageRef.putFile(imageUri).await()
                imageUrl = storageRef.downloadUrl.await().toString()
            }

            val newComplaint = complaint.copy(
                id = firestore.collection("complaints").document().id,
                imageUrl = imageUrl,
                priority = if (!existing.isEmpty) "High" else "Low" // AI: Increased priority if multiple reports
            )
            firestore.collection("complaints").document(newComplaint.id).set(newComplaint).await()
            trySend(Resource.Success("Complaint submitted successfully"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to submit complaint"))
        }
        awaitClose()
    }

    override fun getMyComplaints(studentId: String): Flow<Resource<List<Complaint>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("complaints")
            .whereEqualTo("studentId", studentId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching complaints"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val complaints = snapshot.toObjects(Complaint::class.java)
                    trySend(Resource.Success(complaints))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getAllComplaints(): Flow<Resource<List<Complaint>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("complaints")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching all complaints"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val complaints = snapshot.toObjects(Complaint::class.java)
                    trySend(Resource.Success(complaints))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun updateComplaintStatus(
        complaintId: String,
        status: String,
        feedback: String
    ): Flow<Resource<String>> = callbackFlow {
        try {
            val updates = mapOf(
                "status" to status,
                "feedback" to feedback
            )
            firestore.collection("complaints").document(complaintId).update(updates).await()
            trySend(Resource.Success("Complaint updated"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to update complaint"))
        }
        awaitClose()
    }
}
