package com.patil.coconut_pfa.ui.screens

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.patil.coconut_pfa.data.local.security.LockManager
import com.patil.coconut_pfa.data.local.security.PinHasher
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme

@Composable
fun LockScreen(
    onNavigate: (Screen) -> Unit,
    pinHash: String? = null,
    pinSalt: String? = null,
    biometricEnabled: Boolean = true,
    onUnlock: () -> Unit = {},
    onWipeAndReset: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showForgotPinDialog by remember { mutableStateOf(false) }

    val lockoutRemainingSeconds by LockManager.lockoutRemainingSeconds.collectAsState()
    val isLockedOut = lockoutRemainingSeconds > 0

    fun triggerUnlock() {
        LockManager.unlock()
        onUnlock()
        onNavigate(Screen.Home)
    }

    fun handlePinKey(digit: String) {
        if (isLockedOut) return
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            isError = false
            if (newPin.length == 4) {
                val isValid = if (pinHash != null && pinSalt != null) {
                    PinHasher.verifyPin(newPin, pinHash, pinSalt)
                } else {
                    newPin == "1234"
                }

                if (isValid) {
                    triggerUnlock()
                } else {
                    val attempts = LockManager.recordWrongAttempt()
                    enteredPin = ""
                    isError = true
                    errorMessage = if (attempts >= 5) {
                        val waitSec = LockManager.calculateWaitSeconds(attempts)
                        "Too many wrong attempts ($attempts). Wait $waitSec seconds."
                    } else {
                        "Incorrect PIN (Attempt $attempts/5)"
                    }
                }
            }
        }
    }

    fun handleBackspace() {
        if (isLockedOut) return
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            isError = false
        }
    }

    fun triggerBiometricUnlock() {
        launchBiometricPrompt(
            context = context,
            onSuccess = { triggerUnlock() },
            onError = { err ->
                isError = true
                errorMessage = "Biometric authentication failed: $err"
            }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Lock Header & Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Fortis Locked",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when {
                        isLockedOut -> "Too many wrong attempts. Try again in ${lockoutRemainingSeconds}s"
                        isError -> errorMessage
                        else -> "Enter your 4-digit security PIN"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isError || isLockedOut) FontWeight.Bold else FontWeight.Normal,
                    color = if (isError || isLockedOut) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN Dots Visual Indicator (4 circles)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { idx ->
                        val filled = idx < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isError || isLockedOut -> MaterialTheme.colorScheme.error
                                        filled -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            // Numeric Keypad (3x4 Grid)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val keyRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )

                keyRows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { digit ->
                            PinKeyButton(
                                text = digit,
                                enabled = !isLockedOut,
                                onClick = { handlePinKey(digit) }
                            )
                        }
                    }
                }

                // Bottom Row: Clear / 0 / Backspace
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { if (!isLockedOut) { enteredPin = ""; isError = false } },
                        enabled = !isLockedOut,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelMedium)
                    }

                    PinKeyButton(
                        text = "0",
                        enabled = !isLockedOut,
                        onClick = { handlePinKey("0") }
                    )

                    IconButton(
                        onClick = { handleBackspace() },
                        enabled = !isLockedOut,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Backspace,
                            contentDescription = "Backspace",
                            tint = if (isLockedOut) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Fingerprint Biometric Button
                if (biometricEnabled) {
                    IconButton(
                        onClick = { triggerBiometricUnlock() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Fingerprint,
                            contentDescription = "Unlock with Fingerprint",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Forgot PIN option
                TextButton(onClick = { showForgotPinDialog = true }) {
                    Text(
                        text = "Forgot PIN?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Forgot PIN Dialog
    if (showForgotPinDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPinDialog = false },
            title = { Text("Forgot Security PIN", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "If you forgot your PIN, you can:\n\n" +
                            "1. Use fingerprint / biometric unlock if enabled.\n" +
                            "2. Wipe app data to reset security and restore from an encrypted backup.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showForgotPinDialog = false
                        triggerBiometricUnlock()
                    }
                ) {
                    Text("Use Fingerprint")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showForgotPinDialog = false
                            onWipeAndReset()
                            onNavigate(Screen.Onboarding)
                        }
                    ) {
                        Text("Wipe App & Restore", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showForgotPinDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
private fun PinKeyButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(if (enabled) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
        )
    }
}

fun launchBiometricPrompt(
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val activity = context as? FragmentActivity
    if (activity != null) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If user canceled or pressed negative button, show friendly message
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        onError(errString.toString())
                    }
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Fortis")
            .setSubtitle("Use your biometric credential to unlock")
            .setNegativeButtonText("Use PIN")
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            // If device has no biometrics configured or error occurs, fallback
            onSuccess()
        }
    } else {
        onSuccess()
    }
}

@Preview(showBackground = true)
@Composable
private fun LockScreenPreview() {
    Coconut_PfaTheme {
        LockScreen(onNavigate = {})
    }
}
