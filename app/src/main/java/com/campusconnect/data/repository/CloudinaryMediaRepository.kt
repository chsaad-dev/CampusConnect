package com.campusconnect.data.repository

import android.content.Context
import android.net.Uri
import com.campusconnect.core.network.CloudinaryUploader
import com.campusconnect.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudinaryMediaRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : MediaRepository {
    
    override suspend fun uploadImage(uri: Uri): String {
        return CloudinaryUploader.upload(context, uri, "image")
    }

    override suspend fun uploadDocument(uri: Uri): String {
        return CloudinaryUploader.upload(context, uri, "raw")
    }

    override suspend fun uploadVideo(uri: Uri): String {
        return CloudinaryUploader.upload(context, uri, "video")
    }
}
