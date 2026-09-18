package com.velora.vault.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DocumentType { PDF, IMAGE, TEXT, OTHER }

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: DocumentType,
    /** Path to the file under the app's private storage; its bytes are AES-GCM encrypted independently
     *  of the SQLCipher database using [com.velora.vault.core.security.KeystoreCryptoManager.ALIAS_DOCUMENT_KEY],
     *  since files live outside the encrypted DB file itself. */
    val encryptedFilePath: String,
    val sizeBytes: Long,
    val thumbnailPath: String? = null,
    val expiresAt: Long? = null,
    val reminderEnabled: Boolean = false,
    val isFavorite: Boolean = false,
    val requiresReauth: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
)
