package com.patil.coconut_pfa.vault

import java.security.SecureRandom
import kotlin.math.log2

data class PasswordGenerationResult(
    val password: String,
    val length: Int,
    val entropyBits: Double,
    val strength: PasswordStrength
) {
    override fun toString(): String = "<redacted>"
}

enum class PasswordStrength {
    WEAK, OK, STRONG
}

object PasswordGenerator {
    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val NUMBERS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?"
    private val LOOK_ALIKES = setOf('0', 'O', 'o', '1', 'l', 'I')

    fun generate(
        length: Int = 20,
        useUpper: Boolean = true,
        useLower: Boolean = true,
        useNumbers: Boolean = true,
        useSymbols: Boolean = true,
        avoidLookAlikes: Boolean = false,
        random: SecureRandom = SecureRandom()
    ): PasswordGenerationResult {
        val clampedLength = length.coerceIn(8, 64)

        var upperChars = UPPERCASE
        var lowerChars = LOWERCASE
        var numberChars = NUMBERS
        val symbolChars = SYMBOLS

        if (avoidLookAlikes) {
            upperChars = upperChars.filter { it !in LOOK_ALIKES }
            lowerChars = lowerChars.filter { it !in LOOK_ALIKES }
            numberChars = numberChars.filter { it !in LOOK_ALIKES }
        }

        val activeSets = mutableListOf<String>()
        if (useUpper && upperChars.isNotEmpty()) activeSets.add(upperChars)
        if (useLower && lowerChars.isNotEmpty()) activeSets.add(lowerChars)
        if (useNumbers && numberChars.isNotEmpty()) activeSets.add(numberChars)
        if (useSymbols && symbolChars.isNotEmpty()) activeSets.add(symbolChars)

        if (activeSets.isEmpty()) {
            activeSets.add(lowerChars)
        }

        val passwordChars = mutableListOf<Char>()

        // Ensure at least one character from each active set
        for (set in activeSets) {
            passwordChars.add(set[random.nextInt(set.length)])
        }

        val allChars = activeSets.joinToString("")

        // Fill remaining length
        while (passwordChars.size < clampedLength) {
            passwordChars.add(allChars[random.nextInt(allChars.length)])
        }

        passwordChars.shuffle(random)
        val password = passwordChars.joinToString("")

        val poolSize = allChars.length
        val entropy = calculateEntropy(clampedLength, poolSize)
        val strength = evaluateStrength(clampedLength, entropy)

        return PasswordGenerationResult(password, clampedLength, entropy, strength)
    }

    fun calculateEntropy(length: Int, poolSize: Int): Double {
        if (poolSize <= 0 || length <= 0) return 0.0
        return length * log2(poolSize.toDouble())
    }

    fun evaluateStrength(length: Int, entropyBits: Double): PasswordStrength {
        // Weak: under 12 characters or under 60 bits
        if (length < 12 || entropyBits < 60.0) {
            return PasswordStrength.WEAK
        }
        // Strong: >= 80 bits
        if (entropyBits >= 80.0) {
            return PasswordStrength.STRONG
        }
        return PasswordStrength.OK
    }
}
