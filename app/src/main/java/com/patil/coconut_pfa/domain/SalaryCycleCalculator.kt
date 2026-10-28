package com.patil.coconut_pfa.domain

import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.min

data class SalaryCyclePeriod(
    val startDate: LocalDate,
    val endDate: LocalDate
)

object SalaryCycleCalculator {

    /**
     * Calculates effective start day for salary cycle in a given [yearMonth].
     * eff(S, month) = min(S, daysInMonth)
     */
    fun effectiveStartDay(startDay: Int, yearMonth: YearMonth): Int {
        val daysInMonth = yearMonth.lengthOfMonth()
        return min(startDay, daysInMonth)
    }

    /**
     * Calculates the salary cycle [startDate, endDate] for a given [startDay] and [date].
     * If date.dayOfMonth >= eff(S, date.month) the cycle starts on eff(S, date.month) of that month,
     * otherwise on eff(S, previous month) of the previous month.
     * The cycle ends the day before the next cycle start date.
     */
    fun calculateCycle(startDay: Int, date: LocalDate): SalaryCyclePeriod {
        require(startDay in 1..31) { "startDay must be between 1 and 31" }

        val currentMonth = YearMonth.from(date)
        val effCurrent = effectiveStartDay(startDay, currentMonth)
        val effectiveStartThisMonth = currentMonth.atDay(effCurrent)

        val (cycleStart, nextStartMonth) = if (date.dayOfMonth >= effCurrent) {
            effectiveStartThisMonth to currentMonth.plusMonths(1)
        } else {
            val prevMonth = currentMonth.minusMonths(1)
            val effPrev = effectiveStartDay(startDay, prevMonth)
            prevMonth.atDay(effPrev) to currentMonth
        }

        val effNext = effectiveStartDay(startDay, nextStartMonth)
        val nextCycleStart = nextStartMonth.atDay(effNext)
        val cycleEnd = nextCycleStart.minusDays(1)

        return SalaryCyclePeriod(cycleStart, cycleEnd)
    }
}
