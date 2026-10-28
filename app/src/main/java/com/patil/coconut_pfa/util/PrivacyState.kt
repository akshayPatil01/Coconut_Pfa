package com.patil.coconut_pfa.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global state holder for Privacy Mode.
 * When enabled, sensitive financial numbers across the app are masked.
 */
object PrivacyManager {
    private val _isPrivacyEnabled = MutableStateFlow(false)
    val isPrivacyEnabled: StateFlow<Boolean> = _isPrivacyEnabled.asStateFlow()

    fun togglePrivacy() {
        _isPrivacyEnabled.value = !_isPrivacyEnabled.value
    }

    fun setPrivacy(enabled: Boolean) {
        _isPrivacyEnabled.value = enabled
    }

    fun isEnabled(): Boolean = _isPrivacyEnabled.value
}
