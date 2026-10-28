package com.patil.coconut_pfa.data.repository

import com.patil.coconut_pfa.data.local.datastore.UserSettings
import com.patil.coconut_pfa.data.local.datastore.UserSettingsDataStore
import kotlinx.coroutines.flow.Flow

class SettingsRepository(
    private val userSettingsDataStore: UserSettingsDataStore
) {
    val settingsFlow: Flow<UserSettings> = userSettingsDataStore.settingsFlow

    suspend fun setSalaryCycleStartDay(day: Int) = userSettingsDataStore.updateSalaryCycleStartDay(day)
    suspend fun setPin(hash: String?, salt: String?) = userSettingsDataStore.updatePin(hash, salt)
    suspend fun setBiometricEnabled(enabled: Boolean) = userSettingsDataStore.updateBiometricEnabled(enabled)
    suspend fun setAutoLockSeconds(seconds: Int) = userSettingsDataStore.updateAutoLockSeconds(seconds)
    suspend fun setPrivacyModeDefault(enabled: Boolean) = userSettingsDataStore.updatePrivacyModeDefault(enabled)
    suspend fun setReminderHour(hour: Int) = userSettingsDataStore.updateReminderHour(hour)
    suspend fun setDefaultReminderDays(days: Int) = userSettingsDataStore.updateDefaultReminderDays(days)
    suspend fun setSmsEnabled(enabled: Boolean) = userSettingsDataStore.updateSmsEnabled(enabled)
    suspend fun setBackupEnabled(enabled: Boolean) = userSettingsDataStore.updateBackupEnabled(enabled)
    suspend fun setLastBackupAt(timestamp: Long?) = userSettingsDataStore.updateLastBackupAt(timestamp)
    suspend fun setBackupKeep(keepCount: Int) = userSettingsDataStore.updateBackupKeep(keepCount)
    suspend fun setOnboardingDone(done: Boolean) = userSettingsDataStore.updateOnboardingDone(done)
}
