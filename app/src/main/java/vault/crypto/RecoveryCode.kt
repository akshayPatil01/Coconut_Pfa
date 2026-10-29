package vault.crypto

import java.security.SecureRandom

object RecoveryCode {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private const val INFO_STRING = "fortis-vault-recovery-v1"
    private val random = SecureRandom()

    fun generateBytes(): ByteArray {
        val bytes = ByteArray(20)
        random.nextBytes(bytes)
        return bytes
    }

    fun generate(): String {
        return formatBytes(generateBytes())
    }

    fun formatBytes(bytes: ByteArray): String {
        require(bytes.size == 20) { "Recovery code raw bytes must be exactly 20 bytes" }
        val rawBase32 = encodeBase32(bytes)
        return rawBase32.chunked(4).joinToString("-")
    }

    fun parse(text: String): ByteArray {
        val cleaned = text.replace("-", "").replace(" ", "").uppercase()
        if (cleaned.length != 32) {
            throw IllegalArgumentException("Invalid recovery code length: expected 32 base32 chars, got ${cleaned.length}")
        }
        val result = ByteArray(20)
        var buffer = 0L
        var bitsLeft = 0
        var byteIdx = 0
        for (ch in cleaned) {
            val valIdx = ALPHABET.indexOf(ch)
            if (valIdx < 0) {
                throw IllegalArgumentException("Invalid base32 character in recovery code: $ch")
            }
            buffer = (buffer shl 5) or valIdx.toLong()
            bitsLeft += 5
            if (bitsLeft >= 8) {
                result[byteIdx++] = ((buffer ushr (bitsLeft - 8)) and 0xFF).toByte()
                bitsLeft -= 8
            }
        }
        return result
    }

    fun key(code: String, salt: ByteArray): ByteArray {
        val codeBytes = parse(code)
        return key(codeBytes, salt)
    }

    fun key(codeBytes: ByteArray, salt: ByteArray): ByteArray {
        require(codeBytes.size == 20) { "Code bytes must be 20 bytes" }
        val info = INFO_STRING.toByteArray(Charsets.UTF_8)
        return Hkdf.deriveKey(salt = salt, ikm = codeBytes, info = info, length = 32)
    }

    private fun encodeBase32(bytes: ByteArray): String {
        val sb = StringBuilder(32)
        var buffer = 0L
        var bitsLeft = 0
        for (b in bytes) {
            buffer = (buffer shl 8) or (b.toInt() and 0xFF).toLong()
            bitsLeft += 8
            while (bitsLeft >= 5) {
                val index = ((buffer ushr (bitsLeft - 5)) and 0x1F).toInt()
                sb.append(ALPHABET[index])
                bitsLeft -= 5
            }
        }
        if (bitsLeft > 0) {
            val index = ((buffer shl (5 - bitsLeft)) and 0x1F).toInt()
            sb.append(ALPHABET[index])
        }
        return sb.toString()
    }
}