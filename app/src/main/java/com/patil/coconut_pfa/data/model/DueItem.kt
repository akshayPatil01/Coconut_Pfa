package com.patil.coconut_pfa.data.model

enum class BillKind(val displayName: String) {
    RENT("Rent"),
    UTILITY("Utility"),
    INSURANCE("Insurance"),
    SUBSCRIPTION("Subscription"),
    EMI("EMI"),
    OTHER("Other")
}

enum class DueTimeframe(val displayName: String) {
    OVERDUE("Overdue"),
    THIS_WEEK("This week"),
    LATER("Later")
}

data class DueItem(
    val id: String,
    val title: String,
    val amountInPaise: Long,
    val dueDate: String,
    val isOverdue: Boolean,
    val kind: BillKind = BillKind.UTILITY,
    val timeframe: DueTimeframe = if (isOverdue) DueTimeframe.OVERDUE else DueTimeframe.THIS_WEEK,
    val category: String = "Bill",
    val accountName: String? = null,
    val isAmountVaries: Boolean = false,
    val frequency: String = "Monthly",
    val dueDay: Int = 1,
    val payFromAccountId: String? = null,
    val remindDaysBefore: Int = 3
)

data class PaymentHistoryItem(
    val id: String,
    val title: String,
    val amountInPaise: Long,
    val paymentDate: String,
    val kind: BillKind,
    val accountName: String,
    val status: String = "Paid"
)
