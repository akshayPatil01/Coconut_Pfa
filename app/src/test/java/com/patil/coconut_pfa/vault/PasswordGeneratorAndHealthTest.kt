package com.patil.coconut_pfa.vault

import com.patil.coconut_pfa.data.local.entity.VaultItem
import com.patil.coconut_pfa.data.local.entity.VaultItemType
import com.patil.coconut_pfa.data.model.LoginMeta
import com.patil.coconut_pfa.data.model.LoginSecret
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test

class PasswordGeneratorAndHealthTest {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val loginMetaAdapter = moshi.adapter(LoginMeta::class.java)
    private val loginSecretAdapter = moshi.adapter(LoginSecret::class.java)

    @Test
    fun testGeneratorLengthAndSetsRespected() {
        val result = PasswordGenerator.generate(
            length = 24,
            useUpper = true,
            useLower = true,
            useNumbers = true,
            useSymbols = true,
            avoidLookAlikes = false
        )
        assertEquals(24, result.password.length)
        assertTrue(result.password.any { it.isUpperCase() })
        assertTrue(result.password.any { it.isLowerCase() })
        assertTrue(result.password.any { it.isDigit() })
        assertTrue(result.password.any { !it.isLetterOrDigit() })
    }

    @Test
    fun testGeneratorAtLeastOneCharFromEachChosenSet() {
        val result = PasswordGenerator.generate(
            length = 16,
            useUpper = true,
            useLower = false,
            useNumbers = true,
            useSymbols = false,
            avoidLookAlikes = false
        )
        assertTrue(result.password.any { it.isUpperCase() })
        assertTrue(result.password.any { it.isDigit() })
        assertFalse(result.password.any { it.isLowerCase() })
        assertFalse(result.password.any { !it.isLetterOrDigit() })
    }

    @Test
    fun testGeneratorLookAlikesExcluded() {
        val lookAlikes = setOf('0', 'O', 'o', '1', 'l', 'I')
        for (i in 0 until 50) {
            val result = PasswordGenerator.generate(
                length = 32,
                useUpper = true,
                useLower = true,
                useNumbers = true,
                useSymbols = true,
                avoidLookAlikes = true
            )
            for (c in result.password) {
                assertFalse("Password contains look-alike character '$c'", c in lookAlikes)
            }
        }
    }

    @Test
    fun testEntropyMaths() {
        val entropy = PasswordGenerator.calculateEntropy(10, 26)
        assertEquals(47.0, entropy, 0.1)

        val strengthWeak = PasswordGenerator.evaluateStrength(10, 47.0)
        assertEquals(PasswordStrength.WEAK, strengthWeak)

        val strengthStrong = PasswordGenerator.evaluateStrength(20, 95.0)
        assertEquals(PasswordStrength.STRONG, strengthStrong)
    }

    @Test
    fun testPasswordReuseDetection() {
        val pass1 = "SecurePassword123!@#"
        val pass2 = "AnotherSecure456\$%^"

        val item1 = VaultItem(
            id = "1",
            type = VaultItemType.LOGIN,
            metaBlob = ByteArray(0),
            secretBlob = ByteArray(0),
            updatedAt = System.currentTimeMillis()
        )
        val item2 = VaultItem(
            id = "2",
            type = VaultItemType.LOGIN,
            metaBlob = ByteArray(0),
            secretBlob = ByteArray(0),
            updatedAt = System.currentTimeMillis()
        )
        val item3 = VaultItem(
            id = "3",
            type = VaultItemType.LOGIN,
            metaBlob = ByteArray(0),
            secretBlob = ByteArray(0),
            updatedAt = System.currentTimeMillis()
        )

        val secrets = mapOf(
            "1" to LoginSecret(password = pass1),
            "2" to LoginSecret(password = pass1),
            "3" to LoginSecret(password = pass2)
        )
        val metas = mapOf(
            "1" to LoginMeta(title = "Item 1", username = "u1", website = ""),
            "2" to LoginMeta(title = "Item 2", username = "u2", website = ""),
            "3" to LoginMeta(title = "Item 3", username = "u3", website = "")
        )

        val report = PasswordHealthAnalyzer.analyze(
            items = listOf(item1, item2, item3),
            secretReader = { item -> loginSecretAdapter.toJson(secrets[item.id]!!) },
            metaReader = { item -> loginMetaAdapter.toJson(metas[item.id]!!) }
        )

        assertEquals(2, report.reusedItems.size)
        assertTrue(report.reusedItems.any { it.item.id == "1" })
        assertTrue(report.reusedItems.any { it.item.id == "2" })
        assertFalse(report.reusedItems.any { it.item.id == "3" })
    }
}
