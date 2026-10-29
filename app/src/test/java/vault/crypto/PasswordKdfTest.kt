package vault.crypto

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordKdfTest {

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
    fun testRfc9106Section53Vector() {
        val password = ByteArray(32) { 0x01.toByte() }
        val salt = ByteArray(16) { 0x02.toByte() }
        val secret = ByteArray(8) { 0x03.toByte() }
        val associatedData = ByteArray(12) { 0x04.toByte() }
        val expectedTagHex = "0d640df58d78766c08c037a34a8b53c9d01ef0452d75b65eb52520e96b01e659"
        val expectedTag = hexToBytes(expectedTagHex)

        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withMemoryAsKB(32)
            .withIterations(3)
            .withParallelism(4)
            .withSalt(salt)
            .withSecret(secret)
            .withAdditional(associatedData)

        val generator = Argon2BytesGenerator()
        generator.init(builder.build())

        val result = ByteArray(32)
        generator.generateBytes(password, result, 0, result.size)

        assertArrayEquals("Argon2id tag should match RFC 9106 vector", expectedTag, result)
        assertEquals(expectedTagHex, bytesToHex(result))
    }

    @Test
    fun testPasswordKdfArgon2idDerivation() {
        val password = charArrayOf('m', 'y', 'S', 'e', 'c', 'r', 'e', 't', '1', '2', '3')
        val salt = ByteArray(16) { 0xAA.toByte() }
        val params = KdfParams(memoryKiB = 1024, iterations = 1, parallelism = 1)

        val key1 = PasswordKdf.argon2id(password, salt, params)
        val key2 = PasswordKdf.argon2id(password, salt, params)

        assertNotNull(key1)
        assertEquals(32, key1.size)
        assertArrayEquals("Same inputs should yield identical derived key", key1, key2)
    }

    @Test
    fun testBenchmarkMillis() {
        val params = KdfParams(memoryKiB = 1024, iterations = 1, parallelism = 1)
        val elapsedMillis = PasswordKdf.benchmarkMillis(params)
        assertTrue("Benchmark should take >= 0 millis", elapsedMillis >= 0)
    }
}