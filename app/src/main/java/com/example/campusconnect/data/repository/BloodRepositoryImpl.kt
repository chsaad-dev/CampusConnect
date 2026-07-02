package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.BloodRequest
import com.example.campusconnect.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BloodRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BloodRepository {

    override fun postBloodRequest(request: BloodRequest): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val id = firestore.collection("blood_requests").document().id
            val newRequest = request.copy(id = id)
            firestore.collection("blood_requests").document(id).set(newRequest).await()
            trySend(Resource.Success("Blood request created"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to create request"))
        }
        awaitClose()
    }

    override fun getBloodRequests(): Flow<Resource<List<BloodRequest>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("blood_requests")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching requests"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val requests = snapshot.toObjects(BloodRequest::class.java)
                    trySend(Resource.Success(requests))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun updateRequestStatus(requestId: String, status: String): Flow<Resource<String>> = callbackFlow {
        try {
            firestore.collection("blood_requests").document(requestId)
                .update("status", status).await()
            trySend(Resource.Success("Status updated"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to update status"))
        }
        awaitClose()
    }

    override fun searchDonors(bloodGroup: String): Flow<Resource<List<BloodRequest>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("blood_requests")
            .whereEqualTo("bloodGroup", bloodGroup)
            .whereEqualTo("status", "Available")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Search failed"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val requests = snapshot.toObjects(BloodRequest::class.java)
                    trySend(Resource.Success(requests))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun searchUsersByBloodGroup(bloodGroup: String): Flow<Resource<List<com.example.campusconnect.data.model.User>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("users")
            .whereEqualTo("bloodGroup", bloodGroup)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Search failed"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val users = snapshot.toObjects(com.example.campusconnect.data.model.User::class.java)
                    trySend(Resource.Success(users))
                }
            }
        awaitClose { subscription.remove() }
    }
}
