package com.patil.coconut_pfa

import com.patil.coconut_pfa.data.local.security.PinHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {

    @Test
    fun testPinHashingAndVerification() {
        val pin = "4321"
        val salt = PinHasher.generateSalt()
        assertEquals(16, salt.size)

        val hashB64 = PinHasher.hashPin(pin, salt)
        val saltB64 = PinHasher.saltToBase64(salt)

        assertTrue(hashB64.isNotEmpty())
        assertTrue(saltB64.isNotEmpty())

        // Verify correct PIN passes
        assertTrue(PinHasher.verifyPin("4321", hashB64, saltB64))

        // Verify wrong PIN fails
        assertFalse(PinHasher.verifyPin("1234", hashB64, saltB64))
        assertFalse(PinHasher.verifyPin("0000", hashB64, saltB64))
    }

    @Test
    fun testDifferentSaltsProduceDifferentHashes() {
        val pin = "1234"
        val salt1 = PinHasher.generateSalt()
        val salt2 = PinHasher.generateSalt()

        val hash1 = PinHasher.hashPin(pin, salt1)
        val hash2 = PinHasher.hashPin(pin, salt2)

        assertNotEquals(hash1, hash2)
    }
}
