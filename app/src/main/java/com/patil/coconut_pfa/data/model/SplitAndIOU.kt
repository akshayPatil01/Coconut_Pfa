package com.patil.coconut_pfa.data.model

data class LedgerEntry(
    val id: String,
    val title: String,
    val amountInPaise: Long,
    val date: String,
    val paidByMe: Boolean,
    val myShareInPaise: Long,
    val personShareInPaise: Long,
    val note: String? = null
)

data class PersonIOU(
    val id: String,
    val name: String,
    val netBalanceInPaise: Long, // Positive = owes you (green), Negative = you owe (red)
    val phone: String? = null,
    val avatarColorHex: Long = 0xFF4CAF50,
    val ledger: List<LedgerEntry> = emptyList()
) {
    val owesYou: Boolean
        get() = netBalanceInPaise > 0

    val youOwe: Boolean
        get() = netBalanceInPaise < 0
}
