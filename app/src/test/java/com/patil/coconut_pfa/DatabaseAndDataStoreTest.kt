package com.patil.coconut_pfa

import com.patil.coconut_pfa.data.local.db.CategoryPresets
import com.patil.coconut_pfa.data.local.db.Converters
import com.patil.coconut_pfa.data.local.datastore.UserSettings
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.DbAccountInstitution
import com.patil.coconut_pfa.data.local.entity.DbAccountType
import com.patil.coconut_pfa.data.local.entity.DbCategoryKind
import com.patil.coconut_pfa.data.local.entity.DbTxnSource
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DatabaseAndDataStoreTest {

    @Test
    fun testCategoryPresetsSeedingList() {
        val presets = CategoryPresets.presets
        assertTrue(presets.isNotEmpty())

        val expensePresets = presets.filter { it.kind == DbCategoryKind.EXPENSE }
        val incomePresets = presets.filter { it.kind == DbCategoryKind.INCOME }

        assertTrue("Should have expense presets", expensePresets.isNotEmpty())
        assertTrue("Should have income presets", incomePresets.isNotEmpty())

        val foodCategory = presets.find { it.name == "Food & dining" }
        assertNotNull(foodCategory)
        val restaurantsCategory = presets.find { it.name == "Restaurants" }
        assertNotNull(restaurantsCategory)
        assertEquals(foodCategory?.id, restaurantsCategory?.parentId)
    }

    @Test
    fun testDefaultUserSettings() {
        val defaultSettings = UserSettings()
        assertEquals(1, defaultSettings.salaryCycleStartDay)
        assertEquals(60, defaultSettings.autoLockSeconds)
        assertFalse(defaultSettings.privacyModeDefault)
        assertEquals(9, defaultSettings.reminderHour)
        assertEquals(3, defaultSettings.defaultReminderDays)
        assertFalse(defaultSettings.smsEnabled)
        assertFalse(defaultSettings.backupEnabled)
        assertEquals(7, defaultSettings.backupKeep)
        assertFalse(defaultSettings.onboardingDone)
    }

    @Test
    fun testKolkataDateTimeUtils() {
        val today = KolkataDateTimeUtils.currentLocalDate()
        val dateString = KolkataDateTimeUtils.currentDateString()
        assertEquals(dateString, today.toString())

        val epochMillis = KolkataDateTimeUtils.dateStringToEpochMillis(dateString)
        val convertedDate = KolkataDateTimeUtils.epochMillisToLocalDate(epochMillis)
        assertEquals(today, convertedDate)
    }

    @Test
    fun testConverters() {
        val converters = Converters()
        assertEquals("BANK", converters.fromAccountType(DbAccountType.BANK))
        assertEquals(DbAccountType.BANK, converters.toAccountType("BANK"))

        assertEquals("EXPENSE", converters.fromCategoryKind(DbCategoryKind.EXPENSE))
        assertEquals(DbCategoryKind.EXPENSE, converters.toCategoryKind("EXPENSE"))
    }

    @Test
    fun testAccountAndTxnEntityCreation() {
        val account = AccountEntity(
            id = "acc_test_1",
            name = "HDFC Salary",
            type = DbAccountType.BANK,
            institution = DbAccountInstitution.HDFC,
            last4 = "1234",
            openingBalancePaise = 1000000L,
            openingDate = "2024-01-01",
            icon = "account_balance",
            colour = 0xFF1E88E5L
        )
        assertEquals("acc_test_1", account.id)
        assertEquals(DbAccountInstitution.HDFC, account.institution)

        val txn = TxnEntity(
            id = "txn_test_1",
            type = DbTxnType.EXPENSE,
            amountPaise = 50000L,
            accountId = account.id,
            occurredAt = System.currentTimeMillis(),
            source = DbTxnSource.MANUAL
        )
        assertEquals("txn_test_1", txn.id)
        assertEquals("acc_test_1", txn.accountId)
    }
}
