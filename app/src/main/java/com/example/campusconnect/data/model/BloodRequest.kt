package com.example.campusconnect.data.model

data class BloodRequest(
    val id: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val bloodGroup: String = "",
    val hospitalName: String = "",
    val location: String = "",
    val reason: String = "",
    val contactNumber: String = "",
    val urgency: String = "Normal", // Normal, Urgent, Critical
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Open" // Open, Fulfilled, Cancelled
)
