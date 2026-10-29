package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.ValuationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ValuationDao {
    @Query("SELECT * FROM valuations WHERE accountId = :accountId ORDER BY date DESC")
    fun getValuationsForAccountFlow(accountId: String): Flow<List<ValuationEntity>>

    @Query("SELECT * FROM valuations ORDER BY date DESC")
    fun getAllValuationsFlow(): Flow<List<ValuationEntity>>

    @Query("SELECT * FROM valuations")
    suspend fun getAll(): List<ValuationEntity>

    @Query("SELECT * FROM valuations WHERE accountId = :accountId ORDER BY date DESC")
    suspend fun getValuationsForAccount(accountId: String): List<ValuationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(valuation: ValuationEntity)

    @Update
    suspend fun update(valuation: ValuationEntity)

    @Delete
    suspend fun delete(valuation: ValuationEntity)
}
