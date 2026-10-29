package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.GoalContributionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalContributionDao {
    @Query("SELECT * FROM goal_contributions WHERE goalId = :goalId ORDER BY date DESC")
    fun getContributionsForGoalFlow(goalId: String): Flow<List<GoalContributionEntity>>

    @Query("SELECT * FROM goal_contributions ORDER BY date DESC")
    fun getAllContributionsFlow(): Flow<List<GoalContributionEntity>>

    @Query("SELECT * FROM goal_contributions")
    suspend fun getAll(): List<GoalContributionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contribution: GoalContributionEntity)

    @Update
    suspend fun update(contribution: GoalContributionEntity)

    @Delete
    suspend fun delete(contribution: GoalContributionEntity)

    @Query("DELETE FROM goal_contributions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM goal_contributions WHERE goalId = :goalId")
    suspend fun deleteByGoalId(goalId: String)
}
