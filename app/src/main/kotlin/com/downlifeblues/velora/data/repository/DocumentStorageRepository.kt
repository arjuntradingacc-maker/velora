package com.downlifeblues.velora.data.repository

import android.content.Context
import com.downlifeblues.velora.core.security.EncryptedPayload
import com.downlifeblues.velora.core.security.KeystoreCryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypts document bytes with a dedicated Keystore-backed AES-GCM key
 * before writing them under the app's private storage. This sits
 * alongside (not instead of) the SQLCipher-encrypted database: files
 * never live inside the SQLite file, so they get their own envelope.
 */
@Singleton
class DocumentStorageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keystoreCrypto: KeystoreCryptoManager,
) {
    private val documentsDir: File
        get() = File(context.filesDir, "encrypted_documents").apply { mkdirs() }

    fun storeDocument(bytes: ByteArray): String {
        val payload = keystoreCrypto.encrypt(KeystoreCryptoManager.ALIAS_DOCUMENT_KEY, bytes)
        val file = File(documentsDir, "${UUID.randomUUID()}.velora")
        file.writeText(payload.toStoredString())
        return file.absolutePath
    }

    fun readDocument(path: String): ByteArray {
        val stored = File(path).readText()
        val payload = EncryptedPayload.fromStoredString(stored)
        return keystoreCrypto.decrypt(KeystoreCryptoManager.ALIAS_DOCUMENT_KEY, payload)
    }

    fun deleteDocument(path: String) {
        File(path).delete()
    }
}
