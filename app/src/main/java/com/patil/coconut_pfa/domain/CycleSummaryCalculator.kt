package com.patil.coconut_pfa.domain

import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import java.time.LocalDate
import java.time.ZoneId

data class CycleTotals(
    val receivedPaise: Long,
    val spentPaise: Long
)

object CycleSummaryCalculator {

    /**
     * Calculates cycle totals:
     * Received = sum of INCOME in the cycle.
     * Spent = sum of EXPENSE (using myShareAmountPaise when set) - sum of REFUND.
     * Transfers, Adjustments, and Settlements are excluded from both.
     */
    fun calculateCycleTotals(
        startDate: LocalDate,
        endDate: LocalDate,
        transactions: List<TxnEntity>,
        zoneId: ZoneId = ZoneId.of("Asia/Kolkata")
    ): CycleTotals {
        var received = 0L
        var expenses = 0L
        var refunds = 0L

        val startEpoch = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endEpoch = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        for (txn in transactions) {
            if (txn.deletedAt != null) continue
            if (txn.occurredAt !in startEpoch..endEpoch) continue

            when (txn.type) {
                DbTxnType.INCOME -> {
                    received += txn.amountPaise
                }
                DbTxnType.EXPENSE -> {
                    val amount = txn.myShareAmountPaise ?: txn.amountPaise
                    expenses += amount
                }
                DbTxnType.REFUND -> {
                    val amount = txn.myShareAmountPaise ?: txn.amountPaise
                    refunds += amount
                }
                DbTxnType.TRANSFER,
                DbTxnType.ADJUSTMENT,
                DbTxnType.SETTLEMENT -> {
                    // Excluded from both Received and Spent
                }
            }
        }

        val spent = expenses - refunds
        return CycleTotals(receivedPaise = received, spentPaise = spent)
    }

    /**
     * Calculates net category spending:
     * sum of EXPENSE (using myShareAmountPaise when set) - sum of REFUND for the given category in cycle.
     */
    fun calculateCategorySpent(
        categoryId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        transactions: List<TxnEntity>,
        zoneId: ZoneId = ZoneId.of("Asia/Kolkata")
    ): Long {
        var expenses = 0L
        var refunds = 0L

        val startEpoch = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endEpoch = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        for (txn in transactions) {
            if (txn.deletedAt != null) continue
            if (txn.categoryId != categoryId) continue
            if (txn.occurredAt !in startEpoch..endEpoch) continue

            when (txn.type) {
                DbTxnType.EXPENSE -> {
                    val amount = txn.myShareAmountPaise ?: txn.amountPaise
                    expenses += amount
                }
                DbTxnType.REFUND -> {
                    val amount = txn.myShareAmountPaise ?: txn.amountPaise
                    refunds += amount
                }
                else -> {}
            }
        }

        return expenses - refunds
    }
}
