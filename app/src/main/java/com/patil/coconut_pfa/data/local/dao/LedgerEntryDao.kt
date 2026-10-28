package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.LedgerEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerEntryDao {
    @Query("SELECT * FROM ledger_entries WHERE personId = :personId ORDER BY date DESC")
    fun getEntriesForPersonFlow(personId: String): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries ORDER BY date DESC")
    fun getAllEntriesFlow(): Flow<List<LedgerEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LedgerEntryEntity)

    @Update
    suspend fun update(entry: LedgerEntryEntity)

    @Delete
    suspend fun delete(entry: LedgerEntryEntity)
}
