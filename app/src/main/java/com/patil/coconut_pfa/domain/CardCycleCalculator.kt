package com.patil.coconut_pfa.domain

import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import java.time.LocalDate
import java.time.ZoneId

enum class CardStatementStatus {
    PAID,
    PARTIAL,
    OVERDUE,
    UNPAID
}

object CardCycleCalculator {

    /**
     * Calculates unbilled amount on card:
     * unbilled = expenses - refunds on the card after the last statement date.
     */
    fun calculateUnbilledAmount(
        accountId: String,
        lastStatementDate: LocalDate?,
        transactions: List<TxnEntity>,
        zoneId: ZoneId = ZoneId.of("Asia/Kolkata")
    ): Long {
        val startEpoch = lastStatementDate?.plusDays(1)?.atStartOfDay(zoneId)?.toInstant()?.toEpochMilli() ?: 0L

        var expenses = 0L
        var refunds = 0L

        for (txn in transactions) {
            if (txn.deletedAt != null) continue
            if (txn.accountId != accountId) continue
            if (txn.occurredAt < startEpoch) continue

            when (txn.type) {
                DbTxnType.EXPENSE -> expenses += txn.amountPaise
                DbTxnType.REFUND -> refunds += txn.amountPaise
                else -> {}
            }
        }

        return (expenses - refunds).coerceAtLeast(0L)
    }

    /**
     * Calculates statement status:
     * PAID if payments since the statement >= amount
     * PARTIAL if > 0 (when not overdue)
     * OVERDUE if past due and not PAID
     * UNPAID otherwise
     */
    fun calculateStatementStatus(
        statementAmountPaise: Long,
        dueDate: LocalDate,
        paymentsSinceStatementPaise: Long,
        currentDate: LocalDate
    ): CardStatementStatus {
        return when {
            paymentsSinceStatementPaise >= statementAmountPaise -> CardStatementStatus.PAID
            currentDate > dueDate -> CardStatementStatus.OVERDUE
            paymentsSinceStatementPaise > 0 -> CardStatementStatus.PARTIAL
            else -> CardStatementStatus.UNPAID
        }
    }
}
