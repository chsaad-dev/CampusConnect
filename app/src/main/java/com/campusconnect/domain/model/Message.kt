package com.campusconnect.domain.model

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "none", // none, image, file, voice
    val seenBy: List<String> = emptyList(),
    val createdAt: Long = 0L
)
