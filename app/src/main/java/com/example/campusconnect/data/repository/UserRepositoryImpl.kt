package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.User
import com.example.campusconnect.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override fun getUserById(userId: String): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching user"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val user = snapshot.toObject(User::class.java)
                    if (user != null) {
                        trySend(Resource.Success(user))
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun updateUser(user: User): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            firestore.collection("users").document(user.uid).set(user).await()
            trySend(Resource.Success("Profile updated successfully"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Update failed"))
        }
        awaitClose()
    }

    override fun updateReputationPoints(userId: String, points: Int): Flow<Resource<String>> = callbackFlow {
        try {
            firestore.collection("users").document(userId)
                .update("reputationPoints", FieldValue.increment(points.toLong())).await()
            trySend(Resource.Success("Points updated"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to update points"))
        }
        awaitClose()
    }

    override fun verifyStudentId(userId: String, idCardImageUrl: String): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            // Logic to submit ID card for admin verification
            val verificationRequest = mapOf(
                "userId" to userId,
                "idCardImageUrl" to idCardImageUrl,
                "status" to "Pending",
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("verification_requests").document(userId).set(verificationRequest).await()
            trySend(Resource.Success("Verification request submitted"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Submission failed"))
        }
        awaitClose()
    }

    override fun getUnverifiedUsers(): Flow<Resource<List<User>>> = callbackFlow {
        trySend(Resource.Loading())
        // Simplified: Fetching users who have a pending verification request
        val subscription = firestore.collection("verification_requests")
            .whereEqualTo("status", "Pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching requests"))
                    return@addSnapshotListener
                }
                // In a real app, you'd then fetch user details for these IDs
                // This is a simplified placeholder
                trySend(Resource.Success(emptyList()))
            }
        awaitClose { subscription.remove() }
    }
}
