package com.campusconnect.feature.post

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.MediaType
import com.campusconnect.domain.model.Post
import com.campusconnect.domain.model.PostType
import com.campusconnect.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uploadState = MutableStateFlow<Resource<Unit>?>(null)
    val uploadState: StateFlow<Resource<Unit>?> = _uploadState.asStateFlow()

    fun createPost(
        caption: String,
        type: PostType,
        mediaType: MediaType,
        fileUri: Uri?,
        department: String,
        visibility: String = "public"
    ) {
        val post = Post(
            type = type,
            caption = caption,
            mediaType = mediaType,
            department = department,
            visibility = visibility
        )

        postRepository.createPost(post, fileUri).onEach { result ->
            _uploadState.value = result
        }.launchIn(viewModelScope)
    }

    fun resetUploadState() {
        _uploadState.value = null
    }
}
