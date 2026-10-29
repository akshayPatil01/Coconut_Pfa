package com.patil.coconut_pfa

import com.patil.coconut_pfa.data.local.entity.DbTxnSource
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.domain.CycleSummaryCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CycleSummaryCalculatorTest {

    private val zoneId = ZoneId.of("Asia/Kolkata")
    private val startDate = LocalDate.of(2026, 10, 1)
    private val endDate = LocalDate.of(2026, 10, 31)

    private fun dateToEpoch(year: Int, month: Int, day: Int): Long {
        return LocalDate.of(year, month, day).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    @Test
    fun testCycleTotalsWithIncomeExpenseRefundAndMyShareAmount() {
        val txns = listOf(
            TxnEntity(
                id = "t1",
                type = DbTxnType.INCOME,
                amountPaise = 10000000L, // ₹1,00,000 salary
                accountId = "acc_bank",
                occurredAt = dateToEpoch(2026, 10, 5),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "t2",
                type = DbTxnType.EXPENSE,
                amountPaise = 500000L, // ₹5,000 full bill
                myShareAmountPaise = 200000L, // my share = ₹2,000
                categoryId = "cat_food",
                accountId = "acc_bank",
                occurredAt = dateToEpoch(2026, 10, 10),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "t3",
                type = DbTxnType.REFUND,
                amountPaise = 50000L, // ₹500 refund
                categoryId = "cat_food",
                accountId = "acc_bank",
                occurredAt = dateToEpoch(2026, 10, 15),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "t4",
                type = DbTxnType.TRANSFER, // CC Bill Payment!
                amountPaise = 3000000L, // ₹30,000 transfer from bank to CC
                accountId = "acc_bank",
                toAccountId = "acc_cc",
                occurredAt = dateToEpoch(2026, 10, 20),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "t5",
                type = DbTxnType.ADJUSTMENT, // Adjustment: excluded!
                amountPaise = 10000L,
                accountId = "acc_bank",
                occurredAt = dateToEpoch(2026, 10, 22),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "t6",
                type = DbTxnType.SETTLEMENT, // Settlement: excluded!
                amountPaise = 50000L,
                accountId = "acc_bank",
                occurredAt = dateToEpoch(2026, 10, 25),
                source = DbTxnSource.MANUAL
            )
        )

        val totals = CycleSummaryCalculator.calculateCycleTotals(startDate, endDate, txns, zoneId)

        // Received = Income (10,000,000) = 10000000
        assertEquals(10000000L, totals.receivedPaise)

        // Spent = Expense (myShare = 200000) - Refund (50000) = 150000 (CC bill payment and adjustments excluded!)
        assertEquals(150000L, totals.spentPaise)
    }

    @Test
    fun testCcBillPaymentNotCountedAsSpending() {
        val txns = listOf(
            TxnEntity(
                id = "cc_pay",
                type = DbTxnType.TRANSFER,
                amountPaise = 5000000L, // ₹50,000 CC payment
                accountId = "acc_bank",
                toAccountId = "acc_cc",
                occurredAt = dateToEpoch(2026, 10, 15),
                source = DbTxnSource.MANUAL
            )
        )

        val totals = CycleSummaryCalculator.calculateCycleTotals(startDate, endDate, txns, zoneId)
        assertEquals(0L, totals.spentPaise)
        assertEquals(0L, totals.receivedPaise)
    }

    @Test
    fun testRefundReducesCategorySpending() {
        val categoryId = "cat_shopping"
        val txns = listOf(
            TxnEntity(
                id = "s1",
                type = DbTxnType.EXPENSE,
                amountPaise = 100000L, // ₹1,000 expense
                categoryId = categoryId,
                accountId = "acc_bank",
                occurredAt = dateToEpoch(2026, 10, 10),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "s2",
                type = DbTxnType.REFUND,
                amountPaise = 20000L, // ₹200 refund
                categoryId = categoryId,
                accountId = "acc_bank",
                occurredAt = dateToEpoch(2026, 10, 12),
                source = DbTxnSource.MANUAL
            )
        )

        val netCategorySpent = CycleSummaryCalculator.calculateCategorySpent(
            categoryId = categoryId,
            startDate = startDate,
            endDate = endDate,
            transactions = txns,
            zoneId = zoneId
        )

        // ₹1,000 expense - ₹200 refund = ₹800 (80000 paise)
        assertEquals(80000L, netCategorySpent)
    }
}
