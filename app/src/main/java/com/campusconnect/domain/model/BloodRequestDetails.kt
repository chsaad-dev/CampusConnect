package com.campusconnect.domain.model

data class BloodRequestDetails(
    val postId: String = "",
    val requesterId: String = "",
    val hospital: String = "",
    val bloodGroup: String = "",
    val urgency: String = "",
    val status: String = "open", // open, fulfilled
    val createdAt: Long = 0L
)
