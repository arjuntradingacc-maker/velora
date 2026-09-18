package com.velora.vault.core.util

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

/**
 * RFC 6238 TOTP using the platform's own HMAC implementation — no custom
 * cryptography. Base32 decoding is hand-rolled (below) to avoid pulling in
 * a whole external codec dependency for one small, well-specified routine.
 */
object TotpGenerator {

    fun currentCode(
        base32Secret: String,
        digits: Int = 6,
        periodSeconds: Int = 30,
        algorithm: String = "SHA1",
        timeMillis: Long = System.currentTimeMillis(),
    ): String {
        val counter = timeMillis / 1000 / periodSeconds
        return generate(base32Secret, counter, digits, algorithm)
    }

    /** Fraction of the current period elapsed, 0f..1f — drives the countdown ring. */
    fun periodProgress(periodSeconds: Int = 30, timeMillis: Long = System.currentTimeMillis()): Float {
        val elapsed = (timeMillis / 1000) % periodSeconds
        return elapsed.toFloat() / periodSeconds
    }

    fun secondsRemaining(periodSeconds: Int = 30, timeMillis: Long = System.currentTimeMillis()): Int {
        val elapsed = (timeMillis / 1000) % periodSeconds
        return (periodSeconds - elapsed).toInt()
    }

    private fun generate(base32Secret: String, counter: Long, digits: Int, algorithm: String): String {
        val keyBytes = decodeBase32(base32Secret)
        val mac = Mac.getInstance("Hmac$algorithm")
        mac.init(SecretKeySpec(keyBytes, "Hmac$algorithm"))
        val counterBytes = ByteBuffer.allocate(8).putLong(counter).array()
        val hash = mac.doFinal(counterBytes)

        val offset = (hash.last().toInt() and 0x0F)
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
            ((hash[offset + 1].toInt() and 0xFF) shl 16) or
            ((hash[offset + 2].toInt() and 0xFF) shl 8) or
            (hash[offset + 3].toInt() and 0xFF)

        val modulus = 10.0.pow(digits).toInt()
        return (binary % modulus).toString().padStart(digits, '0')
    }

    private const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    private fun decodeBase32(input: String): ByteArray {
        val clean = input.trim().uppercase().replace("=", "").replace(" ", "")
        var buffer = 0L
        var bitsLeft = 0
        val out = ArrayList<Byte>()
        for (c in clean) {
            val value = BASE32_ALPHABET.indexOf(c)
            if (value < 0) continue
            buffer = (buffer shl 5) or value.toLong()
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bitsLeft -= 8
                out.add(((buffer shr bitsLeft) and 0xFF).toByte())
            }
        }
        return out.toByteArray()
    }

    fun randomBase32Secret(lengthBytes: Int = 20): String {
        val random = java.security.SecureRandom()
        val bytes = ByteArray(lengthBytes)
        random.nextBytes(bytes)
        var buffer = 0L
        var bitsLeft = 0
        val sb = StringBuilder()
        for (b in bytes) {
            buffer = (buffer shl 8) or (b.toLong() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                bitsLeft -= 5
                sb.append(BASE32_ALPHABET[((buffer shr bitsLeft) and 0x1F).toInt()])
            }
        }
        if (bitsLeft > 0) {
            sb.append(BASE32_ALPHABET[((buffer shl (5 - bitsLeft)) and 0x1F).toInt()])
        }
        return sb.toString()
    }
}
