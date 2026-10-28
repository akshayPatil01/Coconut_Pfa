package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val note: String? = null
)
