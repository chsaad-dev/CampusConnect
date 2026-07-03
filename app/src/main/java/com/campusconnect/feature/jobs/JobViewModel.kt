package com.campusconnect.feature.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.Job
import com.campusconnect.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class JobViewModel @Inject constructor(
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _jobsList = MutableStateFlow<Resource<List<Job>>>(Resource.Loading)
    val jobsList: StateFlow<Resource<List<Job>>> = _jobsList.asStateFlow()

    private val _jobDetail = MutableStateFlow<Resource<Job>>(Resource.Loading)
    val jobDetail: StateFlow<Resource<Job>> = _jobDetail.asStateFlow()

    fun loadJobs() {
        jobRepository.getJobs().onEach { result ->
            _jobsList.value = result
        }.launchIn(viewModelScope)
    }

    fun loadJobDetail(jobId: String) {
        jobRepository.getJobs().onEach { result ->
            if (result is Resource.Success) {
                val found = result.data.firstOrNull { it.jobId == jobId }
                if (found != null) {
                    _jobDetail.value = Resource.Success(found)
                } else {
                    _jobDetail.value = Resource.Error("Job not found")
                }
            } else if (result is Resource.Error) {
                _jobDetail.value = Resource.Error(result.message)
            }
        }.launchIn(viewModelScope)
    }
}
