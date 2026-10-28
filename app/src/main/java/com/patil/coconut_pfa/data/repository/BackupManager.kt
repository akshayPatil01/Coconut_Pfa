package com.patil.coconut_pfa.data.repository

import android.content.Context
import android.net.Uri
import com.patil.coconut_pfa.data.local.datastore.UserSettingsDataStore
import com.patil.coconut_pfa.data.local.db.CoconutDatabase
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.BillEntity
import com.patil.coconut_pfa.data.local.entity.BillOccurrenceEntity
import com.patil.coconut_pfa.data.local.entity.CardDetailsEntity
import com.patil.coconut_pfa.data.local.entity.CardStatementEntity
import com.patil.coconut_pfa.data.local.entity.CategoryEntity
import com.patil.coconut_pfa.data.local.entity.FdDetailsEntity
import com.patil.coconut_pfa.data.local.entity.GoalContributionEntity
import com.patil.coconut_pfa.data.local.entity.GoalEntity
import com.patil.coconut_pfa.data.local.entity.LedgerEntryEntity
import com.patil.coconut_pfa.data.local.entity.LoanDetailsEntity
import com.patil.coconut_pfa.data.local.entity.MerchantMemoryEntity
import com.patil.coconut_pfa.data.local.entity.NetWorthSnapshotEntity
import com.patil.coconut_pfa.data.local.entity.PersonEntity
import com.patil.coconut_pfa.data.local.entity.SmsItemEntity
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.local.entity.ValuationEntity
import com.patil.coconut_pfa.data.local.security.BackupPassphraseManager
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.SecureRandom
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@JsonClass(generateAdapter = true)
data class BackupContainer(
    val formatVersion: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val settings: SettingsBackupDto,
    val accounts: List<AccountEntity>,
    val cardDetails: List<CardDetailsEntity>,
    val cardStatements: List<CardStatementEntity>,
    val loanDetails: List<LoanDetailsEntity>,
    val fdDetails: List<FdDetailsEntity>,
    val valuations: List<ValuationEntity>,
    val categories: List<CategoryEntity>,
    val transactions: List<TxnEntity>,
    val bills: List<BillEntity>,
    val billOccurrences: List<BillOccurrenceEntity>,
    val persons: List<PersonEntity>,
    val ledgerEntries: List<LedgerEntryEntity>,
    val goals: List<GoalEntity>,
    val goalContributions: List<GoalContributionEntity>,
    val smsItems: List<SmsItemEntity>,
    val merchantMemory: List<MerchantMemoryEntity>,
    val netWorthSnapshots: List<NetWorthSnapshotEntity>
)

@JsonClass(generateAdapter = true)
data class SettingsBackupDto(
    val salaryCycleStartDay: Int = 1,
    val biometricEnabled: Boolean = false,
    val autoLockSeconds: Int = 60,
    val privacyModeDefault: Boolean = false,
    val reminderHour: Int = 9,
    val defaultReminderDays: Int = 3,
    val smsEnabled: Boolean = false,
    val backupEnabled: Boolean = false,
    val lastBackupAt: Long? = null,
    val backupKeep: Int = 7,
    val onboardingDone: Boolean = true,
    val lastUsedAccountId: String? = null,
    val lastUsedCategoryId: String? = null,
    val lastSmsCatchupTimestamp: Long = 0L
)

data class BackupSummary(
    val createdAt: Long,
    val appVersion: String,
    val accountCount: Int,
    val transactionCount: Int,
    val categoryCount: Int,
    val billCount: Int,
    val goalCount: Int,
    val personCount: Int,
    val smsItemCount: Int
)

object BackupCrypto {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128

    fun encryptPayload(derivedKey: ByteArray, plaintextBytes: ByteArray): Pair<ByteArray, ByteArray> {
        val iv = ByteArray(GCM_IV_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val secretKey = SecretKeySpec(derivedKey, "AES")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        val ciphertext = cipher.doFinal(plaintextBytes)
        return Pair(iv, ciphertext)
    }

    fun decryptPayload(derivedKey: ByteArray, iv: ByteArray, ciphertext: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(derivedKey, "AES")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        return cipher.doFinal(ciphertext)
    }

    fun gzipCompress(data: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { it.write(data) }
        return bos.toByteArray()
    }

    fun gzipDecompress(compressed: ByteArray): ByteArray {
        val bis = ByteArrayInputStream(compressed)
        return GZIPInputStream(bis).use { it.readBytes() }
    }
}

object BackupManager {

    suspend fun exportBackup(context: Context, destinationUri: Uri, password: String? = null) {
        val db = CoconutDatabase.getInstance(context)
        val settingsDataStore = UserSettingsDataStore(context)
        val currentSettings = settingsDataStore.settingsFlow.first()

        val settingsDto = SettingsBackupDto(
            salaryCycleStartDay = currentSettings.salaryCycleStartDay,
            biometricEnabled = currentSettings.biometricEnabled,
            autoLockSeconds = currentSettings.autoLockSeconds,
            privacyModeDefault = currentSettings.privacyModeDefault,
            reminderHour = currentSettings.reminderHour,
            defaultReminderDays = currentSettings.defaultReminderDays,
            smsEnabled = currentSettings.smsEnabled,
            backupEnabled = currentSettings.backupEnabled,
            lastBackupAt = System.currentTimeMillis(),
            backupKeep = currentSettings.backupKeep,
            onboardingDone = currentSettings.onboardingDone,
            lastUsedAccountId = currentSettings.lastUsedAccountId,
            lastUsedCategoryId = currentSettings.lastUsedCategoryId,
            lastSmsCatchupTimestamp = currentSettings.lastSmsCatchupTimestamp
        )

        val container = BackupContainer(
            formatVersion = 1,
            createdAt = System.currentTimeMillis(),
            appVersion = "1.0",
            settings = settingsDto,
            accounts = db.accountDao().getAllAccountsList(),
            cardDetails = db.cardDetailsDao().getAll(),
            cardStatements = db.cardStatementDao().getAll(),
            loanDetails = db.loanDetailsDao().getAll(),
            fdDetails = db.fdDetailsDao().getAll(),
            valuations = db.valuationDao().getAll(),
            categories = db.categoryDao().getAll(),
            transactions = db.txnDao().getAll(),
            bills = db.billDao().getAll(),
            billOccurrences = db.billOccurrenceDao().getAll(),
            persons = db.personDao().getAll(),
            ledgerEntries = db.ledgerEntryDao().getAll(),
            goals = db.goalDao().getAll(),
            goalContributions = db.goalContributionDao().getAll(),
            smsItems = db.smsItemDao().getAll(),
            merchantMemory = db.merchantMemoryDao().getAll(),
            netWorthSnapshots = db.netWorthSnapshotDao().getAll()
        )

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonString = moshi.adapter(BackupContainer::class.java).toJson(container)
        val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)
        val gzipBytes = BackupCrypto.gzipCompress(jsonBytes)

        val derivedKey = BackupPassphraseManager.getDerivedKey(context, password)
        val salt = BackupPassphraseManager.getSalt(context) ?: ByteArray(16).also { SecureRandom().nextBytes(it) }
        val (iv, ciphertext) = BackupCrypto.encryptPayload(derivedKey, gzipBytes)

        val bos = ByteArrayOutputStream()
        DataOutputStream(bos).use { dos ->
            dos.write("CBK1".toByteArray(Charsets.UTF_8))
            dos.writeInt(1)
            dos.writeInt(salt.size)
            dos.write(salt)
            dos.writeInt(iv.size)
            dos.write(iv)
            dos.writeLong(container.createdAt)
            val versionBytes = container.appVersion.toByteArray(Charsets.UTF_8)
            dos.writeInt(versionBytes.size)
            dos.write(versionBytes)
            dos.write(ciphertext)
        }

        context.contentResolver.openOutputStream(destinationUri)?.use { os ->
            os.write(bos.toByteArray())
        }

        settingsDataStore.updateLastBackupAt(container.createdAt)
    }

    fun readAndDecryptBackup(fileBytes: ByteArray, password: String): BackupContainer {
        val bis = ByteArrayInputStream(fileBytes)
        val dis = DataInputStream(bis)
        val magic = ByteArray(4)
        dis.readFully(magic)
        require(String(magic, Charsets.UTF_8) == "CBK1") { "Invalid backup file format (magic mismatch)" }

        val formatVersion = dis.readInt()
        val saltLen = dis.readInt()
        val salt = ByteArray(saltLen)
        dis.readFully(salt)

        val ivLen = dis.readInt()
        val iv = ByteArray(ivLen)
        dis.readFully(iv)

        val createdAt = dis.readLong()
        val versionLen = dis.readInt()
        val versionBytes = ByteArray(versionLen)
        dis.readFully(versionBytes)
        val appVersion = String(versionBytes, Charsets.UTF_8)

        val ciphertext = dis.readBytes()

        val derivedKey = BackupPassphraseManager.deriveKey(password, salt)
        val decryptedGzip = BackupCrypto.decryptPayload(derivedKey, iv, ciphertext)
        val jsonBytes = BackupCrypto.gzipDecompress(decryptedGzip)
        val jsonString = String(jsonBytes, Charsets.UTF_8)

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(BackupContainer::class.java)
        return adapter.fromJson(jsonString) ?: throw IllegalStateException("Failed to parse backup JSON")
    }

    suspend fun inspectBackup(context: Context, sourceUri: Uri, password: String): BackupSummary {
        val fileBytes = context.contentResolver.openInputStream(sourceUri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("Cannot read backup file")
        val container = readAndDecryptBackup(fileBytes, password)
        return BackupSummary(
            createdAt = container.createdAt,
            appVersion = container.appVersion,
            accountCount = container.accounts.size,
            transactionCount = container.transactions.size,
            categoryCount = container.categories.size,
            billCount = container.bills.size,
            goalCount = container.goals.size,
            personCount = container.persons.size,
            smsItemCount = container.smsItems.size
        )
    }

    suspend fun restoreBackup(context: Context, sourceUri: Uri, password: String) {
        val fileBytes = context.contentResolver.openInputStream(sourceUri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("Cannot read backup file")

        val container = readAndDecryptBackup(fileBytes, password)

        val db = CoconutDatabase.getInstance(context)
        val rawDb = db.openHelper.writableDatabase

        rawDb.beginTransaction()
        try {
            rawDb.execSQL("PRAGMA foreign_keys = OFF")
            rawDb.execSQL("DELETE FROM card_details")
            rawDb.execSQL("DELETE FROM card_statements")
            rawDb.execSQL("DELETE FROM loan_details")
            rawDb.execSQL("DELETE FROM fd_details")
            rawDb.execSQL("DELETE FROM valuations")
            rawDb.execSQL("DELETE FROM categories")
            rawDb.execSQL("DELETE FROM transactions")
            rawDb.execSQL("DELETE FROM bills")
            rawDb.execSQL("DELETE FROM bill_occurrences")
            rawDb.execSQL("DELETE FROM persons")
            rawDb.execSQL("DELETE FROM ledger_entries")
            rawDb.execSQL("DELETE FROM goals")
            rawDb.execSQL("DELETE FROM goal_contributions")
            rawDb.execSQL("DELETE FROM sms_items")
            rawDb.execSQL("DELETE FROM merchant_memories")
            rawDb.execSQL("DELETE FROM net_worth_snapshots")
            rawDb.execSQL("DELETE FROM accounts")

            val accountDao = db.accountDao()
            container.accounts.forEach { accountDao.insert(it) }
            val cardDao = db.cardDetailsDao()
            container.cardDetails.forEach { cardDao.insert(it) }
            val cardStmtDao = db.cardStatementDao()
            container.cardStatements.forEach { cardStmtDao.insert(it) }
            val loanDao = db.loanDetailsDao()
            container.loanDetails.forEach { loanDao.insert(it) }
            val fdDao = db.fdDetailsDao()
            container.fdDetails.forEach { fdDao.insert(it) }
            val valDao = db.valuationDao()
            container.valuations.forEach { valDao.insert(it) }
            val catDao = db.categoryDao()
            container.categories.forEach { catDao.insert(it) }
            val txnDao = db.txnDao()
            container.transactions.forEach { txnDao.insert(it) }
            val billDao = db.billDao()
            container.bills.forEach { billDao.insert(it) }
            val billOccDao = db.billOccurrenceDao()
            container.billOccurrences.forEach { billOccDao.insert(it) }
            val personDao = db.personDao()
            container.persons.forEach { personDao.insert(it) }
            val ledgerDao = db.ledgerEntryDao()
            container.ledgerEntries.forEach { ledgerDao.insert(it) }
            val goalDao = db.goalDao()
            container.goals.forEach { goalDao.insert(it) }
            val goalContribDao = db.goalContributionDao()
            container.goalContributions.forEach { goalContribDao.insert(it) }
            val smsDao = db.smsItemDao()
            container.smsItems.forEach { smsDao.insert(it) }
            val merchantDao = db.merchantMemoryDao()
            container.merchantMemory.forEach { merchantDao.insert(it) }
            val netWorthDao = db.netWorthSnapshotDao()
            container.netWorthSnapshots.forEach { netWorthDao.insert(it) }

            rawDb.execSQL("PRAGMA foreign_keys = ON")
            rawDb.setTransactionSuccessful()
        } finally {
            rawDb.endTransaction()
        }

        // Restore settings (except pinHash/pinSalt)
        val settingsDataStore = UserSettingsDataStore(context)
        val s = container.settings
        settingsDataStore.updateSalaryCycleStartDay(s.salaryCycleStartDay)
        settingsDataStore.updateBiometricEnabled(s.biometricEnabled)
        settingsDataStore.updateAutoLockSeconds(s.autoLockSeconds)
        settingsDataStore.updatePrivacyModeDefault(s.privacyModeDefault)
        settingsDataStore.updateReminderHour(s.reminderHour)
        settingsDataStore.updateDefaultReminderDays(s.defaultReminderDays)
        settingsDataStore.updateSmsEnabled(s.smsEnabled)
        settingsDataStore.updateBackupEnabled(s.backupEnabled)
        settingsDataStore.updateLastBackupAt(s.lastBackupAt)
        settingsDataStore.updateBackupKeep(s.backupKeep)
        settingsDataStore.updateOnboardingDone(s.onboardingDone)
        settingsDataStore.updateLastUsedAccount(s.lastUsedAccountId)
        settingsDataStore.updateLastUsedCategory(s.lastUsedCategoryId)
        settingsDataStore.updateLastSmsCatchupTimestamp(s.lastSmsCatchupTimestamp)

        // Clear PIN as PINs are not included in backups, and require setting new PIN
        settingsDataStore.updatePin(null, null)
    }
}
