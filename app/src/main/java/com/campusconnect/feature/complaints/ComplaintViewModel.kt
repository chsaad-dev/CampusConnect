package com.campusconnect.feature.complaints

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.Complaint
import com.campusconnect.domain.repository.ComplaintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ComplaintViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository
) : ViewModel() {

    private val _submitState = MutableStateFlow<Resource<Unit>?>(null)
    val submitState: StateFlow<Resource<Unit>?> = _submitState.asStateFlow()

    private val _complaintsList = MutableStateFlow<Resource<List<Complaint>>>(Resource.Loading)
    val complaintsList: StateFlow<Resource<List<Complaint>>> = _complaintsList.asStateFlow()

    private val _complaintDetail = MutableStateFlow<Resource<Complaint>>(Resource.Loading)
    val complaintDetail: StateFlow<Resource<Complaint>> = _complaintDetail.asStateFlow()

    private val _duplicateCheckState = MutableStateFlow<Resource<List<Complaint>>?>(null)
    val duplicateCheckState: StateFlow<Resource<List<Complaint>>?> = _duplicateCheckState.asStateFlow()

    fun checkForDuplicates(category: String, location: String, description: String) {
        complaintRepository.getRecentComplaints(category, location, 24 * 60 * 60 * 1000)
            .onEach { result ->
                if (result is Resource.Success) {
                    val potentialDuplicates = result.data.filter { existing ->
                        com.campusconnect.core.utils.SmartAlgorithms.calculateTextSimilarity(
                            description,
                            existing.description
                        ) >= 0.5
                    }
                    _duplicateCheckState.value = Resource.Success(potentialDuplicates)
                } else if (result is Resource.Error) {
                    _duplicateCheckState.value = Resource.Error(result.message)
                } else if (result is Resource.Loading) {
                    _duplicateCheckState.value = Resource.Loading
                }
            }.launchIn(viewModelScope)
    }

    fun resetDuplicateCheck() {
        _duplicateCheckState.value = null
    }

    fun submitComplaint(category: String, description: String, location: String, priority: String, imageUri: Uri?) {
        val complaint = Complaint(
            category = category,
            description = description,
            location = location,
            priority = priority
        )
        complaintRepository.submitComplaint(complaint, imageUri).onEach { result ->
            _submitState.value = result
        }.launchIn(viewModelScope)
    }

    fun loadStudentComplaints() {
        complaintRepository.getStudentComplaints().onEach { result ->
            _complaintsList.value = result
        }.launchIn(viewModelScope)
    }

    fun loadComplaintDetail(complaintId: String) {
        // Query from local/all list
        complaintRepository.getStudentComplaints().onEach { result ->
            if (result is Resource.Success) {
                val found = result.data.firstOrNull { it.complaintId == complaintId }
                if (found != null) {
                    _complaintDetail.value = Resource.Success(found)
                } else {
                    _complaintDetail.value = Resource.Error("Complaint not found")
                }
            } else if (result is Resource.Error) {
                _complaintDetail.value = Resource.Error(result.message)
            }
        }.launchIn(viewModelScope)
    }

    fun resetSubmitState() {
        _submitState.value = null
    }
}
