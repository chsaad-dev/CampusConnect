package com.campusconnect.domain.model

data class LostFoundDetails(
    val postId: String = "",
    val ownerId: String = "",
    val itemName: String = "",
    val category: String = "",
    val location: String = "",
    val status: String = "lost", // lost, recovered
    val createdAt: Long = 0L
)
