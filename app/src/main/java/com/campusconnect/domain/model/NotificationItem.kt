package com.campusconnect.domain.model

data class NotificationItem(
    val notifId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "", // friend_request, chat_message, note, blood_request, ride, complaint_status
    val refId: String = "", // ID of the referenced post, chat, or complaint
    val read: Boolean = false,
    val createdAt: Long = 0L
)
