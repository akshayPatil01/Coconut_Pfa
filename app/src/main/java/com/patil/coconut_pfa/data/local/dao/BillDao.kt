package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.BillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getBillById(id: String): BillEntity?

    @Query("SELECT * FROM bills WHERE active = 1 ORDER BY name ASC")
    fun getActiveBillsFlow(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE active = 1 ORDER BY name ASC")
    suspend fun getActiveBills(): List<BillEntity>

    @Query("SELECT * FROM bills ORDER BY name ASC")
    fun getAllBillsFlow(): Flow<List<BillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: BillEntity)

    @Update
    suspend fun update(bill: BillEntity)

    @Delete
    suspend fun delete(bill: BillEntity)
}
