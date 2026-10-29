package com.patil.coconut_pfa.data.model

enum class TransactionType(val displayName: String) {
    EXPENSE("Expense"),
    INCOME("Income"),
    TRANSFER("Transfer"),
    REFUND("Refund"),
    ADJUSTMENT("Adjustment"),
    SETTLEMENT("Settlement")
}

data class SplitDetail(
    val category: String,
    val amountInPaise: Long,
    val note: String? = null
)

data class Transaction(
    val id: String,
    val amountInPaise: Long,
    val type: TransactionType,
    val category: String,
    val accountId: String,
    val targetAccountId: String? = null,
    val payee: String,
    val note: String? = null,
    val timestamp: Long,
    val isSplit: Boolean = false,
    val splits: List<SplitDetail> = emptyList(),
    val tags: List<String> = emptyList()
)
