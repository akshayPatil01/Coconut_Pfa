package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val kind: DbBillKind,
    val amountPaise: Long,
    val isVariable: Boolean = false,
    val frequency: DbBillFrequency,
    val interval: Int = 1,
    val anchorDate: String,
    val payFromAccountId: String? = null,
    val categoryId: String? = null,
    val reminderDaysBefore: Int = 3,
    val active: Boolean = true
)
