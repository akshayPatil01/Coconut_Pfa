package vault.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RecoveryCodeTest {

    @Test
    fun testGenerateAndFormatFormat() {
        val code = RecoveryCode.generate()
        assertNotNull(code)
        // Format should be 8 groups of 4 characters separated by 7 dashes -> 39 total characters
        assertEquals(39, code.length)
        assertTrue("Formatted code should contain 7 dashes", code.count { it == '-' } == 7)
    }

    @Test
    fun testGenerateFormatParseRoundTrip() {
        val rawBytes = RecoveryCode.generateBytes()
        val formattedCode = RecoveryCode.formatBytes(rawBytes)

        val parsedFromFormatted = RecoveryCode.parse(formattedCode)
        assertArrayEquals("Parsed bytes from formatted string should match original raw bytes", rawBytes, parsedFromFormatted)

        // Test lowercase version
        val parsedFromLower = RecoveryCode.parse(formattedCode.lowercase())
        assertArrayEquals("Parsed bytes from lowercase string should match original raw bytes", rawBytes, parsedFromLower)

        // Test missing dashes
        val noDashes = formattedCode.replace("-", "")
        val parsedFromNoDashes = RecoveryCode.parse(noDashes)
        assertArrayEquals("Parsed bytes from string with no dashes should match original raw bytes", rawBytes, parsedFromNoDashes)

        // Test with extra spaces and mixed case
        val spacesAndMixed = formattedCode.lowercase().replace("-", " ")
        val parsedFromSpaces = RecoveryCode.parse(spacesAndMixed)
        assertArrayEquals("Parsed bytes from string with spaces should match original raw bytes", rawBytes, parsedFromSpaces)
    }

    @Test
    fun testParseRejectsInvalidLength() {
        try {
            RecoveryCode.parse("ABCD-EFGH-IJKL")
            fail("Parsing short recovery code should fail")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun testParseRejectsInvalidCharacters() {
        try {
            // '1', '8', '9', '0' are invalid in Base32 alphabet
            RecoveryCode.parse("ABCD-EFGH-IJKL-MNOP-QRST-UVWX-YZ18-9000")
            fail("Parsing code with invalid Base32 chars should fail")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun testKeyDerivationFromCode() {
        val code = RecoveryCode.generate()
        val salt = ByteArray(16) { 0x55.toByte() }

        val key1 = RecoveryCode.key(code, salt)
        val key2 = RecoveryCode.key(code, salt)

        assertNotNull(key1)
        assertEquals(32, key1.size)
        assertArrayEquals("Deriving key twice with same code and salt produces identical key", key1, key2)
    }
}