package com.velora.vault.data.repository

import com.velora.vault.data.local.entity.BreachRecordEntity
import com.velora.vault.data.local.entity.BreachStatus
import com.velora.vault.data.local.entity.LoginEntity
import com.velora.vault.data.remote.BreachCheckApi
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

sealed interface BreachCheckResult {
    data class Completed(val checkedCount: Int, val flaggedCount: Int) : BreachCheckResult
    data object Offline : BreachCheckResult
}

/**
 * Privacy-preserving breach lookup, modeled on the k-anonymity range API
 * pattern (as used by Have I Been Pwned): only the first 5 hex characters
 * of a SHA-1 hash of each password ever leave the device. The full hash,
 * the password, and the username are compared against the returned
 * candidate list locally. This performs a periodic, on-demand check
 * against a snapshot dataset — it is explicitly *not* real-time monitoring,
 * and the UI must not describe it as such.
 */
@Singleton
class BreachCheckRepository @Inject constructor(
    private val breachCheckApi: BreachCheckApi,
    private val vaultRepository: VaultRepository,
) {
    suspend fun checkAll(logins: List<LoginEntity>): BreachCheckResult {
        var flagged = 0
        var reachedNetwork = false
        for (login in logins) {
            if (login.password.isBlank()) continue
            val hash = sha1Hex(login.password)
            val prefix = hash.take(5)
            val suffix = hash.substring(5)

            val matchedSuffixes = runCatching { breachCheckApi.checkPrefix(prefix) }.getOrNull()
            if (matchedSuffixes == null) continue
            reachedNetwork = true

            val isCompromised = matchedSuffixes.any { it.equals(suffix, ignoreCase = true) }
            if (isCompromised) {
                flagged++
                vaultRepository.saveBreachRecord(
                    BreachRecordEntity(
                        loginId = login.id,
                        status = BreachStatus.CRITICAL,
                        serviceName = login.name,
                        discoveredAt = System.currentTimeMillis(),
                        lastCheckedAt = System.currentTimeMillis(),
                        recommendedAction = "Change this password now — it appeared in a known breach dataset.",
                    ),
                )
            } else {
                vaultRepository.saveBreachRecord(
                    BreachRecordEntity(
                        loginId = login.id,
                        status = BreachStatus.SAFE,
                        serviceName = login.name,
                        discoveredAt = 0L,
                        lastCheckedAt = System.currentTimeMillis(),
                        recommendedAction = "No action needed.",
                    ),
                )
            }
        }
        return if (reachedNetwork) {
            BreachCheckResult.Completed(checkedCount = logins.size, flaggedCount = flagged)
        } else {
            BreachCheckResult.Offline
        }
    }

    private fun sha1Hex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02X".format(it) }
    }
}
