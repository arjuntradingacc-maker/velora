package com.velora.vault.core.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Arrays
import javax.inject.Inject
import javax.inject.Singleton

sealed interface VaultSession {
    data object Locked : VaultSession
    data class Unlocked(val vaultKey: ByteArray) : VaultSession
}

/**
 * The single source of truth for whether the vault is open. Holds the raw
 * vault key only in memory while unlocked, zeroes it out the moment the
 * vault locks, and enforces both "lock immediately on background" and a
 * configurable auto-lock timer by observing the process lifecycle —
 * exactly the behavior described in the security settings, independent of
 * which screen happens to be on top.
 */
@Singleton
class VaultSessionManager @Inject constructor() : DefaultLifecycleObserver {

    private val _session = MutableStateFlow<VaultSession>(VaultSession.Locked)
    val session: StateFlow<VaultSession> = _session.asStateFlow()

    /** 0 = lock immediately on background. Otherwise a grace period in seconds. */
    var autoLockSeconds: Int = 30
    var lockImmediatelyOnBackground: Boolean = true

    private var backgroundedAtMillis: Long? = null

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        backgroundedAtMillis = System.currentTimeMillis()
    }

    override fun onStart(owner: LifecycleOwner) {
        val backgroundedAt = backgroundedAtMillis ?: return
        backgroundedAtMillis = null
        val elapsedSeconds = (System.currentTimeMillis() - backgroundedAt) / 1000
        if (lockImmediatelyOnBackground || elapsedSeconds >= autoLockSeconds) {
            lock()
        }
    }

    fun unlock(vaultKey: ByteArray) {
        _session.value = VaultSession.Unlocked(vaultKey)
    }

    fun lock() {
        (_session.value as? VaultSession.Unlocked)?.vaultKey?.let { Arrays.fill(it, 0) }
        _session.value = VaultSession.Locked
    }

    fun isUnlocked(): Boolean = _session.value is VaultSession.Unlocked

    fun currentVaultKeyOrNull(): ByteArray? = (_session.value as? VaultSession.Unlocked)?.vaultKey
}
