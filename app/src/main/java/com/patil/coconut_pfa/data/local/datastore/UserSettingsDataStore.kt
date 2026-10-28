package com.patil.coconut_pfa.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserSettings(
    val salaryCycleStartDay: Int = 1,
    val pinHash: String? = null,
    val pinSalt: String? = null,
    val biometricEnabled: Boolean = false,
    val autoLockSeconds: Int = 60,
    val privacyModeDefault: Boolean = false,
    val reminderHour: Int = 9,
    val defaultReminderDays: Int = 3,
    val smsEnabled: Boolean = false,
    val backupEnabled: Boolean = false,
    val lastBackupAt: Long? = null,
    val backupKeep: Int = 7,
    val onboardingDone: Boolean = false,
    val lastUsedAccountId: String? = null,
    val lastUsedCategoryId: String? = null
)

class UserSettingsDataStore(private val context: Context) {

    private object Keys {
        val SALARY_CYCLE_START_DAY = intPreferencesKey("salary_cycle_start_day")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val AUTO_LOCK_SECONDS = intPreferencesKey("auto_lock_seconds")
        val PRIVACY_MODE_DEFAULT = booleanPreferencesKey("privacy_mode_default")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val DEFAULT_REMINDER_DAYS = intPreferencesKey("default_reminder_days")
        val SMS_ENABLED = booleanPreferencesKey("sms_enabled")
        val BACKUP_ENABLED = booleanPreferencesKey("backup_enabled")
        val LAST_BACKUP_AT = longPreferencesKey("last_backup_at")
        val BACKUP_KEEP = intPreferencesKey("backup_keep")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val LAST_USED_ACCOUNT_ID = stringPreferencesKey("last_used_account_id")
        val LAST_USED_CATEGORY_ID = stringPreferencesKey("last_used_category_id")
    }

    val settingsFlow: Flow<UserSettings> = context.settingsDataStore.data.map { prefs ->
        UserSettings(
            salaryCycleStartDay = prefs[Keys.SALARY_CYCLE_START_DAY] ?: 1,
            pinHash = prefs[Keys.PIN_HASH],
            pinSalt = prefs[Keys.PIN_SALT],
            biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
            autoLockSeconds = prefs[Keys.AUTO_LOCK_SECONDS] ?: 60,
            privacyModeDefault = prefs[Keys.PRIVACY_MODE_DEFAULT] ?: false,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 9,
            defaultReminderDays = prefs[Keys.DEFAULT_REMINDER_DAYS] ?: 3,
            smsEnabled = prefs[Keys.SMS_ENABLED] ?: false,
            backupEnabled = prefs[Keys.BACKUP_ENABLED] ?: false,
            lastBackupAt = prefs[Keys.LAST_BACKUP_AT],
            backupKeep = prefs[Keys.BACKUP_KEEP] ?: 7,
            onboardingDone = prefs[Keys.ONBOARDING_DONE] ?: false,
            lastUsedAccountId = prefs[Keys.LAST_USED_ACCOUNT_ID],
            lastUsedCategoryId = prefs[Keys.LAST_USED_CATEGORY_ID]
        )
    }

    suspend fun updateSalaryCycleStartDay(day: Int) {
        context.settingsDataStore.edit { it[Keys.SALARY_CYCLE_START_DAY] = day }
    }

    suspend fun updatePin(hash: String?, salt: String?) {
        context.settingsDataStore.edit { prefs ->
            if (hash != null) prefs[Keys.PIN_HASH] = hash else prefs.remove(Keys.PIN_HASH)
            if (salt != null) prefs[Keys.PIN_SALT] = salt else prefs.remove(Keys.PIN_SALT)
        }
    }

    suspend fun updateBiometricEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun updateAutoLockSeconds(seconds: Int) {
        context.settingsDataStore.edit { it[Keys.AUTO_LOCK_SECONDS] = seconds }
    }

    suspend fun updatePrivacyModeDefault(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.PRIVACY_MODE_DEFAULT] = enabled }
    }

    suspend fun updateReminderHour(hour: Int) {
        context.settingsDataStore.edit { it[Keys.REMINDER_HOUR] = hour }
    }

    suspend fun updateDefaultReminderDays(days: Int) {
        context.settingsDataStore.edit { it[Keys.DEFAULT_REMINDER_DAYS] = days }
    }

    suspend fun updateSmsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.SMS_ENABLED] = enabled }
    }

    suspend fun updateBackupEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.BACKUP_ENABLED] = enabled }
    }

    suspend fun updateLastBackupAt(timestamp: Long?) {
        context.settingsDataStore.edit { prefs ->
            if (timestamp != null) prefs[Keys.LAST_BACKUP_AT] = timestamp else prefs.remove(Keys.LAST_BACKUP_AT)
        }
    }

    suspend fun updateBackupKeep(keepCount: Int) {
        context.settingsDataStore.edit { it[Keys.BACKUP_KEEP] = keepCount }
    }

    suspend fun updateOnboardingDone(done: Boolean) {
        context.settingsDataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun updateLastUsedAccount(accountId: String?) {
        context.settingsDataStore.edit { prefs ->
            if (accountId != null) prefs[Keys.LAST_USED_ACCOUNT_ID] = accountId else prefs.remove(Keys.LAST_USED_ACCOUNT_ID)
        }
    }

    suspend fun updateLastUsedCategory(categoryId: String?) {
        context.settingsDataStore.edit { prefs ->
            if (categoryId != null) prefs[Keys.LAST_USED_CATEGORY_ID] = categoryId else prefs.remove(Keys.LAST_USED_CATEGORY_ID)
        }
    }
}
