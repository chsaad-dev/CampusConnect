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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val complaintRepo: ComplaintRepository
) : ViewModel() {

    private val _pendingComplaints = MutableLiveData<Resource<List<Complaint>>>()
    val pendingComplaints: LiveData<Resource<List<Complaint>>> = _pendingComplaints

    private val _userStats = MutableLiveData<Resource<Map<String, Int>>>()
    val userStats: LiveData<Resource<Map<String, Int>>> = _userStats

    fun fetchPendingComplaints() {
        complaintRepo.getAllComplaints().onEach { _pendingComplaints.value = it }.launchIn(viewModelScope)
    }

    fun fetchStats() {
        viewModelScope.launch {
            _userStats.value = Resource.Success(mapOf("Students" to 500, "Complaints" to 24, "Events" to 8, "Jobs" to 15))
        }
    }
}
