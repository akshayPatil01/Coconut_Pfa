package com.patil.coconut_pfa.data.repository

import com.patil.coconut_pfa.data.local.dao.VaultItemDao
import com.patil.coconut_pfa.data.local.dao.VaultKeysetDao
import com.patil.coconut_pfa.data.local.entity.VaultItem
import com.patil.coconut_pfa.data.local.entity.VaultItemType
import com.patil.coconut_pfa.data.local.entity.VaultKeyset
import com.patil.coconut_pfa.data.local.security.VaultLockedException
import com.patil.coconut_pfa.data.local.security.VaultSession
import kotlinx.coroutines.flow.Flow
import vault.crypto.VaultCrypto
import java.util.UUID

class VaultRepository(
    private val keysetDao: VaultKeysetDao,
    private val itemDao: VaultItemDao
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

    override fun toString(): String = "<redacted>"
}
