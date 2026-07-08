package com.campusconnect.core.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

object TranslationHelper {
    fun translate(text: String, targetLanguageCode: String, onResult: (String?) -> Unit) {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { sourceLangCode ->
                val detectedCode = if (sourceLangCode == "und") "en" else sourceLangCode
                
                // If detected source language is the same as target language, no need to translate
                if (detectedCode.equals(targetLanguageCode, ignoreCase = true)) {
                    onResult(text)
                    return@addOnSuccessListener
                }

                val sourceLang = getTranslateLanguage(detectedCode)
                val targetLang = getTranslateLanguage(targetLanguageCode)

                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLang)
                    .setTargetLanguage(targetLang)
                    .build()

                val translator = Translation.getClient(options)
                val conditions = DownloadConditions.Builder().build()

                translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener {
                        translator.translate(text)
                            .addOnSuccessListener { translatedText ->
                                onResult(translatedText)
                                translator.close()
                            }
                            .addOnFailureListener {
                                onResult(null)
                                translator.close()
                            }
                    }
                    .addOnFailureListener {
                        onResult(null)
                        translator.close()
                    }
            }
            .addOnFailureListener {
                // Fallback to English as source language if identification fails
                val sourceLang = TranslateLanguage.ENGLISH
                val targetLang = getTranslateLanguage(targetLanguageCode)

                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLang)
                    .setTargetLanguage(targetLang)
                    .build()

                val translator = Translation.getClient(options)
                val conditions = DownloadConditions.Builder().build()

                translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener {
                        translator.translate(text)
                            .addOnSuccessListener { translatedText ->
                                onResult(translatedText)
                                translator.close()
                            }
                            .addOnFailureListener {
                                onResult(null)
                                translator.close()
                            }
                    }
                    .addOnFailureListener {
                        onResult(null)
                        translator.close()
                    }
            }
    }

    private fun getTranslateLanguage(langCode: String): String {
        return when (langCode.lowercase()) {
            "ur" -> TranslateLanguage.URDU
            "ar" -> TranslateLanguage.ARABIC
            "es" -> TranslateLanguage.SPANISH
            "fr" -> TranslateLanguage.FRENCH
            "en" -> TranslateLanguage.ENGLISH
            else -> TranslateLanguage.ENGLISH
        }
    }
}
