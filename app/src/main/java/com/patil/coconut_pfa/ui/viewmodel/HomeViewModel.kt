package com.patil.coconut_pfa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.BillEntity
import com.patil.coconut_pfa.data.local.entity.BillOccurrenceEntity
import com.patil.coconut_pfa.data.local.entity.CardStatementEntity
import com.patil.coconut_pfa.data.local.entity.CategoryEntity
import com.patil.coconut_pfa.data.local.entity.DbAccountType
import com.patil.coconut_pfa.data.local.entity.DbBillKind
import com.patil.coconut_pfa.data.local.entity.DbBillOccurrenceStatus
import com.patil.coconut_pfa.data.local.entity.DbLedgerEntryKind
import com.patil.coconut_pfa.data.local.entity.DbSmsStatus
import com.patil.coconut_pfa.data.local.entity.LedgerEntryEntity
import com.patil.coconut_pfa.data.local.entity.NetWorthSnapshotEntity
import com.patil.coconut_pfa.data.local.entity.PersonEntity
import com.patil.coconut_pfa.data.local.entity.SmsItemEntity
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.model.Account
import com.patil.coconut_pfa.data.model.AccountType
import com.patil.coconut_pfa.data.model.BillKind
import com.patil.coconut_pfa.data.model.DueItem
import com.patil.coconut_pfa.data.model.DueTimeframe
import com.patil.coconut_pfa.data.repository.AccountRepository
import com.patil.coconut_pfa.data.repository.BillRepository
import com.patil.coconut_pfa.data.repository.CategoryRepository
import com.patil.coconut_pfa.data.repository.NetWorthRepository
import com.patil.coconut_pfa.data.repository.PersonLedgerRepository
import com.patil.coconut_pfa.data.repository.SettingsRepository
import com.patil.coconut_pfa.data.repository.SmsRepository
import com.patil.coconut_pfa.data.repository.TransactionRepository
import com.patil.coconut_pfa.domain.AccountDomainEngine
import com.patil.coconut_pfa.domain.AccountNetWorthItem
import com.patil.coconut_pfa.domain.CycleSummaryCalculator
import com.patil.coconut_pfa.domain.NetWorthCalculator
import com.patil.coconut_pfa.domain.PersonLedgerItem
import com.patil.coconut_pfa.domain.SalaryCycleCalculator
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val hasAccounts: Boolean = false,

    // Net worth
    val netWorthInPaise: Long = 0L,
    val sparklineData: List<Float> = emptyList(),

    // Pending SMS count for review inbox
    val pendingSmsCount: Int = 0,

    // Salary cycle totals
    val cycleStartDateFormatted: String = "",
    val cycleEndDateFormatted: String = "",
    val receivedInPaise: Long = 0L,
    val spentInPaise: Long = 0L,

    // Unbilled credit cards
    val unbilledCreditCards: List<Account> = emptyList(),

    // Due soon items (occurrences in next 7 days + all overdue)
    val dueSoonItems: List<DueItem> = emptyList()
)

class HomeViewModel(
    private val accountRepository: AccountRepository,
    transactionRepository: TransactionRepository,
    private val netWorthRepository: NetWorthRepository,
    private val personLedgerRepository: PersonLedgerRepository,
    private val billRepository: BillRepository,
    private val smsRepository: SmsRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                billRepository.generateAllOccurrencesAndStatements()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private val baseDataFlow = combine(
        accountRepository.getAllAccounts(),
        transactionRepository.getAllActiveTransactions(),
        netWorthRepository.getAllSnapshots(),
        personLedgerRepository.getAllPersons(),
        personLedgerRepository.getAllLedgerEntries()
    ) { accounts, txns, snapshots, persons, ledgerEntries ->
        HomeDataTuple(accounts, txns, snapshots, persons, ledgerEntries)
    }

    private val secondaryDataFlow = combine(
        billRepository.getAllOccurrences(),
        billRepository.getAllBills(),
        billRepository.getAllCardStatements(),
        smsRepository.getAllSms(),
        categoryRepository.getAllCategories()
    ) { occurrences, bills, statements, smsList, categories ->
        HomeSecondaryTuple(occurrences, bills, statements, smsList, categories)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        baseDataFlow,
        secondaryDataFlow,
        settingsRepository.settingsFlow,
        _isRefreshing
    ) { tuple1, tuple2, settings, isRefreshing ->
        val accounts = tuple1.accounts
        val txns = tuple1.txns
        val snapshots = tuple1.snapshots
        val persons = tuple1.persons
        val ledgerEntries = tuple1.ledgerEntries

        val occurrences = tuple2.occurrences
        val bills = tuple2.bills
        val statements = tuple2.statements
        val smsList = tuple2.smsList
        val categories = tuple2.categories

        // Map domain accounts
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

        val activeAccounts = domainAccounts.filter { !it.archived }
        val hasAccounts = activeAccounts.isNotEmpty()

        // 1. Net worth calculation
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

        val netWorthItems = activeAccounts.map { acc ->
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
            .map { (it.assetsPaise - it.liabilitiesPaise).toFloat() }

        val finalSparkline = when {
            sparkline.isEmpty() -> listOf(nwSummary.netWorthPaise.toFloat(), nwSummary.netWorthPaise.toFloat())
            sparkline.size == 1 -> listOf(sparkline[0], sparkline[0])
            else -> sparkline
        }

        // 2. Pending SMS count
        val pendingSmsCount = smsList.count { it.status == DbSmsStatus.PENDING }

        // 3. Current Cycle summary
        val today = KolkataDateTimeUtils.currentLocalDate()
        val cyclePeriod = SalaryCycleCalculator.calculateCycle(settings.salaryCycleStartDay, today)
        val cycleTotals = CycleSummaryCalculator.calculateCycleTotals(cyclePeriod.startDate, cyclePeriod.endDate, txns)

        val dayMonthFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
        val cycleStartFormatted = cyclePeriod.startDate.format(dayMonthFormatter)
        val cycleEndFormatted = cyclePeriod.endDate.format(dayMonthFormatter)

        // 4. Credit cards - unbilled
        val unbilledCards = activeAccounts.filter { it.type == AccountType.CREDIT_CARD }

        // 5. Due soon items (occurrences in next 7 days + all overdue)
        val cutoff = today.plusDays(7)
        val accountMap = accounts.associateBy { it.id }
        val categoryMap = categories.associateBy { it.id }
        val billMap = bills.associateBy { it.id }
        val statementMap = statements.associateBy { it.id }

        val upcomingOccurrences = occurrences.filter { it.status == DbBillOccurrenceStatus.UPCOMING }

        val dueSoonItems = upcomingOccurrences.mapNotNull { occ ->
            val occDate = try {
                LocalDate.parse(occ.dueDate, KolkataDateTimeUtils.DATE_FORMATTER)
            } catch (_: Exception) { today }

            if (occDate > cutoff) return@mapNotNull null // only next 7 days + all overdue

            val isOverdue = occDate < today
            val timeframe = if (isOverdue) DueTimeframe.OVERDUE else DueTimeframe.THIS_WEEK

            var title = "Bill Payment"
            var kind = BillKind.UTILITY
            var categoryName = "Bill"
            var payFromAccountName: String? = null
            var payFromAccountId: String? = null

            when {
                occ.billId != null -> {
                    val bill = billMap[occ.billId]
                    title = bill?.name ?: "Recurring Bill"
                    kind = mapDbBillKindToModel(bill?.kind ?: DbBillKind.UTILITY)
                    categoryName = categoryMap[bill?.categoryId]?.name ?: kind.displayName
                    payFromAccountId = bill?.payFromAccountId
                    payFromAccountName = accountMap[bill?.payFromAccountId]?.name
                }
                occ.statementId != null -> {
                    val st = statementMap[occ.statementId]
                    val cardAcc = accountMap[st?.accountId]
                    title = "${cardAcc?.name ?: "Credit Card"} Statement"
                    kind = BillKind.UTILITY
                    categoryName = "Credit Card Bill"
                }
                occ.loanAccountId != null -> {
                    val loanAcc = accountMap[occ.loanAccountId]
                    title = "${loanAcc?.name ?: "Loan"} EMI"
                    kind = BillKind.EMI
                    categoryName = "Loan EMI"
                }
            }

            val displayDueDate = try {
                occDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
            } catch (e: Exception) { occ.dueDate }

            DueItem(
                id = occ.id,
                title = title,
                amountInPaise = occ.amountPaise,
                dueDate = displayDueDate,
                isOverdue = isOverdue,
                kind = kind,
                timeframe = timeframe,
                category = categoryName,
                accountName = payFromAccountName,
                payFromAccountId = payFromAccountId,
                occurrenceId = occ.id,
                billId = occ.billId,
                statementId = occ.statementId,
                loanAccountId = occ.loanAccountId,
                canSkip = true
            )
        }.sortedWith(compareBy({ if (it.isOverdue) 0 else 1 }, { it.dueDate }))

        HomeUiState(
            isLoading = false,
            isRefreshing = isRefreshing,
            hasAccounts = hasAccounts,
            netWorthInPaise = nwSummary.netWorthPaise,
            sparklineData = finalSparkline,
            pendingSmsCount = pendingSmsCount,
            cycleStartDateFormatted = cycleStartFormatted,
            cycleEndDateFormatted = cycleEndFormatted,
            receivedInPaise = cycleTotals.receivedPaise,
            spentInPaise = cycleTotals.spentPaise,
            unbilledCreditCards = unbilledCards,
            dueSoonItems = dueSoonItems
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
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

    private fun mapDbBillKindToModel(kind: DbBillKind): BillKind = when (kind) {
        DbBillKind.RENT -> BillKind.RENT
        DbBillKind.UTILITY -> BillKind.UTILITY
        DbBillKind.INSURANCE -> BillKind.INSURANCE
        DbBillKind.SUBSCRIPTION -> BillKind.SUBSCRIPTION
        DbBillKind.EMI -> BillKind.EMI
        DbBillKind.OTHER -> BillKind.OTHER
    }

    private data class HomeDataTuple(
        val accounts: List<AccountEntity>,
        val txns: List<TxnEntity>,
        val snapshots: List<NetWorthSnapshotEntity>,
        val persons: List<PersonEntity>,
        val ledgerEntries: List<LedgerEntryEntity>
    )

    private data class HomeSecondaryTuple(
        val occurrences: List<BillOccurrenceEntity>,
        val bills: List<BillEntity>,
        val statements: List<CardStatementEntity>,
        val smsList: List<SmsItemEntity>,
        val categories: List<CategoryEntity>
    )

    class Factory(
        private val accountRepository: AccountRepository,
        private val transactionRepository: TransactionRepository,
        private val netWorthRepository: NetWorthRepository,
        private val personLedgerRepository: PersonLedgerRepository,
        private val billRepository: BillRepository,
        private val smsRepository: SmsRepository,
        private val categoryRepository: CategoryRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(
                accountRepository,
                transactionRepository,
                netWorthRepository,
                personLedgerRepository,
                billRepository,
                smsRepository,
                categoryRepository,
                settingsRepository
            ) as T
        }
    }
}
