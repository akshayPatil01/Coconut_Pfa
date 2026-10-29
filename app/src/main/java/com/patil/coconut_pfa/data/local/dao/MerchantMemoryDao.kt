package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.MerchantMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantMemoryDao {
    @Query("SELECT * FROM merchant_memories WHERE merchantKey = :merchantKey")
    suspend fun getByMerchantKey(merchantKey: String): MerchantMemoryEntity?

    @Query("SELECT * FROM merchant_memories")
    suspend fun getAll(): List<MerchantMemoryEntity>

    @Query("SELECT * FROM merchant_memories ORDER BY lastUsedAt DESC")
    fun getAllMerchantMemoriesFlow(): Flow<List<MerchantMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(merchantMemory: MerchantMemoryEntity)

    @Update
    suspend fun update(merchantMemory: MerchantMemoryEntity)

    @Delete
    suspend fun delete(merchantMemory: MerchantMemoryEntity)
}
