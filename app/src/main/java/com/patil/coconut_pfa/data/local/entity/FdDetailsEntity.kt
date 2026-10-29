package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fd_details",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("accountId")]
)
data class FdDetailsEntity(
    @PrimaryKey val accountId: String,
    val principalPaise: Long,
    val annualRateBps: Int,
    val startDate: String,
    val maturityDate: String,
    val maturityAmountPaise: Long? = null
)
