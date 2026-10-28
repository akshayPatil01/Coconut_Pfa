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
import javax.crypto.spec.GCMParameterSpec

object PassphraseManager {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "coconut_db_passphrase_key"
    private const val PREF_NAME = "coconut_secure_db_prefs"
    private const val PREF_KEY_PASSPHRASE = "encrypted_db_passphrase_b64"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val PASSPHRASE_SIZE_BYTES = 32

    @Synchronized
    fun getOrCreatePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val encryptedB64 = prefs.getString(PREF_KEY_PASSPHRASE, null)

        if (encryptedB64 != null) {
            try {
                return decryptPassphrase(Base64.decode(encryptedB64, Base64.NO_WRAP))
            } catch (e: Exception) {
                // In case of any keystore decryption issue, clear and regenerate
                prefs.edit().remove(PREF_KEY_PASSPHRASE).apply()
            }
        }

        val passphrase = ByteArray(PASSPHRASE_SIZE_BYTES).also {
            SecureRandom().nextBytes(it)
        }
        val encryptedBytes = encryptPassphrase(passphrase)
        val encodedB64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        prefs.edit().putString(PREF_KEY_PASSPHRASE, encodedB64).apply()
        return passphrase
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

    private fun encryptPassphrase(passphrase: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(passphrase)
        return iv + encryptedData
    }

    private fun decryptPassphrase(encryptedWithIv: ByteArray): ByteArray {
        val iv = encryptedWithIv.copyOfRange(0, GCM_IV_LENGTH_BYTES)
        val encryptedData = encryptedWithIv.copyOfRange(GCM_IV_LENGTH_BYTES, encryptedWithIv.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), gcmSpec)
        return cipher.doFinal(encryptedData)
    }
}
