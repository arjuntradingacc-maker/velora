package com.downlifeblues.velora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passkeys")
data class PasskeyEntity(
    @PrimaryKey val id: String,
    val relyingPartyName: String,
    val relyingPartyId: String,
    val username: String,
    val credentialId: String,
    /** Opaque handle returned by Android's Credential Manager / FIDO provider — never a raw private key. */
    val providerCredentialRef: String,
    val deviceLabel: String,
    val iconUrl: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long,
    val lastUsedAt: Long? = null,
)

@Entity(tableName = "wifi_networks")
data class WifiEntity(
    @PrimaryKey val id: String,
    val ssid: String,
    val password: String,
    val security: String = "WPA2",
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "otp_accounts")
data class OtpEntity(
    @PrimaryKey val id: String,
    val issuer: String,
    val accountName: String,
    /** AES-GCM-encrypted TOTP seed (see KeystoreCryptoManager.ALIAS_DOCUMENT_KEY-style per-secret wrapping). */
    val encryptedSecret: String,
    val digits: Int = 6,
    val periodSeconds: Int = 30,
    val algorithm: String = "SHA1",
    val iconUrl: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long,
)

@Entity(tableName = "api_keys")
data class ApiKeyEntity(
    @PrimaryKey val id: String,
    val serviceName: String,
    val keyLabel: String,
    val keyValue: String,
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "recovery_codes")
data class RecoveryCodeEntity(
    @PrimaryKey val id: String,
    val serviceName: String,
    val codes: List<String>,
    val usedCodes: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val createdAt: Long,
)
