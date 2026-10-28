package com.patil.coconut_pfa.data.local.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupPassphraseManager {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "coconut_backup_passphrase_key"
    private const val PREF_NAME = "coconut_secure_backup_prefs"
    private const val PREF_KEY_WRAPPED_KEY = "encrypted_backup_derived_key_b64"
    private const val PREF_KEY_SALT = "backup_salt_b64"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val PBKDF2_ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256

    fun hasBackupPassword(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_KEY_WRAPPED_KEY, null) != null &&
                prefs.getString(PREF_KEY_SALT, null) != null
    }

    fun setBackupPassword(context: Context, password: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val derivedKey = deriveKey(password, salt)
        val encryptedDerivedKeyWithIv = encryptDerivedKey(derivedKey)

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(PREF_KEY_WRAPPED_KEY, Base64.encodeToString(encryptedDerivedKeyWithIv, Base64.NO_WRAP))
            .putString(PREF_KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .apply()
    }

    fun getDerivedKey(context: Context, password: String? = null): ByteArray {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val saltB64 = prefs.getString(PREF_KEY_SALT, null)
        val wrappedB64 = prefs.getString(PREF_KEY_WRAPPED_KEY, null)

        if (password != null) {
            val salt = if (saltB64 != null) {
                Base64.decode(saltB64, Base64.NO_WRAP)
            } else {
                ByteArray(16).also { SecureRandom().nextBytes(it) }
            }
            return deriveKey(password, salt)
        }

        if (wrappedB64 != null) {
            try {
                val encryptedBytes = Base64.decode(wrappedB64, Base64.NO_WRAP)
                return decryptDerivedKey(encryptedBytes)
            } catch (e: Exception) {
                throw SecurityException("Failed to decrypt backup derived key from Keystore", e)
            }
        }

        throw IllegalStateException("No backup password set and no password provided.")
    }

    fun getSalt(context: Context): ByteArray? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val saltB64 = prefs.getString(PREF_KEY_SALT, null) ?: return null
        return Base64.decode(saltB64, Base64.NO_WRAP)
    }

    fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private fun encryptDerivedKey(derivedKey: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(derivedKey)
        return iv + ciphertext
    }

    private fun decryptDerivedKey(encryptedWithIv: ByteArray): ByteArray {
        val iv = encryptedWithIv.copyOfRange(0, GCM_IV_LENGTH_BYTES)
        val ciphertext = encryptedWithIv.copyOfRange(GCM_IV_LENGTH_BYTES, encryptedWithIv.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), gcmSpec)
        return cipher.doFinal(ciphertext)
    }
}
