package com.example.campusconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.data.repository.SearchRepository
import com.example.campusconnect.data.repository.SearchResult
import com.example.campusconnect.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: SearchRepository
) : ViewModel() {
    private val _searchResults = MutableLiveData<Resource<List<SearchResult>>>()
    val searchResults: LiveData<Resource<List<SearchResult>>> = _searchResults

    fun searchAll(query: String) {
        repo.searchAll(query).onEach { _searchResults.value = it }.launchIn(viewModelScope)
    }
}
