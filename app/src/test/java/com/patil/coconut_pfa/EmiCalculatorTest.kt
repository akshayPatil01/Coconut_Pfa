package com.patil.coconut_pfa

import com.patil.coconut_pfa.domain.EmiCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class EmiCalculatorTest {

    @Test
    fun testEmiSplitCalculation() {
        // Outstanding = ₹10,00,000 (100,000,000 paise)
        // Rate = 12.00% (1200 bps)
        // EMI = ₹20,000 (2,000,000 paise)
        // Interest = round(100,000,000 * 1200 / 10000 / 12) = 1,000,000 paise (₹10,000)
        // Principal = 2,000,000 - 1,000,000 = 1,000,000 paise (₹10,000)
        val split = EmiCalculator.calculateEmiSplit(
            outstandingPaise = 100000000L,
            annualRateBps = 1200,
            emiPaise = 2000000L
        )

        assertEquals(1000000L, split.interestPaise)
        assertEquals(1000000L, split.principalPaise)
    }

    @Test
    fun testEmiSplitWithRounding() {
        // Outstanding = ₹5,00,000 (50,000,000 paise)
        // Rate = 8.50% (850 bps)
        // EMI = ₹10,000 (1,000,000 paise)
        // Interest = round(50,000,000 * 850 / 10000 / 12) = round(354,166.6667) = 354,167 paise
        // Principal = 1,000,000 - 354,167 = 645,833 paise
        val split = EmiCalculator.calculateEmiSplit(
            outstandingPaise = 50000000L,
            annualRateBps = 850,
            emiPaise = 1000000L
        )

        assertEquals(354167L, split.interestPaise)
        assertEquals(645833L, split.principalPaise)
    }

    @Test
    fun testZeroOutstandingEmiSplit() {
        val split = EmiCalculator.calculateEmiSplit(
            outstandingPaise = 0L,
            annualRateBps = 1000,
            emiPaise = 100000L
        )

        assertEquals(0L, split.interestPaise)
        assertEquals(100000L, split.principalPaise)
    }
}
