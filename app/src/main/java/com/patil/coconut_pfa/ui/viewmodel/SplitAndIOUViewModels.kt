package com.patil.coconut_pfa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.DbLedgerEntryKind
import com.patil.coconut_pfa.data.local.entity.DbTxnSource
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.LedgerEntryEntity
import com.patil.coconut_pfa.data.local.entity.PersonEntity
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.repository.AccountRepository
import com.patil.coconut_pfa.data.repository.PersonLedgerRepository
import com.patil.coconut_pfa.data.repository.TransactionRepository
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class PersonItemWithNet(
    val person: PersonEntity,
    val entries: List<LedgerEntryEntity>,
    val netBalancePaise: Long
)

data class SplitAndIOUsUiState(
    val persons: List<PersonItemWithNet> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val totalOwesYouPaise: Long = 0L,
    val totalYouOwePaise: Long = 0L,
    val isLoading: Boolean = true
)

class SplitAndIOUsViewModel(
    private val personLedgerRepository: PersonLedgerRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<SplitAndIOUsUiState> = combine(
        personLedgerRepository.getAllPersons(),
        personLedgerRepository.getAllLedgerEntries(),
        accountRepository.getActiveAccounts()
    ) { persons, entries, accounts ->
        var owesYouTotal = 0L
        var youOweTotal = 0L

        val personItems = persons.map { person ->
            val pEntries = entries.filter { it.personId == person.id }
            var net = 0L
            for (entry in pEntries) {
                val amt = entry.amountPaise
                when (entry.kind) {
                    DbLedgerEntryKind.LENT -> net += amt
                    DbLedgerEntryKind.BORROWED -> net -= amt
                    DbLedgerEntryKind.SPLIT_SHARE -> net += amt
                    DbLedgerEntryKind.SETTLEMENT -> net -= amt
                }
            }
            if (net > 0) owesYouTotal += net
            else if (net < 0) youOweTotal += kotlin.math.abs(net)

            PersonItemWithNet(
                person = person,
                entries = pEntries,
                netBalancePaise = net
            )
        }

        SplitAndIOUsUiState(
            persons = personItems,
            accounts = accounts,
            totalOwesYouPaise = owesYouTotal,
            totalYouOwePaise = youOweTotal,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SplitAndIOUsUiState(isLoading = true)
    )

    fun savePerson(name: String, note: String?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val person = PersonEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                note = note?.trim()?.ifEmpty { null }
            )
            personLedgerRepository.savePerson(person)
        }
    }

    fun addLendOrBorrow(
        personId: String,
        amountPaise: Long,
        isLend: Boolean,
        note: String?,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        if (amountPaise <= 0L) return
        viewModelScope.launch {
            val entry = LedgerEntryEntity(
                id = UUID.randomUUID().toString(),
                personId = personId,
                amountPaise = amountPaise,
                kind = if (isLend) DbLedgerEntryKind.LENT else DbLedgerEntryKind.BORROWED,
                date = KolkataDateTimeUtils.epochMillisToLocalDate(dateMillis).format(KolkataDateTimeUtils.DATE_FORMATTER),
                note = note?.trim()?.ifEmpty { null }
            )
            personLedgerRepository.saveLedgerEntry(entry)
        }
    }

    fun settleUp(
        personId: String,
        accountId: String,
        amountPaise: Long,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        if (amountPaise <= 0L) return
        viewModelScope.launch {
            val dateStr = KolkataDateTimeUtils.epochMillisToLocalDate(dateMillis).format(KolkataDateTimeUtils.DATE_FORMATTER)
            val txnId = UUID.randomUUID().toString()

            val txn = TxnEntity(
                id = txnId,
                type = DbTxnType.SETTLEMENT,
                amountPaise = amountPaise,
                accountId = accountId,
                personId = personId,
                payee = "Settlement",
                note = "Debt settlement",
                occurredAt = dateMillis,
                source = DbTxnSource.MANUAL
            )
            transactionRepository.saveTransaction(txn)

            val entry = LedgerEntryEntity(
                id = UUID.randomUUID().toString(),
                personId = personId,
                amountPaise = amountPaise,
                kind = DbLedgerEntryKind.SETTLEMENT,
                date = dateStr,
                note = "Settlement",
                txnId = txnId
            )
            personLedgerRepository.saveLedgerEntry(entry)
        }
    }

    fun deleteLedgerEntry(entry: LedgerEntryEntity) {
        viewModelScope.launch {
            personLedgerRepository.deleteLedgerEntry(entry)
            if (entry.txnId != null) {
                val txn = transactionRepository.getTransactionById(entry.txnId)
                if (txn != null) {
                    transactionRepository.deleteTransaction(txn)
                }
            }
        }
    }

    class Factory(
        private val personLedgerRepository: PersonLedgerRepository,
        private val accountRepository: AccountRepository,
        private val transactionRepository: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SplitAndIOUsViewModel(personLedgerRepository, accountRepository, transactionRepository) as T
        }
    }
}
