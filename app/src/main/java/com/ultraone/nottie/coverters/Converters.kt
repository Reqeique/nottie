package com.ultraone.nottie.coverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ultraone.nottie.util.NULL_VALUE_INT
;

class Converters {
    @TypeConverter
    fun toIntList(value : String): List<Int>?{
        val type = object : TypeToken<List<Int>?>() {}.type
        return if(value.toIntOrNull() == null) {
            Gson().fromJson(value, type)

        } else {
            if(value.toInt() == NULL_VALUE_INT)  null else null

        }

    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return if(value != null) Gson().toJson(value) else NULL_VALUE_INT.toString()
    }

}