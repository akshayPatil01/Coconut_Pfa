package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "transactions",
    indices = [
        Index("accountId"),
        Index("toAccountId"),
        Index("categoryId"),
        Index("personId"),
        Index("billOccurrenceId"),
        Index("smsItemId"),
        Index("occurredAt")
    ]
)
data class TxnEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: DbTxnType,
    val amountPaise: Long,
    val myShareAmountPaise: Long? = null,
    val accountId: String? = null,
    val toAccountId: String? = null,
    val categoryId: String? = null,
    val payee: String? = null,
    val note: String? = null,
    val occurredAt: Long,
    val source: DbTxnSource,
    val billOccurrenceId: String? = null,
    val refundOfTxnId: String? = null,
    val personId: String? = null,
    val smsItemId: String? = null,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
