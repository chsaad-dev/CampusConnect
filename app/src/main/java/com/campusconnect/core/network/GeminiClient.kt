package com.campusconnect.core.network

import com.campusconnect.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(prompt: String, base64Image: String? = null, mimeType: String? = null): String =
        withContext(Dispatchers.IO) {
            val parts = JSONArray().apply {
                put(JSONObject().put("text", prompt))
                if (base64Image != null) {
                    put(
                        JSONObject().put(
                            "inline_data", JSONObject()
                                .put("mime_type", mimeType ?: "image/jpeg")
                                .put("data", base64Image)
                        )
                    )
                }
            }

            val contents = JSONArray().put(
                JSONObject().put("parts", parts)
            )

            val payload = JSONObject().put("contents", contents)

            val request = Request.Builder()
                .url(BuildConfig.GEMINI_PROXY_URL)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    throw IOException("Gemini request failed (status ${response.code}): $bodyString")
                }
                
                try {
                    val json = JSONObject(bodyString)
                    json.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                } catch (e: Exception) {
                    throw IOException("Failed to parse Gemini response: ${e.message}. Raw: $bodyString")
                }
            }
        }

    suspend fun <T> withBackoff(block: suspend () -> T): T {
        var attempt = 0
        while (true) {
            try {
                return block()
            } catch (e: IOException) {
                if (attempt >= 3) throw e
                delay((1000L * (1 shl attempt)))
                attempt++
            }
        }
    }
}
