package com.campusconnect.feature.admin

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.Complaint
import com.campusconnect.domain.model.Event
import com.campusconnect.domain.model.Job
import com.campusconnect.domain.repository.ComplaintRepository
import com.campusconnect.domain.repository.EventRepository
import com.campusconnect.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository,
    private val eventRepository: EventRepository,
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _complaintsList = MutableStateFlow<Resource<List<Complaint>>>(Resource.Loading)
    val complaintsList: StateFlow<Resource<List<Complaint>>> = _complaintsList.asStateFlow()

    private val _createEventState = MutableStateFlow<Resource<Unit>?>(null)
    val createEventState: StateFlow<Resource<Unit>?> = _createEventState.asStateFlow()

    private val _createJobState = MutableStateFlow<Resource<Unit>?>(null)
    val createJobState: StateFlow<Resource<Unit>?> = _createJobState.asStateFlow()

    private val _updateComplaintState = MutableStateFlow<Resource<Unit>?>(null)
    val updateComplaintState: StateFlow<Resource<Unit>?> = _updateComplaintState.asStateFlow()

    fun loadAllComplaints() {
        complaintRepository.getAllComplaints().onEach { result ->
            _complaintsList.value = result
        }.launchIn(viewModelScope)
    }

    fun updateComplaintStatus(complaintId: String, status: String, duplicateOfId: String) {
        complaintRepository.updateComplaintStatus(complaintId, status, duplicateOfId).onEach { result ->
            _updateComplaintState.value = result
            if (result is Resource.Success) {
                loadAllComplaints()
            }
        }.launchIn(viewModelScope)
    }

    fun createEvent(
        title: String,
        description: String,
        hostType: String,
        date: Long,
        location: String,
        bannerUri: Uri?
    ) {
        val event = Event(
            title = title,
            description = description,
            hostType = hostType,
            date = date,
            location = location
        )
        eventRepository.createEvent(event, bannerUri).onEach { result ->
            _createEventState.value = result
        }.launchIn(viewModelScope)
    }

    fun createJob(
        companyName: String,
        title: String,
        description: String,
        type: String,
        skills: List<String>,
        applyLink: String,
        deadline: Long
    ) {
        val job = Job(
            companyName = companyName,
            title = title,
            description = description,
            type = type,
            skillsRequired = skills,
            applyLink = applyLink,
            deadline = deadline
        )
        jobRepository.createJob(job).onEach { result ->
            _createJobState.value = result
        }.launchIn(viewModelScope)
    }

    fun resetStates() {
        _createEventState.value = null
        _createJobState.value = null
        _updateComplaintState.value = null
    }
}
