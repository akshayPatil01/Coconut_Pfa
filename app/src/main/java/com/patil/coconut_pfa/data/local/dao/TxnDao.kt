package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TxnDao {
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTxnById(id: String): TxnEntity?

    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<TxnEntity>

    @Query("SELECT * FROM transactions WHERE deletedAt IS NULL ORDER BY occurredAt DESC")
    fun getAllActiveTxnsFlow(): Flow<List<TxnEntity>>

    @Query("SELECT * FROM transactions WHERE (accountId = :accountId OR toAccountId = :accountId) AND deletedAt IS NULL ORDER BY occurredAt DESC")
    fun getTxnsForAccountFlow(accountId: String): Flow<List<TxnEntity>>

    @Query("SELECT * FROM transactions WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getTrashTxnsFlow(): Flow<List<TxnEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(txn: TxnEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(txns: List<TxnEntity>)

    @Update
    suspend fun update(txn: TxnEntity)

    @Delete
    suspend fun delete(txn: TxnEntity)

    @Query("UPDATE transactions SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE transactions SET deletedAt = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restore(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT DISTINCT payee FROM transactions WHERE payee IS NOT NULL AND payee != '' AND deletedAt IS NULL ORDER BY payee ASC")
    fun getDistinctPayeesFlow(): Flow<List<String>>

    @Query("DELETE FROM transactions WHERE deletedAt IS NOT NULL AND deletedAt < :cutoffMillis")
    suspend fun purgeOldTrash(cutoffMillis: Long)
}
