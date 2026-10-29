package com.patil.coconut_pfa

import com.patil.coconut_pfa.data.local.entity.DbTxnSource
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.repository.BackupCrypto
import com.patil.coconut_pfa.data.local.security.BackupPassphraseManager
import com.patil.coconut_pfa.domain.CardCycleCalculator
import com.patil.coconut_pfa.domain.CycleSummaryCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom
import java.time.LocalDate
import java.time.ZoneId

class HardeningRulesTest {

    private val zoneId = ZoneId.of("Asia/Kolkata")

    @Test
    fun testRefundReducesExpenseAndCardOutstanding() {
        val ccId = "acc_cc"
        val txns = listOf(
            TxnEntity(
                id = "t1",
                type = DbTxnType.EXPENSE,
                amountPaise = 100000L, // ₹1,000 expense
                accountId = ccId,
                occurredAt = System.currentTimeMillis(),
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "t2",
                type = DbTxnType.REFUND,
                amountPaise = 30000L, // ₹300 refund
                accountId = ccId,
                occurredAt = System.currentTimeMillis(),
                source = DbTxnSource.MANUAL
            )
        )

        val unbilled = CardCycleCalculator.calculateUnbilledAmount(ccId, null, txns, zoneId)
        assertEquals(70000L, unbilled) // 100000 - 30000 = 70000

        val startDate = LocalDate.of(2026, 10, 1)
        val endDate = LocalDate.of(2026, 10, 31)
        val cycleTotals = CycleSummaryCalculator.calculateCycleTotals(startDate, endDate, txns, zoneId)
        assertEquals(70000L, cycleTotals.spentPaise) // 100000 - 30000
    }

    @Test
    fun testTransfersExcludedFromCycleTotals() {
        val startDate = LocalDate.of(2026, 10, 1)
        val endDate = LocalDate.of(2026, 10, 31)
        val txns = listOf(
            TxnEntity(
                id = "tr1",
                type = DbTxnType.TRANSFER,
                amountPaise = 500000L, // ₹5,000 transfer
                accountId = "acc_1",
                toAccountId = "acc_2",
                occurredAt = LocalDate.of(2026, 10, 15).atStartOfDay(zoneId).toInstant().toEpochMilli(),
                source = DbTxnSource.MANUAL
            )
        )

        val totals = CycleSummaryCalculator.calculateCycleTotals(startDate, endDate, txns, zoneId)
        assertEquals(0L, totals.receivedPaise)
        assertEquals(0L, totals.spentPaise)
    }

    @Test
    fun testTransactionAtExactCycleBoundaries() {
        val startDate = LocalDate.of(2026, 10, 1)
        val endDate = LocalDate.of(2026, 10, 31)

        val startEpoch = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endEpoch = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        val txns = listOf(
            TxnEntity(
                id = "b1",
                type = DbTxnType.EXPENSE,
                amountPaise = 1000L,
                accountId = "acc_1",
                categoryId = "cat_food",
                occurredAt = startEpoch, // Exactly at cycle start
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "b2",
                type = DbTxnType.EXPENSE,
                amountPaise = 2000L,
                accountId = "acc_1",
                categoryId = "cat_food",
                occurredAt = endEpoch, // Exactly at cycle end (inclusive)
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "b3",
                type = DbTxnType.EXPENSE,
                amountPaise = 5000L,
                accountId = "acc_1",
                categoryId = "cat_food",
                occurredAt = startEpoch - 1, // Before cycle start
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "b4",
                type = DbTxnType.EXPENSE,
                amountPaise = 8000L,
                accountId = "acc_1",
                categoryId = "cat_food",
                occurredAt = endEpoch + 1, // After cycle end
                source = DbTxnSource.MANUAL
            )
        )

        val totals = CycleSummaryCalculator.calculateCycleTotals(startDate, endDate, txns, zoneId)
        assertEquals(3000L, totals.spentPaise) // 1000 + 2000

        val catSpent = CycleSummaryCalculator.calculateCategorySpent("cat_food", startDate, endDate, txns, zoneId)
        assertEquals(3000L, catSpent)
    }

    @Test
    fun testBackupEncryptDecryptRoundTripFull() {
        val password = "SecureBackupPassword!987"
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val derivedKey = BackupPassphraseManager.deriveKey(password, salt)

        val sensitiveData = "{\"coconut\": \"private finance container with sensitive txns & accounts\"}"
        val compressed = BackupCrypto.gzipCompress(sensitiveData.toByteArray(Charsets.UTF_8))
        val (iv, cipherText) = BackupCrypto.encryptPayload(derivedKey, compressed)

        val decrypted = BackupCrypto.decryptPayload(derivedKey, iv, cipherText)
        val decompressed = BackupCrypto.gzipDecompress(decrypted)
        val restoredString = String(decompressed, Charsets.UTF_8)

        assertEquals(sensitiveData, restoredString)
    }
}
