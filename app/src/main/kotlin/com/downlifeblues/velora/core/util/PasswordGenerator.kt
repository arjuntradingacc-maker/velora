package com.downlifeblues.velora.core.util

import java.security.SecureRandom

data class GeneratorOptions(
    val length: Int = 20,
    val useUppercase: Boolean = true,
    val useLowercase: Boolean = true,
    val useNumbers: Boolean = true,
    val useSymbols: Boolean = true,
    val excludeAmbiguous: Boolean = true,
)

data class PassphraseOptions(
    val wordCount: Int = 4,
    val capitalize: Boolean = true,
    val separator: String = "-",
    val includeNumber: Boolean = true,
)

/**
 * Generates passwords and passphrases using [SecureRandom], the platform's
 * CSPRNG. No custom cryptography — this is character/word selection only,
 * not encryption.
 */
object PasswordGenerator {

    private const val LOWER = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?"
    private const val AMBIGUOUS = "il1LoO0|B8S5Z2"

    private val secureRandom = SecureRandom()

    fun generate(options: GeneratorOptions): String {
        var pool = buildString {
            if (options.useLowercase) append(LOWER)
            if (options.useUppercase) append(UPPER)
            if (options.useNumbers) append(DIGITS)
            if (options.useSymbols) append(SYMBOLS)
        }
        if (pool.isEmpty()) pool = LOWER + DIGITS

        if (options.excludeAmbiguous) {
            pool = pool.filterNot { AMBIGUOUS.contains(it) }
        }
        if (pool.isEmpty()) pool = LOWER

        // Guarantee at least one character from each selected category so
        // toggles are never silently ignored by pure chance.
        val required = mutableListOf<Char>()
        if (options.useLowercase) required += randomFrom(LOWER, options.excludeAmbiguous)
        if (options.useUppercase) required += randomFrom(UPPER, options.excludeAmbiguous)
        if (options.useNumbers) required += randomFrom(DIGITS, options.excludeAmbiguous)
        if (options.useSymbols) required += randomFrom(SYMBOLS, options.excludeAmbiguous)

        val length = options.length.coerceAtLeast(required.size)
        val chars = CharArray(length)
        for (i in required.indices) chars[i] = required[i]
        for (i in required.size until length) {
            chars[i] = pool[secureRandom.nextInt(pool.length)]
        }
        // Fisher–Yates shuffle so the guaranteed characters aren't always leading.
        for (i in chars.indices.reversed()) {
            val j = secureRandom.nextInt(i + 1)
            val tmp = chars[i]
            chars[i] = chars[j]
            chars[j] = tmp
        }
        return String(chars)
    }

    fun generatePassphrase(options: PassphraseOptions): String {
        val words = (1..options.wordCount).map {
            val word = WORDLIST[secureRandom.nextInt(WORDLIST.size)]
            if (options.capitalize) word.replaceFirstChar(Char::uppercase) else word
        }.toMutableList()
        if (options.includeNumber) {
            val index = secureRandom.nextInt(words.size)
            words[index] = words[index] + secureRandom.nextInt(100).toString()
        }
        return words.joinToString(options.separator)
    }

    private fun randomFrom(set: String, excludeAmbiguous: Boolean): Char {
        val filtered = if (excludeAmbiguous) set.filterNot { AMBIGUOUS.contains(it) } else set
        val source = filtered.ifEmpty { set }
        return source[secureRandom.nextInt(source.length)]
    }

    // A small, original, diceware-style word list used for passphrase mode.
    // Production builds should swap this for a vetted large list (e.g. the
    // EFF long wordlist) shipped as a raw resource.
    private val WORDLIST = listOf(
        "amber", "anchor", "arbor", "argon", "atlas", "aurora", "basil", "beacon",
        "birch", "bramble", "canyon", "cascade", "cedar", "cinder", "clover", "coast",
        "comet", "copper", "coral", "crest", "cypress", "dawn", "delta", "dune",
        "ember", "falcon", "feather", "fern", "flint", "forge", "fossil", "garnet",
        "glacier", "granite", "harbor", "hazel", "heather", "horizon", "indigo", "ivory",
        "jasper", "juniper", "kestrel", "lagoon", "lantern", "lark", "linen", "lotus",
        "lumen", "maple", "marble", "meadow", "mesa", "mist", "moss", "nebula",
        "nectar", "nettle", "nimbus", "nomad", "oasis", "obsidian", "olive", "onyx",
        "opal", "orbit", "orchid", "otter", "palm", "pebble", "petal", "pine",
        "plateau", "plume", "prairie", "quartz", "quiet", "raven", "reef", "ridge",
        "river", "rowan", "saffron", "sage", "sequoia", "shale", "shore", "sienna",
        "slate", "sparrow", "spruce", "storm", "summit", "sundew", "swift", "sycamore",
        "tangerine", "terra", "thistle", "timber", "tundra", "umbra", "valley", "velvet",
        "verdant", "violet", "vista", "walnut", "willow", "wren", "yarrow", "zephyr",
    )
}
