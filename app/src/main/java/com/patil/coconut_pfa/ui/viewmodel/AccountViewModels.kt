package com.patil.coconut_pfa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.CardDetailsEntity
import com.patil.coconut_pfa.data.local.entity.DbAccountType
import com.patil.coconut_pfa.data.local.entity.DbLedgerEntryKind
import com.patil.coconut_pfa.data.local.entity.DbTxnSource
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.FdDetailsEntity
import com.patil.coconut_pfa.data.local.entity.LedgerEntryEntity
import com.patil.coconut_pfa.data.local.entity.LoanDetailsEntity
import com.patil.coconut_pfa.data.local.entity.NetWorthSnapshotEntity
import com.patil.coconut_pfa.data.local.entity.PersonEntity
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.local.entity.ValuationEntity
import com.patil.coconut_pfa.data.model.Account
import com.patil.coconut_pfa.data.model.AccountType
import com.patil.coconut_pfa.data.repository.AccountRepository
import com.patil.coconut_pfa.data.repository.NetWorthRepository
import com.patil.coconut_pfa.data.repository.PersonLedgerRepository
import com.patil.coconut_pfa.data.repository.SettingsRepository
import com.patil.coconut_pfa.data.repository.TransactionRepository
import com.patil.coconut_pfa.domain.AccountDomainEngine
import com.patil.coconut_pfa.domain.AccountNetWorthItem
import com.patil.coconut_pfa.domain.NetWorthCalculator
import com.patil.coconut_pfa.domain.PersonLedgerItem
import com.patil.coconut_pfa.domain.SalaryCycleCalculator
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

private data class AccountsDataTuple(
    val accounts: List<AccountEntity>,
    val txns: List<TxnEntity>,
    val snapshots: List<NetWorthSnapshotEntity>,
    val persons: List<PersonEntity>,
    val ledgerEntries: List<LedgerEntryEntity>
)

data class AccountsScreenUiState(
    val activeAccounts: List<Account> = emptyList(),
    val archivedAccounts: List<Account> = emptyList(),
    val totalNetWorth: Long = 0L,
    val totalAssets: Long = 0L,
    val totalLiabilities: Long = 0L,
    val sparklineTrend: List<Double> = emptyList(),
    val isLoading: Boolean = false
)

class AccountsViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val netWorthRepository: NetWorthRepository,
    private val personLedgerRepository: PersonLedgerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val baseDataFlow = combine(
        accountRepository.getAllAccounts(),
        transactionRepository.getAllActiveTransactions(),
        netWorthRepository.getAllSnapshots(),
        personLedgerRepository.getAllPersons(),
        personLedgerRepository.getAllLedgerEntries()
    ) { accounts, txns, snapshots, persons, ledgerEntries ->
        AccountsDataTuple(accounts, txns, snapshots, persons, ledgerEntries)
    }

    val uiState: StateFlow<AccountsScreenUiState> = combine(
        baseDataFlow,
        settingsRepository.settingsFlow
    ) { tuple, settings ->
        val accounts = tuple.accounts
        val txns = tuple.txns
        val snapshots = tuple.snapshots
        val persons = tuple.persons
        val ledgerEntries = tuple.ledgerEntries

        val domainAccounts = accounts.map { entity ->
            val cardDetails = if (entity.type == DbAccountType.CREDIT_CARD) accountRepository.getCardDetails(entity.id) else null
            val loanDetails = if (entity.type == DbAccountType.LOAN) accountRepository.getLoanDetails(entity.id) else null
            val fdDetails = if (entity.type == DbAccountType.FD) accountRepository.getFdDetails(entity.id) else null
            val valuations = accountRepository.getValuationsList(entity.id)
            val accTxns = txns.filter { it.accountId == entity.id || it.toAccountId == entity.id }

            AccountDomainEngine.buildAccountModel(
                entity = entity,
                cardDetails = cardDetails,
                loanDetails = loanDetails,
                fdDetails = fdDetails,
                valuations = valuations,
                transactions = accTxns
            )
        }

        val active = domainAccounts.filter { !it.archived }
        val archived = domainAccounts.filter { it.archived }

        val personItems = persons.map { person ->
            val pEntries = ledgerEntries.filter { it.personId == person.id }
            var net = 0L
            for (entry in pEntries) {
                val amount = entry.amountPaise
                when (entry.kind) {
                    DbLedgerEntryKind.LENT -> net += amount
                    DbLedgerEntryKind.BORROWED -> net -= amount
                    DbLedgerEntryKind.SPLIT_SHARE -> net += amount
                    DbLedgerEntryKind.SETTLEMENT -> net -= amount
                }
            }
            PersonLedgerItem(personId = person.id, netBalancePaise = net)
        }

        val netWorthItems = active.map { acc ->
            AccountNetWorthItem(
                id = acc.id,
                name = acc.name,
                isLiabilityAccount = acc.type == AccountType.CREDIT_CARD || acc.type == AccountType.LOAN,
                balancePaise = acc.balanceInPaise,
                includeInNetWorth = acc.includeInNetWorth
            )
        }

        val nwSummary = NetWorthCalculator.calculateNetWorth(netWorthItems, personItems)

        checkAndSaveCycleSnapshot(
            salaryCycleStartDay = settings.salaryCycleStartDay,
            assetsPaise = nwSummary.totalAssetsPaise,
            liabilitiesPaise = nwSummary.totalLiabilitiesPaise,
            existingSnapshots = snapshots
        )

        val sparkline = snapshots
            .sortedBy { it.cycleStart }
            .takeLast(6)
            .map { (it.assetsPaise - it.liabilitiesPaise).toDouble() }

        val finalSparkline = when {
            sparkline.isEmpty() -> listOf(nwSummary.netWorthPaise.toDouble(), nwSummary.netWorthPaise.toDouble())
            sparkline.size == 1 -> listOf(sparkline[0], sparkline[0])
            else -> sparkline
        }

        AccountsScreenUiState(
            activeAccounts = active,
            archivedAccounts = archived,
            totalNetWorth = nwSummary.netWorthPaise,
            totalAssets = nwSummary.totalAssetsPaise,
            totalLiabilities = nwSummary.totalLiabilitiesPaise,
            sparklineTrend = finalSparkline,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountsScreenUiState(isLoading = true)
    )

    private fun checkAndSaveCycleSnapshot(
        salaryCycleStartDay: Int,
        assetsPaise: Long,
        liabilitiesPaise: Long,
        existingSnapshots: List<NetWorthSnapshotEntity>
    ) {
        val today = LocalDate.now(ZoneId.of("Asia/Kolkata"))
        val cyclePeriod = SalaryCycleCalculator.calculateCycle(salaryCycleStartDay, today)
        val cycleStartStr = cyclePeriod.startDate.toString()

        val alreadySaved = existingSnapshots.any { it.cycleStart == cycleStartStr }
        if (!alreadySaved) {
            viewModelScope.launch {
                val snapshot = NetWorthSnapshotEntity(
                    cycleStart = cycleStartStr,
                    assetsPaise = assetsPaise,
                    liabilitiesPaise = liabilitiesPaise
                )
                netWorthRepository.saveSnapshot(snapshot)
            }
        }
    }

    class Factory(
        private val accountRepository: AccountRepository,
        private val transactionRepository: TransactionRepository,
        private val netWorthRepository: NetWorthRepository,
        private val personLedgerRepository: PersonLedgerRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AccountsViewModel(
                accountRepository,
                transactionRepository,
                netWorthRepository,
                personLedgerRepository,
                settingsRepository
            ) as T
        }
    }
}

data class AccountDetailScreenUiState(
    val account: Account? = null,
    val transactions: List<TxnEntity> = emptyList(),
    val isLoading: Boolean = true
)

class AccountDetailViewModel(
    private val accountId: String,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<AccountDetailScreenUiState> = combine(
        accountRepository.getAccountByIdFlow(accountId),
        transactionRepository.getTransactionsForAccount(accountId)
    ) { entity, txns ->
        if (entity == null) {
            AccountDetailScreenUiState(isLoading = false)
        } else {
            val cardDetails = if (entity.type == DbAccountType.CREDIT_CARD) accountRepository.getCardDetails(entity.id) else null
            val loanDetails = if (entity.type == DbAccountType.LOAN) accountRepository.getLoanDetails(entity.id) else null
            val fdDetails = if (entity.type == DbAccountType.FD) accountRepository.getFdDetails(entity.id) else null
            val valuations = accountRepository.getValuationsList(entity.id)

            val domainAccount = AccountDomainEngine.buildAccountModel(
                entity = entity,
                cardDetails = cardDetails,
                loanDetails = loanDetails,
                fdDetails = fdDetails,
                valuations = valuations,
                transactions = txns
            )

            AccountDetailScreenUiState(
                account = domainAccount,
                transactions = txns,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountDetailScreenUiState(isLoading = true)
    )

    fun reconcileBalance(newRealBalancePaise: Long) {
        viewModelScope.launch {
            val entity = accountRepository.getAccountById(accountId) ?: return@launch
            val cardDetails = if (entity.type == DbAccountType.CREDIT_CARD) accountRepository.getCardDetails(entity.id) else null
            val loanDetails = if (entity.type == DbAccountType.LOAN) accountRepository.getLoanDetails(entity.id) else null
            val fdDetails = if (entity.type == DbAccountType.FD) accountRepository.getFdDetails(entity.id) else null
            val valuations = accountRepository.getValuationsList(entity.id)
            val txns = transactionRepository.getTransactionById(accountId)?.let { listOf(it) } ?: emptyList()

            val currentAccount = AccountDomainEngine.buildAccountModel(
                entity = entity,
                cardDetails = cardDetails,
                loanDetails = loanDetails,
                fdDetails = fdDetails,
                valuations = valuations,
                transactions = txns
            )

            val currentCalcBalance = currentAccount.balanceInPaise
            val delta = newRealBalancePaise - currentCalcBalance

            if (delta != 0L) {
                val adjustmentTxn = TxnEntity(
                    id = UUID.randomUUID().toString(),
                    type = DbTxnType.ADJUSTMENT,
                    amountPaise = delta,
                    accountId = accountId,
                    payee = "Balance Reconciliation",
                    note = "Reconciliation adjustment",
                    occurredAt = System.currentTimeMillis(),
                    source = DbTxnSource.MANUAL
                )
                transactionRepository.saveTransaction(adjustmentTxn)
            }
        }
    }

    fun updateInvestmentValue(newValuationPaise: Long) {
        viewModelScope.launch {
            val valuation = ValuationEntity(
                id = UUID.randomUUID().toString(),
                accountId = accountId,
                date = KolkataDateTimeUtils.currentDateString(),
                valuePaise = newValuationPaise
            )
            accountRepository.saveValuation(valuation)
        }
    }

    fun toggleArchived() {
        viewModelScope.launch {
            val entity = accountRepository.getAccountById(accountId) ?: return@launch
            accountRepository.setAccountArchived(accountId, !entity.archived)
        }
    }

    class Factory(
        private val accountId: String,
        private val accountRepository: AccountRepository,
        private val transactionRepository: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AccountDetailViewModel(accountId, accountRepository, transactionRepository) as T
        }
    }
}

class AddAccountViewModel(
    private val accountId: String?,
    private val accountRepository: AccountRepository
) : ViewModel() {

    suspend fun getAccountToEdit(): AccountEntity? {
        return accountId?.let { accountRepository.getAccountById(it) }
    }

    suspend fun getCardDetailsToEdit(): CardDetailsEntity? {
        return accountId?.let { accountRepository.getCardDetails(it) }
    }

    suspend fun getLoanDetailsToEdit(): LoanDetailsEntity? {
        return accountId?.let { accountRepository.getLoanDetails(it) }
    }

    suspend fun getFdDetailsToEdit(): FdDetailsEntity? {
        return accountId?.let { accountRepository.getFdDetails(it) }
    }

    suspend fun saveAccount(
        accountEntity: AccountEntity,
        cardDetails: CardDetailsEntity? = null,
        loanDetails: LoanDetailsEntity? = null,
        fdDetails: FdDetailsEntity? = null,
        valuation: ValuationEntity? = null,
        isEditMode: Boolean = false
    ) {
        if (isEditMode) {
            accountRepository.updateAccount(accountEntity)
        } else {
            accountRepository.saveAccount(accountEntity)
        }
        cardDetails?.let { accountRepository.saveCardDetails(it) }
        loanDetails?.let { accountRepository.saveLoanDetails(it) }
        fdDetails?.let { accountRepository.saveFdDetails(it) }
        valuation?.let { accountRepository.saveValuation(it) }
    }

    class Factory(
        private val accountId: String?,
        private val accountRepository: AccountRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddAccountViewModel(accountId, accountRepository) as T
        }
    }
}
