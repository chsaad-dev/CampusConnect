package com.campusconnect.domain.repository

import com.campusconnect.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface AssistantRepository {
    fun getCampusFacts(): Flow<Resource<List<Pair<String, String>>>>
}
