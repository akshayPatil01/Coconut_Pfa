package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.NetWorthSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NetWorthSnapshotDao {
    @Query("SELECT * FROM net_worth_snapshots ORDER BY cycleStart DESC")
    fun getAllSnapshotsFlow(): Flow<List<NetWorthSnapshotEntity>>

    @Query("SELECT * FROM net_worth_snapshots")
    suspend fun getAll(): List<NetWorthSnapshotEntity>

    @Query("SELECT * FROM net_worth_snapshots WHERE cycleStart = :cycleStart")
    suspend fun getByCycleStart(cycleStart: String): NetWorthSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: NetWorthSnapshotEntity)

    @Update
    suspend fun update(snapshot: NetWorthSnapshotEntity)

    @Delete
    suspend fun delete(snapshot: NetWorthSnapshotEntity)
}
