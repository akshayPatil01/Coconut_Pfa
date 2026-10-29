package com.patil.coconut_pfa

import com.patil.coconut_pfa.data.repository.BackupCrypto
import com.patil.coconut_pfa.data.repository.BackupContainer
import com.patil.coconut_pfa.data.repository.SettingsBackupDto
import com.patil.coconut_pfa.data.local.security.BackupPassphraseManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom

class BackupManagerTest {

    @Test
    fun testGzipAndAesEncryption() {
        val password = "StrongBackupPassword123!"
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val derivedKey = BackupPassphraseManager.deriveKey(password, salt)

        assertEquals(32, derivedKey.size)

        val plaintext = "Hello Fortis Backup & Restore!".toByteArray(Charsets.UTF_8)
        val compressed = BackupCrypto.gzipCompress(plaintext)
        assertTrue(compressed.isNotEmpty())

        val (iv, ciphertext) = BackupCrypto.encryptPayload(derivedKey, compressed)
        assertTrue(iv.size == 12)
        assertTrue(ciphertext.isNotEmpty())

        val decrypted = BackupCrypto.decryptPayload(derivedKey, iv, ciphertext)
        val decompressed = BackupCrypto.gzipDecompress(decrypted)
        val result = String(decompressed, Charsets.UTF_8)

        assertEquals("Hello Fortis Backup & Restore!", result)
    }

    @Test
    fun testBackupContainerSerialization() {
        val container = BackupContainer(
            formatVersion = 1,
            createdAt = System.currentTimeMillis(),
            appVersion = "1.0",
            settings = SettingsBackupDto(salaryCycleStartDay = 5),
            accounts = emptyList(),
            cardDetails = emptyList(),
            cardStatements = emptyList(),
            loanDetails = emptyList(),
            fdDetails = emptyList(),
            valuations = emptyList(),
            categories = emptyList(),
            transactions = emptyList(),
            bills = emptyList(),
            billOccurrences = emptyList(),
            persons = emptyList(),
            ledgerEntries = emptyList(),
            goals = emptyList(),
            goalContributions = emptyList(),
            smsItems = emptyList(),
            merchantMemory = emptyList(),
            netWorthSnapshots = emptyList()
        )

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val json = moshi.adapter(BackupContainer::class.java).toJson(container)
        assertNotNull(json)
        assertTrue(json.contains("salaryCycleStartDay"))

        val deserialized = moshi.adapter(BackupContainer::class.java).fromJson(json)
        assertNotNull(deserialized)
        assertEquals(5, deserialized?.settings?.salaryCycleStartDay)
    }
}
