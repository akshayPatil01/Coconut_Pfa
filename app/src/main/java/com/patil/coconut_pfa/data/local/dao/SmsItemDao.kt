package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.DbSmsStatus
import com.patil.coconut_pfa.data.local.entity.SmsItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsItemDao {
    @Query("SELECT * FROM sms_items WHERE id = :id")
    suspend fun getSmsById(id: String): SmsItemEntity?

    @Query("SELECT * FROM sms_items WHERE bodyHash = :bodyHash LIMIT 1")
    suspend fun getSmsByHash(bodyHash: String): SmsItemEntity?

    @Query("SELECT * FROM sms_items WHERE status = :status ORDER BY receivedAt DESC")
    fun getSmsByStatusFlow(status: DbSmsStatus): Flow<List<SmsItemEntity>>

    @Query("SELECT * FROM sms_items ORDER BY receivedAt DESC")
    fun getAllSmsFlow(): Flow<List<SmsItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(smsItem: SmsItemEntity)

    @Update
    suspend fun update(smsItem: SmsItemEntity)

    @Delete
    suspend fun delete(smsItem: SmsItemEntity)
}
