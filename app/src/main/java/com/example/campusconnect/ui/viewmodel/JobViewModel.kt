package com.example.campusconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.data.model.Job
import com.example.campusconnect.data.model.JobApplication
import com.example.campusconnect.data.repository.JobRepository
import com.example.campusconnect.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class JobViewModel @Inject constructor(
    private val repo: JobRepository
) : ViewModel() {
    private val _jobs = MutableLiveData<Resource<List<Job>>>()
    val jobs: LiveData<Resource<List<Job>>> = _jobs

    private val _applyStatus = MutableLiveData<Resource<String>>()
    val applyStatus: LiveData<Resource<String>> = _applyStatus

    fun fetchJobs() {
        repo.getJobs().onEach { _jobs.value = it }.launchIn(viewModelScope)
    }

    fun searchJobs(query: String) {
        repo.searchJobs(query).onEach { _jobs.value = it }.launchIn(viewModelScope)
    }

    fun applyForJob(application: JobApplication) {
        repo.applyForJob(application).onEach { _applyStatus.value = it }.launchIn(viewModelScope)
    }
}
