package com.patil.coconut_pfa.data.local.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Base64
import androidx.biometric.BiometricPrompt
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec

object BiometricKeyManager {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "coconut_vault_bio_v1"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_SIZE_BITS = 128

    private fun getKeyStore(): KeyStore {
        return KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
    }

    fun hasKey(): Boolean {
        return try {
            getKeyStore().containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            false
        }
    }

    fun deleteKey() {
        try {
            val ks = getKeyStore()
            if (ks.containsAlias(KEY_ALIAS)) {
                ks.deleteEntry(KEY_ALIAS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getOrCreateSecretKey(context: Context): SecretKey {
        val ks = getKeyStore()
        val existingKey = ks.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }

        return generateKey(context, tryStrongBox = true)
    }

    private fun generateKey(context: Context, tryStrongBox: Boolean): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )

        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
        } else {
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(-1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setUnlockedDeviceRequired(true)
            val hasStrongBox = context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
            if (tryStrongBox && hasStrongBox) {
                try {
                    builder.setIsStrongBoxBacked(true)
                    keyGenerator.init(builder.build())
                    return keyGenerator.generateKey()
                } catch (e: Exception) {
                    if (e is StrongBoxUnavailableException || e.cause is StrongBoxUnavailableException) {
                        return generateKey(context, tryStrongBox = false)
                    }
                    // Retry without StrongBox if any StrongBox initialization error occurs
                    return generateKey(context, tryStrongBox = false)
                }
            }
        }

        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }

    fun initEncryptCipher(secretKey: SecretKey): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    fun createEncryptCryptoObject(context: Context): BiometricPrompt.CryptoObject {
        val key = getOrCreateSecretKey(context)
        val cipher = initEncryptCipher(key)
        return BiometricPrompt.CryptoObject(cipher)
    }

    fun encryptVkWithCryptoObject(cipher: Cipher, vk: ByteArray): String {
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(vk)
        val combined = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun createDecryptCryptoObject(context: Context, b64Blob: String): Pair<BiometricPrompt.CryptoObject, ByteArray> {
        val raw = Base64.decode(b64Blob, Base64.NO_WRAP)
        require(raw.size > IV_SIZE) { "Invalid biometric blob size" }

        val iv = ByteArray(IV_SIZE)
        val ciphertext = ByteArray(raw.size - IV_SIZE)
        System.arraycopy(raw, 0, iv, 0, IV_SIZE)
        System.arraycopy(raw, IV_SIZE, ciphertext, 0, ciphertext.size)

        val secretKey = getOrCreateSecretKey(context)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_SIZE_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        return Pair(BiometricPrompt.CryptoObject(cipher), ciphertext)
    }

    fun decryptVkWithCryptoObject(cipher: Cipher, ciphertext: ByteArray): ByteArray {
        return cipher.doFinal(ciphertext)
    }

    fun getKeyStorageType(context: Context): String {
        if (!hasKey()) return "TEE"
        return try {
            val ks = getKeyStore()
            val secretKey = ks.getKey(KEY_ALIAS, null) as? SecretKey ?: return "TEE"
            val factory = SecretKeyFactory.getInstance(secretKey.algorithm, KEYSTORE_PROVIDER)
            val keyInfo = factory.getKeySpec(secretKey, KeyInfo::class.java) as KeyInfo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && keyInfo.isInsideSecureHardware) {
                if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)) {
                    "StrongBox / TEE"
                } else {
                    "TEE"
                }
            } else {
                "TEE"
            }
        } catch (e: Exception) {
            "TEE"
        }
    }
}
