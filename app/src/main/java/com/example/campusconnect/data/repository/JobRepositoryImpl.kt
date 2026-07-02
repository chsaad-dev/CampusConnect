package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Job
import com.example.campusconnect.data.model.JobApplication
import com.example.campusconnect.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : JobRepository {

    override fun getJobs(): Flow<Resource<List<Job>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("jobs")
            .orderBy("postedTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching jobs"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val jobs = snapshot.toObjects(Job::class.java)
                    trySend(Resource.Success(jobs))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun postJob(job: Job): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val id = firestore.collection("jobs").document().id
            val newJob = job.copy(id = id)
            firestore.collection("jobs").document(id).set(newJob).await()
            trySend(Resource.Success("Job posted successfully"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to post job"))
        }
        awaitClose()
    }

    override fun searchJobs(query: String): Flow<Resource<List<Job>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("jobs")
            .whereGreaterThanOrEqualTo("title", query)
            .whereLessThanOrEqualTo("title", query + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Search failed"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val jobs = snapshot.toObjects(Job::class.java)
                    trySend(Resource.Success(jobs))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun applyForJob(application: JobApplication): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val id = firestore.collection("job_applications").document().id
            val newApp = application.copy(id = id)
            firestore.collection("job_applications").document(id).set(newApp).await()
            trySend(Resource.Success("Application submitted successfully"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to apply"))
        }
        awaitClose()
    }

    override fun getMyApplications(studentId: String): Flow<Resource<List<JobApplication>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("job_applications")
            .whereEqualTo("studentId", studentId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching applications"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val apps = snapshot.toObjects(JobApplication::class.java)
                    trySend(Resource.Success(apps))
                }
            }
        awaitClose { subscription.remove() }
    }
}
