package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: DbAccountType,
    val institution: DbAccountInstitution,
    val last4: String? = null,
    val openingBalancePaise: Long,
    val openingDate: String,
    val icon: String,
    val colour: Long,
    val includeInNetWorth: Boolean = true,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
