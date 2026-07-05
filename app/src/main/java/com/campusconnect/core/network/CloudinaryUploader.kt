package com.campusconnect.core.network

import android.content.Context
import android.net.Uri
import com.campusconnect.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object CloudinaryUploader {
    private val client = OkHttpClient()

    suspend fun upload(context: Context, uri: Uri, resourceType: String = "image"): String =
        withContext(Dispatchers.IO) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IOException("Failed to open input stream for URI: $uri")

            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "upload", bytes.toRequestBody("application/octet-stream".toMediaType()))
                .addFormDataPart("upload_preset", BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/${BuildConfig.CLOUDINARY_CLOUD_NAME}/$resourceType/upload")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Cloudinary upload failed: ${response.code} - ${response.message}")
                val responseBodyStr = response.body?.string() ?: throw IOException("Empty response body from Cloudinary")
                val json = JSONObject(responseBodyStr)
                json.getString("secure_url")
            }
        }
}
