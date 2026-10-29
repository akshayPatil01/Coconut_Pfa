package com.patil.coconut_pfa.data.local.security

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object LockManager {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _wrongAttempts = MutableStateFlow(0)
    val wrongAttempts: StateFlow<Int> = _wrongAttempts.asStateFlow()

    private val _lockoutRemainingSeconds = MutableStateFlow(0)
    val lockoutRemainingSeconds: StateFlow<Int> = _lockoutRemainingSeconds.asStateFlow()

    private var lockoutJob: Job? = null
    private var lastBackgroundTimeMillis: Long = 0L

    fun lock() {
        _isLocked.value = true
    }

    fun unlock() {
        _isLocked.value = false
        _wrongAttempts.value = 0
        _lockoutRemainingSeconds.value = 0
        lockoutJob?.cancel()
    }

    fun onAppBackgrounded() {
        lastBackgroundTimeMillis = System.currentTimeMillis()
    }

    fun onAppForegrounded(autoLockSeconds: Int, isOnboardingDone: Boolean, hasPin: Boolean) {
        if (!isOnboardingDone || !hasPin) return
        if (_isLocked.value) return

        if (lastBackgroundTimeMillis > 0L) {
            val elapsedSeconds = (System.currentTimeMillis() - lastBackgroundTimeMillis) / 1000
            if (elapsedSeconds >= autoLockSeconds) {
                _isLocked.value = true
            }
        }
    }

    fun recordWrongAttempt(): Int {
        val attempts = _wrongAttempts.value + 1
        _wrongAttempts.value = attempts

        if (attempts >= 5) {
            val exponent = (attempts - 5).coerceAtMost(10)
            val waitSeconds = 30 * (1 shl exponent)
            startLockoutTimer(waitSeconds)
        }
        return attempts
    }

    fun calculateWaitSeconds(attempts: Int): Int {
        if (attempts < 5) return 0
        val exponent = (attempts - 5).coerceAtMost(10)
        return 30 * (1 shl exponent)
    }

    private fun startLockoutTimer(seconds: Int) {
        lockoutJob?.cancel()
        _lockoutRemainingSeconds.value = seconds
        lockoutJob = scope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _lockoutRemainingSeconds.value = remaining
            }
        }
    }
}
