package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class VaultItemType {
    LOGIN,
    BANK_LOGIN,
    NOTE,
    CARD
}

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: VaultItemType,
    val metaBlob: ByteArray,
    val secretBlob: ByteArray,
    val favourite: Boolean = false,
    val linkedAccountId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null,
    val deletedAt: Long? = null,
    val keyVersion: Int = 1
) {
    override fun toString(): String = "<redacted>"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VaultItem

        if (id != other.id) return false
        if (type != other.type) return false
        if (!metaBlob.contentEquals(other.metaBlob)) return false
        if (!secretBlob.contentEquals(other.secretBlob)) return false
        if (favourite != other.favourite) return false
        if (linkedAccountId != other.linkedAccountId) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (lastUsedAt != other.lastUsedAt) return false
        if (deletedAt != other.deletedAt) return false
        if (keyVersion != other.keyVersion) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + metaBlob.contentHashCode()
        result = 31 * result + secretBlob.contentHashCode()
        result = 31 * result + favourite.hashCode()
        result = 31 * result + (linkedAccountId?.hashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + (lastUsedAt?.hashCode() ?: 0)
        result = 31 * result + (deletedAt?.hashCode() ?: 0)
        result = 31 * result + keyVersion
        return result
    }
}
