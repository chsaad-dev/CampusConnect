package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Ride
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow

interface RideRepository {
    fun createRide(ride: Ride): Flow<Resource<String>>
    fun getAvailableRides(): Flow<Resource<List<Ride>>>
    fun joinRide(rideId: String, userId: String): Flow<Resource<String>>
    fun getMyRides(userId: String): Flow<Resource<List<Ride>>>
}
