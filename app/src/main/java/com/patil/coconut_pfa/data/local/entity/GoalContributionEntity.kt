package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "goal_contributions",
    indices = [Index("goalId")]
)
data class GoalContributionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val amountPaise: Long,
    val date: String,
    val note: String? = null
)
