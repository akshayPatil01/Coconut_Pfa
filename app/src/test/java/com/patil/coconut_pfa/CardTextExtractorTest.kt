package com.patil.coconut_pfa

import com.patil.coconut_pfa.domain.CardTextExtractor
import org.junit.Assert.*
import org.junit.Test

class CardTextExtractorTest {

    @Test
    fun testSpacedNumberExtraction() {
        val lines = listOf(
            "HDFC BANK",
            "4242 4242 4242 4242",
            "12/26",
            "JOHN DOE"
        )
        val result = CardTextExtractor.extract(lines)
        assertNotNull(result)
        assertEquals("4242424242424242", result?.number)
        assertEquals("12/26", result?.expiry)
        assertEquals("JOHN DOE", result?.cardholderName)
    }

    @Test
    fun testNumberSplitOverTwoLines() {
        val lines = listOf(
            "ICICI BANK",
            "4242 4242",
            "4242 4242",
            "05/28",
            "JANE SMITH"
        )
        val result = CardTextExtractor.extract(lines)
        assertNotNull(result)
        assertEquals("4242424242424242", result?.number)
        assertEquals("05/28", result?.expiry)
        assertEquals("JANE SMITH", result?.cardholderName)
    }

    @Test
    fun testValidFromAndValidThru() {
        val lines = listOf(
            "SBI",
            "4242424242424242",
            "VALID FROM 01/22",
            "VALID THRU 12/26",
            "TEST USER"
        )
        val result = CardTextExtractor.extract(lines)
        assertNotNull(result)
        assertEquals("4242424242424242", result?.number)
        assertEquals("12/26", result?.expiry)
    }

    @Test
    fun testInvalidLuhnNumberFails() {
        val lines = listOf(
            "BANK",
            "1234567890123456",
            "12/25",
            "SOME NAME"
        )
        val result = CardTextExtractor.extract(lines)
        assertNull(result)
    }

    @Test
    fun testNoNumberAtAll() {
        val lines = listOf(
            "SOME BANK",
            "CREDIT CARD",
            "12/25",
            "ANOTHER NAME"
        )
        val result = CardTextExtractor.extract(lines)
        assertNull(result)
    }
}
