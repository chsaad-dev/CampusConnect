package com.campusconnect.core.utils

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.campusconnect.core.network.GeminiClient
import com.google.gson.Gson

data class ModerationResult(
    val safe: Boolean = true,
    val category: String = "none",
    val confidence: Float = 1.0f
)

object ContentModerator {
    suspend fun moderateImage(context: Context, uri: Uri): ModerationResult {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
            val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val prompt = """
                Classify this image for a university social app. Respond only JSON, no markdown:
                {"safe": true/false, "category": "none|nudity|violence|gore|drugs|other", "confidence": 0.0}
            """.trimIndent()
            val response = GeminiClient.withBackoff {
                GeminiClient.generateContent(prompt, base64Image, "image/jpeg")
            }
            val cleaned = response.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()
            Gson().fromJson(cleaned, ModerationResult::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            // In case of API failure, default to safe to prevent blocking user actions
            ModerationResult(safe = true, category = "none", confidence = 1.0f)
        }
    }
}
