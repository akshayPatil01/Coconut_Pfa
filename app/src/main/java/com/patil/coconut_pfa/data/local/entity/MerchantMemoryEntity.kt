package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merchant_memories")
data class MerchantMemoryEntity(
    @PrimaryKey val merchantKey: String,
    val categoryId: String,
    val lastUsedAt: Long
)
