package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.VaultItem
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultItemDao {
    @Query("SELECT * FROM vault_items WHERE deletedAt IS NULL ORDER BY favourite DESC, updatedAt DESC")
    fun getAllItems(): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items")
    suspend fun getAllRawItems(): List<VaultItem>

    @Query("SELECT * FROM vault_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): VaultItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItem)

    @Update
    suspend fun updateItem(item: VaultItem)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteItem(id: String)
}
