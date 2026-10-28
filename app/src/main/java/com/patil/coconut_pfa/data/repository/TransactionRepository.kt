package com.patil.coconut_pfa.data.repository

import android.content.Context
import com.patil.coconut_pfa.data.local.dao.TxnDao
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.widget.CoconutWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TransactionRepository(
    private val txnDao: TxnDao,
    private val context: Context? = null
) {
    fun getAllActiveTransactions(): Flow<List<TxnEntity>> = txnDao.getAllActiveTxnsFlow()
    fun getTransactionsForAccount(accountId: String): Flow<List<TxnEntity>> = txnDao.getTxnsForAccountFlow(accountId)
    fun getTrashTransactions(): Flow<List<TxnEntity>> = txnDao.getTrashTxnsFlow()
    fun getDistinctPayees(): Flow<List<String>> = txnDao.getDistinctPayeesFlow()
    suspend fun getTransactionById(id: String): TxnEntity? = txnDao.getTxnById(id)

    private fun triggerWidgetUpdate() {
        context?.let { ctx ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    CoconutWidget.updateAll(ctx)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    suspend fun saveTransaction(txn: TxnEntity) {
        txnDao.insert(txn)
        triggerWidgetUpdate()
    }
    suspend fun saveTransactions(txns: List<TxnEntity>) {
        txnDao.insertAll(txns)
        triggerWidgetUpdate()
    }
    suspend fun updateTransaction(txn: TxnEntity) {
        txnDao.update(txn)
        triggerWidgetUpdate()
    }
    suspend fun softDeleteTransaction(id: String) {
        txnDao.softDelete(id)
        triggerWidgetUpdate()
    }
    suspend fun restoreTransaction(id: String) {
        txnDao.restore(id)
        triggerWidgetUpdate()
    }
    suspend fun deleteTransaction(txn: TxnEntity) {
        txnDao.delete(txn)
        triggerWidgetUpdate()
    }
    suspend fun purgeOldTrash(cutoffMillis: Long) {
        txnDao.purgeOldTrash(cutoffMillis)
        triggerWidgetUpdate()
    }
}
