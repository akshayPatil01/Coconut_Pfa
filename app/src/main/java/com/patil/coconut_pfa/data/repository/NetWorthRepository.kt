package com.patil.coconut_pfa.data.repository

import com.patil.coconut_pfa.data.local.dao.NetWorthSnapshotDao
import com.patil.coconut_pfa.data.local.entity.NetWorthSnapshotEntity
import kotlinx.coroutines.flow.Flow

class NetWorthRepository(
    private val netWorthSnapshotDao: NetWorthSnapshotDao
) {
    fun getAllSnapshots(): Flow<List<NetWorthSnapshotEntity>> = netWorthSnapshotDao.getAllSnapshotsFlow()
    suspend fun getSnapshotByCycleStart(cycleStart: String): NetWorthSnapshotEntity? = netWorthSnapshotDao.getByCycleStart(cycleStart)
    suspend fun saveSnapshot(snapshot: NetWorthSnapshotEntity) = netWorthSnapshotDao.insert(snapshot)
    suspend fun deleteSnapshot(snapshot: NetWorthSnapshotEntity) = netWorthSnapshotDao.delete(snapshot)
}
