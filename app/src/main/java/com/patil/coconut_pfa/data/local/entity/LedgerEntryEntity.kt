package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "ledger_entries",
    indices = [
        Index("personId"),
        Index("txnId")
    ]
)
data class LedgerEntryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val personId: String,
    val amountPaise: Long,
    val kind: DbLedgerEntryKind,
    val date: String,
    val note: String? = null,
    val txnId: String? = null
)
