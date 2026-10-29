package com.patil.coconut_pfa

import com.patil.coconut_pfa.data.local.entity.DbTxnSource
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.domain.CardCycleCalculator
import com.patil.coconut_pfa.domain.CardStatementStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CardCycleCalculatorTest {

    private val zoneId = ZoneId.of("Asia/Kolkata")
    private val ccId = "acc_cc_hdfc"

    private fun dateToEpoch(year: Int, month: Int, day: Int): Long {
        return LocalDate.of(year, month, day).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    @Test
    fun testUnbilledAmountExpensesMinusRefundsAfterStatementDate() {
        val lastStatementDate = LocalDate.of(2026, 9, 25)

        val txns = listOf(
            TxnEntity(
                id = "c1",
                type = DbTxnType.EXPENSE,
                amountPaise = 50000L, // Before statement date (Sep 20): ignored!
                accountId = ccId,
                occurredAt = dateToEpoch(2026, 9, 20),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "c2",
                type = DbTxnType.EXPENSE,
                amountPaise = 150000L, // After statement date (Oct 2): ₹1,500
                accountId = ccId,
                occurredAt = dateToEpoch(2026, 10, 2),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "c3",
                type = DbTxnType.REFUND,
                amountPaise = 30000L, // After statement date (Oct 5): -₹300 refund
                accountId = ccId,
                occurredAt = dateToEpoch(2026, 10, 5),
                source = DbTxnSource.MANUAL
            )
        )

        val unbilled = CardCycleCalculator.calculateUnbilledAmount(
            accountId = ccId,
            lastStatementDate = lastStatementDate,
            transactions = txns,
            zoneId = zoneId
        )

        // ₹1,500 - ₹300 = ₹1,200 (120000 paise)
        assertEquals(120000L, unbilled)
    }

    @Test
    fun testStatementStatusPaid() {
        val status = CardCycleCalculator.calculateStatementStatus(
            statementAmountPaise = 1000000L, // ₹10,000
            dueDate = LocalDate.of(2026, 10, 15),
            paymentsSinceStatementPaise = 1000000L, // Fully paid
            currentDate = LocalDate.of(2026, 10, 20)
        )
        assertEquals(CardStatementStatus.PAID, status)
    }

    @Test
    fun testStatementStatusPartialBeforeDueDate() {
        val status = CardCycleCalculator.calculateStatementStatus(
            statementAmountPaise = 1000000L, // ₹10,000
            dueDate = LocalDate.of(2026, 10, 15),
            paymentsSinceStatementPaise = 400000L, // Partially paid ₹4,000
            currentDate = LocalDate.of(2026, 10, 10)
        )
        assertEquals(CardStatementStatus.PARTIAL, status)
    }

    @Test
    fun testStatementStatusOverdue() {
        val status = CardCycleCalculator.calculateStatementStatus(
            statementAmountPaise = 1000000L, // ₹10,000
            dueDate = LocalDate.of(2026, 10, 15),
            paymentsSinceStatementPaise = 400000L, // Partial payment but current date is past due date!
            currentDate = LocalDate.of(2026, 10, 16)
        )
        assertEquals(CardStatementStatus.OVERDUE, status)
    }

    @Test
    fun testStatementStatusUnpaidBeforeDueDate() {
        val status = CardCycleCalculator.calculateStatementStatus(
            statementAmountPaise = 1000000L, // ₹10,000
            dueDate = LocalDate.of(2026, 10, 15),
            paymentsSinceStatementPaise = 0L,
            currentDate = LocalDate.of(2026, 10, 10)
        )
        assertEquals(CardStatementStatus.UNPAID, status)
    }
}
