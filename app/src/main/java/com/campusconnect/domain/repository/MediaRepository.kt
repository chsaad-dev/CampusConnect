package com.campusconnect.domain.repository

import android.net.Uri

interface MediaRepository {
    suspend fun uploadImage(uri: Uri): String
    suspend fun uploadDocument(uri: Uri): String
    suspend fun uploadVideo(uri: Uri): String
}
