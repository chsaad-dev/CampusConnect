package com.campusconnect.data.local

import androidx.room.TypeConverter
import com.campusconnect.domain.model.MediaType
import com.campusconnect.domain.model.PostType

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",")
    }

    @TypeConverter
    fun fromPostType(value: PostType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPostType(value: String?): PostType? {
        return value?.let { PostType.valueOf(it) }
    }

    @TypeConverter
    fun fromMediaType(value: MediaType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toMediaType(value: String?): MediaType? {
        return value?.let { MediaType.valueOf(it) }
    }
}
