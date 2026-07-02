package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Event
import com.example.campusconnect.data.model.Job
import com.example.campusconnect.data.model.Note
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchAll(query: String): Flow<Resource<List<SearchResult>>>
}

sealed class SearchResult {
    data class NoteResult(val note: Note) : SearchResult()
    data class JobResult(val job: Job) : SearchResult()
    data class EventResult(val event: Event) : SearchResult()
}
