package com.downlifeblues.velora.data.repository

import com.downlifeblues.velora.core.util.PasswordStrength
import com.downlifeblues.velora.core.util.StrengthLabel
import com.downlifeblues.velora.data.local.entity.BreachRecordEntity
import com.downlifeblues.velora.data.local.entity.BreachStatus
import com.downlifeblues.velora.data.local.entity.LoginEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

enum class SecurityIssueType { WEAK, REUSED, COMPROMISED, OLD, MISSING_MFA }

data class SecurityIssueGroup(
    val type: SecurityIssueType,
    val title: String,
    val riskExplanation: String,
    val affected: List<LoginEntity>,
)

data class SecurityOverview(
    /** 0..100. See [SecurityRepository.SCORE_EXPLANATION] for exactly how this is computed. */
    val score: Int,
    val totalLogins: Int,
    val issues: List<SecurityIssueGroup>,
)

private const val OLD_PASSWORD_THRESHOLD_DAYS = 180L

/**
 * Computes the Security Center's content entirely from what's already on
 * the device — no network round-trip is required for weak/reused/old/MFA
 * checks, only for the opt-in breach lookup (see [BreachCheckRepository]).
 */
@Singleton
class SecurityRepository @Inject constructor(
    private val vaultRepository: VaultRepository,
) {
    fun observeOverview(): Flow<SecurityOverview> = combine(
        vaultRepository.observeLogins(),
        vaultRepository.observeOtp(),
        vaultRepository.observeBreachRecords(),
    ) { logins, otpAccounts, breachRecords ->
        val weak = logins.filter { login ->
            PasswordStrength.evaluate(login.password).label.let {
                it == StrengthLabel.VeryWeak || it == StrengthLabel.Weak
            }
        }
        val reused = logins.groupBy { it.password }
            .filter { (password, group) -> password.isNotBlank() && group.size > 1 }
            .flatMap { it.value }
            .distinctBy { it.id }
        val old = logins.filter { login ->
            val ageDays = (System.currentTimeMillis() - login.passwordUpdatedAt) / (1000 * 60 * 60 * 24)
            ageDays >= OLD_PASSWORD_THRESHOLD_DAYS
        }
        val otpIssuers = otpAccounts.map { it.issuer.lowercase() }.toSet()
        val missingMfa = logins.filter { login ->
            val host = login.websiteUrl?.lowercase().orEmpty()
            val name = login.name.lowercase()
            otpIssuers.none { host.contains(it) || name.contains(it) }
        }
        val compromisedIds = breachRecords.filter { it.status != BreachStatus.SAFE }.map { it.loginId }.toSet()
        val compromised = logins.filter { it.id in compromisedIds }

        val issues = buildList {
            if (weak.isNotEmpty()) {
                add(
                    SecurityIssueGroup(
                        SecurityIssueType.WEAK,
                        "Weak passwords",
                        "These passwords are short or predictable enough that they could be guessed quickly.",
                        weak,
                    ),
                )
            }
            if (reused.isNotEmpty()) {
                add(
                    SecurityIssueGroup(
                        SecurityIssueType.REUSED,
                        "Reused passwords",
                        "The same password protects more than one account — if one site is breached, all of them are at risk.",
                        reused,
                    ),
                )
            }
            if (compromised.isNotEmpty()) {
                add(
                    SecurityIssueGroup(
                        SecurityIssueType.COMPROMISED,
                        "Exposed credentials",
                        "These logins matched a known breach dataset in your last check.",
                        compromised,
                    ),
                )
            }
            if (old.isNotEmpty()) {
                add(
                    SecurityIssueGroup(
                        SecurityIssueType.OLD,
                        "Old passwords",
                        "Not changed in over ${OLD_PASSWORD_THRESHOLD_DAYS / 30} months. Rotating old passwords limits how long a leaked credential stays useful.",
                        old,
                    ),
                )
            }
            if (missingMfa.isNotEmpty()) {
                add(
                    SecurityIssueGroup(
                        SecurityIssueType.MISSING_MFA,
                        "Missing MFA",
                        "No authenticator code saved for these accounts. Adding one makes a stolen password alone insufficient to sign in.",
                        missingMfa,
                    ),
                )
            }
        }

        SecurityOverview(
            score = computeScore(logins.size, weak.size, reused.size, compromised.size, old.size),
            totalLogins = logins.size,
            issues = issues,
        )
    }

    /**
     * Explainable score: start at 100, subtract a weighted penalty per
     * affected account for each issue category (compromised weighs most,
     * age weighs least), floor at 0. Fewer flagged accounts relative to
     * the vault's total size always score higher.
     */
    private fun computeScore(total: Int, weak: Int, reused: Int, compromised: Int, old: Int): Int {
        if (total == 0) return 100
        val penalty = (compromised * 14.0 + weak * 8.0 + reused * 6.0 + old * 3.0) / total
        return (100 - penalty).toInt().coerceIn(0, 100)
    }

    companion object {
        const val SCORE_EXPLANATION =
            "Start at 100. Subtract, per affected account and averaged across your vault: " +
                "14 points for a confirmed exposure, 8 for a weak password, 6 for a reused password, " +
                "3 for a password older than six months."
    }
}
