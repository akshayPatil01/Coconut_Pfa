package com.patil.coconut_pfa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import javax.crypto.Cipher
import android.content.Context
import com.patil.coconut_pfa.data.local.datastore.VaultDataStore
import com.patil.coconut_pfa.data.local.datastore.VaultDeviceSettings
import com.patil.coconut_pfa.data.local.entity.VaultKeyset
import com.patil.coconut_pfa.data.local.security.BiometricKeyManager
import com.patil.coconut_pfa.data.local.security.VaultSession
import com.patil.coconut_pfa.data.local.security.VaultState
import com.patil.coconut_pfa.data.repository.VaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vault.crypto.KdfParams
import vault.crypto.PasswordKdf
import vault.crypto.RecoveryCode
import vault.crypto.Slot
import vault.crypto.VaultCrypto
import java.security.SecureRandom

enum class VaultScreenState {
    NOT_SETUP,
    LOCKED,
    UNLOCKED
}

enum class VaultSetupStep {
    PASSWORD,
    RECOVERY_CODE,
    BIOMETRIC_PROMPT,
    COMPLETE
}

enum class PasswordStrength {
    WEAK,
    OK,
    STRONG
}

data class VaultUiState(
    val screenState: VaultScreenState = VaultScreenState.LOCKED,
    val setupStep: VaultSetupStep = VaultSetupStep.PASSWORD,
    val isDerivingKey: Boolean = false,
    val recoveryCode: String? = null,
    val recoveryCodeLastGroup: String? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val failedAttempts: Int = 0,
    val lockoutRemainingSeconds: Int = 0,
    val showRecoveryMode: Boolean = false,
    val deviceSettings: VaultDeviceSettings = VaultDeviceSettings()
)

class VaultViewModel(
    private val vaultRepository: VaultRepository,
    private val vaultDataStore: VaultDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private var pendingVk: ByteArray? = null
    private var lockoutJob: Job? = null
    private var inactivityCheckJob: Job? = null
    private var lastTouchTimeMillis: Long = System.currentTimeMillis()

    init {
        observeVaultState()
        observeDeviceSettings()
        startInactivityTimer()
    }

    private fun observeVaultState() {
        viewModelScope.launch {
            vaultRepository.getKeysetFlow().collectLatest { keyset ->
                if (keyset == null) {
                    _uiState.value = _uiState.value.copy(screenState = VaultScreenState.NOT_SETUP)
                } else {
                    val currentSessionState = VaultSession.state.value
                    val screenState = if (currentSessionState is VaultState.Unlocked) {
                        VaultScreenState.UNLOCKED
                    } else {
                        VaultScreenState.LOCKED
                    }
                    _uiState.value = _uiState.value.copy(screenState = screenState)
                }
            }
        }

        viewModelScope.launch {
            VaultSession.state.collectLatest { sessionState ->
                val keyset = vaultRepository.getKeyset()
                if (keyset != null) {
                    val screenState = if (sessionState is VaultState.Unlocked) {
                        VaultScreenState.UNLOCKED
                    } else {
                        VaultScreenState.LOCKED
                    }
                    _uiState.value = _uiState.value.copy(screenState = screenState)
                }
            }
        }
    }

    private fun observeDeviceSettings() {
        viewModelScope.launch {
            vaultDataStore.vaultSettingsFlow.collectLatest { settings ->
                _uiState.value = _uiState.value.copy(
                    deviceSettings = settings,
                    failedAttempts = settings.vaultFailedAttempts
                )
                checkLockoutState(settings.vaultLockedUntil)
            }
        }
    }

    private fun checkLockoutState(lockedUntil: Long) {
        val now = System.currentTimeMillis()
        if (lockedUntil > now) {
            val remainingSecs = ((lockedUntil - now) / 1000).toInt().coerceAtLeast(1)
            startLockoutTimer(remainingSecs)
        } else {
            lockoutJob?.cancel()
            _uiState.value = _uiState.value.copy(lockoutRemainingSeconds = 0)
        }
    }

    private fun startLockoutTimer(seconds: Int) {
        lockoutJob?.cancel()
        _uiState.value = _uiState.value.copy(lockoutRemainingSeconds = seconds)
        lockoutJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _uiState.value = _uiState.value.copy(lockoutRemainingSeconds = remaining)
            }
        }
    }

    fun updateLastTouchTime() {
        lastTouchTimeMillis = System.currentTimeMillis()
    }

    private fun startInactivityTimer() {
        inactivityCheckJob?.cancel()
        inactivityCheckJob = viewModelScope.launch {
            while (true) {
                delay(10_000L) // check every 10s
                if (VaultSession.isUnlocked) {
                    val elapsed = System.currentTimeMillis() - lastTouchTimeMillis
                    if (elapsed >= 120_000L) { // 2 minutes without touches
                        lockVault()
                    }
                }
            }
        }
    }

    fun lockVault(context: Context? = null) {
        VaultSession.lock(context)
        pendingVk?.fill(0)
        pendingVk = null
        _uiState.value = _uiState.value.copy(errorMessage = null, showRecoveryMode = false)
    }

    fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.length < 12) return PasswordStrength.WEAK

        var score = 0
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score >= 3 && (password.length >= 14) -> PasswordStrength.STRONG
            score >= 2 -> PasswordStrength.OK
            else -> PasswordStrength.WEAK
        }
    }

    fun startSetupWithPassword(password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(errorMessage = "Passwords do not match")
            return
        }
        if (password.length < 12) {
            _uiState.value = _uiState.value.copy(errorMessage = "Master password must be at least 12 characters")
            return
        }

        _uiState.value = _uiState.value.copy(isDerivingKey = true, errorMessage = null)

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val random = SecureRandom()

                val masterSalt = ByteArray(16).also { random.nextBytes(it) }
                val recoverySalt = ByteArray(16).also { random.nextBytes(it) }

                val kdfParams = KdfParams(memoryKiB = 65536, iterations = 3, parallelism = 1)
                val masterKey = PasswordKdf.argon2id(password.toCharArray(), masterSalt, kdfParams)

                val generatedRecoveryCode = RecoveryCode.generate()
                val recoveryKey = RecoveryCode.key(generatedRecoveryCode, recoverySalt)

                val vk = VaultCrypto.newKey()
                val vkByMaster = VaultCrypto.wrapKey(masterKey, vk, Slot.MASTER)
                val vkByRecovery = VaultCrypto.wrapKey(recoveryKey, vk, Slot.RECOVERY)

                masterKey.fill(0)
                recoveryKey.fill(0)

                val now = System.currentTimeMillis()
                val keyset = VaultKeyset(
                    id = 1,
                    kdf = "ARGON2ID",
                    salt = masterSalt,
                    memoryKiB = kdfParams.memoryKiB,
                    iterations = kdfParams.iterations,
                    parallelism = kdfParams.parallelism,
                    vkByMaster = vkByMaster,
                    vkByRecovery = vkByRecovery,
                    recoverySalt = recoverySalt,
                    keyVersion = 1,
                    createdAt = now,
                    masterChangedAt = now
                )

                vaultRepository.saveKeyset(keyset)
                pendingVk = vk.clone()

                val lastGroup = generatedRecoveryCode.split("-").last()

                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isDerivingKey = false,
                        setupStep = VaultSetupStep.RECOVERY_CODE,
                        recoveryCode = generatedRecoveryCode,
                        recoveryCodeLastGroup = lastGroup
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isDerivingKey = false,
                        errorMessage = "Failed to setup vault keys: ${e.message}"
                    )
                }
            }
        }
    }

    fun confirmRecoveryCodeWrittenDown() {
        _uiState.value = _uiState.value.copy(setupStep = VaultSetupStep.BIOMETRIC_PROMPT)
    }

    fun completeSetupWithBiometric(cipher: Cipher?) {
        viewModelScope.launch {
            val vk = pendingVk
            if (vk != null) {
                if (cipher != null) {
                    try {
                        val b64Blob = BiometricKeyManager.encryptVkWithCryptoObject(cipher, vk)
                        vaultDataStore.setVkByBiometric(b64Blob)
                        vaultDataStore.setVaultBioEnabled(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    vaultDataStore.setVaultBioEnabled(false)
                }

                VaultSession.unlock(vk)
                vk.fill(0)
                pendingVk = null
            }
            _uiState.value = _uiState.value.copy(
                setupStep = VaultSetupStep.COMPLETE,
                screenState = VaultScreenState.UNLOCKED,
                recoveryCode = null
            )
        }
    }

    fun unlockWithMasterPassword(password: String) {
        if (_uiState.value.lockoutRemainingSeconds > 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Vault is locked out. Please wait.")
            return
        }

        _uiState.value = _uiState.value.copy(isDerivingKey = true, errorMessage = null)

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val keyset = vaultRepository.getKeyset()
                if (keyset == null) {
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(isDerivingKey = false, errorMessage = "Vault is not configured")
                    }
                    return@launch
                }

                val kdfParams = KdfParams(
                    memoryKiB = keyset.memoryKiB,
                    iterations = keyset.iterations,
                    parallelism = keyset.parallelism
                )
                val masterKey = PasswordKdf.argon2id(password.toCharArray(), keyset.salt, kdfParams)

                try {
                    val vk = VaultCrypto.unwrapKey(masterKey, keyset.vkByMaster, Slot.MASTER)
                    masterKey.fill(0)

                    withContext(Dispatchers.Main) {
                        vaultDataStore.resetFailedAttemptsAndLockout()
                        VaultSession.unlock(vk)
                        vk.fill(0)
                        _uiState.value = _uiState.value.copy(
                            isDerivingKey = false,
                            errorMessage = null,
                            screenState = VaultScreenState.UNLOCKED
                        )
                    }
                } catch (e: Exception) {
                    masterKey.fill(0)
                    withContext(Dispatchers.Main) {
                        handleWrongPasswordAttempt()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isDerivingKey = false,
                        errorMessage = "Error during key derivation: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun handleWrongPasswordAttempt() {
        val currentAttempts = _uiState.value.failedAttempts + 1
        vaultDataStore.setFailedAttempts(currentAttempts)

        if (currentAttempts >= 5) {
            val exponent = (currentAttempts - 5).coerceAtMost(10)
            val waitSeconds = 30 * (1 shl exponent)
            val lockedUntil = System.currentTimeMillis() + (waitSeconds * 1000L)
            vaultDataStore.setLockedUntil(lockedUntil)
            startLockoutTimer(waitSeconds)
            _uiState.value = _uiState.value.copy(
                isDerivingKey = false,
                errorMessage = "Too many failed attempts. Try again in $waitSeconds seconds."
            )
        } else {
            val remaining = 5 - currentAttempts
            _uiState.value = _uiState.value.copy(
                isDerivingKey = false,
                errorMessage = "Incorrect master password. $remaining attempts remaining before lockout."
            )
        }
    }

    fun unlockWithBiometricCipher(cipher: Cipher, ciphertext: ByteArray) {
        try {
            val vk = BiometricKeyManager.decryptVkWithCryptoObject(cipher, ciphertext)
            viewModelScope.launch {
                vaultDataStore.resetFailedAttemptsAndLockout()
                VaultSession.unlock(vk)
                vk.fill(0)
                _uiState.value = _uiState.value.copy(
                    errorMessage = null,
                    screenState = VaultScreenState.UNLOCKED
                )
            }
        } catch (e: Exception) {
            onBiometricKeyInvalidated()
        }
    }

    fun onBiometricKeyInvalidated() {
        viewModelScope.launch {
            BiometricKeyManager.deleteKey()
            vaultDataStore.setVkByBiometric(null)
            vaultDataStore.setVaultBioEnabled(false)
            _uiState.value = _uiState.value.copy(
                infoMessage = "Fingerprints on this phone changed. Enter your master password, then turn fingerprint unlock on again."
            )
        }
    }

    fun toggleRecoveryMode(show: Boolean) {
        _uiState.value = _uiState.value.copy(showRecoveryMode = show, errorMessage = null)
    }

    fun resetMasterPasswordWithRecoveryCode(code: String, newPassword: String, confirmPassword: String) {
        if (newPassword != confirmPassword) {
            _uiState.value = _uiState.value.copy(errorMessage = "Passwords do not match")
            return
        }
        if (newPassword.length < 12) {
            _uiState.value = _uiState.value.copy(errorMessage = "Master password must be at least 12 characters")
            return
        }

        _uiState.value = _uiState.value.copy(isDerivingKey = true, errorMessage = null)

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val keyset = vaultRepository.getKeyset()
                if (keyset == null) {
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(isDerivingKey = false, errorMessage = "Vault is not configured")
                    }
                    return@launch
                }

                val recoveryKey = RecoveryCode.key(code.trim(), keyset.recoverySalt)

                val vk = try {
                    VaultCrypto.unwrapKey(recoveryKey, keyset.vkByRecovery, Slot.RECOVERY)
                } catch (e: Exception) {
                    recoveryKey.fill(0)
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(isDerivingKey = false, errorMessage = "Invalid recovery code")
                    }
                    return@launch
                }
                recoveryKey.fill(0)

                val random = SecureRandom()
                val newSalt = ByteArray(16).also { random.nextBytes(it) }
                val kdfParams = KdfParams(memoryKiB = 65536, iterations = 3, parallelism = 1)
                val newMasterKey = PasswordKdf.argon2id(newPassword.toCharArray(), newSalt, kdfParams)

                val newVkByMaster = VaultCrypto.wrapKey(newMasterKey, vk, Slot.MASTER)
                newMasterKey.fill(0)

                val updatedKeyset = keyset.copy(
                    salt = newSalt,
                    memoryKiB = kdfParams.memoryKiB,
                    iterations = kdfParams.iterations,
                    parallelism = kdfParams.parallelism,
                    vkByMaster = newVkByMaster,
                    masterChangedAt = System.currentTimeMillis()
                )

                vaultRepository.updateKeyset(updatedKeyset)

                withContext(Dispatchers.Main) {
                    vaultDataStore.resetFailedAttemptsAndLockout()
                    VaultSession.unlock(vk)
                    vk.fill(0)
                    _uiState.value = _uiState.value.copy(
                        isDerivingKey = false,
                        showRecoveryMode = false,
                        errorMessage = null,
                        infoMessage = "Master password updated successfully.",
                        screenState = VaultScreenState.UNLOCKED
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isDerivingKey = false,
                        errorMessage = "Failed to reset master password: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, infoMessage = null)
    }

    class Factory(
        private val vaultRepository: VaultRepository,
        private val vaultDataStore: VaultDataStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VaultViewModel(vaultRepository, vaultDataStore) as T
        }
    }
}
