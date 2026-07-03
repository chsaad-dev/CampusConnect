package com.campusconnect.feature.post_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.BloodRequestDetails
import com.campusconnect.domain.model.LostFoundDetails
import com.campusconnect.domain.model.NoteDetails
import com.campusconnect.domain.model.PostType
import com.campusconnect.domain.model.RideDetails
import com.campusconnect.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: com.campusconnect.domain.repository.UserRepository
) : ViewModel() {

    private val _noteDetails = MutableStateFlow<Resource<NoteDetails>>(Resource.Loading)
    val noteDetails: StateFlow<Resource<NoteDetails>> = _noteDetails.asStateFlow()

    private val _bloodRequestDetails = MutableStateFlow<Resource<BloodRequestDetails>>(Resource.Loading)
    val bloodRequestDetails: StateFlow<Resource<BloodRequestDetails>> = _bloodRequestDetails.asStateFlow()

    private val _lostFoundDetails = MutableStateFlow<Resource<LostFoundDetails>>(Resource.Loading)
    val lostFoundDetails: StateFlow<Resource<LostFoundDetails>> = _lostFoundDetails.asStateFlow()

    private val _rideDetails = MutableStateFlow<Resource<RideDetails>>(Resource.Loading)
    val rideDetails: StateFlow<Resource<RideDetails>> = _rideDetails.asStateFlow()

    fun loadDetails(postId: String, postType: PostType) {
        when (postType) {
            PostType.NOTE -> {
                postRepository.getNoteDetails(postId).onEach { result ->
                    _noteDetails.value = result
                    if (result is Resource.Success) {
                        trackSubject(result.data.subject)
                    }
                }.launchIn(viewModelScope)
            }
            PostType.BLOOD -> {
                postRepository.getBloodRequestDetails(postId).onEach { result ->
                    _bloodRequestDetails.value = result
                }.launchIn(viewModelScope)
            }
            PostType.LOST_FOUND -> {
                postRepository.getLostFoundDetails(postId).onEach { result ->
                    _lostFoundDetails.value = result
                }.launchIn(viewModelScope)
            }
            PostType.RIDE -> {
                postRepository.getRideDetails(postId).onEach { result ->
                    _rideDetails.value = result
                }.launchIn(viewModelScope)
            }
        }
    }

    private fun trackSubject(subject: String) {
        if (subject.isNotEmpty()) {
            userRepository.trackSubjectView(subject).launchIn(viewModelScope)
        }
    }
}
