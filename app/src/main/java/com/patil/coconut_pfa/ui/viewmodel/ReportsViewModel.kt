package com.patil.coconut_pfa.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.patil.coconut_pfa.data.local.entity.CategoryEntity
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.model.CategorySpendItem
import com.patil.coconut_pfa.data.model.CycleSpendItem
import com.patil.coconut_pfa.data.model.DailySpendHeatmapItem
import com.patil.coconut_pfa.data.repository.CategoryRepository
import com.patil.coconut_pfa.data.repository.SettingsRepository
import com.patil.coconut_pfa.data.repository.TransactionRepository
import com.patil.coconut_pfa.domain.CycleSummaryCalculator
import com.patil.coconut_pfa.domain.SalaryCycleCalculator
import com.patil.coconut_pfa.domain.SalaryCyclePeriod
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class SalaryCycleOption(
    val period: SalaryCyclePeriod,
    val label: String
)

data class ReportsUiState(
    val isLoading: Boolean = true,
    val availableCycles: List<SalaryCycleOption> = emptyList(),
    val selectedCycle: SalaryCycleOption? = null,
    val categorySpendItems: List<CategorySpendItem> = emptyList(),
    val cycleSpendHistory6: List<CycleSpendItem> = emptyList(),
    val cycleSpendHistory12: List<CycleSpendItem> = emptyList(),
    val showTwelveCycles: Boolean = false,
    val dailyHeatmapItems: List<DailySpendHeatmapItem> = emptyList(),
    val selectedHeatmapDay: DailySpendHeatmapItem? = null
)

class ReportsViewModel(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedCycleOption = MutableStateFlow<SalaryCycleOption?>(null)
    private val _showTwelveCycles = MutableStateFlow(false)
    private val _selectedHeatmapDay = MutableStateFlow<DailySpendHeatmapItem?>(null)

    private val baseFlow = combine(
        transactionRepository.getAllActiveTransactions(),
        categoryRepository.getAllCategories(),
        settingsRepository.settingsFlow
    ) { txns, categories, settings ->
        Triple(txns, categories, settings)
    }

    private val userControlsFlow = combine(
        _selectedCycleOption,
        _showTwelveCycles,
        _selectedHeatmapDay
    ) { cycle, show12, heatDay ->
        Triple(cycle, show12, heatDay)
    }

    val uiState: StateFlow<ReportsUiState> = combine(
        baseFlow,
        userControlsFlow
    ) { (txns, categories, settings), (selectedCycleOverride, show12, heatDay) ->
        val startDay = settings.salaryCycleStartDay
        val today = KolkataDateTimeUtils.currentLocalDate()

        // Generate last 12 salary cycles
        val cycles = mutableListOf<SalaryCyclePeriod>()
        var tempDate = today
        for (i in 0 until 12) {
            val period = SalaryCycleCalculator.calculateCycle(startDay, tempDate)
            if (cycles.none { it.startDate == period.startDate }) {
                cycles.add(period)
            }
            tempDate = period.startDate.minusDays(1)
        }
        val sortedDesc = cycles.sortedByDescending { it.startDate }

        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
        val shortFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)

        val cycleOptions = sortedDesc.map { period ->
            val isCurrent = !today.isBefore(period.startDate) && !today.isAfter(period.endDate)
            val labelStr = "${period.startDate.format(shortFormatter)} – ${period.endDate.format(formatter)}${if (isCurrent) " (Current)" else ""}"
            SalaryCycleOption(period = period, label = labelStr)
        }

        val currentCycleOption = cycleOptions.find { !today.isBefore(it.period.startDate) && !today.isAfter(it.period.endDate) } ?: cycleOptions.firstOrNull()
        val activeCycle = selectedCycleOptionOverride(selectedCycleOverride, currentCycleOption, cycleOptions.firstOrNull(), startDay, today)

        // 1. Category Spend for selected cycle
        val categoryMap = categories.associateBy { it.id }
        fun getTopLevelId(catId: String?): String {
            if (catId == null) return "uncategorized"
            var cur = categoryMap[catId]
            var topId = catId
            while (cur != null && cur.parentId != null) {
                topId = cur.parentId!!
                cur = categoryMap[topId]
            }
            return topId
        }

        val zoneId = ZoneId.of("Asia/Kolkata")
        val startEpoch = activeCycle.period.startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endEpoch = activeCycle.period.endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        val categorySpendMap = mutableMapOf<String, Long>()
        for (txn in txns) {
            if (txn.deletedAt != null) continue
            if (txn.occurredAt !in startEpoch..endEpoch) continue

            val amount = txn.myShareAmountPaise ?: txn.amountPaise
            when (txn.type) {
                DbTxnType.EXPENSE -> {
                    val topId = getTopLevelId(txn.categoryId)
                    categorySpendMap[topId] = (categorySpendMap[topId] ?: 0L) + amount
                }
                DbTxnType.REFUND -> {
                    val topId = getTopLevelId(txn.categoryId)
                    categorySpendMap[topId] = (categorySpendMap[topId] ?: 0L) - amount
                }
                else -> {}
            }
        }

        val totalSpend = categorySpendMap.values.filter { it > 0 }.sumOf { it }

        val palette = listOf(
            Color(0xFF2E7D32),
            Color(0xFFFB8C00),
            Color(0xFF0288D1),
            Color(0xFF8E24AA),
            Color(0xFFD81B60),
            Color(0xFF795548),
            Color(0xFF00BCD4),
            Color(0xFF3F51B5)
        )

        val categorySpendItems = categorySpendMap.entries
            .filter { it.value > 0L }
            .sortedByDescending { it.value }
            .mapIndexed { index, entry ->
                val catEntity = categoryMap[entry.key]
                val name = catEntity?.name ?: "Other"
                val color = catEntity?.let { Color(it.colour) } ?: palette[index % palette.size]
                val percentage = if (totalSpend > 0) (entry.value.toFloat() / totalSpend.toFloat()) * 100f else 0f
                CategorySpendItem(
                    categoryName = name,
                    amountInPaise = entry.value,
                    percentage = percentage,
                    color = color
                )
            }

        // 2. Cycle History (6 and 12 cycles)
        val chronologicalCycles = cycleOptions.map { it.period }.sortedBy { it.startDate }
        val history12 = chronologicalCycles.map { period ->
            val totals = CycleSummaryCalculator.calculateCycleTotals(period.startDate, period.endDate, txns, zoneId)
            val lbl = period.startDate.format(DateTimeFormatter.ofPattern("MMM yy", Locale.ENGLISH))
            CycleSpendItem(cycleLabel = lbl, incomeInPaise = totals.receivedPaise, expenseInPaise = totals.spentPaise)
        }
        val history6 = if (history12.size >= 6) history12.takeLast(6) else history12

        // 3. Daily Heatmap for active cycle
        val dayFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
        val daysList = mutableListOf<LocalDate>()
        var d = activeCycle.period.startDate
        while (!d.isAfter(activeCycle.period.endDate)) {
            daysList.add(d)
            d = d.plusDays(1)
        }

        val dailySpends = daysList.map { date ->
            val dStart = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val dEnd = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
            var daySpend = 0L
            for (txn in txns) {
                if (txn.deletedAt != null) continue
                if (txn.occurredAt !in dStart..dEnd) continue
                val amt = txn.myShareAmountPaise ?: txn.amountPaise
                if (txn.type == DbTxnType.EXPENSE) daySpend += amt
                if (txn.type == DbTxnType.REFUND) daySpend -= amt
            }
            Pair(date, maxOf(0L, daySpend))
        }

        val maxDaily = dailySpends.maxOfOrNull { it.second } ?: 1L
        val heatmapItems = dailySpends.map { (date, spend) ->
            val intensity = if (maxDaily > 0L) (spend.toFloat() / maxDaily.toFloat()).coerceIn(0.1f, 1.0f) else 0.1f
            DailySpendHeatmapItem(
                dayOfMonth = date.dayOfMonth,
                dateLabel = date.format(dayFormatter),
                amountInPaise = spend,
                intensity = intensity
            )
        }

        ReportsUiState(
            isLoading = false,
            availableCycles = cycleOptions,
            selectedCycle = activeCycle,
            categorySpendItems = categorySpendItems,
            cycleSpendHistory6 = history6,
            cycleSpendHistory12 = history12,
            showTwelveCycles = show12,
            dailyHeatmapItems = heatmapItems,
            selectedHeatmapDay = heatDay
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState(isLoading = true)
    )

    private fun selectedCycleOptionOverride(
        override: SalaryCycleOption?,
        current: SalaryCycleOption?,
        fallback: SalaryCycleOption?,
        startDay: Int,
        today: LocalDate
    ): SalaryCycleOption {
        if (override != null) return override
        if (current != null) return current
        return fallback ?: SalaryCycleOption(SalaryCycleCalculator.calculateCycle(startDay, today), "Current Cycle")
    }

    fun selectCycle(cycleOption: SalaryCycleOption) {
        _selectedCycleOption.value = cycleOption
        _selectedHeatmapDay.value = null
    }

    fun toggle12Cycles(show: Boolean) {
        _showTwelveCycles.value = show
    }

    fun selectHeatmapDay(day: DailySpendHeatmapItem?) {
        _selectedHeatmapDay.value = day
    }

    class Factory(
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportsViewModel(transactionRepository, categoryRepository, settingsRepository) as T
        }
    }
}
