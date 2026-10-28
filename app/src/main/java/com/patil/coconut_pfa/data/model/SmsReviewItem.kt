package com.patil.coconut_pfa.data.model

data class SmsReviewItem(
    val id: String,
    val sender: String,
    val text: String,
    val amountInPaise: Long,
    val accountName: String,
    val timestamp: Long,
    val detectedCategory: String? = null,
    val type: TransactionType = TransactionType.EXPENSE
)
