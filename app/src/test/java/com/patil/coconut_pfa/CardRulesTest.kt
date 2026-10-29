package com.patil.coconut_pfa

import com.patil.coconut_pfa.domain.CardNetwork
import com.patil.coconut_pfa.domain.CardRules
import org.junit.Assert.*
import org.junit.Test

class CardRulesTest {

    @Test
    fun testDetectNetworksAndLuhn() {
        // 1. Visa: 4111111111111111
        val visa = "4111111111111111"
        assertEquals(CardNetwork.VISA, CardRules.detectNetwork(visa))
        assertTrue(CardRules.luhnCheck(visa))
        assertTrue(CardRules.isValidLength(visa))
        assertEquals("4111 1111 1111 1111", CardRules.formatNumber(visa, CardNetwork.VISA))

        // 2. Mastercard: 5555555555554444
        val mc = "5555555555554444"
        assertEquals(CardNetwork.MASTERCARD, CardRules.detectNetwork(mc))
        assertTrue(CardRules.luhnCheck(mc))
        assertTrue(CardRules.isValidLength(mc))
        assertEquals("5555 5555 5555 4444", CardRules.formatNumber(mc, CardNetwork.MASTERCARD))

        // 3. Amex: 378282246310005
        val amex = "378282246310005"
        assertEquals(CardNetwork.AMEX, CardRules.detectNetwork(amex))
        assertTrue(CardRules.luhnCheck(amex))
        assertTrue(CardRules.isValidLength(amex))
        assertTrue(CardRules.validateCvv("1234", CardNetwork.AMEX))
        assertEquals("3782 822463 10005", CardRules.formatNumber(amex, CardNetwork.AMEX))

        // 4. Discover: 6011111111111117
        val discover = "6011111111111117"
        assertEquals(CardNetwork.DISCOVER, CardRules.detectNetwork(discover))
        assertTrue(CardRules.luhnCheck(discover))
        assertTrue(CardRules.isValidLength(discover))

        // 5. Diners: 30569309025904
        val diners = "30569309025904"
        assertEquals(CardNetwork.DINERS, CardRules.detectNetwork(diners))
        assertTrue(CardRules.luhnCheck(diners))
        assertTrue(CardRules.isValidLength(diners))
        assertEquals("3056 930902 5904", CardRules.formatNumber(diners, CardNetwork.DINERS))
    }

    @Test
    fun testExpiryValidation() {
        assertTrue(CardRules.validateExpiry("12/30"))
        assertFalse(CardRules.validateExpiry("13/30"))
        assertFalse(CardRules.validateExpiry("01/20"))
    }
}
