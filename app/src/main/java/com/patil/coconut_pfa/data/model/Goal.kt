package com.patil.coconut_pfa.data.model

data class GoalContribution(
    val id: String,
    val amountInPaise: Long,
    val date: String,
    val sourceAccountName: String,
    val note: String? = null
)

data class Goal(
    val id: String,
    val title: String,
    val targetAmountInPaise: Long,
    val currentAmountInPaise: Long,
    val targetDate: String,
    val perCycleNeededInPaise: Long,
    val category: String = "Savings",
    val contributions: List<GoalContribution> = emptyList()
) {
    val progressFraction: Float
        get() = if (targetAmountInPaise > 0) {
            (currentAmountInPaise.toFloat() / targetAmountInPaise.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val remainingAmountInPaise: Long
        get() = (targetAmountInPaise - currentAmountInPaise).coerceAtLeast(0L)
}
