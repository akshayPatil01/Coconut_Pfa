package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.VaultKeyset
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultKeysetDao {
    @Query("SELECT * FROM vault_keysets WHERE id = 1 LIMIT 1")
    fun getKeysetFlow(): Flow<VaultKeyset?>

    @Query("SELECT * FROM vault_keysets WHERE id = 1 LIMIT 1")
    suspend fun getKeyset(): VaultKeyset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyset(keyset: VaultKeyset)

    @Update
    suspend fun updateKeyset(keyset: VaultKeyset)
}
