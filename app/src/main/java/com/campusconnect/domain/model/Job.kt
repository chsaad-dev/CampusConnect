package com.campusconnect.domain.model

data class Job(
    val jobId: String = "",
    val companyName: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "job", // job, internship
    val skillsRequired: List<String> = emptyList(),
    val applyLink: String = "",
    val postedBy: String = "",
    val deadline: Long = 0L,
    val createdAt: Long = 0L
)
