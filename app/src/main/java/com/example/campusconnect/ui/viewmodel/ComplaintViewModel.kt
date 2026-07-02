package com.example.campusconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.data.model.Complaint
import com.example.campusconnect.data.repository.ComplaintRepository
import com.example.campusconnect.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ComplaintViewModel @Inject constructor(
    private val repo: ComplaintRepository
) : ViewModel() {
    private val _complaints = MutableLiveData<Resource<List<Complaint>>>()
    val complaints: LiveData<Resource<List<Complaint>>> = _complaints

    private val _submitStatus = MutableLiveData<Resource<String>>()
    val submitStatus: LiveData<Resource<String>> = _submitStatus

    fun fetchMyComplaints(userId: String) {
        repo.getMyComplaints(userId).onEach { _complaints.value = it }.launchIn(viewModelScope)
    }

    fun submitComplaint(complaint: Complaint, imageUri: android.net.Uri? = null) {
        repo.submitComplaint(complaint, imageUri).onEach { _submitStatus.value = it }.launchIn(viewModelScope)
    }

    fun fetchAllComplaints() {
        repo.getAllComplaints().onEach { _complaints.value = it }.launchIn(viewModelScope)
    }
}
