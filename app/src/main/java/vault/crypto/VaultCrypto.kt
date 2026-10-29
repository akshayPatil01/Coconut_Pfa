package vault.crypto

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class VaultCryptoException(message: String, cause: Throwable? = null) : Exception(message, cause)

enum class Slot {
    MASTER,
    RECOVERY,
    BIOMETRIC
}

object VaultCrypto {
    private const val VERSION_BYTE: Byte = 1
    private const val IV_SIZE = 12
    private const val TAG_SIZE_BITS = 128
    private const val TAG_SIZE_BYTES = 16
    private const val MIN_BLOB_SIZE = 1 + IV_SIZE + TAG_SIZE_BYTES
    private const val CIPHER_ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"

    private val random = SecureRandom()

    fun newKey(): ByteArray {
        val key = ByteArray(32)
        random.nextBytes(key)
        return key
    }

    fun encrypt(key: ByteArray, plaintext: ByteArray, aad: ByteArray): ByteArray {
        require(key.size == 32) { "Key must be 32 bytes" }
        val iv = ByteArray(IV_SIZE)
        random.nextBytes(iv)

        val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        val keySpec = SecretKeySpec(key, KEY_ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_SIZE_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)
        cipher.updateAAD(aad)
        val ciphertextAndTag = cipher.doFinal(plaintext)

        val blob = ByteArray(1 + IV_SIZE + ciphertextAndTag.size)
        blob[0] = VERSION_BYTE
        System.arraycopy(iv, 0, blob, 1, IV_SIZE)
        System.arraycopy(ciphertextAndTag, 0, blob, 1 + IV_SIZE, ciphertextAndTag.size)
        return blob
    }

    fun decrypt(key: ByteArray, blob: ByteArray, aad: ByteArray): ByteArray {
        require(key.size == 32) { "Key must be 32 bytes" }
        if (blob.size < MIN_BLOB_SIZE) {
            throw VaultCryptoException("Truncated or invalid blob size: ${blob.size}")
        }
        val version = blob[0]
        if (version != VERSION_BYTE) {
            throw VaultCryptoException("Unknown version byte: $version")
        }

        val iv = ByteArray(IV_SIZE)
        System.arraycopy(blob, 1, iv, 0, IV_SIZE)

        val ciphertextAndTagSize = blob.size - 1 - IV_SIZE
        val ciphertextAndTag = ByteArray(ciphertextAndTagSize)
        System.arraycopy(blob, 1 + IV_SIZE, ciphertextAndTag, 0, ciphertextAndTagSize)

        return try {
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            val keySpec = SecretKeySpec(key, KEY_ALGORITHM)
            val gcmSpec = GCMParameterSpec(TAG_SIZE_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)
            cipher.updateAAD(aad)
            cipher.doFinal(ciphertextAndTag)
        } catch (e: Exception) {
            throw VaultCryptoException("Decryption failed: ${e.message}", e)
        }
    }

    fun wrapKey(kek: ByteArray, vk: ByteArray, slot: Slot): ByteArray {
        val aad = "fortis-vk-wrap-v1|${slot.name}".toByteArray(Charsets.UTF_8)
        return encrypt(kek, vk, aad)
    }

    fun unwrapKey(kek: ByteArray, blob: ByteArray, slot: Slot): ByteArray {
        val aad = "fortis-vk-wrap-v1|${slot.name}".toByteArray(Charsets.UTF_8)
        return decrypt(kek, blob, aad)
    }
}