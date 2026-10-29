package com.patil.coconut_pfa.data.repository

import com.patil.coconut_pfa.data.local.dao.GoalContributionDao
import com.patil.coconut_pfa.data.local.dao.GoalDao
import com.patil.coconut_pfa.data.local.entity.GoalContributionEntity
import com.patil.coconut_pfa.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

class GoalRepository(
    private val goalDao: GoalDao,
    private val goalContributionDao: GoalContributionDao
) {
    fun getAllGoals(): Flow<List<GoalEntity>> = goalDao.getAllGoalsFlow()
    suspend fun getGoalById(id: String): GoalEntity? = goalDao.getGoalById(id)
    suspend fun saveGoal(goal: GoalEntity) = goalDao.insert(goal)
    suspend fun updateGoal(goal: GoalEntity) = goalDao.update(goal)
    suspend fun deleteGoal(goal: GoalEntity) = goalDao.delete(goal)

    fun getContributionsForGoal(goalId: String): Flow<List<GoalContributionEntity>> = goalContributionDao.getContributionsForGoalFlow(goalId)
    suspend fun saveContribution(contribution: GoalContributionEntity) = goalContributionDao.insert(contribution)
    suspend fun deleteContribution(contribution: GoalContributionEntity) = goalContributionDao.delete(contribution)
    suspend fun deleteContributionById(id: String) = goalContributionDao.deleteById(id)
    suspend fun deleteContributionsForGoal(goalId: String) = goalContributionDao.deleteByGoalId(goalId)
}
