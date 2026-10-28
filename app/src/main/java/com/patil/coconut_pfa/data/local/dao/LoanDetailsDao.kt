package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.LoanDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDetailsDao {
    @Query("SELECT * FROM loan_details WHERE accountId = :accountId")
    suspend fun getByAccountId(accountId: String): LoanDetailsEntity?

    @Query("SELECT * FROM loan_details WHERE accountId = :accountId")
    fun getByAccountIdFlow(accountId: String): Flow<LoanDetailsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loanDetails: LoanDetailsEntity)

    @Update
    suspend fun update(loanDetails: LoanDetailsEntity)

    @Delete
    suspend fun delete(loanDetails: LoanDetailsEntity)
}
