package com.downlifeblues.velora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NoteFormat { PLAIN, RICH_TEXT, CHECKLIST }

@Entity(tableName = "secure_notes")
data class SecureNoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val format: NoteFormat = NoteFormat.PLAIN,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "custom_items")
data class CustomItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val categoryLabel: String,
    val fields: List<com.downlifeblues.velora.data.model.CustomField> = emptyList(),
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
