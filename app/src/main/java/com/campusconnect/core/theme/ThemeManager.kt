package com.campusconnect.core.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.campusconnect.core.common.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME
)

/**
 * Manages theme preference using DataStore. Default is LIGHT.
 * Never auto-follows system dark mode — only changes via explicit user toggle.
 */
@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeKey = stringPreferencesKey(Constants.PREF_THEME_MODE)

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.fromString(prefs[themeKey] ?: ThemeMode.LIGHT.name)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeKey] = mode.name
        }
    }

    suspend fun toggleTheme() {
        context.dataStore.edit { prefs ->
            val current = ThemeMode.fromString(prefs[themeKey] ?: ThemeMode.LIGHT.name)
            prefs[themeKey] = when (current) {
                ThemeMode.LIGHT -> ThemeMode.DARK.name
                ThemeMode.DARK -> ThemeMode.LIGHT.name
            }
        }
    }
}
