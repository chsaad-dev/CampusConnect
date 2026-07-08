package com.campusconnect.core.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "campus_connect_preferences"
)

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val targetLangKey = stringPreferencesKey("target_translation_language")

    val targetTranslationLanguage: Flow<String> = context.preferencesDataStore.data.map { prefs ->
        prefs[targetLangKey] ?: "ur" // Default is Urdu
    }

    suspend fun setTargetTranslationLanguage(langCode: String) {
        context.preferencesDataStore.edit { prefs ->
            prefs[targetLangKey] = langCode
        }
    }
}
