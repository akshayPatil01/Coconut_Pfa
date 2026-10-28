package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "loan_details",
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
data class LoanDetailsEntity(
    @PrimaryKey val accountId: String,
    val principalPaise: Long,
    val annualRateBps: Int,
    val tenureMonths: Int,
    val emiPaise: Long,
    val startDate: String,
    val emiDay: Int,
    val payFromAccountId: String? = null
)
