package com.velora.vault.data.local

import android.content.Context
import androidx.room.Room
import com.velora.vault.core.security.VaultSession
import com.velora.vault.core.security.VaultSessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.sqlcipher.database.SupportFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the lifecycle of the encrypted Room/SQLCipher database. The database
 * only exists while the vault is unlocked — it is opened the instant a
 * vault key becomes available and closed (releasing the passphrase and any
 * cached pages) the instant the session locks, so there is never a window
 * where the plaintext store is reachable without an authenticated session.
 */
@Singleton
class VeloraDatabaseProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    sessionManager: VaultSessionManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _database = MutableStateFlow<VeloraDatabase?>(null)
    val database: StateFlow<VeloraDatabase?> = _database.asStateFlow()

    init {
        scope.launch {
            sessionManager.session.collect { session ->
                when (session) {
                    is VaultSession.Unlocked -> open(session.vaultKey)
                    VaultSession.Locked -> close()
                }
            }
        }
    }

    private fun open(vaultKey: ByteArray) {
        close()
        val factory = SupportFactory(vaultKey.copyOf())
        _database.value = Room.databaseBuilder(context, VeloraDatabase::class.java, VeloraDatabase.FILE_NAME)
            .openHelperFactory(factory)
            // v1 has no prior schema to migrate from; real migrations land here as the schema evolves.
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    private fun close() {
        _database.value?.close()
        _database.value = null
    }

    fun requireDatabase(): VeloraDatabase =
        _database.value ?: error("Vault database accessed while the vault is locked")
}
