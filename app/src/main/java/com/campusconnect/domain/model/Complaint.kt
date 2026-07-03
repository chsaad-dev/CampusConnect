package com.campusconnect.domain.model

data class Complaint(
    val complaintId: String = "",
    val studentId: String = "",
    val category: String = "",
    val description: String = "",
    val location: String = "",
    val mediaUrl: String = "",
    val status: String = "submitted", // submitted, in_progress, resolved, duplicate
    val priority: String = "Medium", // Low, Medium, High
    val department: String = "",
    val duplicateOfId: String = "",
    val createdAt: Long = 0L
)
