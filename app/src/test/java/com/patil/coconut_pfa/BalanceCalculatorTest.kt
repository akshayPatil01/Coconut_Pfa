package com.patil.coconut_pfa

import com.patil.coconut_pfa.data.local.entity.DbTxnSource
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.local.entity.ValuationEntity
import com.patil.coconut_pfa.domain.BalanceCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class BalanceCalculatorTest {

    private val zoneId = ZoneId.of("Asia/Kolkata")

    @Test
    fun testAssetBalanceWithInflowsOutflowsAndDeletedTxns() {
        val accId = "acc_bank"
        val opening = 100000L // ₹1,000

        val txns = listOf(
            TxnEntity(
                id = "t1",
                type = DbTxnType.INCOME,
                amountPaise = 50000L, // +₹500
                accountId = accId,
                occurredAt = System.currentTimeMillis(),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "t2",
                type = DbTxnType.EXPENSE,
                amountPaise = 20000L, // -₹200
                accountId = accId,
                occurredAt = System.currentTimeMillis(),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "t3",
                type = DbTxnType.TRANSFER,
                amountPaise = 10000L, // -₹100 transfer out
                accountId = accId,
                toAccountId = "acc_cash",
                occurredAt = System.currentTimeMillis(),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "t4",
                type = DbTxnType.TRANSFER,
                amountPaise = 30000L, // +₹300 transfer in
                accountId = "acc_other",
                toAccountId = accId,
                occurredAt = System.currentTimeMillis(),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "t5",
                type = DbTxnType.EXPENSE,
                amountPaise = 99900L, // Deleted expense, should be ignored!
                accountId = accId,
                occurredAt = System.currentTimeMillis(),
                source = DbTxnSource.MANUAL,
                deletedAt = System.currentTimeMillis()
            )
        )

        // Opening (100000) + Income (50000) - Expense (20000) - TransferOut (10000) + TransferIn (30000) = 150000
        val balance = BalanceCalculator.calculateAssetBalance(opening, accId, txns)
        assertEquals(150000L, balance)
    }

    @Test
    fun testCreditCardOutstandingAndAvailableLimit() {
        val ccId = "acc_cc"
        val openingOutstanding = 50000L // ₹500
        val limit = 500000L // ₹5,000

        val txns = listOf(
            TxnEntity(
                id = "c1",
                type = DbTxnType.EXPENSE,
                amountPaise = 100000L, // +₹1,000 expense
                accountId = ccId,
                occurredAt = System.currentTimeMillis(),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "c2",
                type = DbTxnType.REFUND,
                amountPaise = 20000L, // -₹200 refund
                accountId = ccId,
                occurredAt = System.currentTimeMillis(),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "c3",
                type = DbTxnType.TRANSFER,
                amountPaise = 80000L, // -₹800 bill payment (transfer in to CC)
                accountId = "acc_bank",
                toAccountId = ccId,
                occurredAt = System.currentTimeMillis(),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "c4",
                type = DbTxnType.INCOME,
                amountPaise = 10000L, // -₹100 cashback credited directly to card
                accountId = ccId,
                occurredAt = System.currentTimeMillis(),
                source = DbTxnSource.MANUAL
            )
        )

        // Opening (50000) + Expense (100000) - Refund (20000) - Payment (80000) - Cashback (10000) = 40000
        val ccResult = BalanceCalculator.calculateCreditCardBalance(
            openingOutstandingPaise = openingOutstanding,
            creditLimitPaise = limit,
            accountId = ccId,
            transactions = txns
        )

        assertEquals(40000L, ccResult.outstandingPaise)
        assertEquals(460000L, ccResult.availableCreditPaise) // 500000 - 40000 = 460000
    }

    @Test
    fun testLoanOutstanding() {
        val principal = 100000000L // ₹10,00,000
        val principalPartsPaid = 25000000L // ₹2,50,000

        val outstanding = BalanceCalculator.calculateLoanOutstanding(principal, principalPartsPaid)
        assertEquals(75000000L, outstanding) // ₹7,50,000
    }

    @Test
    fun testInvestmentValueWithValuationAndPostValuationTransfers() {
        val invId = "acc_inv"
        val opening = 500000L // ₹5,000

        val valuations = listOf(
            ValuationEntity(
                id = "v1",
                accountId = invId,
                date = "2026-10-01",
                valuePaise = 600000L // Valuation on Oct 1: ₹6,000
            )
        )

        val oct1Epoch = LocalDate.of(2026, 10, 1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val oct5Epoch = LocalDate.of(2026, 10, 5).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val oct10Epoch = LocalDate.of(2026, 10, 10).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val txns = listOf(
            TxnEntity(
                id = "i1",
                type = DbTxnType.TRANSFER,
                amountPaise = 100000L, // Pre-valuation transfer: should be ignored in post-valuation calc!
                accountId = "acc_bank",
                toAccountId = invId,
                occurredAt = oct1Epoch,
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "i2",
                type = DbTxnType.TRANSFER,
                amountPaise = 200000L, // Post-valuation deposit (+₹2,000)
                accountId = "acc_bank",
                toAccountId = invId,
                occurredAt = oct5Epoch,
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "i3",
                type = DbTxnType.TRANSFER,
                amountPaise = 50000L, // Post-valuation withdrawal (-₹500)
                accountId = invId,
                toAccountId = "acc_bank",
                occurredAt = oct10Epoch,
                source = DbTxnSource.MANUAL
            )
        )

        // Latest valuation (600000) + Post-valuation deposit (200000) - Post-valuation withdrawal (50000) = 750000
        val invValue = BalanceCalculator.calculateInvestmentOrFdValue(
            openingBalancePaise = opening,
            accountId = invId,
            valuations = valuations,
            transactions = txns,
            zoneId = zoneId
        )

        assertEquals(750000L, invValue)
    }
}
