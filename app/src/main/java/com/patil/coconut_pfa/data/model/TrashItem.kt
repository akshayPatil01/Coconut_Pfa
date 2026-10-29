package com.patil.coconut_pfa.data.model

data class TrashItem(
    val id: String,
    val title: String,
    val itemType: String, // "Transaction", "Account", "Bill", "Goal"
    val amountInPaise: Long? = null,
    val deletedDate: String,
    val daysLeft: Int = 30
)
