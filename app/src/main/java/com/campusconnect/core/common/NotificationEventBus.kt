package com.campusconnect.core.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NotificationEventBus {
    private val _events = MutableSharedFlow<NotificationEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<NotificationEvent> = _events.asSharedFlow()

    fun postEvent(title: String, body: String, type: String, refId: String) {
        _events.tryEmit(NotificationEvent(title, body, type, refId))
    }
}

data class NotificationEvent(
    val title: String,
    val body: String,
    val type: String,
    val refId: String
)
