package com.patil.coconut_pfa.data.repository

import androidx.room.withTransaction
import com.patil.coconut_pfa.data.local.dao.VaultItemDao
import com.patil.coconut_pfa.data.local.dao.VaultKeysetDao
import com.patil.coconut_pfa.data.local.db.CoconutDatabase
import com.patil.coconut_pfa.data.local.entity.VaultItem
import com.patil.coconut_pfa.data.local.entity.VaultItemType
import com.patil.coconut_pfa.data.local.entity.VaultKeyset
import com.patil.coconut_pfa.data.local.security.VaultLockedException
import com.patil.coconut_pfa.data.local.security.VaultSession
import kotlinx.coroutines.flow.Flow
import vault.crypto.KdfParams
import vault.crypto.PasswordKdf
import vault.crypto.RecoveryCode
import vault.crypto.Slot
import vault.crypto.VaultCrypto
import java.security.SecureRandom
import java.util.UUID

class VaultRepository(
    private val keysetDao: VaultKeysetDao,
    private val itemDao: VaultItemDao,
    private val database: CoconutDatabase? = null
) {
    fun getKeysetFlow(): Flow<VaultKeyset?> = keysetDao.getKeysetFlow()

    suspend fun getKeyset(): VaultKeyset? = keysetDao.getKeyset()

    suspend fun saveKeyset(keyset: VaultKeyset) {
        keysetDao.insertKeyset(keyset)
    }

    suspend fun updateKeyset(keyset: VaultKeyset) {
        keysetDao.updateKeyset(keyset)
    }

    fun getAllItemsFlow(): Flow<List<VaultItem>> = itemDao.getAllItems()

    suspend fun getAllRawItems(): List<VaultItem> = itemDao.getAllRawItems()

    suspend fun getItemById(id: String): VaultItem? = itemDao.getItemById(id)

    suspend fun saveItem(
        type: VaultItemType,
        metaJson: String,
        secretJson: String,
        id: String = UUID.randomUUID().toString(),
        linkedAccountId: String? = null,
        favourite: Boolean = false
    ): VaultItem {
        val vk = VaultSession.requireVk()
        return try {
            val metaAad = "coconut-vault-v1|$id|meta".toByteArray(Charsets.UTF_8)
            val secretAad = "coconut-vault-v1|$id|secret".toByteArray(Charsets.UTF_8)

            val metaBlob = VaultCrypto.encrypt(vk, metaJson.toByteArray(Charsets.UTF_8), metaAad)
            val secretBlob = VaultCrypto.encrypt(vk, secretJson.toByteArray(Charsets.UTF_8), secretAad)

            val now = System.currentTimeMillis()
            val item = VaultItem(
                id = id,
                type = type,
                metaBlob = metaBlob,
                secretBlob = secretBlob,
                favourite = favourite,
                linkedAccountId = linkedAccountId,
                createdAt = now,
                updatedAt = now
            )
            itemDao.insertItem(item)
            item
        } finally {
            vk.fill(0)
        }
    }

    suspend fun readMeta(item: VaultItem): String {
        val vk = VaultSession.requireVk()
        return try {
            val metaAad = "coconut-vault-v1|${item.id}|meta".toByteArray(Charsets.UTF_8)
            val decrypted = VaultCrypto.decrypt(vk, item.metaBlob, metaAad)
            String(decrypted, Charsets.UTF_8)
        } finally {
            vk.fill(0)
        }
    }

    suspend fun readSecret(item: VaultItem): String {
        val vk = VaultSession.requireVk()
        return try {
            val secretAad = "coconut-vault-v1|${item.id}|secret".toByteArray(Charsets.UTF_8)
            val decrypted = VaultCrypto.decrypt(vk, item.secretBlob, secretAad)
            String(decrypted, Charsets.UTF_8)
        } finally {
            vk.fill(0)
        }
    }

    suspend fun deleteItem(id: String) {
        if (VaultSession.isLocked) {
            throw VaultLockedException("Vault is locked")
        }
        itemDao.deleteItem(id)
    }

    suspend fun softDeleteItem(id: String) {
        if (VaultSession.isLocked) {
            throw VaultLockedException("Vault is locked")
        }
        val item = itemDao.getItemById(id) ?: return
        val updated = item.copy(deletedAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        itemDao.updateItem(updated)
    }

    suspend fun restoreItem(id: String) {
        if (VaultSession.isLocked) {
            throw VaultLockedException("Vault is locked")
        }
        val item = itemDao.getItemById(id) ?: return
        val updated = item.copy(deletedAt = null, updatedAt = System.currentTimeMillis())
        itemDao.updateItem(updated)
    }

    suspend fun verifyMasterPassword(password: String): Boolean {
        val keyset = keysetDao.getKeyset() ?: return false
        val kdfParams = KdfParams(
            memoryKiB = keyset.memoryKiB,
            iterations = keyset.iterations,
            parallelism = keyset.parallelism
        )
        val masterKey = PasswordKdf.argon2id(password.toCharArray(), keyset.salt, kdfParams)
        return try {
            val unwrappedVk = VaultCrypto.unwrapKey(masterKey, keyset.vkByMaster, Slot.MASTER)
            val sessionVk = VaultSession.requireVk()
            val match = unwrappedVk.contentEquals(sessionVk)
            unwrappedVk.fill(0)
            sessionVk.fill(0)
            match
        } catch (e: Exception) {
            false
        } finally {
            masterKey.fill(0)
        }
    }

    suspend fun changeMasterPassword(
        currentPassword: String,
        newPassword: String,
        rotateIfExposed: Boolean
    ): String? {
        if (rotateIfExposed) {
            return rotateVaultKey(currentPassword = currentPassword, newPassword = newPassword)
        }

        val keyset = keysetDao.getKeyset() ?: throw Exception("Vault is not configured")

        val kdfParams = KdfParams(
            memoryKiB = keyset.memoryKiB,
            iterations = keyset.iterations,
            parallelism = keyset.parallelism
        )
        val currentMasterKey = PasswordKdf.argon2id(currentPassword.toCharArray(), keyset.salt, kdfParams)

        val vk = try {
            VaultCrypto.unwrapKey(currentMasterKey, keyset.vkByMaster, Slot.MASTER)
        } catch (e: Exception) {
            currentMasterKey.fill(0)
            throw Exception("Incorrect current master password")
        } finally {
            currentMasterKey.fill(0)
        }

        val random = SecureRandom()
        val newMasterSalt = ByteArray(16).also { random.nextBytes(it) }
        val newMasterKey = PasswordKdf.argon2id(newPassword.toCharArray(), newMasterSalt, kdfParams)

        try {
            val newVkByMaster = VaultCrypto.wrapKey(newMasterKey, vk, Slot.MASTER)
            val now = System.currentTimeMillis()
            val updatedKeyset = keyset.copy(
                salt = newMasterSalt,
                vkByMaster = newVkByMaster,
                masterChangedAt = now
            )
            keysetDao.updateKeyset(updatedKeyset)
            return null
        } finally {
            vk.fill(0)
            newMasterKey.fill(0)
        }
    }

    suspend fun generateNewRecoveryCode(currentPassword: String): String {
        val keyset = keysetDao.getKeyset() ?: throw Exception("Vault is not configured")

        val kdfParams = KdfParams(
            memoryKiB = keyset.memoryKiB,
            iterations = keyset.iterations,
            parallelism = keyset.parallelism
        )
        val currentMasterKey = PasswordKdf.argon2id(currentPassword.toCharArray(), keyset.salt, kdfParams)

        val vk = try {
            VaultCrypto.unwrapKey(currentMasterKey, keyset.vkByMaster, Slot.MASTER)
        } catch (e: Exception) {
            currentMasterKey.fill(0)
            throw Exception("Incorrect current master password")
        } finally {
            currentMasterKey.fill(0)
        }

        val random = SecureRandom()
        val newRecoveryCode = RecoveryCode.generate()
        val newRecoverySalt = ByteArray(16).also { random.nextBytes(it) }
        val newRecoveryKey = RecoveryCode.key(newRecoveryCode, newRecoverySalt)

        try {
            val newVkByRecovery = VaultCrypto.wrapKey(newRecoveryKey, vk, Slot.RECOVERY)
            val updatedKeyset = keyset.copy(
                recoverySalt = newRecoverySalt,
                vkByRecovery = newVkByRecovery
            )
            keysetDao.updateKeyset(updatedKeyset)
            return newRecoveryCode
        } finally {
            vk.fill(0)
            newRecoveryKey.fill(0)
        }
    }

    suspend fun rotateVaultKey(currentPassword: String, newPassword: String? = null): String {
        val keyset = keysetDao.getKeyset() ?: throw Exception("Vault is not configured")

        val kdfParams = KdfParams(
            memoryKiB = keyset.memoryKiB,
            iterations = keyset.iterations,
            parallelism = keyset.parallelism
        )
        val currentMasterKey = PasswordKdf.argon2id(currentPassword.toCharArray(), keyset.salt, kdfParams)

        val oldVk = try {
            VaultCrypto.unwrapKey(currentMasterKey, keyset.vkByMaster, Slot.MASTER)
        } catch (e: Exception) {
            currentMasterKey.fill(0)
            throw Exception("Incorrect current master password")
        } finally {
            currentMasterKey.fill(0)
        }

        val newVk = VaultCrypto.newKey()

        val random = SecureRandom()
        val newMasterSalt = ByteArray(16).also { random.nextBytes(it) }
        val passwordToUse = newPassword ?: currentPassword
        val newMasterKey = PasswordKdf.argon2id(passwordToUse.toCharArray(), newMasterSalt, kdfParams)

        val newRecoveryCode = RecoveryCode.generate()
        val newRecoverySalt = ByteArray(16).also { random.nextBytes(it) }
        val newRecoveryKey = RecoveryCode.key(newRecoveryCode, newRecoverySalt)

        try {
            val newVkByMaster = VaultCrypto.wrapKey(newMasterKey, newVk, Slot.MASTER)
            val newVkByRecovery = VaultCrypto.wrapKey(newRecoveryKey, newVk, Slot.RECOVERY)

            val executeRotation = suspend {
                val allItems = itemDao.getAllRawItems()
                for (item in allItems) {
                    val metaAad = "coconut-vault-v1|${item.id}|meta".toByteArray(Charsets.UTF_8)
                    val secretAad = "coconut-vault-v1|${item.id}|secret".toByteArray(Charsets.UTF_8)

                    val decryptedMeta = VaultCrypto.decrypt(oldVk, item.metaBlob, metaAad)
                    val decryptedSecret = VaultCrypto.decrypt(oldVk, item.secretBlob, secretAad)

                    val newMetaBlob = VaultCrypto.encrypt(newVk, decryptedMeta, metaAad)
                    val newSecretBlob = VaultCrypto.encrypt(newVk, decryptedSecret, secretAad)

                    decryptedMeta.fill(0)
                    decryptedSecret.fill(0)

                    val updatedItem = item.copy(
                        metaBlob = newMetaBlob,
                        secretBlob = newSecretBlob,
                        updatedAt = System.currentTimeMillis()
                    )
                    itemDao.updateItem(updatedItem)
                }

                val now = System.currentTimeMillis()
                val updatedKeyset = keyset.copy(
                    salt = newMasterSalt,
                    vkByMaster = newVkByMaster,
                    vkByRecovery = newVkByRecovery,
                    recoverySalt = newRecoverySalt,
                    keyVersion = keyset.keyVersion + 1,
                    masterChangedAt = now
                )
                keysetDao.updateKeyset(updatedKeyset)
            }

            if (database != null) {
                database.withTransaction {
                    executeRotation()
                }
            } else {
                executeRotation()
            }

            VaultSession.unlock(newVk)
            return newRecoveryCode
        } finally {
            oldVk.fill(0)
            newVk.fill(0)
            newMasterKey.fill(0)
            newRecoveryKey.fill(0)
        }
    }

    override fun toString(): String = "<redacted>"
}