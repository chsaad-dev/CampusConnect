package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Event
import com.example.campusconnect.data.model.Attendance
import com.example.campusconnect.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EventRepository {

    override fun getEvents(): Flow<Resource<List<Event>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("events")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching events"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val events = snapshot.toObjects(Event::class.java)
                    trySend(Resource.Success(events))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun createEvent(event: Event): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val id = firestore.collection("events").document().id
            val newEvent = event.copy(id = id)
            firestore.collection("events").document(id).set(newEvent).await()
            trySend(Resource.Success("Event created"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to create event"))
        }
        awaitClose()
    }

    override fun registerForEvent(eventId: String, userId: String): Flow<Resource<String>> = callbackFlow {
        try {
            firestore.collection("events").document(eventId)
                .update("attendeesCount", FieldValue.increment(1)).await()
            trySend(Resource.Success("Registered successfully"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Registration failed"))
        }
        awaitClose()
    }

    override fun verifyAttendance(eventId: String, userId: String): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val attendanceId = "${eventId}_${userId}"
            val attendance = Attendance(
                id = attendanceId,
                eventId = eventId,
                studentId = userId
            )
            firestore.collection("attendance").document(attendanceId).set(attendance).await()
            trySend(Resource.Success("Attendance verified successfully"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to verify attendance"))
        }
        awaitClose()
    }

    override fun getUpcomingEvents(): Flow<Resource<List<Event>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("events")
            .whereGreaterThan("timestamp", System.currentTimeMillis())
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching upcoming events"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val events = snapshot.toObjects(Event::class.java)
                    trySend(Resource.Success(events))
                }
            }
        awaitClose { subscription.remove() }
    }
}
