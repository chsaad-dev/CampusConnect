package com.campusconnect.domain.repository

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.FriendRequest
import com.campusconnect.domain.model.User
import kotlinx.coroutines.flow.Flow

interface FriendRepository {
    fun searchUsers(query: String): Flow<Resource<List<User>>>
    fun sendFriendRequest(toUser: User): Flow<Resource<Unit>>
    fun getPendingRequests(): Flow<Resource<List<FriendRequest>>>
    fun getFriends(): Flow<Resource<List<User>>>
    fun acceptFriendRequest(request: FriendRequest): Flow<Resource<Unit>>
    fun rejectFriendRequest(request: FriendRequest): Flow<Resource<Unit>>
    fun checkFriendshipStatus(targetUid: String): Flow<Resource<String>> // "pending_sent", "pending_received", "friends", "none"
}
