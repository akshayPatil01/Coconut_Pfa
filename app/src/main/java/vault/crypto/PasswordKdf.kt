package vault.crypto

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets

data class KdfParams(
    val memoryKiB: Int = 65536,
    val iterations: Int = 3,
    val parallelism: Int = 1
)

object PasswordKdf {
    fun argon2id(password: CharArray, salt: ByteArray, params: KdfParams = KdfParams()): ByteArray {
        val passwordBytes = charArrayToUtf8Bytes(password)
        try {
            val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withMemoryAsKB(params.memoryKiB)
                .withIterations(params.iterations)
                .withParallelism(params.parallelism)
                .withSalt(salt)

            val generator = Argon2BytesGenerator()
            generator.init(builder.build())

            val result = ByteArray(32)
            generator.generateBytes(passwordBytes, result, 0, result.size)
            return result
        } finally {
            passwordBytes.fill(0)
        }
    }

    fun benchmarkMillis(params: KdfParams): Long {
        val dummyPassword = charArrayOf('b', 'e', 'n', 'c', 'h', 'm', 'a', 'r', 'k')
        val dummySalt = ByteArray(16)
        val startTime = System.currentTimeMillis()
        argon2id(dummyPassword, dummySalt, params)
        return System.currentTimeMillis() - startTime
    }

    private fun charArrayToUtf8Bytes(chars: CharArray): ByteArray {
        val charBuffer = CharBuffer.wrap(chars)
        val byteBuffer = StandardCharsets.UTF_8.encode(charBuffer)
        val bytes = ByteArray(byteBuffer.remaining())
        byteBuffer.get(bytes)
        return bytes
    }
}