package com.campusconnect.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.Event
import com.campusconnect.domain.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val currentUid: String
        get() = auth.currentUser?.uid ?: ""

    private val _eventsList = MutableStateFlow<Resource<List<Event>>>(Resource.Loading)
    val eventsList: StateFlow<Resource<List<Event>>> = _eventsList.asStateFlow()

    private val _eventDetail = MutableStateFlow<Resource<Event>>(Resource.Loading)
    val eventDetail: StateFlow<Resource<Event>> = _eventDetail.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<Unit>?>(null)
    val registerState: StateFlow<Resource<Unit>?> = _registerState.asStateFlow()

    fun loadEvents() {
        eventRepository.getEvents().onEach { result ->
            _eventsList.value = result
        }.launchIn(viewModelScope)
    }

    fun loadEventDetails(eventId: String) {
        eventRepository.getEventDetails(eventId).onEach { result ->
            _eventDetail.value = result
        }.launchIn(viewModelScope)
    }

    fun registerForEvent(eventId: String) {
        eventRepository.registerForEvent(eventId).onEach { result ->
            _registerState.value = result
            if (result is Resource.Success) {
                // Reload details to show ticket
                loadEventDetails(eventId)
            }
        }.launchIn(viewModelScope)
    }

    fun resetRegisterState() {
        _registerState.value = null
    }
}
