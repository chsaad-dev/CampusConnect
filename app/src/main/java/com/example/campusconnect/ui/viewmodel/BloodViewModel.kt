package com.example.campusconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.data.model.BloodRequest
import com.example.campusconnect.data.repository.BloodRepository
import com.example.campusconnect.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class BloodViewModel @Inject constructor(
    private val repo: BloodRepository
) : ViewModel() {
    private val _requests = MutableLiveData<Resource<List<BloodRequest>>>()
    val requests: LiveData<Resource<List<BloodRequest>>> = _requests

    private val _donors = MutableLiveData<Resource<List<com.example.campusconnect.data.model.User>>>()
    val donors: LiveData<Resource<List<com.example.campusconnect.data.model.User>>> = _donors

    private val _postStatus = MutableLiveData<Resource<String>>()
    val postStatus: LiveData<Resource<String>> = _postStatus

    fun fetchRequests() {
        repo.getBloodRequests().onEach { _requests.value = it }.launchIn(viewModelScope)
    }

    fun postRequest(req: BloodRequest) {
        repo.postBloodRequest(req).onEach { _postStatus.value = it }.launchIn(viewModelScope)
    }

    fun searchDonors(bloodGroup: String) {
        repo.searchUsersByBloodGroup(bloodGroup).onEach { _donors.value = it }.launchIn(viewModelScope)
    }
}
