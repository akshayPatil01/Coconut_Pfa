package com.patil.coconut_pfa.data.repository

import com.patil.coconut_pfa.data.local.dao.AccountDao
import com.patil.coconut_pfa.data.local.dao.BillDao
import com.patil.coconut_pfa.data.local.dao.BillOccurrenceDao
import com.patil.coconut_pfa.data.local.dao.CardDetailsDao
import com.patil.coconut_pfa.data.local.dao.CardStatementDao
import com.patil.coconut_pfa.data.local.dao.LoanDetailsDao
import com.patil.coconut_pfa.data.local.dao.TxnDao
import com.patil.coconut_pfa.data.local.entity.BillEntity
import com.patil.coconut_pfa.data.local.entity.BillOccurrenceEntity
import com.patil.coconut_pfa.data.local.entity.CardStatementEntity
import com.patil.coconut_pfa.data.local.entity.DbAccountType
import com.patil.coconut_pfa.data.local.entity.DbBillFrequency
import com.patil.coconut_pfa.data.local.entity.DbBillOccurrenceStatus
import com.patil.coconut_pfa.data.local.entity.DbCardStatementSource
import com.patil.coconut_pfa.data.local.entity.DbTxnSource
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.domain.BalanceCalculator
import com.patil.coconut_pfa.domain.EmiCalculator
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.util.UUID
import kotlin.math.abs

class BillRepository(
    private val billDao: BillDao,
    private val billOccurrenceDao: BillOccurrenceDao,
    private val cardStatementDao: CardStatementDao,
    private val accountDao: AccountDao,
    private val cardDetailsDao: CardDetailsDao,
    private val loanDetailsDao: LoanDetailsDao,
    private val txnDao: TxnDao
) {
    fun getActiveBills(): Flow<List<BillEntity>> = billDao.getActiveBillsFlow()
    fun getAllBills(): Flow<List<BillEntity>> = billDao.getAllBillsFlow()
    suspend fun getBillById(id: String): BillEntity? = billDao.getBillById(id)

    suspend fun saveBill(bill: BillEntity) {
        billDao.insert(bill)
        generateAllOccurrencesAndStatements()
    }

    suspend fun updateBill(bill: BillEntity) {
        billDao.update(bill)
        generateAllOccurrencesAndStatements()
    }

    suspend fun deactivateBill(billId: String) {
        val bill = billDao.getBillById(billId) ?: return
        billDao.update(bill.copy(active = false))
    }

    suspend fun deleteBill(bill: BillEntity) = billDao.delete(bill)

    fun getAllOccurrences(): Flow<List<BillOccurrenceEntity>> = billOccurrenceDao.getAllOccurrencesFlow()
    fun getOccurrencesForBill(billId: String): Flow<List<BillOccurrenceEntity>> = billOccurrenceDao.getOccurrencesForBillFlow(billId)
    fun getUpcomingOccurrences(): Flow<List<BillOccurrenceEntity>> = billOccurrenceDao.getUpcomingOccurrencesFlow()
    suspend fun getOccurrenceById(id: String): BillOccurrenceEntity? = billOccurrenceDao.getById(id)

    suspend fun saveOccurrence(occurrence: BillOccurrenceEntity) = billOccurrenceDao.insert(occurrence)
    suspend fun updateOccurrence(occurrence: BillOccurrenceEntity) = billOccurrenceDao.update(occurrence)

    fun getAllCardStatements(): Flow<List<CardStatementEntity>> = cardStatementDao.getAllStatementsFlow()
    fun getStatementsForAccount(accountId: String): Flow<List<CardStatementEntity>> = cardStatementDao.getStatementsForAccountFlow(accountId)
    suspend fun getCardStatementById(id: String): CardStatementEntity? = cardStatementDao.getById(id)

    suspend fun updateCardStatement(statementId: String, newAmountPaise: Long, newMinDuePaise: Long?) {
        val st = cardStatementDao.getById(statementId) ?: return
        val updated = st.copy(amountPaise = newAmountPaise, minDuePaise = newMinDuePaise)
        cardStatementDao.update(updated)

        val occ = billOccurrenceDao.getByStatementId(statementId)
        if (occ != null && occ.status == DbBillOccurrenceStatus.UPCOMING) {
            billOccurrenceDao.update(occ.copy(amountPaise = newAmountPaise))
        }
    }

    suspend fun skipOccurrence(occurrenceId: String) {
        billOccurrenceDao.updateStatus(occurrenceId, DbBillOccurrenceStatus.SKIPPED, null)
    }

    /**
     * Executes payment for an occurrence:
     * - Regular bill -> Expense transaction
     * - CC Statement due -> Transfer to credit card account
     * - EMI -> Two transactions: Transfer (principal) to loan + Expense (interest, "cat_exp_loan_interest")
     * Marks occurrence PAID with paidTxnId.
     */
    suspend fun payBillOccurrence(
        occurrenceId: String,
        payFromAccountId: String,
        amountPaise: Long? = null,
        occurredAtMillis: Long = System.currentTimeMillis()
    ): Boolean {
        val occurrence = billOccurrenceDao.getById(occurrenceId) ?: return false
        val payAmount = amountPaise ?: occurrence.amountPaise

        when {
            occurrence.billId != null -> {
                val bill = billDao.getBillById(occurrence.billId)
                val categoryId = bill?.categoryId ?: "cat_exp_bills_recharge"
                val payeeName = bill?.name ?: "Bill Payment"

                val txn = TxnEntity(
                    id = UUID.randomUUID().toString(),
                    type = DbTxnType.EXPENSE,
                    amountPaise = payAmount,
                    accountId = payFromAccountId,
                    categoryId = categoryId,
                    payee = payeeName,
                    note = "Bill payment: $payeeName",
                    occurredAt = occurredAtMillis,
                    source = DbTxnSource.BILL,
                    billOccurrenceId = occurrence.id
                )
                txnDao.insert(txn)
                billOccurrenceDao.updateStatus(occurrence.id, DbBillOccurrenceStatus.PAID, txn.id)
                return true
            }

            occurrence.statementId != null -> {
                val statement = cardStatementDao.getById(occurrence.statementId) ?: return false
                val cardAccount = accountDao.getAccountById(statement.accountId)
                val cardName = cardAccount?.name ?: "Credit Card"

                val txn = TxnEntity(
                    id = UUID.randomUUID().toString(),
                    type = DbTxnType.TRANSFER,
                    amountPaise = payAmount,
                    accountId = payFromAccountId,
                    toAccountId = statement.accountId,
                    payee = "Card Payment - $cardName",
                    note = "Credit card bill payment",
                    occurredAt = occurredAtMillis,
                    source = DbTxnSource.BILL,
                    billOccurrenceId = occurrence.id
                )
                txnDao.insert(txn)
                billOccurrenceDao.updateStatus(occurrence.id, DbBillOccurrenceStatus.PAID, txn.id)
                return true
            }

            occurrence.loanAccountId != null -> {
                val loanAccount = accountDao.getAccountById(occurrence.loanAccountId) ?: return false
                val loanDetails = loanDetailsDao.getByAccountId(occurrence.loanAccountId)
                val loanName = loanAccount.name

                val principalTotal = loanDetails?.principalPaise ?: 0L
                val rateBps = loanDetails?.annualRateBps ?: 0
                val emiPaise = loanDetails?.emiPaise ?: payAmount

                val allTxns = txnDao.getAllActiveTxnsFlow().firstOrNull() ?: emptyList()
                val paidPrincipalSoFar = allTxns.filter {
                    it.toAccountId == occurrence.loanAccountId && it.type == DbTxnType.TRANSFER && it.deletedAt == null
                }.sumOf { it.amountPaise }
                val currentOutstanding = (principalTotal - paidPrincipalSoFar).coerceAtLeast(0L)

                val split = EmiCalculator.calculateEmiSplit(
                    outstandingPaise = currentOutstanding,
                    annualRateBps = rateBps,
                    emiPaise = emiPaise
                )

                val principalTxn = TxnEntity(
                    id = UUID.randomUUID().toString(),
                    type = DbTxnType.TRANSFER,
                    amountPaise = split.principalPaise,
                    accountId = payFromAccountId,
                    toAccountId = occurrence.loanAccountId,
                    payee = "EMI Principal - $loanName",
                    note = "Loan EMI principal repayment",
                    occurredAt = occurredAtMillis,
                    source = DbTxnSource.BILL,
                    billOccurrenceId = occurrence.id
                )

                val interestTxn = TxnEntity(
                    id = UUID.randomUUID().toString(),
                    type = DbTxnType.EXPENSE,
                    amountPaise = split.interestPaise,
                    accountId = payFromAccountId,
                    categoryId = "cat_exp_loan_interest",
                    payee = "EMI Interest - $loanName",
                    note = "Loan EMI interest payment",
                    occurredAt = occurredAtMillis,
                    source = DbTxnSource.BILL,
                    billOccurrenceId = occurrence.id
                )

                txnDao.insert(principalTxn)
                txnDao.insert(interestTxn)

                billOccurrenceDao.updateStatus(occurrence.id, DbBillOccurrenceStatus.PAID, principalTxn.id)
                return true
            }
        }
        return false
    }

    /**
     * Generates card statements on statementDay and projects bill occurrences 60 days ahead.
     */
    suspend fun generateAllOccurrencesAndStatements() {
        generateCardStatements()
        generateBillOccurrences()
    }

    private suspend fun generateCardStatements() {
        val activeAccounts = accountDao.getActiveAccountsFlow().firstOrNull() ?: emptyList()
        val creditCardAccounts = activeAccounts.filter { it.type == DbAccountType.CREDIT_CARD }
        val today = KolkataDateTimeUtils.currentLocalDate()

        for (cardAccount in creditCardAccounts) {
            val details = cardDetailsDao.getByAccountId(cardAccount.id) ?: continue
            val statementDay = details.statementDay.coerceIn(1, 28)

            val currentMonthStatementDate = try {
                today.withDayOfMonth(minOf(statementDay, today.lengthOfMonth()))
            } catch (e: Exception) { today }

            val statementDates = listOf(
                currentMonthStatementDate,
                currentMonthStatementDate.minusMonths(1)
            )

            for (stDate in statementDates) {
                if (today >= stDate) {
                    val periodEndStr = stDate.format(KolkataDateTimeUtils.DATE_FORMATTER)
                    val existing = cardStatementDao.getStatementForPeriod(cardAccount.id, periodEndStr)
                    if (existing == null) {
                        val txns = txnDao.getAllActiveTxnsFlow().firstOrNull() ?: emptyList()
                        val cardBal = BalanceCalculator.calculateCreditCardBalance(
                            openingOutstandingPaise = abs(cardAccount.openingBalancePaise),
                            creditLimitPaise = details.creditLimitPaise,
                            accountId = cardAccount.id,
                            transactions = txns
                        )
                        val outstanding = cardBal.outstandingPaise
                        if (outstanding > 0) {
                            val periodStart = stDate.minusMonths(1).plusDays(1).format(KolkataDateTimeUtils.DATE_FORMATTER)
                            val dueDay = details.dueDay.coerceIn(1, 28)
                            val dueLocalDate = try {
                                stDate.plusMonths(1).withDayOfMonth(minOf(dueDay, stDate.plusMonths(1).lengthOfMonth()))
                            } catch (e: Exception) { stDate.plusDays(20) }
                            val dueDateStr = dueLocalDate.format(KolkataDateTimeUtils.DATE_FORMATTER)
                            val minDue = (outstanding * 5 / 100).coerceAtLeast(0L)

                            val newStatement = CardStatementEntity(
                                id = UUID.randomUUID().toString(),
                                accountId = cardAccount.id,
                                periodStart = periodStart,
                                periodEnd = periodEndStr,
                                amountPaise = outstanding,
                                minDuePaise = minDue,
                                dueDate = dueDateStr,
                                source = DbCardStatementSource.AUTO
                            )
                            cardStatementDao.insert(newStatement)

                            val occurrence = BillOccurrenceEntity(
                                id = UUID.randomUUID().toString(),
                                statementId = newStatement.id,
                                dueDate = dueDateStr,
                                amountPaise = outstanding,
                                status = DbBillOccurrenceStatus.UPCOMING
                            )
                            billOccurrenceDao.insert(occurrence)
                        }
                    }
                }
            }
        }
    }

    private suspend fun generateBillOccurrences() {
        val today = KolkataDateTimeUtils.currentLocalDate()
        val cutoff = today.plusDays(60)

        // 1. Regular active bills
        val activeBills = billDao.getActiveBills()
        for (bill in activeBills) {
            val anchorLocalDate = try {
                LocalDate.parse(bill.anchorDate, KolkataDateTimeUtils.DATE_FORMATTER)
            } catch (e: Exception) { today }

            val interval = bill.interval.coerceAtLeast(1)
            var nextDate = anchorLocalDate

            val minWindowDate = today.minusDays(30)
            while (nextDate < minWindowDate) {
                nextDate = advanceBillDate(nextDate, bill.frequency, interval)
            }

            while (nextDate <= cutoff) {
                val dueDateStr = nextDate.format(KolkataDateTimeUtils.DATE_FORMATTER)
                val existing = billOccurrenceDao.getByBillIdAndDueDate(bill.id, dueDateStr)
                if (existing == null) {
                    val occurrence = BillOccurrenceEntity(
                        id = UUID.randomUUID().toString(),
                        billId = bill.id,
                        dueDate = dueDateStr,
                        amountPaise = bill.amountPaise,
                        status = DbBillOccurrenceStatus.UPCOMING
                    )
                    billOccurrenceDao.insert(occurrence)
                }
                nextDate = advanceBillDate(nextDate, bill.frequency, interval)
            }
        }

        // 2. Loan EMI occurrences
        val activeAccounts = accountDao.getActiveAccountsFlow().firstOrNull() ?: emptyList()
        val loanAccounts = activeAccounts.filter { it.type == DbAccountType.LOAN }
        for (loanAcc in loanAccounts) {
            val loanDetails = loanDetailsDao.getByAccountId(loanAcc.id) ?: continue
            if (loanDetails.emiPaise <= 0L) continue

            val emiDay = loanDetails.emiDay.coerceIn(1, 28)

            var mDate = today.withDayOfMonth(minOf(emiDay, today.lengthOfMonth()))
            if (mDate < today.minusDays(30)) {
                mDate = mDate.plusMonths(1)
            }

            while (mDate <= cutoff) {
                val dueDateStr = mDate.format(KolkataDateTimeUtils.DATE_FORMATTER)
                val existing = billOccurrenceDao.getByLoanAccountAndDueDate(loanAcc.id, dueDateStr)
                if (existing == null) {
                    val occurrence = BillOccurrenceEntity(
                        id = UUID.randomUUID().toString(),
                        loanAccountId = loanAcc.id,
                        dueDate = dueDateStr,
                        amountPaise = loanDetails.emiPaise,
                        status = DbBillOccurrenceStatus.UPCOMING
                    )
                    billOccurrenceDao.insert(occurrence)
                }
                val nextMonth = mDate.plusMonths(1)
                mDate = nextMonth.withDayOfMonth(minOf(emiDay, nextMonth.lengthOfMonth()))
            }
        }

        // 3. Card statements
        val statements = cardStatementDao.getAllStatementsFlow().firstOrNull() ?: emptyList()
        for (st in statements) {
            val existing = billOccurrenceDao.getByStatementId(st.id)
            if (existing == null) {
                val occurrence = BillOccurrenceEntity(
                    id = UUID.randomUUID().toString(),
                    statementId = st.id,
                    dueDate = st.dueDate,
                    amountPaise = st.amountPaise,
                    status = DbBillOccurrenceStatus.UPCOMING
                )
                billOccurrenceDao.insert(occurrence)
            }
        }
    }

    private fun advanceBillDate(current: LocalDate, frequency: DbBillFrequency, interval: Int): LocalDate {
        return when (frequency) {
            DbBillFrequency.MONTHLY -> current.plusMonths(interval.toLong())
            DbBillFrequency.QUARTERLY -> current.plusMonths((3 * interval).toLong())
            DbBillFrequency.YEARLY -> current.plusYears(interval.toLong())
            DbBillFrequency.EVERY_N_DAYS -> current.plusDays(interval.toLong())
        }
    }
}
