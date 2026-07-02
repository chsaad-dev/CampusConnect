package com.example.campusconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.data.model.Note
import com.example.campusconnect.data.repository.NotesRepository
import com.example.campusconnect.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repo: NotesRepository
) : ViewModel() {
    private val _notes = MutableLiveData<Resource<List<Note>>>()
    val notes: LiveData<Resource<List<Note>>> = _notes

    private val _uploadStatus = MutableLiveData<Resource<String>>()
    val uploadStatus: LiveData<Resource<String>> = _uploadStatus

    fun fetchNotes(department: String, semester: String) {
        repo.getNotes(department, semester).onEach { _notes.value = it }.launchIn(viewModelScope)
    }

    fun uploadNote(note: Note, uri: android.net.Uri) {
        repo.uploadNote(note, uri).onEach { _uploadStatus.value = it }.launchIn(viewModelScope)
    }

    fun searchNotes(query: String) {
        repo.searchNotes(query).onEach { _notes.value = it }.launchIn(viewModelScope)
    }

    fun downloadNote(note: Note) {
        repo.downloadNote(note)
    }
}
