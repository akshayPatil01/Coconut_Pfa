package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.CardStatementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardStatementDao {
    @Query("SELECT * FROM card_statements WHERE id = :id")
    suspend fun getById(id: String): CardStatementEntity?

    @Query("SELECT * FROM card_statements WHERE accountId = :accountId ORDER BY periodEnd DESC")
    fun getStatementsForAccountFlow(accountId: String): Flow<List<CardStatementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(statement: CardStatementEntity)

    @Update
    suspend fun update(statement: CardStatementEntity)

    @Delete
    suspend fun delete(statement: CardStatementEntity)
}
