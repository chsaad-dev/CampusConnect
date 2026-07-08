package com.campusconnect.data.repository

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.repository.AssistantRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AssistantRepository {

    private var cachedFacts: List<Pair<String, String>>? = null

    override fun getCampusFacts(): Flow<Resource<List<Pair<String, String>>>> = flow {
        val cached = cachedFacts
        if (cached != null) {
            emit(Resource.Success(cached))
            return@flow
        }
        
        emit(Resource.Loading)
        try {
            val snapshot = firestore.collection("campusFacts")
                .get()
                .await()
            val list = snapshot.documents.mapNotNull { doc ->
                val topic = doc.getString("topic") ?: ""
                val content = doc.getString("content") ?: ""
                if (topic.isNotEmpty() && content.isNotEmpty()) {
                    Pair(topic, content)
                } else {
                    null
                }
            }
            cachedFacts = list
            emit(Resource.Success(list))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch facts"))
        }
    }
}
