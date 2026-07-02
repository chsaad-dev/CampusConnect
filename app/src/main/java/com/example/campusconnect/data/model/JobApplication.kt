package com.example.campusconnect.data.model

data class JobApplication(
    val id: String = "",
    val jobId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val resumeUrl: String = "",
    val status: String = "Applied", // Applied, Under Review, Interview Scheduled, Accepted, Rejected
    val timestamp: Long = System.currentTimeMillis(),
    val companyName: String = "",
    val jobTitle: String = ""
)
