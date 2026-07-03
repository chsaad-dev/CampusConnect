package com.campusconnect.domain.repository

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.Job
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    fun getJobs(): Flow<Resource<List<Job>>>
    fun createJob(job: Job): Flow<Resource<Unit>>
}
