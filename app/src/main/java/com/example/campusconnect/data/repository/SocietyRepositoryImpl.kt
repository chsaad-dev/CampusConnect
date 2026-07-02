package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Society
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
class SocietyRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SocietyRepository {

    override fun getSocieties(): Flow<Resource<List<Society>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("societies")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching societies"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val societies = snapshot.toObjects(Society::class.java)
                    trySend(Resource.Success(societies))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getSocietyDetails(societyId: String): Flow<Resource<Society>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("societies").document(societyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching details"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val society = snapshot.toObject(Society::class.java)
                    if (society != null) {
                        trySend(Resource.Success(society))
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun joinSociety(societyId: String, userId: String): Flow<Resource<String>> = callbackFlow {
        try {
            firestore.collection("societies").document(societyId)
                .update("membersCount", FieldValue.increment(1)).await()
            // In a real app, you'd also add the userId to a 'members' sub-collection or array
            trySend(Resource.Success("Joined society successfully"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to join society"))
        }
        awaitClose()
    }
}
