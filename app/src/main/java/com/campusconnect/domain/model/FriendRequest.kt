package com.campusconnect.domain.model

data class FriendRequest(
    val requestId: String = "",
    val fromUid: String = "",
    val fromUsername: String = "",
    val fromName: String = "",
    val fromPhotoUrl: String = "",
    val toUid: String = "",
    val toUsername: String = "",
    val toName: String = "",
    val toPhotoUrl: String = "",
    val status: String = "pending", // pending, accepted, rejected
    val createdAt: Long = 0L
)
