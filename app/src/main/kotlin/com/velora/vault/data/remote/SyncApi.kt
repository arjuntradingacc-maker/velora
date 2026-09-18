package com.velora.vault.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST

/** One already client-side-encrypted vault record. The server only ever stores [ciphertext]. */
@Serializable
data class EncryptedVaultRecord(
    val id: String,
    val category: String,
    val ciphertext: String,
    val nonce: String,
    val version: Long,
    val updatedAt: Long,
)

@Serializable
data class SyncPushRequest(val deviceId: String, val records: List<EncryptedVaultRecord>)

@Serializable
data class SyncPullResponse(val records: List<EncryptedVaultRecord>, val serverTimeMillis: Long)

@Serializable
data class DeviceDto(val id: String, val name: String, val platform: String, val lastSyncAt: Long?)

/**
 * Encrypted sync transport. Every [EncryptedVaultRecord.ciphertext] is
 * produced client-side (AES-GCM, sealed under the vault key) before it ever
 * reaches this API — the backend is architecturally incapable of reading
 * vault contents, only of storing and versioning opaque blobs and
 * resolving push/pull conflicts by [EncryptedVaultRecord.version].
 */
interface SyncApi {
    @POST("v1/sync/push")
    suspend fun push(@Body request: SyncPushRequest)

    @GET("v1/sync/pull")
    suspend fun pull(): SyncPullResponse

    @GET("v1/sync/devices")
    suspend fun devices(): List<DeviceDto>

    @DELETE("v1/sync/devices/{id}")
    suspend fun removeDevice(@Path("id") id: String)
}

/**
 * k-anonymity breach lookup: the client sends only the first 5 hex
 * characters of a SHA-1 hash of the credential, and filters the returned
 * suffix list locally — mirroring the Have I Been Pwned range API design
 * so the full credential (or its full hash) never leaves the device.
 */
interface BreachCheckApi {
    @GET("v1/breach-check/{hashPrefix}")
    suspend fun checkPrefix(@Path("hashPrefix") hashPrefix: String): List<String>
}
