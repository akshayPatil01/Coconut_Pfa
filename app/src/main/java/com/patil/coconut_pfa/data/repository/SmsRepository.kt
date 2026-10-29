package com.patil.coconut_pfa.data.repository

import com.patil.coconut_pfa.data.local.dao.MerchantMemoryDao
import com.patil.coconut_pfa.data.local.dao.SmsItemDao
import com.patil.coconut_pfa.data.local.entity.DbSmsStatus
import com.patil.coconut_pfa.data.local.entity.MerchantMemoryEntity
import com.patil.coconut_pfa.data.local.entity.SmsItemEntity
import kotlinx.coroutines.flow.Flow

class SmsRepository(
    private val smsItemDao: SmsItemDao,
    private val merchantMemoryDao: MerchantMemoryDao
) {
    fun getAllSms(): Flow<List<SmsItemEntity>> = smsItemDao.getAllSmsFlow()
    fun getSmsByStatus(status: DbSmsStatus): Flow<List<SmsItemEntity>> = smsItemDao.getSmsByStatusFlow(status)
    suspend fun getSmsById(id: String): SmsItemEntity? = smsItemDao.getSmsById(id)
    suspend fun getSmsByHash(bodyHash: String): SmsItemEntity? = smsItemDao.getSmsByHash(bodyHash)

    suspend fun saveSms(smsItem: SmsItemEntity) = smsItemDao.insert(smsItem)
    suspend fun updateSms(smsItem: SmsItemEntity) = smsItemDao.update(smsItem)
    suspend fun deleteSms(smsItem: SmsItemEntity) = smsItemDao.delete(smsItem)

    suspend fun getMerchantMemory(merchantKey: String): MerchantMemoryEntity? = merchantMemoryDao.getByMerchantKey(merchantKey)
    fun getAllMerchantMemories(): Flow<List<MerchantMemoryEntity>> = merchantMemoryDao.getAllMerchantMemoriesFlow()
    suspend fun saveMerchantMemory(merchantMemory: MerchantMemoryEntity) = merchantMemoryDao.insert(merchantMemory)
}
