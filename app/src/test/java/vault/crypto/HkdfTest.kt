package vault.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class HkdfTest {

    private fun hexToBytes(hex: String): ByteArray {
        val cleanHex = hex.replace(" ", "")
        val len = cleanHex.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(cleanHex[i], 16) shl 4) +
                    Character.digit(cleanHex[i + 1], 16)).toByte()
        }
        return data
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    @Test
    fun testRfc5869TestCase1() {
        val ikm = ByteArray(22) { 0x0b.toByte() }
        val salt = hexToBytes("000102030405060708090a0b0c")
        val info = hexToBytes("f0f1f2f3f4f5f6f7f8f9")
        val length = 42
        val expectedOkmHex = "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf34007208d5b887185865"
        val expectedOkm = hexToBytes(expectedOkmHex)

        val okm = Hkdf.deriveKey(salt = salt, ikm = ikm, info = info, length = length)

        assertArrayEquals("OKM should match RFC 5869 Test Case 1", expectedOkm, okm)
        org.junit.Assert.assertEquals(expectedOkmHex, bytesToHex(okm))
    }
}