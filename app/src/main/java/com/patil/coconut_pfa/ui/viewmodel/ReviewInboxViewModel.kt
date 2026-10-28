package com.patil.coconut_pfa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.CategoryEntity
import com.patil.coconut_pfa.data.local.entity.DbSmsStatus
import com.patil.coconut_pfa.data.local.entity.DbTxnSource
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.MerchantMemoryEntity
import com.patil.coconut_pfa.data.local.entity.SmsItemEntity
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.repository.AccountRepository
import com.patil.coconut_pfa.data.repository.CategoryRepository
import com.patil.coconut_pfa.data.repository.SmsRepository
import com.patil.coconut_pfa.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

data class ReviewInboxUiState(
    val pendingSmsList: List<SmsItemEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val activeTxns: List<TxnEntity> = emptyList(),
    val accountMap: Map<String, AccountEntity> = emptyMap(),
    val categoryMap: Map<String, CategoryEntity> = emptyMap(),
    val isLoading: Boolean = true
)

class ReviewInboxViewModel(
    private val smsRepository: SmsRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<ReviewInboxUiState> = combine(
        smsRepository.getSmsByStatus(DbSmsStatus.PENDING),
        accountRepository.getActiveAccounts(),
        categoryRepository.getAllCategories(),
        transactionRepository.getAllActiveTransactions()
    ) { pendingSms, accounts, categories, txns ->
        ReviewInboxUiState(
            pendingSmsList = pendingSms,
            accounts = accounts,
            categories = categories,
            activeTxns = txns,
            accountMap = accounts.associateBy { it.id },
            categoryMap = categories.associateBy { it.id },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReviewInboxUiState(isLoading = true)
    )

    fun confirmSms(
        sms: SmsItemEntity,
        selectedAccountId: String,
        selectedCategoryId: String,
        payee: String?,
        customAmountPaise: Long? = null
    ) {
        viewModelScope.launch {
            val isIncome = sms.parsedType == "CREDIT"
            val txnType = if (isIncome) DbTxnType.INCOME else DbTxnType.EXPENSE
            val txnId = UUID.randomUUID().toString()
            val finalAmountPaise = customAmountPaise ?: sms.amountPaise ?: 0L

            val txn = TxnEntity(
                id = txnId,
                type = txnType,
                amountPaise = finalAmountPaise,
                accountId = selectedAccountId,
                categoryId = selectedCategoryId,
                payee = payee?.trim()?.ifEmpty { null } ?: sms.merchant,
                occurredAt = sms.receivedAt,
                source = DbTxnSource.SMS
            )
            transactionRepository.saveTransaction(txn)

            // Update SMS Item as CONFIRMED
            val updatedSms = sms.copy(
                status = DbSmsStatus.CONFIRMED,
                matchedAccountId = selectedAccountId,
                suggestedCategoryId = selectedCategoryId,
                amountPaise = finalAmountPaise,
                resolvedAt = System.currentTimeMillis(),
                txnId = txnId
            )
            smsRepository.updateSms(updatedSms)

            // Update MerchantMemory
            val merchantName = payee ?: sms.merchant
            if (!merchantName.isNullOrBlank()) {
                val merchantKey = merchantName.lowercase().replace(Regex("[0-9]"), "").trim()
                val memory = MerchantMemoryEntity(
                    merchantKey = merchantKey,
                    categoryId = selectedCategoryId,
                    lastUsedAt = System.currentTimeMillis()
                )
                smsRepository.saveMerchantMemory(memory)
            }
        }
    }

    fun linkToExisting(sms: SmsItemEntity, existingTxnId: String) {
        viewModelScope.launch {
            val updatedSms = sms.copy(
                status = DbSmsStatus.CONFIRMED,
                resolvedAt = System.currentTimeMillis(),
                txnId = existingTxnId
            )
            smsRepository.updateSms(updatedSms)
        }
    }

    fun ignoreSms(sms: SmsItemEntity) {
        viewModelScope.launch {
            val updated = sms.copy(
                status = DbSmsStatus.IGNORED,
                resolvedAt = System.currentTimeMillis()
            )
            smsRepository.updateSms(updated)
        }
    }

    fun markDuplicate(sms: SmsItemEntity) {
        viewModelScope.launch {
            val updated = sms.copy(
                status = DbSmsStatus.DUPLICATE,
                resolvedAt = System.currentTimeMillis()
            )
            smsRepository.updateSms(updated)
        }
    }

    fun saveLast4(accountId: String, last4: String) {
        viewModelScope.launch {
            val acc = accountRepository.getAccountById(accountId) ?: return@launch
            val updatedAcc = acc.copy(last4 = last4, updatedAt = System.currentTimeMillis())
            accountRepository.saveAccount(updatedAcc)
        }
    }

    fun confirmAllMatched() {
        viewModelScope.launch {
            val pendingList = uiState.value.pendingSmsList
            for (sms in pendingList) {
                if (sms.matchedAccountId != null && sms.suggestedCategoryId != null && sms.amountPaise != null) {
                    confirmSms(sms, sms.matchedAccountId, sms.suggestedCategoryId, sms.merchant)
                }
            }
        }
    }

    fun findMatchingManualTxn(sms: SmsItemEntity, txns: List<TxnEntity>): TxnEntity? {
        if (sms.amountPaise == null || sms.matchedAccountId == null) return null
        return txns.firstOrNull { tx ->
            tx.source == DbTxnSource.MANUAL &&
                    tx.amountPaise == sms.amountPaise &&
                    tx.accountId == sms.matchedAccountId &&
                    abs(tx.occurredAt - sms.receivedAt) <= 24 * 60 * 60 * 1000L
        }
    }

    class Factory(
        private val smsRepository: SmsRepository,
        private val accountRepository: AccountRepository,
        private val categoryRepository: CategoryRepository,
        private val transactionRepository: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReviewInboxViewModel(
                smsRepository,
                accountRepository,
                categoryRepository,
                transactionRepository
            ) as T
        }
    }
}
