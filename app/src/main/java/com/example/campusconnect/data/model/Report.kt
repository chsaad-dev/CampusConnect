package com.example.campusconnect.data.model

data class Report(
    val id: String = "",
    val targetId: String = "", // ID of the Note, Ride, Job, etc.
    val targetType: String = "", // "Note", "Ride", "Job", "User"
    val reporterId: String = "",
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pending" // Pending, Investigated, Dismissed, ActionTaken
)
