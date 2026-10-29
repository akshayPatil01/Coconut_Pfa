package com.patil.coconut_pfa

import com.patil.coconut_pfa.domain.SalaryCycleCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SalaryCycleCalculatorTest {

    @Test
    fun testStartDay25AfterStartDay() {
        // S=25, 2026-10-28 -> 2026-10-25..2026-11-24
        val date = LocalDate.of(2026, 10, 28)
        val cycle = SalaryCycleCalculator.calculateCycle(25, date)
        assertEquals(LocalDate.of(2026, 10, 25), cycle.startDate)
        assertEquals(LocalDate.of(2026, 11, 24), cycle.endDate)
    }

    @Test
    fun testStartDay25BeforeStartDay() {
        // S=25, 2026-10-24 -> 2026-09-25..2026-10-24
        val date = LocalDate.of(2026, 10, 24)
        val cycle = SalaryCycleCalculator.calculateCycle(25, date)
        assertEquals(LocalDate.of(2026, 9, 25), cycle.startDate)
        assertEquals(LocalDate.of(2026, 10, 24), cycle.endDate)
    }

    @Test
    fun testStartDay31NonLeapFebruary() {
        // S=31, 2027-03-15 -> 2027-02-28..2027-03-30
        val date = LocalDate.of(2027, 3, 15)
        val cycle = SalaryCycleCalculator.calculateCycle(31, date)
        assertEquals(LocalDate.of(2027, 2, 28), cycle.startDate)
        assertEquals(LocalDate.of(2027, 3, 30), cycle.endDate)
    }

    @Test
    fun testStartDay30LeapYearFebruary() {
        // S=30, 2028-02-29 -> 2028-02-29..2028-03-29
        val date = LocalDate.of(2028, 2, 29)
        val cycle = SalaryCycleCalculator.calculateCycle(30, date)
        assertEquals(LocalDate.of(2028, 2, 29), cycle.startDate)
        assertEquals(LocalDate.of(2028, 3, 29), cycle.endDate)
    }

    @Test
    fun testStartDay1CalendarMonth() {
        // S=1 -> calendar month (e.g., 2026-10-15 -> 2026-10-01..2026-10-31)
        val date = LocalDate.of(2026, 10, 15)
        val cycle = SalaryCycleCalculator.calculateCycle(1, date)
        assertEquals(LocalDate.of(2026, 10, 1), cycle.startDate)
        assertEquals(LocalDate.of(2026, 10, 31), cycle.endDate)
    }
}
