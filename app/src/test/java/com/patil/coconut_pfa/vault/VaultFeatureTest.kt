package com.patil.coconut_pfa.vault

import com.patil.coconut_pfa.data.local.entity.VaultItem
import com.patil.coconut_pfa.data.local.entity.VaultItemType
import com.patil.coconut_pfa.data.local.entity.VaultKeyset
import com.patil.coconut_pfa.data.local.security.VaultLockedException
import com.patil.coconut_pfa.data.local.security.VaultSession
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import vault.crypto.KdfParams
import vault.crypto.PasswordKdf
import vault.crypto.RecoveryCode
import vault.crypto.Slot
import vault.crypto.VaultCrypto

class VaultFeatureTest {

    @Test
    fun testVaultSessionLifecycle() {
        VaultSession.lock()
        assertTrue("Session should start locked", VaultSession.isLocked)

        try {
            VaultSession.requireVk()
            fail("requireVk should throw VaultLockedException when locked")
        } catch (e: VaultLockedException) {
            // expected
        }

        val dummyVk = ByteArray(32) { (it + 1).toByte() }
        VaultSession.unlock(dummyVk)

        assertFalse("Session should be unlocked", VaultSession.isLocked)
        val retrievedVk = VaultSession.requireVk()
        assertArrayEquals("Retrieved VK should match unlocked VK", dummyVk, retrievedVk)

        VaultSession.lock()
        assertTrue("Session should be locked after lock()", VaultSession.isLocked)

        try {
            VaultSession.requireVk()
            fail("requireVk should throw VaultLockedException after lock()")
        } catch (e: VaultLockedException) {
            // expected
        }
    }

    @Test
    fun testRedactedToString() {
        val keyset = VaultKeyset(
            id = 1,
            salt = ByteArray(16),
            vkByMaster = ByteArray(32),
            vkByRecovery = ByteArray(32),
            recoverySalt = ByteArray(16)
        )
        assertEquals("<redacted>", keyset.toString())

        val item = VaultItem(
            type = VaultItemType.LOGIN,
            metaBlob = ByteArray(16),
            secretBlob = ByteArray(32)
        )
        assertEquals("<redacted>", item.toString())
        assertEquals("<redacted>", VaultSession.toString())
    }

    @Test
    fun testMasterAndPasswordKdfIntegration() {
        val password = "StrongMasterPassword123!".toCharArray()
        val salt = ByteArray(16) { 0x01.toByte() }
        val params = KdfParams(memoryKiB = 1024, iterations = 1, parallelism = 1)

        val masterKey = PasswordKdf.argon2id(password, salt, params)
        assertEquals(32, masterKey.size)

        val vk = VaultCrypto.newKey()
        val wrappedVk = VaultCrypto.wrapKey(masterKey, vk, Slot.MASTER)
        val unwrappedVk = VaultCrypto.unwrapKey(masterKey, wrappedVk, Slot.MASTER)

        assertArrayEquals(vk, unwrappedVk)
    }

    @Test
    fun testRecoveryCodeAndHkdfIntegration() {
        val recoveryCode = RecoveryCode.generate()
        val recoverySalt = ByteArray(16) { 0x02.toByte() }

        val recoveryKey = RecoveryCode.key(recoveryCode, recoverySalt)
        assertEquals(32, recoveryKey.size)

        val vk = VaultCrypto.newKey()
        val wrappedVk = VaultCrypto.wrapKey(recoveryKey, vk, Slot.RECOVERY)
        val unwrappedVk = VaultCrypto.unwrapKey(recoveryKey, wrappedVk, Slot.RECOVERY)

        assertArrayEquals(vk, unwrappedVk)
    }
}
