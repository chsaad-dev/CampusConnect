package com.campusconnect.core.theme

/**
 * Theme mode enum. The app uses manual user toggle only — never follows system dark mode.
 */
enum class ThemeMode {
    LIGHT,
    DARK;

    companion object {
        fun fromString(value: String): ThemeMode {
            return when (value.uppercase()) {
                "DARK" -> DARK
                else -> LIGHT
            }
        }
    }
}
