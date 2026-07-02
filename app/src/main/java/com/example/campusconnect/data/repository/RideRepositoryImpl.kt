package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Ride
import com.example.campusconnect.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RideRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RideRepository {

    override fun createRide(ride: Ride): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val id = firestore.collection("rides").document().id
            val newRide = ride.copy(id = id)
            firestore.collection("rides").document(id).set(newRide).await()
            trySend(Resource.Success("Ride created successfully"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to create ride"))
        }
        awaitClose()
    }

    override fun getAvailableRides(): Flow<Resource<List<Ride>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("rides")
            .whereEqualTo("status", "Available")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching rides"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val rides = snapshot.toObjects(Ride::class.java)
                    trySend(Resource.Success(rides))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun joinRide(rideId: String, userId: String): Flow<Resource<String>> = callbackFlow {
        try {
            val rideRef = firestore.collection("rides").document(rideId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(rideRef)
                val availableSeats = snapshot.getLong("availableSeats") ?: 0
                if (availableSeats > 0) {
                    transaction.update(rideRef, "availableSeats", availableSeats - 1)
                    transaction.update(rideRef, "passengers", FieldValue.arrayUnion(userId))
                } else {
                    throw Exception("No seats available")
                }
            }.await()
            trySend(Resource.Success("Joined ride successfully"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to join ride"))
        }
        awaitClose()
    }

    override fun getMyRides(userId: String): Flow<Resource<List<Ride>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("rides")
            .whereArrayContains("passengers", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching my rides"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val rides = snapshot.toObjects(Ride::class.java)
                    trySend(Resource.Success(rides))
                }
            }
        awaitClose { subscription.remove() }
    }
}
