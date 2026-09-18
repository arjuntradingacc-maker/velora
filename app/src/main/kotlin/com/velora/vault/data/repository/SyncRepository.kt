package com.velora.vault.data.repository

import android.os.Build
import com.velora.vault.core.security.SecureVaultPrefs
import com.velora.vault.data.local.entity.DeviceSessionEntity
import com.velora.vault.data.remote.SyncApi
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SyncOutcome {
    data class Success(val pushedCount: Int, val pulledCount: Int) : SyncOutcome
    data class Failed(val reason: String) : SyncOutcome
}

/**
 * Sync is intentionally best-effort and fail-soft: the vault is fully
 * usable offline (per the performance requirements), and a missing or
 * unreachable backend just means "last synced" doesn't advance rather than
 * blocking any local operation. Every record pushed here is expected to
 * already be client-side encrypted by the caller — this repository does
 * not perform encryption itself, only transport and device bookkeeping.
 */
@Singleton
class SyncRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val vaultRepository: VaultRepository,
    private val prefs: SecureVaultPrefs,
) {
    fun observeDevices() = vaultRepository.observeDeviceSessions()

    suspend fun ensureCurrentDeviceRegistered() {
        val deviceId = currentDeviceId()
        vaultRepository.saveDeviceSession(
            DeviceSessionEntity(
                id = deviceId,
                deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
                platform = "Android ${Build.VERSION.RELEASE}",
                lastSyncAt = null,
                isCurrentDevice = true,
                addedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun sync(encryptedRecords: List<com.velora.vault.data.remote.EncryptedVaultRecord>): SyncOutcome {
        return runCatching {
            syncApi.push(com.velora.vault.data.remote.SyncPushRequest(currentDeviceId(), encryptedRecords))
            val pulled = syncApi.pull()
            vaultRepository.markDeviceSynced(currentDeviceId(), System.currentTimeMillis())
            SyncOutcome.Success(pushedCount = encryptedRecords.size, pulledCount = pulled.records.size)
        }.getOrElse {
            SyncOutcome.Failed(it.message ?: "The sync service could not be reached.")
        }
    }

    suspend fun logoutOtherDevices() {
        runCatching { syncApi.devices() }
        vaultRepository.removeAllOtherDeviceSessions()
    }

    fun currentDeviceId(): String {
        val existing = prefs.getString(DEVICE_ID_KEY)
        if (existing != null) return existing
        val id = UUID.randomUUID().toString()
        prefs.putString(DEVICE_ID_KEY, id)
        return id
    }

    companion object {
        private const val DEVICE_ID_KEY = "device_id"
    }
}
