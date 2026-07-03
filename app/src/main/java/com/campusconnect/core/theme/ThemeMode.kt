package com.campusconnect.core.theme


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
