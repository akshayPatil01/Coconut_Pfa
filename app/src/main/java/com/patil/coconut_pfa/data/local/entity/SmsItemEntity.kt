package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sms_items",
    indices = [
        Index("matchedAccountId"),
        Index("suggestedCategoryId"),
        Index("txnId"),
        Index("receivedAt"),
        Index("status")
    ]
)
data class SmsItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val receivedAt: Long,
    val body: String? = null,
    val bodyHash: String,
    val parsedType: String? = null,
    val amountPaise: Long? = null,
    val last4: String? = null,
    val merchant: String? = null,
    val availableBalancePaise: Long? = null,
    val refNo: String? = null,
    val matchedAccountId: String? = null,
    val suggestedCategoryId: String? = null,
    val status: DbSmsStatus,
    val txnId: String? = null,
    val source: DbSmsSource,
    val resolvedAt: Long? = null
)
