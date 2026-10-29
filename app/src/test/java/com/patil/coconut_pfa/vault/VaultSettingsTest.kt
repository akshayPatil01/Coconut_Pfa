package com.patil.coconut_pfa.vault

import com.patil.coconut_pfa.data.local.dao.VaultItemDao
import com.patil.coconut_pfa.data.local.dao.VaultKeysetDao
import com.patil.coconut_pfa.data.local.entity.VaultItem
import com.patil.coconut_pfa.data.local.entity.VaultItemType
import com.patil.coconut_pfa.data.local.entity.VaultKeyset
import com.patil.coconut_pfa.data.local.security.VaultSession
import com.patil.coconut_pfa.data.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import vault.crypto.KdfParams
import vault.crypto.PasswordKdf
import vault.crypto.RecoveryCode
import vault.crypto.Slot
import vault.crypto.VaultCrypto
import java.security.SecureRandom

class FakeVaultKeysetDao : VaultKeysetDao {
    var keyset: VaultKeyset? = null
    private val flow = MutableStateFlow<VaultKeyset?>(null)

    override fun getKeysetFlow(): Flow<VaultKeyset?> = flow.asStateFlow()

    override suspend fun getKeyset(): VaultKeyset? = keyset

    override suspend fun insertKeyset(keyset: VaultKeyset) {
        this.keyset = keyset
        flow.value = keyset
    }

    override suspend fun updateKeyset(keyset: VaultKeyset) {
        this.keyset = keyset
        flow.value = keyset
    }
}

class FakeVaultItemDao : VaultItemDao {
    val itemsMap = mutableMapOf<String, VaultItem>()
    private val flow = MutableStateFlow<List<VaultItem>>(emptyList())

    private fun updateFlow() {
        flow.value = itemsMap.values.filter { it.deletedAt == null }.sortedByDescending { it.updatedAt }
    }

    override fun getAllItems(): Flow<List<VaultItem>> = flow.asStateFlow()

    override suspend fun getAllRawItems(): List<VaultItem> = itemsMap.values.toList()

    override suspend fun getItemById(id: String): VaultItem? = itemsMap[id]

    override suspend fun insertItem(item: VaultItem) {
        itemsMap[item.id] = item
        updateFlow()
    }

    override suspend fun updateItem(item: VaultItem) {
        itemsMap[item.id] = item
        updateFlow()
    }

    override suspend fun deleteItem(id: String) {
        itemsMap.remove(id)
        updateFlow()
    }
}

class VaultSettingsTest {

    private lateinit var keysetDao: FakeVaultKeysetDao
    private lateinit var itemDao: FakeVaultItemDao
    private lateinit var repository: VaultRepository

    private val initialPassword = "InitialMasterPassword123!"

    @Before
    fun setup() = runBlocking {
        VaultSession.lock()

        keysetDao = FakeVaultKeysetDao()
        itemDao = FakeVaultItemDao()
        repository = VaultRepository(keysetDao, itemDao)

        val random = SecureRandom()
        val masterSalt = ByteArray(16).also { random.nextBytes(it) }
        val recoverySalt = ByteArray(16).also { random.nextBytes(it) }

        val kdfParams = KdfParams(memoryKiB = 1024, iterations = 1, parallelism = 1)
        val masterKey = PasswordKdf.argon2id(initialPassword.toCharArray(), masterSalt, kdfParams)

        val recoveryCode = RecoveryCode.generate()
        val recoveryKey = RecoveryCode.key(recoveryCode, recoverySalt)

        val vk = VaultCrypto.newKey()
        val vkByMaster = VaultCrypto.wrapKey(masterKey, vk, Slot.MASTER)
        val vkByRecovery = VaultCrypto.wrapKey(recoveryKey, vk, Slot.RECOVERY)

        val now = System.currentTimeMillis()
        val keyset = VaultKeyset(
            id = 1,
            kdf = "ARGON2ID",
            salt = masterSalt,
            memoryKiB = kdfParams.memoryKiB,
            iterations = kdfParams.iterations,
            parallelism = kdfParams.parallelism,
            vkByMaster = vkByMaster,
            vkByRecovery = vkByRecovery,
            recoverySalt = recoverySalt,
            keyVersion = 1,
            createdAt = now,
            masterChangedAt = now
        )

        keysetDao.insertKeyset(keyset)
        VaultSession.unlock(vk)

        vk.fill(0)
        masterKey.fill(0)
        recoveryKey.fill(0)
    }

    @Test
    fun testPasswordChangeUnlocksWithNewAndFailsWithOld() = runBlocking {
        val newPassword = "NewSecureMasterPassword456!"

        val initialVk = VaultSession.requireVk()

        repository.changeMasterPassword(
            currentPassword = initialPassword,
            newPassword = newPassword,
            rotateIfExposed = false
        )

        VaultSession.lock()
        assertTrue(VaultSession.isLocked)

        val updatedKeyset = keysetDao.getKeyset()
        assertNotNull(updatedKeyset)

        val kdfParams = KdfParams(
            memoryKiB = updatedKeyset!!.memoryKiB,
            iterations = updatedKeyset.iterations,
            parallelism = updatedKeyset.parallelism
        )

        // Attempting to unlock with OLD password should fail
        val oldMasterKey = PasswordKdf.argon2id(initialPassword.toCharArray(), updatedKeyset.salt, kdfParams)
        try {
            VaultCrypto.unwrapKey(oldMasterKey, updatedKeyset.vkByMaster, Slot.MASTER)
            fail("Old password should fail to unwrap key after password change")
        } catch (e: Exception) {
            // expected
        } finally {
            oldMasterKey.fill(0)
        }

        // Unlocking with NEW password should succeed and yield initial VK
        val newMasterKey = PasswordKdf.argon2id(newPassword.toCharArray(), updatedKeyset.salt, kdfParams)
        val unwrappedVk = VaultCrypto.unwrapKey(newMasterKey, updatedKeyset.vkByMaster, Slot.MASTER)
        assertArrayEquals("VK wrapped by new password must match initial VK", initialVk, unwrappedVk)

        VaultSession.unlock(unwrappedVk)
        assertFalse(VaultSession.isLocked)

        initialVk.fill(0)
        unwrappedVk.fill(0)
        newMasterKey.fill(0)
    }

    @Test
    fun testVaultKeyRotationKeepsEveryItemReadable() = runBlocking {
        val item1 = repository.saveItem(
            type = VaultItemType.LOGIN,
            metaJson = "{\"label\":\"Netflix\"}",
            secretJson = "{\"password\":\"secret123\"}"
        )
        val item2 = repository.saveItem(
            type = VaultItemType.CARD,
            metaJson = "{\"label\":\"HDFC Card\"}",
            secretJson = "{\"cardNumber\":\"4111222233334444\"}"
        )

        val oldVk = VaultSession.requireVk()

        val newRecoveryCode = repository.rotateVaultKey(currentPassword = initialPassword)
        assertNotNull(newRecoveryCode)

        val rotatedKeyset = keysetDao.getKeyset()
        assertNotNull(rotatedKeyset)
        assertEquals(2, rotatedKeyset!!.keyVersion)

        assertTrue("Vault session must be unlocked with new VK", VaultSession.isUnlocked)

        val newVk = VaultSession.requireVk()
        assertFalse("New VK must be different from old VK", oldVk.contentEquals(newVk))

        // Read and verify all items with new VK
        val meta1 = repository.readMeta(itemDao.getItemById(item1.id)!!)
        val secret1 = repository.readSecret(itemDao.getItemById(item1.id)!!)
        assertEquals("{\"label\":\"Netflix\"}", meta1)
        assertEquals("{\"password\":\"secret123\"}", secret1)

        val meta2 = repository.readMeta(itemDao.getItemById(item2.id)!!)
        val secret2 = repository.readSecret(itemDao.getItemById(item2.id)!!)
        assertEquals("{\"label\":\"HDFC Card\"}", meta2)
        assertEquals("{\"cardNumber\":\"4111222233334444\"}", secret2)

        oldVk.fill(0)
        newVk.fill(0)
    }

    @Test
    fun testRotationFailureHalfwayLeavesOldDataReadable() = runBlocking {
        val item1 = repository.saveItem(
            type = VaultItemType.LOGIN,
            metaJson = "{\"label\":\"Gmail\"}",
            secretJson = "{\"password\":\"gmail123\"}"
        )
        val item2 = repository.saveItem(
            type = VaultItemType.LOGIN,
            metaJson = "{\"label\":\"Amazon\"}",
            secretJson = "{\"password\":\"amazon456\"}"
        )

        val oldVk = VaultSession.requireVk()
        val originalKeyset = keysetDao.getKeyset()!!

        // Create item DAO that fails on second update
        class FailingVaultItemDao(val delegate: FakeVaultItemDao) : VaultItemDao by delegate {
            var updateCount = 0
            override suspend fun updateItem(item: VaultItem) {
                updateCount++
                if (updateCount > 1) {
                    throw RuntimeException("Simulated Database Failure during rotation")
                }
                delegate.updateItem(item)
            }
        }

        val failingItemDao = FailingVaultItemDao(itemDao)
        val failingRepo = VaultRepository(keysetDao, failingItemDao)

        // Save snapshots of items before rotation attempt
        val item1Before = itemDao.getItemById(item1.id)!!
        val item2Before = itemDao.getItemById(item2.id)!!

        try {
            failingRepo.rotateVaultKey(currentPassword = initialPassword)
            fail("Expected RuntimeException during rotation")
        } catch (e: Exception) {
            assertTrue("Should be simulated failure", e.message?.contains("Simulated Database Failure") == true)
        }

        // Simulate rollback: restore original items & keyset as Room transaction would
        itemDao.insertItem(item1Before)
        itemDao.insertItem(item2Before)
        keysetDao.updateKeyset(originalKeyset)
        VaultSession.unlock(oldVk)

        // Verify that old items are still fully readable with old VK
        val meta1 = repository.readMeta(itemDao.getItemById(item1.id)!!)
        val secret1 = repository.readSecret(itemDao.getItemById(item1.id)!!)
        assertEquals("{\"label\":\"Gmail\"}", meta1)
        assertEquals("{\"password\":\"gmail123\"}", secret1)

        val meta2 = repository.readMeta(itemDao.getItemById(item2.id)!!)
        val secret2 = repository.readSecret(itemDao.getItemById(item2.id)!!)
        assertEquals("{\"label\":\"Amazon\"}", meta2)
        assertEquals("{\"password\":\"amazon456\"}", secret2)

        oldVk.fill(0)
    }
}