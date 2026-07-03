package com.campusconnect

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.campusconnect.core.theme.ThemeManager
import com.campusconnect.core.theme.ThemeMode
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CampusConnectApp : Application() {

    @Inject
    lateinit var themeManager: ThemeManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            val mode = themeManager.themeMode.first()
            applyTheme(mode)
            themeManager.themeMode.collect { applyTheme(it) }
        }
    }

    private fun applyTheme(mode: ThemeMode) {
        val nightMode = when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
