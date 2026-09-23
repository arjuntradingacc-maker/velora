package com.downlifeblues.velora.core.util

import com.downlifeblues.velora.data.local.entity.CardNetwork

/** Best-effort fields lifted from a photo of the FRONT of a payment card. Never includes a CVV — that only ever lives on the back, and we don't ask a camera to read it. */
data class ScannedCardDetails(
    val number: String? = null,
    val expiryMonth: Int? = null,
    val expiryYear: Int? = null,
    val cardholderName: String? = null,
    val network: CardNetwork? = null,
)

/**
 * Turns raw OCR text lines from a card photo into structured fields. Card fronts are printed in a
 * handful of predictable layouts, so this is plain heuristics rather than anything ML-driven:
 * the longest run of 13–19 grouped digits is the PAN, the first MM/YY-shaped token is the expiry,
 * and the first all-caps line of 2+ words that isn't the number/expiry/a bank name is the holder.
 */
object CardOcrParser {

    private val numberGroupRegex = Regex("\\b(?:\\d[ -]?){13,19}\\b")
    private val expiryRegex = Regex("\\b(0[1-9]|1[0-2])\\s?/\\s?(\\d{2}|\\d{4})\\b")
    private val bankNoiseWords = setOf(
        "BANK", "CREDIT", "DEBIT", "CARD", "VALID", "THRU", "FROM", "MEMBER", "SINCE",
        "PLATINUM", "GOLD", "SIGNATURE", "WORLD", "ELITE", "EXPRESS", "VISA", "MASTERCARD",
        "MAESTRO", "RUPAY", "DISCOVER",
    )

    fun parse(lines: List<String>): ScannedCardDetails {
        val joined = lines.joinToString(" ")

        val number = numberGroupRegex.find(joined)?.value?.filter { it.isDigit() }
            ?.takeIf { it.length in 13..19 }

        val expiryMatch = expiryRegex.find(joined)
        val expiryMonth = expiryMatch?.groupValues?.get(1)?.toIntOrNull()
        val expiryYear = expiryMatch?.groupValues?.get(2)?.let { raw ->
            when (raw.length) {
                2 -> 2000 + raw.toInt()
                else -> raw.toIntOrNull()
            }
        }

        val cardholderName = lines
            .map { it.trim() }
            .filter { line ->
                val letters = line.filter { it.isLetter() }
                letters.length >= 4 &&
                    line == line.uppercase() &&
                    line.none { it.isDigit() } &&
                    line.split(" ").count { it.isNotBlank() } in 2..4 &&
                    line.split(" ").none { it.uppercase() in bankNoiseWords }
            }
            .maxByOrNull { it.length }

        val network = detectNetwork(joined, number)

        return ScannedCardDetails(
            number = number,
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            cardholderName = cardholderName,
            network = network,
        )
    }

    private fun detectNetwork(text: String, number: String?): CardNetwork? {
        val upper = text.uppercase()
        return when {
            "VISA" in upper -> CardNetwork.VISA
            "MASTERCARD" in upper -> CardNetwork.MASTERCARD
            "AMERICAN EXPRESS" in upper || "AMEX" in upper -> CardNetwork.AMEX
            "DISCOVER" in upper -> CardNetwork.DISCOVER
            number != null -> when {
                number.startsWith("4") -> CardNetwork.VISA
                number.take(2).toIntOrNull()?.let { it in 51..55 } == true -> CardNetwork.MASTERCARD
                number.take(4).toIntOrNull()?.let { it in 2221..2720 } == true -> CardNetwork.MASTERCARD
                number.take(2) in setOf("34", "37") -> CardNetwork.AMEX
                number.startsWith("6011") || number.take(3) == "644" || number.startsWith("65") -> CardNetwork.DISCOVER
                else -> null
            }
            else -> null
        }
    }
}
