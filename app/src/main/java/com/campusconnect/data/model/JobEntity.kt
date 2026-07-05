package com.campusconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.campusconnect.domain.model.Job

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey
    val jobId: String = "",
    val companyName: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "job",
    val skillsRequired: List<String> = emptyList(),
    val applyLink: String = "",
    val postedBy: String = "",
    val deadline: Long = 0L,
    val createdAt: Long = 0L,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Job = Job(
        jobId = jobId,
        companyName = companyName,
        title = title,
        description = description,
        type = type,
        skillsRequired = skillsRequired,
        applyLink = applyLink,
        postedBy = postedBy,
        deadline = deadline,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(job: Job): JobEntity = JobEntity(
            jobId = job.jobId,
            companyName = job.companyName,
            title = job.title,
            description = job.description,
            type = job.type,
            skillsRequired = job.skillsRequired,
            applyLink = job.applyLink,
            postedBy = job.postedBy,
            deadline = job.deadline,
            createdAt = job.createdAt
        )
    }
}
