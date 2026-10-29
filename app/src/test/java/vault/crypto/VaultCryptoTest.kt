package vault.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Test

class VaultCryptoTest {

    @Test
    fun testNewKeyReturns32Bytes() {
        val key = VaultCrypto.newKey()
        assertNotNull(key)
        org.junit.Assert.assertEquals(32, key.size)
    }

    @Test
    fun testEncryptDecryptRoundTrip() {
        val key = VaultCrypto.newKey()
        val plaintext = "Hello Coconut Vault!".toByteArray(Charsets.UTF_8)
        val aad = "user-123".toByteArray(Charsets.UTF_8)

        val blob = VaultCrypto.encrypt(key, plaintext, aad)
        val decrypted = VaultCrypto.decrypt(key, blob, aad)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun testSamePlaintextTwiceGivesDifferentBlobs() {
        val key = VaultCrypto.newKey()
        val plaintext = "Secret payload".toByteArray(Charsets.UTF_8)
        val aad = "aad-data".toByteArray(Charsets.UTF_8)

        val blob1 = VaultCrypto.encrypt(key, plaintext, aad)
        val blob2 = VaultCrypto.encrypt(key, plaintext, aad)

        assertFalse("Two encryptions of the same plaintext should produce different blobs due to random IV", blob1.contentEquals(blob2))
    }

    @Test
    fun testWrongKeyFails() {
        val key1 = VaultCrypto.newKey()
        val key2 = VaultCrypto.newKey()
        val plaintext = "Sensitive data".toByteArray(Charsets.UTF_8)
        val aad = "aad-data".toByteArray(Charsets.UTF_8)

        val blob = VaultCrypto.encrypt(key1, plaintext, aad)

        try {
            VaultCrypto.decrypt(key2, blob, aad)
            fail("Decryption with wrong key should throw VaultCryptoException")
        } catch (e: VaultCryptoException) {
            // expected
        }
    }

    @Test
    fun testWrongAadFails() {
        val key = VaultCrypto.newKey()
        val plaintext = "Data for Item A".toByteArray(Charsets.UTF_8)
        val aadA = "item-A".toByteArray(Charsets.UTF_8)
        val aadB = "item-B".toByteArray(Charsets.UTF_8)

        val blob = VaultCrypto.encrypt(key, plaintext, aadA)

        try {
            VaultCrypto.decrypt(key, blob, aadB)
            fail("Decryption with wrong AAD should throw VaultCryptoException")
        } catch (e: VaultCryptoException) {
            // expected
        }
    }

    @Test
    fun testFlippingSingleByteFails() {
        val key = VaultCrypto.newKey()
        val plaintext = "Tamper check text".toByteArray(Charsets.UTF_8)
        val aad = "aad-check".toByteArray(Charsets.UTF_8)

        val blob = VaultCrypto.encrypt(key, plaintext, aad)

        for (i in blob.indices) {
            val tamperedBlob = blob.clone()
            tamperedBlob[i] = (tamperedBlob[i].toInt() xor 0x01).toByte()

            try {
                VaultCrypto.decrypt(key, tamperedBlob, aad)
                fail("Flipping byte at index $i should cause decryption to fail")
            } catch (e: VaultCryptoException) {
                // expected
            }
        }
    }

    @Test
    fun testTruncatedBlobFails() {
        val key = VaultCrypto.newKey()
        val plaintext = "Testing truncated blob behavior".toByteArray(Charsets.UTF_8)
        val aad = "aad".toByteArray(Charsets.UTF_8)

        val blob = VaultCrypto.encrypt(key, plaintext, aad)
        val truncated = blob.copyOf(blob.size / 2)

        try {
            VaultCrypto.decrypt(key, truncated, aad)
            fail("Truncated blob should throw VaultCryptoException")
        } catch (e: VaultCryptoException) {
            // expected
        }
    }

    @Test
    fun testWrapUnwrapKeyRoundTrip() {
        val kek = VaultCrypto.newKey()
        val vk = VaultCrypto.newKey()

        val blob = VaultCrypto.wrapKey(kek, vk, Slot.MASTER)
        val unwrapped = VaultCrypto.unwrapKey(kek, blob, Slot.MASTER)

        assertArrayEquals(vk, unwrapped)
    }

    @Test
    fun testUnwrapWithWrongSlotFails() {
        val kek = VaultCrypto.newKey()
        val vk = VaultCrypto.newKey()

        val blob = VaultCrypto.wrapKey(kek, vk, Slot.MASTER)

        try {
            VaultCrypto.unwrapKey(kek, blob, Slot.RECOVERY)
            fail("Unwrapping with wrong slot should throw VaultCryptoException")
        } catch (e: VaultCryptoException) {
            // expected
        }
    }
}