package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: String): GoalEntity?

    @Query("SELECT * FROM goals")
    suspend fun getAll(): List<GoalEntity>

    @Query("SELECT * FROM goals ORDER BY name ASC")
    fun getAllGoalsFlow(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity)

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)
}
