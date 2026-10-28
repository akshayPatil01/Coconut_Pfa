package com.patil.coconut_pfa.data.repository

import com.patil.coconut_pfa.data.local.dao.TxnDao
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val txnDao: TxnDao
) {
    fun getAllActiveTransactions(): Flow<List<TxnEntity>> = txnDao.getAllActiveTxnsFlow()
    fun getTransactionsForAccount(accountId: String): Flow<List<TxnEntity>> = txnDao.getTxnsForAccountFlow(accountId)
    fun getTrashTransactions(): Flow<List<TxnEntity>> = txnDao.getTrashTxnsFlow()
    fun getDistinctPayees(): Flow<List<String>> = txnDao.getDistinctPayeesFlow()
    suspend fun getTransactionById(id: String): TxnEntity? = txnDao.getTxnById(id)

    suspend fun saveTransaction(txn: TxnEntity) = txnDao.insert(txn)
    suspend fun updateTransaction(txn: TxnEntity) = txnDao.update(txn)
    suspend fun softDeleteTransaction(id: String) = txnDao.softDelete(id)
    suspend fun restoreTransaction(id: String) = txnDao.restore(id)
    suspend fun deleteTransaction(txn: TxnEntity) = txnDao.delete(txn)
    suspend fun purgeOldTrash(cutoffMillis: Long) = txnDao.purgeOldTrash(cutoffMillis)
}
