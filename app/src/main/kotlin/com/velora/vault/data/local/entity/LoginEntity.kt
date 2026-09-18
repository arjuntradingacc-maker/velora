package com.velora.vault.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.velora.vault.data.model.CustomField

/**
 * A saved login. The whole database file is encrypted at rest via SQLCipher
 * (passphrase = the session's vault key, see VeloraDatabaseProvider), so
 * [password] is stored as plaintext *within that encrypted file* rather
 * than being separately re-encrypted per field — the same approach used by
 * the on-disk format of most desktop password managers.
 */
@Entity(tableName = "logins")
data class LoginEntity(
    @PrimaryKey val id: String,
    val name: String,
    val websiteUrl: String?,
    val username: String?,
    val password: String,
    val notes: String?,
    val tags: List<String> = emptyList(),
    val customFields: List<CustomField> = emptyList(),
    val iconUrl: String? = null,
    val isFavorite: Boolean = false,
    val passwordUpdatedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val lastUsedAt: Long? = null,
)
