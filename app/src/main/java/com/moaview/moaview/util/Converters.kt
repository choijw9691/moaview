package com.moaview.moaview.util

import android.R.attr.data
import androidx.room.TypeConverter
import com.google.gson.Gson
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long? {
        return if (data == null) null else date.time
    }

    @TypeConverter
    fun listToJson(value: List<Date>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): List<Date>? {
        return Gson().fromJson(value,Array<Date>::class.java)?.toList()
    }
}


