package com.patil.coconut_pfa.data.local.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(salt)
        return salt
    }

    fun hashPin(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return encodeBase64(hash)
    }

    fun saltToBase64(salt: ByteArray): String {
        return encodeBase64(salt)
    }

    fun base64ToSalt(saltB64: String): ByteArray {
        return decodeBase64(saltB64)
    }

    fun verifyPin(enteredPin: String, storedHashB64: String, storedSaltB64: String): Boolean {
        return try {
            val salt = base64ToSalt(storedSaltB64)
            val computedHashB64 = hashPin(enteredPin, salt)
            computedHashB64 == storedHashB64
        } catch (_: Exception) {
            false
        }
    }

    private fun encodeBase64(bytes: ByteArray): String {
        return try {
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        } catch (e: Throwable) {
            java.util.Base64.getEncoder().encodeToString(bytes)
        }
    }

    private fun decodeBase64(b64: String): ByteArray {
        return try {
            android.util.Base64.decode(b64, android.util.Base64.NO_WRAP)
        } catch (e: Throwable) {
            java.util.Base64.getDecoder().decode(b64)
        }
    }
}
