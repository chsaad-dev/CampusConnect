package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Job
import com.example.campusconnect.data.model.JobApplication
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    fun getJobs(): Flow<Resource<List<Job>>>
    fun postJob(job: Job): Flow<Resource<String>>
    fun searchJobs(query: String): Flow<Resource<List<Job>>>
    fun applyForJob(application: JobApplication): Flow<Resource<String>>
    fun getMyApplications(studentId: String): Flow<Resource<List<JobApplication>>>
}
