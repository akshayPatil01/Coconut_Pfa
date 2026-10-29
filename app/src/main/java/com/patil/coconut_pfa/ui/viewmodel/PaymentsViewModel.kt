package com.patil.coconut_pfa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.BillEntity
import com.patil.coconut_pfa.data.local.entity.BillOccurrenceEntity
import com.patil.coconut_pfa.data.local.entity.CardDetailsEntity
import com.patil.coconut_pfa.data.local.entity.CardStatementEntity
import com.patil.coconut_pfa.data.local.entity.CategoryEntity
import com.patil.coconut_pfa.data.local.entity.DbAccountType
import com.patil.coconut_pfa.data.local.entity.DbBillKind
import com.patil.coconut_pfa.data.local.entity.DbBillOccurrenceStatus
import com.patil.coconut_pfa.data.local.entity.DbLedgerEntryKind
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.model.BillKind
import com.patil.coconut_pfa.data.model.CardPaymentStatus
import com.patil.coconut_pfa.data.model.DueItem
import com.patil.coconut_pfa.data.model.DueTimeframe
import com.patil.coconut_pfa.data.model.PaymentHistoryItem
import com.patil.coconut_pfa.data.repository.AccountRepository
import com.patil.coconut_pfa.data.repository.BillRepository
import com.patil.coconut_pfa.data.repository.CategoryRepository
import com.patil.coconut_pfa.data.repository.PersonLedgerRepository
import com.patil.coconut_pfa.data.repository.TransactionRepository
import com.patil.coconut_pfa.domain.CardCycleCalculator
import com.patil.coconut_pfa.domain.CardStatementStatus
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CreditCardPaymentItem(
    val account: AccountEntity,
    val cardDetails: CardDetailsEntity?,
    val statement: CardStatementEntity?,
    val statementAmountPaise: Long,
    val minimumDuePaise: Long,
    val dueDate: String,
    val status: CardPaymentStatus,
    val unbilledAmountPaise: Long
)

data class PaymentsUiState(
    val dueIn7DaysInPaise: Long = 0L,
    val overdueInPaise: Long = 0L,
    val peopleOweYouInPaise: Long = 0L,
    val youOwePeopleInPaise: Long = 0L,
    val upcomingItems: List<DueItem> = emptyList(),
    val cardItems: List<CreditCardPaymentItem> = emptyList(),
    val historyItems: List<PaymentHistoryItem> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val bills: List<BillEntity> = emptyList(),
    val isLoading: Boolean = true
)

class PaymentsViewModel(
    private val billRepository: BillRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val personLedgerRepository: PersonLedgerRepository
) : ViewModel() {

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            billRepository.generateAllOccurrencesAndStatements()
        }
    }

    val uiState: StateFlow<PaymentsUiState> = combine(
        combine(
            billRepository.getAllOccurrences(),
            billRepository.getAllBills(),
            billRepository.getAllCardStatements()
        ) { occurrences, bills, statements ->
            Triple(occurrences, bills, statements)
        },
        combine(
            accountRepository.getAllAccounts(),
            categoryRepository.getAllCategories(),
            transactionRepository.getAllActiveTransactions(),
            personLedgerRepository.getAllLedgerEntries()
        ) { accounts, categories, txns, ledgerEntries ->
            Quadruple(accounts, categories, txns, ledgerEntries)
        }
    ) { (occurrences, bills, statements), (accounts, categories, txns, ledgerEntries) ->

        val accountMap = accounts.associateBy { it.id }
        val categoryMap = categories.associateBy { it.id }
        val billMap = bills.associateBy { it.id }
        val statementMap = statements.associateBy { it.id }

        val today = KolkataDateTimeUtils.currentLocalDate()
        val dueIn7DaysCutoff = today.plusDays(7)

        // 1. Process Upcoming Occurrences
        val upcomingOccurrences = occurrences.filter { it.status == DbBillOccurrenceStatus.UPCOMING }

        var due7DaysTotal = 0L
        var overdueTotal = 0L

        val upcomingItems = upcomingOccurrences.mapNotNull { occ ->
            val occDate = try {
                LocalDate.parse(occ.dueDate, KolkataDateTimeUtils.DATE_FORMATTER)
            } catch (e: Exception) { today }

            val isOverdue = occDate < today
            val timeframe = when {
                isOverdue -> DueTimeframe.OVERDUE
                occDate <= dueIn7DaysCutoff -> DueTimeframe.THIS_WEEK
                else -> DueTimeframe.LATER
            }

            if (isOverdue) {
                overdueTotal += occ.amountPaise
            } else if (occDate <= dueIn7DaysCutoff) {
                due7DaysTotal += occ.amountPaise
            }

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

        // 2. Process Cards Sub-Tab Data
        val creditCardAccounts = accounts.filter { it.type == DbAccountType.CREDIT_CARD && !it.archived }
        val cardItems = creditCardAccounts.map { cardAcc ->
            val accountStatements = statements.filter { it.accountId == cardAcc.id }.sortedByDescending { it.periodEnd }
            val latestStatement = accountStatements.firstOrNull()

            val unbilled = CardCycleCalculator.calculateUnbilledAmount(
                accountId = cardAcc.id,
                lastStatementDate = latestStatement?.periodEnd?.let {
                    try { LocalDate.parse(it, KolkataDateTimeUtils.DATE_FORMATTER) } catch (e: Exception) { null }
                },
                transactions = txns
            )

            val (stAmount, minDue, dueDateStr, status) = if (latestStatement != null) {
                val stDate = try { LocalDate.parse(latestStatement.dueDate, KolkataDateTimeUtils.DATE_FORMATTER) } catch (e: Exception) { today }
                val periodEndMillis = try { KolkataDateTimeUtils.dateStringToEpochMillis(latestStatement.periodStart) } catch (e: Exception) { 0L }

                val paymentsSince = txns.filter {
                    it.deletedAt == null && it.toAccountId == cardAcc.id && it.occurredAt >= periodEndMillis
                }.sumOf { it.amountPaise }

                val stStatus = CardCycleCalculator.calculateStatementStatus(
                    statementAmountPaise = latestStatement.amountPaise,
                    dueDate = stDate,
                    paymentsSinceStatementPaise = paymentsSince,
                    currentDate = today
                )
                val statusModel = mapCardStatementStatusToModel(stStatus)
                val formattedDueDate = try { stDate.format(DateTimeFormatter.ofPattern("d MMM yyyy")) } catch (e: Exception) { latestStatement.dueDate }

                Tuple4(latestStatement.amountPaise, latestStatement.minDuePaise ?: (latestStatement.amountPaise * 5 / 100), formattedDueDate, statusModel)
            } else {
                Tuple4(0L, 0L, "No Statement", CardPaymentStatus.PAID)
            }

            CreditCardPaymentItem(
                account = cardAcc,
                cardDetails = null, // Can be fetched if needed
                statement = latestStatement,
                statementAmountPaise = stAmount,
                minimumDuePaise = minDue,
                dueDate = dueDateStr,
                status = status,
                unbilledAmountPaise = unbilled
            )
        }

        // 3. Process History Tab Data
        val historyTxns = txns.filter { tx ->
            tx.billOccurrenceId != null || (tx.type == DbTxnType.TRANSFER && tx.toAccountId != null && (accountMap[tx.toAccountId]?.type == DbAccountType.CREDIT_CARD || accountMap[tx.toAccountId]?.type == DbAccountType.LOAN))
        }.sortedByDescending { it.occurredAt }

        val historyItems = historyTxns.map { tx ->
            val dateStr = try {
                KolkataDateTimeUtils.epochMillisToLocalDate(tx.occurredAt).format(DateTimeFormatter.ofPattern("d MMM yyyy"))
            } catch (e: Exception) { "Recent" }

            val title = tx.payee ?: tx.note ?: "Payment"
            val kind = when {
                tx.toAccountId != null && accountMap[tx.toAccountId]?.type == DbAccountType.LOAN -> BillKind.EMI
                tx.toAccountId != null && accountMap[tx.toAccountId]?.type == DbAccountType.CREDIT_CARD -> BillKind.OTHER
                else -> BillKind.UTILITY
            }
            val accName = accountMap[tx.accountId]?.name ?: "Account"

            PaymentHistoryItem(
                id = tx.id,
                title = title,
                amountInPaise = tx.amountPaise,
                paymentDate = dateStr,
                kind = kind,
                accountName = accName,
                status = "Paid"
            )
        }

        // 4. Process People Ledger Chip Totals
        var oweYouSum = 0L
        var youOweSum = 0L

        val personGrouped = ledgerEntries.groupBy { it.personId }
        for ((_, entries) in personGrouped) {
            var netBal = 0L
            for (entry in entries) {
                val amt = entry.amountPaise
                when (entry.kind) {
                    DbLedgerEntryKind.LENT -> netBal += amt
                    DbLedgerEntryKind.BORROWED -> netBal -= amt
                    DbLedgerEntryKind.SPLIT_SHARE -> netBal += amt
                    DbLedgerEntryKind.SETTLEMENT -> netBal -= amt
                }
            }
            if (netBal > 0) oweYouSum += netBal
            else if (netBal < 0) youOweSum += Math.abs(netBal)
        }

        PaymentsUiState(
            dueIn7DaysInPaise = due7DaysTotal,
            overdueInPaise = overdueTotal,
            peopleOweYouInPaise = oweYouSum,
            youOwePeopleInPaise = youOweSum,
            upcomingItems = upcomingItems,
            cardItems = cardItems,
            historyItems = historyItems,
            accounts = accounts,
            categories = categories,
            bills = bills,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PaymentsUiState(isLoading = true)
    )

    fun payOccurrence(occurrenceId: String, payFromAccountId: String, amountPaise: Long? = null) {
        viewModelScope.launch {
            billRepository.payBillOccurrence(occurrenceId, payFromAccountId, amountPaise)
        }
    }

    fun skipOccurrence(occurrenceId: String) {
        viewModelScope.launch {
            billRepository.skipOccurrence(occurrenceId)
        }
    }

    fun deactivateBill(billId: String) {
        viewModelScope.launch {
            billRepository.deactivateBill(billId)
        }
    }

    fun updateCardStatement(statementId: String, newAmountPaise: Long, newMinDuePaise: Long?) {
        viewModelScope.launch {
            billRepository.updateCardStatement(statementId, newAmountPaise, newMinDuePaise)
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

    private fun mapCardStatementStatusToModel(status: CardStatementStatus): CardPaymentStatus = when (status) {
        CardStatementStatus.PAID -> CardPaymentStatus.PAID
        CardStatementStatus.PARTIAL -> CardPaymentStatus.PARTIAL
        CardStatementStatus.OVERDUE -> CardPaymentStatus.OVERDUE
        CardStatementStatus.UNPAID -> CardPaymentStatus.UNPAID
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
    private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    class Factory(
        private val billRepository: BillRepository,
        private val accountRepository: AccountRepository,
        private val categoryRepository: CategoryRepository,
        private val transactionRepository: TransactionRepository,
        private val personLedgerRepository: PersonLedgerRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PaymentsViewModel(
                billRepository,
                accountRepository,
                categoryRepository,
                transactionRepository,
                personLedgerRepository
            ) as T
        }
    }
}
