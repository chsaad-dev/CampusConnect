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
    private val firestore: FirebaseFirestore,
    private val jobDao: com.campusconnect.data.local.JobDao
) : JobRepository {

    private val currentUid: String
        get() = auth.currentUser?.uid ?: throw Exception("User not logged in")

    override fun getJobs(): Flow<Resource<List<Job>>> = flow {
        val cached = jobDao.getCachedJobs()
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached.map { it.toDomain() }))
        } else {
            emit(Resource.Loading)
        }
        try {
            val snapshot = firestore.collection(Constants.COLLECTION_JOBS)
                .orderBy(Constants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .await()

            val jobs = snapshot.toObjects(Job::class.java)

            jobDao.clearAllJobs()
            jobDao.insertJobs(jobs.map { com.campusconnect.data.model.JobEntity.fromDomain(it) })

            emit(Resource.Success(jobs))
        } catch (e: Exception) {
            val cachedAgain = jobDao.getCachedJobs()
            if (cachedAgain.isEmpty()) {
                emit(Resource.Error(e.message ?: "Failed to fetch jobs"))
            }
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
                val userSkills = doc.get("skills") as? List<String> ?: emptyList()
                val matchScore = com.campusconnect.core.utils.SmartAlgorithms.calculateSkillMatch(userSkills, finalJob.skillsRequired)

                if (matchScore >= 50.0) {
                    val notifRef = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                        .document(targetUid)
                        .collection("items")
                        .document()

                    val notifItem = com.campusconnect.domain.model.NotificationItem(
                        notifId = notifRef.id,
                        title = "Smart Job Match: ${finalJob.title}",
                        body = "At ${finalJob.companyName}. Matches ${matchScore.toInt()}% of your skill profile!",
                        type = "job",
                        refId = jobId,
                        createdAt = System.currentTimeMillis()
                    )
                    batch.set(notifRef, notifItem)
                }
            }

            batch.commit().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create job"))
        }
    }
}
