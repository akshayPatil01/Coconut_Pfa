package com.patil.coconut_pfa.data.model

import androidx.compose.ui.graphics.Color

data class CategorySpendItem(
    val categoryName: String,
    val amountInPaise: Long,
    val percentage: Float,
    val color: Color
)

data class CycleSpendItem(
    val cycleLabel: String,
    val incomeInPaise: Long,
    val expenseInPaise: Long
)

data class DailySpendHeatmapItem(
    val dayOfMonth: Int,
    val dateLabel: String,
    val amountInPaise: Long,
    val intensity: Float // 0.0f to 1.0f
)
