package com.campusconnect.data.repository

import android.net.Uri
import com.campusconnect.core.common.Constants
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.Event
import com.campusconnect.domain.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.campusconnect.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val mediaRepository: MediaRepository,
    private val eventDao: com.campusconnect.data.local.EventDao
) : EventRepository {

    private val currentUid: String
        get() = auth.currentUser?.uid ?: throw Exception("User not logged in")

    override fun getEvents(): Flow<Resource<List<Event>>> = flow {
        val cached = eventDao.getCachedEvents()
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached.map { it.toDomain() }))
        } else {
            emit(Resource.Loading)
        }
        try {
            val snapshot = firestore.collection(Constants.COLLECTION_EVENTS)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()

            val events = snapshot.toObjects(Event::class.java)

            eventDao.clearAllEvents()
            eventDao.insertEvents(events.map { com.campusconnect.data.model.EventEntity.fromDomain(it) })

            emit(Resource.Success(events))
        } catch (e: Exception) {
            val cachedAgain = eventDao.getCachedEvents()
            if (cachedAgain.isEmpty()) {
                emit(Resource.Error(e.message ?: "Failed to fetch events"))
            }
        }
    }

    override fun getEventDetails(eventId: String): Flow<Resource<Event>> = flow {
        emit(Resource.Loading)
        try {
            val doc = firestore.collection(Constants.COLLECTION_EVENTS)
                .document(eventId)
                .get()
                .await()

            val event = doc.toObject(Event::class.java) ?: throw Exception("Event not found")
            emit(Resource.Success(event))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch event details"))
        }
    }

    override fun registerForEvent(eventId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val uid = currentUid
            firestore.collection(Constants.COLLECTION_EVENTS)
                .document(eventId)
                .update("registeredUsers", FieldValue.arrayUnion(uid))
                .await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Registration failed"))
        }
    }

    override fun createEvent(event: Event, bannerUri: Uri?): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val eventId = firestore.collection(Constants.COLLECTION_EVENTS).document().id
            var uploadUrl = ""

            if (bannerUri != null) {
                uploadUrl = mediaRepository.uploadImage(bannerUri)
            }

            val finalEvent = event.copy(
                eventId = eventId,
                bannerUrl = uploadUrl,
                qrCode = "TICKET_$eventId",
                createdAt = System.currentTimeMillis()
            )

            val usersSnapshot = firestore.collection(Constants.COLLECTION_USERS).get().await()
            val batch = firestore.batch()
            batch.set(firestore.collection(Constants.COLLECTION_EVENTS).document(eventId), finalEvent)

            for (doc in usersSnapshot.documents) {
                val targetUid = doc.id
                val notifRef = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                    .document(targetUid)
                    .collection("items")
                    .document()

                val notifItem = com.campusconnect.domain.model.NotificationItem(
                    notifId = notifRef.id,
                    title = "New Campus Event: ${finalEvent.title}",
                    body = "${finalEvent.hostType} is hosting an event at ${finalEvent.location}.",
                    type = "event",
                    refId = eventId,
                    createdAt = System.currentTimeMillis()
                )
                batch.set(notifRef, notifItem)
            }

            batch.commit().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create event"))
        }
    }
}
