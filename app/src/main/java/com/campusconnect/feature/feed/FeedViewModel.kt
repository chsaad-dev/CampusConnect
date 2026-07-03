package com.campusconnect.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.Comment
import com.campusconnect.domain.model.Post
import com.campusconnect.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _feedState = MutableStateFlow<Resource<List<Post>>?>(null)
    val feedState: StateFlow<Resource<List<Post>>?> = _feedState.asStateFlow()

    private val _commentsState = MutableStateFlow<Resource<List<Comment>>?>(null)
    val commentsState: StateFlow<Resource<List<Comment>>?> = _commentsState.asStateFlow()

    private val _commentActionState = MutableStateFlow<Resource<Comment>?>(null)
    val commentActionState: StateFlow<Resource<Comment>?> = _commentActionState.asStateFlow()

    private val postsList = mutableListOf<Post>()
    private var lastVisibleTimestamp: Long? = null
    private var isEndReached = false

    fun loadInitialFeed() {
        postsList.clear()
        lastVisibleTimestamp = null
        isEndReached = false
        fetchNextFeedBatch()
    }

    fun fetchNextFeedBatch() {
        if (isEndReached) return
        
        postRepository.getFeed(lastVisibleTimestamp, 10).onEach { resource ->
            when (resource) {
                is Resource.Success -> {
                    val newPosts = resource.data
                    if (newPosts.isEmpty()) {
                        isEndReached = true
                    } else {
                        lastVisibleTimestamp = newPosts.last().createdAt
                        postsList.addAll(newPosts)
                    }
                    _feedState.value = Resource.Success(postsList.toList())
                }
                is Resource.Error -> {
                    _feedState.value = resource
                }
                is Resource.Loading -> {
                    if (postsList.isEmpty()) {
                        _feedState.value = Resource.Loading
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun toggleLike(post: Post) {
        postRepository.toggleLike(post.postId).onEach { resource ->
            if (resource is Resource.Success) {
                val isLiked = resource.data
                val updatedList = postsList.map { p ->
                    if (p.postId == post.postId) {
                        p.copy(
                            isLikedByCurrentUser = isLiked,
                            likeCount = if (isLiked) p.likeCount + 1 else p.likeCount - 1
                        )
                    } else p
                }
                postsList.clear()
                postsList.addAll(updatedList)
                _feedState.value = Resource.Success(postsList.toList())
            }
        }.launchIn(viewModelScope)
    }

    fun fetchComments(postId: String) {
        postRepository.getComments(postId).onEach { result ->
            _commentsState.value = result
        }.launchIn(viewModelScope)
    }

    fun addComment(postId: String, text: String) {
        postRepository.addComment(postId, text).onEach { result ->
            _commentActionState.value = result
            
            // Optimistic update of comment count in local feed state
            if (result is Resource.Success) {
                val updatedList = postsList.map { p ->
                    if (p.postId == postId) {
                        p.copy(commentCount = p.commentCount + 1)
                    } else p
                }
                postsList.clear()
                postsList.addAll(updatedList)
                _feedState.value = Resource.Success(postsList.toList())
            }
        }.launchIn(viewModelScope)
    }

    fun clearCommentActionState() { _commentActionState.value = null }
}
