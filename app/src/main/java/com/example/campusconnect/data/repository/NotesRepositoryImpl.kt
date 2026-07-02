package com.example.campusconnect.data.repository

import android.net.Uri
import com.example.campusconnect.data.local.NoteDao
import com.example.campusconnect.data.model.Note
import com.example.campusconnect.data.model.toEntity
import com.example.campusconnect.data.model.toNote
import com.example.campusconnect.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val noteDao: NoteDao
) : NotesRepository {

    override fun uploadNote(note: Note, fileUri: Uri): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val fileName = "notes/${System.currentTimeMillis()}_${note.title}"
            val storageRef = storage.reference.child(fileName)
            storageRef.putFile(fileUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            val newNote = note.copy(id = firestore.collection("notes").document().id, fileUrl = downloadUrl)
            firestore.collection("notes").document(newNote.id).set(newNote).await()
            
            trySend(Resource.Success("Note uploaded successfully"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Upload failed"))
        }
        awaitClose()
    }

    override fun getNotes(department: String, semester: String): Flow<Resource<List<Note>>> = callbackFlow {
        trySend(Resource.Loading())
        
        // Fetch from Room first for offline capability
        val cached = noteDao.getNotes(department, semester).first()
        if (cached.isNotEmpty()) {
            trySend(Resource.Success(cached.map { it.toNote() }))
        }

        val subscription = firestore.collection("notes")
            .whereEqualTo("department", department)
            .whereEqualTo("semester", semester)
            .orderBy("uploadTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching notes"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notes = snapshot.toObjects(Note::class.java)
                    trySend(Resource.Success(notes))
                    
                    // Save to local cache
                    GlobalScope.launch {
                        notes.forEach { noteDao.insertNote(it.toEntity()) }
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun searchNotes(query: String): Flow<Resource<List<Note>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("notes")
            .whereGreaterThanOrEqualTo("title", query)
            .whereLessThanOrEqualTo("title", query + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Search failed"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notes = snapshot.toObjects(Note::class.java)
                    trySend(Resource.Success(notes))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun addRating(noteId: String, rating: Float): Flow<Resource<String>> = callbackFlow {
        try {
            val noteRef = firestore.collection("notes").document(noteId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(noteRef)
                val currentRating = snapshot.getDouble("rating") ?: 0.0
                val currentCount = snapshot.getLong("ratingsCount") ?: 0
                val newCount = currentCount + 1
                val newRating = ((currentRating * currentCount) + rating) / newCount
                
                transaction.update(noteRef, "rating", newRating)
                transaction.update(noteRef, "ratingsCount", newCount)
            }.await()
            trySend(Resource.Success("Rating added"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to add rating"))
        }
        awaitClose()
    }

    override fun incrementDownloadCount(noteId: String): Flow<Resource<String>> = callbackFlow {
        try {
            val noteRef = firestore.collection("notes").document(noteId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(noteRef)
                val currentCount = snapshot.getLong("downloadsCount") ?: 0
                transaction.update(noteRef, "downloadsCount", currentCount + 1)
            }.await()
            trySend(Resource.Success("Count updated"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to update count"))
        }
        awaitClose()
    }

    override fun getRecommendedNotes(department: String, interests: List<String>): Flow<Resource<List<Note>>> = callbackFlow {
        trySend(Resource.Loading())
        // Smart AI Filtering: Fetch notes from department, but prioritize those matching interests
        val subscription = firestore.collection("notes")
            .whereEqualTo("department", department)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Recommendation failed"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notes = snapshot.toObjects(Note::class.java)
                    // Simple AI: Sort by interest match
                    val sorted = notes.sortedByDescending { note ->
                        interests.count { interest -> note.title.contains(interest, ignoreCase = true) || note.subject.contains(interest, ignoreCase = true) }
                    }
                    trySend(Resource.Success(sorted.take(10)))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getPopularNotes(): Flow<Resource<List<Note>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("notes")
            .orderBy("downloadsCount", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Fetching popular notes failed"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notes = snapshot.toObjects(Note::class.java)
                    trySend(Resource.Success(notes))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun downloadNote(note: Note) {
        // Implementation for downloading note (e.g., using DownloadManager)
    }
}
