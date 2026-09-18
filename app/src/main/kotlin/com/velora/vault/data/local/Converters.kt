package com.velora.vault.data.local

import androidx.room.TypeConverter
import com.velora.vault.data.model.CustomField
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

private val json = Json { ignoreUnknownKeys = true }

class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        json.encodeToString(ListSerializer(String.serializer()), value ?: emptyList())

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        if (value.isNullOrBlank()) emptyList()
        else json.decodeFromString(ListSerializer(String.serializer()), value)

    @TypeConverter
    fun fromCustomFieldList(value: List<CustomField>?): String =
        json.encodeToString(ListSerializer(CustomField.serializer()), value ?: emptyList())

    @TypeConverter
    fun toCustomFieldList(value: String?): List<CustomField> =
        if (value.isNullOrBlank()) emptyList()
        else json.decodeFromString(ListSerializer(CustomField.serializer()), value)
}
