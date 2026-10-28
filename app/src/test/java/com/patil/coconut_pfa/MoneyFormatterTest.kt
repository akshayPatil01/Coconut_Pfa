package com.patil.coconut_pfa

import com.patil.coconut_pfa.util.MoneyFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {

    @Test
    fun testPositiveRupeesFormatting() {
        val result = MoneyFormatter.format(10000000L) // 1 lakh rupees
        assertEquals("₹1,00,000", result)
    }

    @Test
    fun testPaiseFormattingWhenNonZero() {
        val result = MoneyFormatter.format(125050L) // 1250.50 rupees
        assertEquals("₹1,250.50", result)
    }

    @Test
    fun testNegativeAmountFormatting() {
        val result = MoneyFormatter.format(-50000L) // -500 rupees
        assertEquals("−₹500", result)
    }

    @Test
    fun testZeroAmountFormatting() {
        val result = MoneyFormatter.format(0L)
        assertEquals("₹0", result)
    }

    @Test
    fun testSmallPaiseOnlyFormatting() {
        val result = MoneyFormatter.format(5L) // 0.05 rupees
        assertEquals("₹0.05", result)
    }

    @Test
    fun testLargeCroresFormatting() {
        val result = MoneyFormatter.format(1234567890L) // 1,23,45,678.90 rupees
        assertEquals("₹1,23,45,678.90", result)
    }

    @Test
    fun testPrivacyMasking() {
        val result = MoneyFormatter.format(10000000L, isPrivacyEnabled = true)
        assertEquals("₹••••••", result)
    }

    @Test
    fun testWithoutCurrencySymbol() {
        val result = MoneyFormatter.format(10000000L, showSymbol = false)
        assertEquals("1,00,000", result)
    }

    @Test
    fun testRupeesToPaiseConversion() {
        assertEquals(100000L, MoneyFormatter.rupeesToPaise(1000L))
        assertEquals(125050L, MoneyFormatter.rupeesToPaise(1250.50))
    }

    @Test
    fun testParseToPaise() {
        assertEquals(10000000L, MoneyFormatter.parseToPaise("1,00,000"))
        assertEquals(125050L, MoneyFormatter.parseToPaise("₹1,250.50"))
        assertEquals(-50000L, MoneyFormatter.parseToPaise("−₹500"))
    }
}
