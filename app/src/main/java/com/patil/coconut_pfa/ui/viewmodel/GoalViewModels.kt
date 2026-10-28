package com.patil.coconut_pfa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.patil.coconut_pfa.data.local.entity.GoalContributionEntity
import com.patil.coconut_pfa.data.local.entity.GoalEntity
import com.patil.coconut_pfa.data.repository.GoalRepository
import com.patil.coconut_pfa.data.repository.SettingsRepository
import com.patil.coconut_pfa.domain.SalaryCycleCalculator
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

data class GoalWithDetails(
    val goal: GoalEntity,
    val contributions: List<GoalContributionEntity>,
    val currentAmountPaise: Long,
    val progressFraction: Float,
    val remainingPaise: Long,
    val daysLeft: Long,
    val cyclesLeft: Int,
    val perCycleNeededPaise: Long
)

data class GoalsUiState(
    val goals: List<GoalWithDetails> = emptyList(),
    val totalTargetPaise: Long = 0L,
    val totalCurrentPaise: Long = 0L,
    val isLoading: Boolean = true
)

class GoalsViewModel(
    private val goalRepository: GoalRepository,
    private val goalContributionDao: com.patil.coconut_pfa.data.local.dao.GoalContributionDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> = combine(
        goalRepository.getAllGoals(),
        goalContributionDao.getAllContributionsFlow(),
        settingsRepository.settingsFlow
    ) { goals, contributions, settings ->
        val today = KolkataDateTimeUtils.currentLocalDate()
        val salaryStartDay = settings.salaryCycleStartDay

        val goalDetailsList = goals.map { goal ->
            val goalContribs = contributions.filter { it.goalId == goal.id }
            val currentPaise = goalContribs.sumOf { it.amountPaise }
            val targetPaise = goal.targetPaise
            val progress = if (targetPaise > 0) (currentPaise.toFloat() / targetPaise.toFloat()).coerceIn(0f, 1f) else 0f
            val remaining = (targetPaise - currentPaise).coerceAtLeast(0L)

            val targetDate = try {
                LocalDate.parse(goal.targetDate, KolkataDateTimeUtils.DATE_FORMATTER)
            } catch (e: Exception) {
                today.plusMonths(6)
            }

            val days = ChronoUnit.DAYS.between(today, targetDate).coerceAtLeast(0L)

            // Calculate cycles left
            var cycles = 0
            var tempDate = today
            while (!tempDate.isAfter(targetDate) && cycles < 120) {
                val cycle = SalaryCycleCalculator.calculateCycle(salaryStartDay, tempDate)
                cycles++
                tempDate = cycle.endDate.plusDays(1)
            }
            val totalCycles = maxOf(1, cycles)
            val perCycleNeeded = if (totalCycles > 0) remaining / totalCycles else remaining

            GoalWithDetails(
                goal = goal,
                contributions = goalContribs,
                currentAmountPaise = currentPaise,
                progressFraction = progress,
                remainingPaise = remaining,
                daysLeft = days,
                cyclesLeft = totalCycles,
                perCycleNeededPaise = perCycleNeeded
            )
        }

        val totalTarget = goalDetailsList.sumOf { it.goal.targetPaise }
        val totalCurrent = goalDetailsList.sumOf { it.currentAmountPaise }

        GoalsUiState(
            goals = goalDetailsList,
            totalTargetPaise = totalTarget,
            totalCurrentPaise = totalCurrent,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalsUiState(isLoading = true)
    )

    fun saveGoal(
        id: String? = null,
        name: String,
        targetPaise: Long,
        targetDate: String,
        icon: String = "Savings"
    ) {
        viewModelScope.launch {
            val goal = GoalEntity(
                id = id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                targetPaise = targetPaise,
                targetDate = targetDate,
                icon = icon
            )
            if (id == null) {
                goalRepository.saveGoal(goal)
            } else {
                goalRepository.updateGoal(goal)
            }
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goal)
        }
    }

    fun addContribution(
        goalId: String,
        amountPaise: Long,
        note: String?
    ) {
        viewModelScope.launch {
            val contribution = GoalContributionEntity(
                id = UUID.randomUUID().toString(),
                goalId = goalId,
                amountPaise = amountPaise,
                date = KolkataDateTimeUtils.currentLocalDate().format(KolkataDateTimeUtils.DATE_FORMATTER),
                note = note?.trim()?.ifEmpty { null }
            )
            goalRepository.saveContribution(contribution)
        }
    }

    class Factory(
        private val goalRepository: GoalRepository,
        private val goalContributionDao: com.patil.coconut_pfa.data.local.dao.GoalContributionDao,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GoalsViewModel(goalRepository, goalContributionDao, settingsRepository) as T
        }
    }
}
