package com.patil.coconut_pfa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "categories",
    indices = [Index("parentId")]
)
data class CategoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val parentId: String? = null,
    val kind: DbCategoryKind,
    val icon: String,
    val colour: Long,
    val isPreset: Boolean = false,
    val archived: Boolean = false,
    val sortOrder: Int = 0
)
