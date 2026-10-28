package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.FdDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FdDetailsDao {
    @Query("SELECT * FROM fd_details WHERE accountId = :accountId")
    suspend fun getByAccountId(accountId: String): FdDetailsEntity?

    @Query("SELECT * FROM fd_details WHERE accountId = :accountId")
    fun getByAccountIdFlow(accountId: String): Flow<FdDetailsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fdDetails: FdDetailsEntity)

    @Update
    suspend fun update(fdDetails: FdDetailsEntity)

    @Delete
    suspend fun delete(fdDetails: FdDetailsEntity)
}
