package com.patil.coconut_pfa.data.local.security

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VaultLockedException(message: String = "Vault is locked") : Exception(message)

sealed interface VaultState {
    data object Locked : VaultState
    data object Unlocked : VaultState
}

object VaultSession {
    private var vk: ByteArray? = null

    private val _state = MutableStateFlow<VaultState>(VaultState.Locked)
    val state: StateFlow<VaultState> = _state.asStateFlow()

    val isLocked: Boolean get() = _state.value is VaultState.Locked
    val isUnlocked: Boolean get() = _state.value is VaultState.Unlocked

    @Synchronized
    fun unlock(unwrappedVk: ByteArray) {
        require(unwrappedVk.size == 32) { "Vault key must be 32 bytes" }
        vk?.fill(0)
        vk = unwrappedVk.clone()
        _state.value = VaultState.Unlocked
    }

    @Synchronized
    fun lock(context: Context? = null) {
        vk?.fill(0)
        vk = null
        _state.value = VaultState.Locked
        context?.let { VaultClipboardManager.clearIfOurs(it) }
    }

    @Synchronized
    fun requireVk(): ByteArray {
        val currentVk = vk
        if (isLocked || currentVk == null) {
            throw VaultLockedException("Vault is locked")
        }
        return currentVk.clone()
    }

    override fun toString(): String = "<redacted>"
}
