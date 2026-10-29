package vault.crypto

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Hkdf {
    private const val HMAC_ALGORITHM = "HmacSHA256"
    private const val HASH_LEN = 32

    fun extract(salt: ByteArray?, ikm: ByteArray): ByteArray {
        val actualSalt = if (salt == null || salt.isEmpty()) {
            ByteArray(HASH_LEN)
        } else {
            salt
        }
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(actualSalt, HMAC_ALGORITHM))
        return mac.doFinal(ikm)
    }

    fun expand(prk: ByteArray, info: ByteArray?, length: Int): ByteArray {
        require(length in 1..(255 * HASH_LEN)) { "Output length L must be between 1 and ${255 * HASH_LEN}" }
        val actualInfo = info ?: ByteArray(0)
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        val keySpec = SecretKeySpec(prk, HMAC_ALGORITHM)

        val n = (length + HASH_LEN - 1) / HASH_LEN
        val okm = ByteArray(length)
        var t = ByteArray(0)
        var offset = 0

        for (i in 1..n) {
            mac.init(keySpec)
            mac.update(t)
            mac.update(actualInfo)
            mac.update(i.toByte())
            t = mac.doFinal()

            val bytesToCopy = minOf(HASH_LEN, length - offset)
            System.arraycopy(t, 0, okm, offset, bytesToCopy)
            offset += bytesToCopy
        }

        return okm
    }

    fun deriveKey(salt: ByteArray?, ikm: ByteArray, info: ByteArray?, length: Int): ByteArray {
        val prk = extract(salt, ikm)
        return expand(prk, info, length)
    }
}