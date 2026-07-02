package com.example.campusconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.data.model.Ride
import com.example.campusconnect.data.repository.RideRepository
import com.example.campusconnect.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class RideViewModel @Inject constructor(
    private val repo: RideRepository
) : ViewModel() {
    private val _rides = MutableLiveData<Resource<List<Ride>>>()
    val rides: LiveData<Resource<List<Ride>>> = _rides

    private val _actionStatus = MutableLiveData<Resource<String>>()
    val actionStatus: LiveData<Resource<String>> = _actionStatus

    fun fetchRides() {
        repo.getAvailableRides().onEach { _rides.value = it }.launchIn(viewModelScope)
    }

    fun postRide(ride: Ride) {
        repo.createRide(ride).onEach { _actionStatus.value = it }.launchIn(viewModelScope)
    }

    fun joinRide(rideId: String, userId: String) {
        repo.joinRide(rideId, userId).onEach { _actionStatus.value = it }.launchIn(viewModelScope)
    }
}
