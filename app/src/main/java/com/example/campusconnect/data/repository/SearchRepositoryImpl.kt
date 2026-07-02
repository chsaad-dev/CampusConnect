package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Event
import com.example.campusconnect.data.model.Job
import com.example.campusconnect.data.model.Note
import com.example.campusconnect.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SearchRepository {

    override fun searchAll(query: String): Flow<Resource<List<SearchResult>>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val results = mutableListOf<SearchResult>()
            
            // Smart Search AI: Basic prefix matching for multiple collections
            // In a real AI setup, we might use Algolia or ElasticSearch for semantic search.
            
            val notesTask = firestore.collection("notes")
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .get()
            
            val jobsTask = firestore.collection("jobs")
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .get()
                
            val eventsTask = firestore.collection("events")
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .get()

            // Wait for all to complete
            val notesSnap = notesTask.await()
            val jobsSnap = jobsTask.await()
            val eventsSnap = eventsTask.await()

            results.addAll(notesSnap.toObjects(Note::class.java).map { SearchResult.NoteResult(it) })
            results.addAll(jobsSnap.toObjects(Job::class.java).map { SearchResult.JobResult(it) })
            results.addAll(eventsSnap.toObjects(Event::class.java).map { SearchResult.EventResult(it) })

            // AI Suggestion: If searching for "Java", also suggest "Operating Systems" or "Object Oriented Programming"
            // (Mocking semantic relation)
            if (query.equals("Java", ignoreCase = true)) {
                // We could fetch related tags/subjects
            }

            trySend(Resource.Success(results))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Search failed"))
        }
    }
}
