package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "bill_occurrences",
    indices = [
        Index("billId"),
        Index("statementId"),
        Index("loanAccountId"),
        Index("paidTxnId")
    ]
)
data class BillOccurrenceEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val billId: String? = null,
    val statementId: String? = null,
    val loanAccountId: String? = null,
    val dueDate: String,
    val amountPaise: Long,
    val status: DbBillOccurrenceStatus,
    val paidTxnId: String? = null
)
