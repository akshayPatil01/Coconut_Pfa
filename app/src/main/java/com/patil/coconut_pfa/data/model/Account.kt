package com.patil.coconut_pfa.data.model

import kotlin.math.abs

enum class AccountType(val displayName: String) {
    BANK("Bank"),
    CASH("Cash & Wallets"),
    WALLET("Wallet"),
    CREDIT_CARD("Credit Card"),
    LOAN("Loan"),
    INVESTMENT("Investment"),
    FIXED_DEPOSIT("Fixed Deposit")
}

enum class CardPaymentStatus(val displayName: String) {
    UNPAID("Unpaid"),
    PARTIAL("Partial"),
    PAID("Paid"),
    OVERDUE("Overdue")
}

data class EmiScheduleItem(
    val installmentNo: Int,
    val dueDate: String,
    val emiAmountInPaise: Long,
    val principalInPaise: Long,
    val interestInPaise: Long,
    val isPaid: Boolean
)

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val balanceInPaise: Long,
    val unbilledAmountInPaise: Long = 0L,
    val dueAmountInPaise: Long = 0L,
    val dueDate: String? = null,
    val accountNumberLast4: String? = null,
    val bankName: String? = null,
    // Credit card specific fields
    val creditLimitInPaise: Long = 0L,
    val minimumDueInPaise: Long = 0L,
    val statementDay: Int? = null,
    val cardStatus: CardPaymentStatus = CardPaymentStatus.UNPAID,
    // Loan specific fields
    val emiAmountInPaise: Long = 0L,
    val interestRate: Double = 0.0,
    val monthsLeft: Int = 0,
    val emiSchedule: List<EmiScheduleItem> = emptyList(),
    // Fixed Deposit specific fields
    val principalInPaise: Long = 0L,
    val fdInterestRate: Double = 0.0,
    val fdStartDate: String? = null,
    val maturityDate: String? = null,
    val maturityAmountInPaise: Long = 0L,
    // Investment specific fields
    val currentValueInPaise: Long = balanceInPaise,
    // Net worth flag
    val includeInNetWorth: Boolean = true,
    val archived: Boolean = false
) {
    val availableCreditInPaise: Long
        get() = (creditLimitInPaise - abs(balanceInPaise)).coerceAtLeast(0L)

    val loanOutstandingInPaise: Long
        get() = abs(balanceInPaise)
}
