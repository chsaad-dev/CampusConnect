package com.example.campusconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.data.model.LostItem
import com.example.campusconnect.data.repository.LostAndFoundRepository
import com.example.campusconnect.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LostAndFoundViewModel @Inject constructor(
    private val repo: LostAndFoundRepository
) : ViewModel() {
    private val _items = MutableLiveData<Resource<List<LostItem>>>()
    val items: LiveData<Resource<List<LostItem>>> = _items

    private val _postStatus = MutableLiveData<Resource<String>>()
    val postStatus: LiveData<Resource<String>> = _postStatus

    fun fetchItems() {
        repo.getItems().onEach { _items.value = it }.launchIn(viewModelScope)
    }

    fun postItem(item: LostItem, uri: android.net.Uri?) {
        repo.reportItem(item, uri).onEach { _postStatus.value = it }.launchIn(viewModelScope)
    }

    fun searchItems(query: String) {
        repo.searchItems(query).onEach { _items.value = it }.launchIn(viewModelScope)
    }
}
