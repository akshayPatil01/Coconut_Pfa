package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "card_details",
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
data class CardDetailsEntity(
    @PrimaryKey val accountId: String,
    val creditLimitPaise: Long,
    val statementDay: Int,
    val dueDay: Int
)
