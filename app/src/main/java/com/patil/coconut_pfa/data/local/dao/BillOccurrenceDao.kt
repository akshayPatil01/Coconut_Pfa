package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.BillOccurrenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillOccurrenceDao {
    @Query("SELECT * FROM bill_occurrences WHERE id = :id")
    suspend fun getById(id: String): BillOccurrenceEntity?

    @Query("SELECT * FROM bill_occurrences WHERE billId = :billId ORDER BY dueDate ASC")
    fun getOccurrencesForBillFlow(billId: String): Flow<List<BillOccurrenceEntity>>

    @Query("SELECT * FROM bill_occurrences WHERE status = 'UPCOMING' ORDER BY dueDate ASC")
    fun getUpcomingOccurrencesFlow(): Flow<List<BillOccurrenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(occurrence: BillOccurrenceEntity)

    @Update
    suspend fun update(occurrence: BillOccurrenceEntity)

    @Delete
    suspend fun delete(occurrence: BillOccurrenceEntity)
}
