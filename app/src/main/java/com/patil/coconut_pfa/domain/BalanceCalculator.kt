package com.patil.coconut_pfa.domain

import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.local.entity.ValuationEntity
import java.time.Instant
import java.time.ZoneId

data class CreditCardBalance(
    val outstandingPaise: Long,
    val availableCreditPaise: Long
)

object BalanceCalculator {

    /**
     * Calculates Asset account balance (Bank, Cash, Wallet).
     * asset balance = opening + inflows - outflows (ignore deletedAt != null)
     */
    fun calculateAssetBalance(
        openingBalancePaise: Long,
        accountId: String,
        transactions: List<TxnEntity>
    ): Long {
        var balance = openingBalancePaise
        for (txn in transactions) {
            if (txn.deletedAt != null) continue

            val amount = txn.amountPaise
            when (txn.type) {
                DbTxnType.INCOME -> {
                    if (txn.accountId == accountId) {
                        balance += amount
                    }
                }
                DbTxnType.EXPENSE -> {
                    if (txn.accountId == accountId) {
                        balance -= amount
                    }
                }
                DbTxnType.TRANSFER -> {
                    if (txn.accountId == accountId) {
                        balance -= amount // Outflow
                    }
                    if (txn.toAccountId == accountId) {
                        balance += amount // Inflow
                    }
                }
                DbTxnType.REFUND -> {
                    if (txn.accountId == accountId) {
                        balance += amount // Inflow
                    }
                }
                DbTxnType.ADJUSTMENT -> {
                    if (txn.accountId == accountId) {
                        balance += amount
                    }
                }
                DbTxnType.SETTLEMENT -> {
                    if (txn.accountId == accountId) {
                        balance -= amount
                    }
                    if (txn.toAccountId == accountId) {
                        balance += amount
                    }
                }
            }
        }
        return balance
    }

    /**
     * Calculates Credit Card outstanding balance and available credit.
     * Credit card outstanding = opening outstanding + expenses - refunds - payments(transfers in) - income credited.
     * Available = limit - outstanding.
     */
    fun calculateCreditCardBalance(
        openingOutstandingPaise: Long,
        creditLimitPaise: Long,
        accountId: String,
        transactions: List<TxnEntity>
    ): CreditCardBalance {
        var outstanding = openingOutstandingPaise
        for (txn in transactions) {
            if (txn.deletedAt != null) continue

            val amount = txn.amountPaise
            if (txn.accountId == accountId) {
                when (txn.type) {
                    DbTxnType.EXPENSE -> outstanding += amount
                    DbTxnType.REFUND -> outstanding -= amount
                    DbTxnType.INCOME -> outstanding -= amount
                    DbTxnType.TRANSFER -> outstanding += amount // Transfer out / Cash advance
                    DbTxnType.ADJUSTMENT -> outstanding += amount
                    DbTxnType.SETTLEMENT -> outstanding += amount
                }
            }
            if (txn.toAccountId == accountId) {
                when (txn.type) {
                    DbTxnType.TRANSFER -> outstanding -= amount // Bill payment (transfer into CC)
                    DbTxnType.SETTLEMENT -> outstanding -= amount
                    else -> {}
                }
            }
        }
        val available = (creditLimitPaise - outstanding).coerceAtLeast(0L)
        return CreditCardBalance(
            outstandingPaise = outstanding,
            availableCreditPaise = available
        )
    }

    /**
     * Calculates Loan outstanding amount.
     * Loan outstanding = principal - principal parts paid.
     */
    fun calculateLoanOutstanding(
        principalPaise: Long,
        principalPartsPaidPaise: Long
    ): Long {
        return (principalPaise - principalPartsPaidPaise).coerceAtLeast(0L)
    }

    /**
     * Calculates Investment or Fixed Deposit value.
     * Investment/FD value = latest Valuation + transfers in - transfers out after that valuation date.
     * If no valuation exists, uses opening balance + net transfers/inflows after opening.
     */
    fun calculateInvestmentOrFdValue(
        openingBalancePaise: Long,
        accountId: String,
        valuations: List<ValuationEntity>,
        transactions: List<TxnEntity>,
        zoneId: ZoneId = ZoneId.of("Asia/Kolkata")
    ): Long {
        val latestValuation = valuations.maxByOrNull { it.date }
        val baseValue: Long
        val valuationDate: String?

        if (latestValuation != null) {
            baseValue = latestValuation.valuePaise
            valuationDate = latestValuation.date
        } else {
            baseValue = openingBalancePaise
            valuationDate = null
        }

        var currentValue = baseValue
        for (txn in transactions) {
            if (txn.deletedAt != null) continue

            if (valuationDate != null) {
                val txnDateStr = Instant.ofEpochMilli(txn.occurredAt)
                    .atZone(zoneId)
                    .toLocalDate()
                    .toString()
                if (txnDateStr <= valuationDate) {
                    continue
                }
            }

            val amount = txn.amountPaise
            if (txn.toAccountId == accountId) {
                if (txn.type == DbTxnType.TRANSFER || txn.type == DbTxnType.INCOME) {
                    currentValue += amount
                }
            }
            if (txn.accountId == accountId) {
                if (txn.type == DbTxnType.TRANSFER || txn.type == DbTxnType.EXPENSE) {
                    currentValue -= amount
                }
            }
        }
        return currentValue
    }
}
