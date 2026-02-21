package com.GR8Studios.souc.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PostConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromPlatforms(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toPlatforms(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromCaptionMap(value: Map<String, String>): String = gson.toJson(value)

    @TypeConverter
    fun toCaptionMap(value: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type)
    }
}
