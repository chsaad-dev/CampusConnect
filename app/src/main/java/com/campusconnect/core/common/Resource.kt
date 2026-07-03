package com.campusconnect.core.common

/**
 * Generic wrapper for UI state that represents Loading, Success, or Error.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}
