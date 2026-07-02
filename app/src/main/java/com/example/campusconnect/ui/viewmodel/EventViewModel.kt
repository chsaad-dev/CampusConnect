package com.example.campusconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.data.model.Event
import com.example.campusconnect.data.repository.EventRepository
import com.example.campusconnect.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repo: EventRepository
) : ViewModel() {
    private val _events = MutableLiveData<Resource<List<Event>>>()
    val events: LiveData<Resource<List<Event>>> = _events

    private val _upcomingEvents = MutableLiveData<Resource<List<Event>>>()
    val upcomingEvents: LiveData<Resource<List<Event>>> = _upcomingEvents

    fun fetchEvents() {
        repo.getEvents().onEach { _events.value = it }.launchIn(viewModelScope)
    }

    fun fetchUpcomingEvents() {
        repo.getUpcomingEvents().onEach { _upcomingEvents.value = it }.launchIn(viewModelScope)
    }
}
