package com.velora.vault.core.util

import kotlin.math.ln
import kotlin.math.min

enum class StrengthLabel { VeryWeak, Weak, Fair, Good, Strong }

data class PasswordStrengthResult(
    /** 0f..1f, meant for progress bars / rings. */
    val score: Float,
    val label: StrengthLabel,
    val estimatedBitsOfEntropy: Double,
    val warnings: List<String>,
)

/**
 * A transparent, explainable strength heuristic — not a cryptographic
 * primitive. Estimates entropy from the effective character-set size and
 * length, then applies clear, named penalties for common weaknesses so the
 * Security Center can show *why* a password scored the way it did.
 *
 * This never claims to model real-world crackability with the precision of
 * a dictionary-backed estimator; it is deliberately conservative.
 */
object PasswordStrength {

    fun evaluate(password: String): PasswordStrengthResult {
        if (password.isEmpty()) {
            return PasswordStrengthResult(0f, StrengthLabel.VeryWeak, 0.0, listOf("This field is empty."))
        }

        var poolSize = 0
        if (password.any { it.isLowerCase() }) poolSize += 26
        if (password.any { it.isUpperCase() }) poolSize += 26
        if (password.any { it.isDigit() }) poolSize += 10
        if (password.any { !it.isLetterOrDigit() }) poolSize += 33
        if (poolSize == 0) poolSize = 26

        val rawBits = password.length * (ln(poolSize.toDouble()) / ln(2.0))

        val warnings = mutableListOf<String>()
        var penalty = 0.0

        val lower = password.lowercase()
        if (hasSequentialRun(lower, 4)) {
            warnings += "Contains a sequential run of characters (e.g. \"abcd\", \"1234\")."
            penalty += 14.0
        }
        if (hasRepeatedRun(password, 3)) {
            warnings += "Contains a repeated character run (e.g. \"aaa\")."
            penalty += 10.0
        }
        if (COMMON_PATTERNS.any { lower.contains(it) }) {
            warnings += "Contains a commonly-used word or pattern."
            penalty += 20.0
        }
        if (password.length < 10) {
            warnings += "Shorter than the recommended 12+ characters."
            penalty += 8.0
        }
        val distinctRatio = password.toSet().size.toDouble() / password.length
        if (distinctRatio < 0.5) {
            warnings += "Uses very few distinct characters."
            penalty += 8.0
        }

        val effectiveBits = (rawBits - penalty).coerceAtLeast(0.0)

        val score = (effectiveBits / 100.0).toFloat().coerceIn(0f, 1f)
        val label = when {
            effectiveBits < 28 -> StrengthLabel.VeryWeak
            effectiveBits < 40 -> StrengthLabel.Weak
            effectiveBits < 60 -> StrengthLabel.Fair
            effectiveBits < 80 -> StrengthLabel.Good
            else -> StrengthLabel.Strong
        }

        return PasswordStrengthResult(
            score = min(1f, score),
            label = label,
            estimatedBitsOfEntropy = effectiveBits,
            warnings = warnings,
        )
    }

    private fun hasSequentialRun(text: String, runLength: Int): Boolean {
        if (text.length < runLength) return false
        for (start in 0..text.length - runLength) {
            var ascending = true
            var descending = true
            for (i in 1 until runLength) {
                val diff = text[start + i].code - text[start + i - 1].code
                if (diff != 1) ascending = false
                if (diff != -1) descending = false
            }
            if (ascending || descending) return true
        }
        return false
    }

    private fun hasRepeatedRun(text: String, runLength: Int): Boolean {
        if (text.length < runLength) return false
        var streak = 1
        for (i in 1 until text.length) {
            streak = if (text[i] == text[i - 1]) streak + 1 else 1
            if (streak >= runLength) return true
        }
        return false
    }

    private val COMMON_PATTERNS = listOf(
        "password", "qwerty", "letmein", "welcome", "admin", "iloveyou",
        "dragon", "monkey", "football", "master", "login", "abc123",
    )
}
