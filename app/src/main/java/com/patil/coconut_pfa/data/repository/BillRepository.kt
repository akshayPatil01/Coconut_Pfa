package com.patil.coconut_pfa.data.repository

import com.patil.coconut_pfa.data.local.dao.BillDao
import com.patil.coconut_pfa.data.local.dao.BillOccurrenceDao
import com.patil.coconut_pfa.data.local.entity.BillEntity
import com.patil.coconut_pfa.data.local.entity.BillOccurrenceEntity
import kotlinx.coroutines.flow.Flow

class BillRepository(
    private val billDao: BillDao,
    private val billOccurrenceDao: BillOccurrenceDao
) {
    fun getActiveBills(): Flow<List<BillEntity>> = billDao.getActiveBillsFlow()
    fun getAllBills(): Flow<List<BillEntity>> = billDao.getAllBillsFlow()
    suspend fun getBillById(id: String): BillEntity? = billDao.getBillById(id)

    suspend fun saveBill(bill: BillEntity) = billDao.insert(bill)
    suspend fun updateBill(bill: BillEntity) = billDao.update(bill)
    suspend fun deleteBill(bill: BillEntity) = billDao.delete(bill)

    fun getOccurrencesForBill(billId: String): Flow<List<BillOccurrenceEntity>> = billOccurrenceDao.getOccurrencesForBillFlow(billId)
    fun getUpcomingOccurrences(): Flow<List<BillOccurrenceEntity>> = billOccurrenceDao.getUpcomingOccurrencesFlow()
    suspend fun saveOccurrence(occurrence: BillOccurrenceEntity) = billOccurrenceDao.insert(occurrence)
    suspend fun updateOccurrence(occurrence: BillOccurrenceEntity) = billOccurrenceDao.update(occurrence)
}
