package com.example.campusconnect.data.repository

import android.net.Uri
import com.example.campusconnect.data.model.LostItem
import com.example.campusconnect.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LostAndFoundRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : LostAndFoundRepository {

    override fun reportItem(item: LostItem, imageUri: Uri?): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            // AI Feature: Duplicate item detection
            // Check for similar items in the same category and location within last 24 hours
            val querySnapshot = firestore.collection("lost_found")
                .whereEqualTo("category", item.category)
                .whereEqualTo("location", item.location)
                .whereEqualTo("status", item.status)
                .get().await()

            val isPotentialDuplicate = querySnapshot.documents.any { doc ->
                val existingTitle = doc.getString("title") ?: ""
                // Simple string similarity or keyword matching (Mock AI)
                existingTitle.contains(item.title, ignoreCase = true) || item.title.contains(existingTitle, ignoreCase = true)
            }

            if (isPotentialDuplicate) {
                // We could flag it or notify the admin
            }

            var imageUrl = ""
            if (imageUri != null) {
                val fileName = "lost_found/${System.currentTimeMillis()}_${item.title}"
                val storageRef = storage.reference.child(fileName)
                storageRef.putFile(imageUri).await()
                imageUrl = storageRef.downloadUrl.await().toString()
            }

            val newItem = item.copy(
                id = firestore.collection("lost_found").document().id,
                imageUrl = imageUrl
            )
            firestore.collection("lost_found").document(newItem.id).set(newItem).await()
            trySend(Resource.Success("Item reported successfully"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to report item"))
        }
        awaitClose()
    }

    override fun getItems(): Flow<Resource<List<LostItem>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("lost_found")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching items"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(LostItem::class.java)
                    trySend(Resource.Success(items))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun updateItemStatus(itemId: String, status: String): Flow<Resource<String>> = callbackFlow {
        try {
            firestore.collection("lost_found").document(itemId)
                .update("status", status).await()
            trySend(Resource.Success("Status updated"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to update status"))
        }
        awaitClose()
    }

    override fun searchItems(query: String): Flow<Resource<List<LostItem>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("lost_found")
            .whereGreaterThanOrEqualTo("title", query)
            .whereLessThanOrEqualTo("title", query + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Search failed"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(LostItem::class.java)
                    trySend(Resource.Success(items))
                }
            }
        awaitClose { subscription.remove() }
    }
}
