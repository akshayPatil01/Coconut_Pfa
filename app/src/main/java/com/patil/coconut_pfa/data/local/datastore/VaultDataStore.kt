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

private val Context.vaultSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "vault_device_settings")

data class VaultDeviceSettings(
    val vaultBioEnabled: Boolean = false,
    val vkByBiometric: String? = null,
    val vaultFailedAttempts: Int = 0,
    val vaultLockedUntil: Long = 0L,
    val vaultBackgroundLockSeconds: Int = 30,
    val clipboardClearSeconds: Int = 30
)

class VaultDataStore(private val context: Context) {

    private object Keys {
        val VAULT_BIO_ENABLED = booleanPreferencesKey("vaultBioEnabled")
        val VK_BY_BIOMETRIC = stringPreferencesKey("vkByBiometric")
        val VAULT_FAILED_ATTEMPTS = intPreferencesKey("vaultFailedAttempts")
        val VAULT_LOCKED_UNTIL = longPreferencesKey("vaultLockedUntil")
        val VAULT_BACKGROUND_LOCK_SECONDS = intPreferencesKey("vaultBackgroundLockSeconds")
        val CLIPBOARD_CLEAR_SECONDS = intPreferencesKey("clipboardClearSeconds")
    }

    val vaultSettingsFlow: Flow<VaultDeviceSettings> = context.vaultSettingsDataStore.data.map { prefs ->
        VaultDeviceSettings(
            vaultBioEnabled = prefs[Keys.VAULT_BIO_ENABLED] ?: false,
            vkByBiometric = prefs[Keys.VK_BY_BIOMETRIC],
            vaultFailedAttempts = prefs[Keys.VAULT_FAILED_ATTEMPTS] ?: 0,
            vaultLockedUntil = prefs[Keys.VAULT_LOCKED_UNTIL] ?: 0L,
            vaultBackgroundLockSeconds = prefs[Keys.VAULT_BACKGROUND_LOCK_SECONDS] ?: 30,
            clipboardClearSeconds = prefs[Keys.CLIPBOARD_CLEAR_SECONDS] ?: 30
        )
    }

    suspend fun setVaultBioEnabled(enabled: Boolean) {
        context.vaultSettingsDataStore.edit { it[Keys.VAULT_BIO_ENABLED] = enabled }
    }

    suspend fun setVkByBiometric(b64Blob: String?) {
        context.vaultSettingsDataStore.edit { prefs ->
            if (b64Blob != null) {
                prefs[Keys.VK_BY_BIOMETRIC] = b64Blob
            } else {
                prefs.remove(Keys.VK_BY_BIOMETRIC)
            }
        }
    }

    suspend fun setFailedAttempts(attempts: Int) {
        context.vaultSettingsDataStore.edit { it[Keys.VAULT_FAILED_ATTEMPTS] = attempts }
    }

    suspend fun setLockedUntil(timestampMillis: Long) {
        context.vaultSettingsDataStore.edit { it[Keys.VAULT_LOCKED_UNTIL] = timestampMillis }
    }

    suspend fun setBackgroundLockSeconds(seconds: Int) {
        context.vaultSettingsDataStore.edit { it[Keys.VAULT_BACKGROUND_LOCK_SECONDS] = seconds }
    }

    suspend fun setClipboardClearSeconds(seconds: Int) {
        context.vaultSettingsDataStore.edit { it[Keys.CLIPBOARD_CLEAR_SECONDS] = seconds }
    }

    suspend fun resetFailedAttemptsAndLockout() {
        context.vaultSettingsDataStore.edit { prefs ->
            prefs[Keys.VAULT_FAILED_ATTEMPTS] = 0
            prefs[Keys.VAULT_LOCKED_UNTIL] = 0L
        }
    }
}
