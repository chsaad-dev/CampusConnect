package com.campusconnect.data.repository

import com.campusconnect.core.common.Constants
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.Job
import com.campusconnect.domain.repository.JobRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : JobRepository {

    private val currentUid: String
        get() = auth.currentUser?.uid ?: throw Exception("User not logged in")

    override fun getJobs(): Flow<Resource<List<Job>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = firestore.collection(Constants.COLLECTION_JOBS)
                .orderBy(Constants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .await()

            val jobs = snapshot.toObjects(Job::class.java)
            emit(Resource.Success(jobs))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch jobs"))
        }
    }

    override fun createJob(job: Job): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val jobId = firestore.collection(Constants.COLLECTION_JOBS).document().id
            val finalJob = job.copy(
                jobId = jobId,
                postedBy = currentUid,
                createdAt = System.currentTimeMillis()
            )

            val usersSnapshot = firestore.collection(Constants.COLLECTION_USERS).get().await()
            val batch = firestore.batch()
            batch.set(firestore.collection(Constants.COLLECTION_JOBS).document(jobId), finalJob)

            for (doc in usersSnapshot.documents) {
                val targetUid = doc.id
                val notifRef = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                    .document(targetUid)
                    .collection("items")
                    .document()

                val notifItem = com.campusconnect.domain.model.NotificationItem(
                    notifId = notifRef.id,
                    title = "New Placement/Job Drive",
                    body = "${finalJob.companyName} is hiring for ${finalJob.title} (${finalJob.type}).",
                    type = "job",
                    refId = jobId,
                    createdAt = System.currentTimeMillis()
                )
                batch.set(notifRef, notifItem)
            }

            batch.commit().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create job"))
        }
    }
}
