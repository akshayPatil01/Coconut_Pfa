package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_keysets")
data class VaultKeyset(
    @PrimaryKey val id: Int = 1,
    val kdf: String = "ARGON2ID",
    val salt: ByteArray,
    val memoryKiB: Int = 65536,
    val iterations: Int = 3,
    val parallelism: Int = 1,
    val vkByMaster: ByteArray,
    val vkByRecovery: ByteArray,
    val recoverySalt: ByteArray,
    val keyVersion: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val masterChangedAt: Long = System.currentTimeMillis()
) {
    override fun toString(): String = "<redacted>"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VaultKeyset

        if (id != other.id) return false
        if (kdf != other.kdf) return false
        if (!salt.contentEquals(other.salt)) return false
        if (memoryKiB != other.memoryKiB) return false
        if (iterations != other.iterations) return false
        if (parallelism != other.parallelism) return false
        if (!vkByMaster.contentEquals(other.vkByMaster)) return false
        if (!vkByRecovery.contentEquals(other.vkByRecovery)) return false
        if (!recoverySalt.contentEquals(other.recoverySalt)) return false
        if (keyVersion != other.keyVersion) return false
        if (createdAt != other.createdAt) return false
        if (masterChangedAt != other.masterChangedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + kdf.hashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + memoryKiB
        result = 31 * result + iterations
        result = 31 * result + parallelism
        result = 31 * result + vkByMaster.contentHashCode()
        result = 31 * result + vkByRecovery.contentHashCode()
        result = 31 * result + recoverySalt.contentHashCode()
        result = 31 * result + keyVersion
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + masterChangedAt.hashCode()
        return result
    }
}
