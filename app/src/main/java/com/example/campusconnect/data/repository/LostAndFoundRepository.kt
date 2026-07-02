package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.LostItem
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow
import android.net.Uri

interface LostAndFoundRepository {
    fun reportItem(item: LostItem, imageUri: Uri?): Flow<Resource<String>>
    fun getItems(): Flow<Resource<List<LostItem>>>
    fun updateItemStatus(itemId: String, status: String): Flow<Resource<String>>
    fun searchItems(query: String): Flow<Resource<List<LostItem>>>
}
