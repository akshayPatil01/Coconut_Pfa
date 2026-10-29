package com.patil.coconut_pfa.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import com.patil.coconut_pfa.ui.components.GradientButton
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import com.patil.coconut_pfa.ui.components.CoconutSnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.abs
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.local.datastore.UserSettingsDataStore
import com.patil.coconut_pfa.data.local.security.PinHasher
import com.patil.coconut_pfa.data.repository.BackupManager
import com.patil.coconut_pfa.data.repository.BackupSummary
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onNavigate: (Screen) -> Unit,
    onCompleteOnboarding: (
        pin: String,
        enableBiometric: Boolean,
        salaryDay: Int,
        addBank: Boolean,
        addCard: Boolean,
        addCash: Boolean,
        addLoan: Boolean,
        addFd: Boolean
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var step by remember { mutableIntStateOf(1) } // Steps 1 to 8

    // Onboarding Form States
    var pin by remember { mutableStateOf("") }
    var enableBiometric by remember { mutableStateOf(true) }
    var salaryDay by remember { mutableIntStateOf(25) }

    var addBank by remember { mutableStateOf(true) }
    var addCard by remember { mutableStateOf(true) }
    var addCash by remember { mutableStateOf(true) }
    var addLoan by remember { mutableStateOf(true) }
    var addFd by remember { mutableStateOf(true) }

    var smsPermissionGranted by remember { mutableStateOf(true) }
    var notifPermissionGranted by remember { mutableStateOf(true) }

    // Restore Dialog States
    var showRestorePasswordDialog by remember { mutableStateOf(false) }
    var selectedRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var restorePasswordInput by remember { mutableStateOf("") }
    var restoreSummary by remember { mutableStateOf<BackupSummary?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showNewPinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedRestoreUri = uri
            restorePasswordInput = ""
            showRestorePasswordDialog = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { CoconutSnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    IconButton(onClick = {
                        if (step == 2 && pin.isNotEmpty()) {
                            pin = pin.dropLast(1)
                        } else {
                            if (step == 2) pin = ""
                            step--
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Text(
                    text = "Step $step of 8",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                TextButton(onClick = {
                    onCompleteOnboarding(pin, enableBiometric, salaryDay, addBank, addCard, addCash, addLoan, addFd)
                    onNavigate(Screen.Home)
                }) {
                    Text("Skip")
                }
            }

            // Step Content Animated
            AnimatedContent(
                targetState = step,
                modifier = Modifier.weight(1f),
                label = "OnboardingStep"
            ) { targetStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (targetStep) {
                        1 -> OnboardingWelcomeStep(onRestoreClick = { restoreLauncher.launch(arrayOf("*/*")) })
                        2 -> OnboardingSetPinStep(pin = pin, onPinChange = { pin = it })
                        3 -> OnboardingFingerprintStep(enabled = enableBiometric, onToggle = { enableBiometric = it })
                        4 -> OnboardingSalaryDayStep(day = salaryDay, onDayChange = { salaryDay = it })
                        5 -> OnboardingInitialAccountsStep(
                            addBank = addBank, onBankChange = { addBank = it },
                            addCard = addCard, onCardChange = { addCard = it },
                            addCash = addCash, onCashChange = { addCash = it },
                            addLoan = addLoan, onLoanChange = { addLoan = it },
                            addFd = addFd, onFdChange = { addFd = it }
                        )
                        6 -> OnboardingCategoriesPreviewStep()
                        7 -> OnboardingPermissionsStep(
                            sms = smsPermissionGranted, onSmsChange = { smsPermissionGranted = it },
                            notif = notifPermissionGranted, onNotifChange = { notifPermissionGranted = it }
                        )
                        8 -> OnboardingDoneStep()
                    }
                }
            }

            // Bottom Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step < 8) {
                    GradientButton(
                        onClick = { step++ },
                        enabled = step != 2 || pin.length == 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = if (step == 1) "Get Started" else "Continue")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null)
                    }
                } else {
                    GradientButton(
                        onClick = {
                            onCompleteOnboarding(pin, enableBiometric, salaryDay, addBank, addCard, addCash, addLoan, addFd)
                            onNavigate(Screen.Home)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Start Managing Finances")
                    }
                }
            }
        }
    }

    // Restore Password Dialog
    if (showRestorePasswordDialog && selectedRestoreUri != null) {
        AlertDialog(
            onDismissRequest = { showRestorePasswordDialog = false },
            title = { Text("Enter Backup Password") },
            text = {
                Column {
                    Text("Enter the password used when creating this backup file.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = restorePasswordInput,
                        onValueChange = { restorePasswordInput = it },
                        label = { Text("Backup Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val summary = BackupManager.inspectBackup(context, selectedRestoreUri!!, restorePasswordInput)
                                restoreSummary = summary
                                showRestorePasswordDialog = false
                                showRestoreConfirmDialog = true
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Incorrect password or invalid backup: ${e.localizedMessage}")
                            }
                        }
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestorePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Restore Confirmation Dialog
    if (showRestoreConfirmDialog && restoreSummary != null) {
        val s = restoreSummary!!
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = { Text("Confirm Data Restore") },
            text = {
                Column {
                    Text("Backup created on: ${java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(s.createdAt))}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Accounts: ${s.accountCount}")
                    Text("• Transactions: ${s.transactionCount}")
                    Text("• Categories: ${s.categoryCount}")
                    Text("• Bills: ${s.billCount}")
                    Text("• Goals: ${s.goalCount}")
                    Text("• Persons/IOUs: ${s.personCount}")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("WARNING: Restoring will replace all current data in your app.", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                BackupManager.restoreBackup(context, selectedRestoreUri!!, restorePasswordInput)
                                showRestoreConfirmDialog = false
                                snackbarHostState.showSnackbar("Data restored successfully!")
                                showNewPinDialog = true
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Restore failed: ${e.localizedMessage}")
                            }
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Replace All Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Set New PIN Dialog after restore
    if (showNewPinDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Set New Security PIN") },
            text = {
                Column {
                    Text("PINs are not included in backups for security. Please set a new 4-digit PIN.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.length <= 4) newPinInput = filtered
                        },
                        label = { Text("4-Digit PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length == 4) {
                            scope.launch {
                                val salt = PinHasher.generateSalt()
                                val hash = PinHasher.hashPin(newPinInput, salt)
                                val saltB64 = PinHasher.saltToBase64(salt)
                                UserSettingsDataStore(context).updatePin(hash, saltB64)
                                showNewPinDialog = false
                                onNavigate(Screen.Home)
                            }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Please enter a valid 4-digit PIN.") }
                        }
                    },
                    enabled = newPinInput.length == 4
                ) {
                    Text("Save PIN & Open")
                }
            }
        )
    }
}

@Composable
private fun OnboardingWelcomeStep(onRestoreClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🥥", style = MaterialTheme.typography.displayMedium)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Welcome to Fortis", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your 100% offline & private Personal Finance Assistant. Zero cloud syncing, total financial sovereignty.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = onRestoreClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Rounded.CloudSync, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restore from backup")
        }
    }
}

@Composable
private fun OnboardingSetPinStep(pin: String, onPinChange: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var textFieldValue by remember(pin) {
        mutableStateOf(TextFieldValue(text = pin, selection = TextRange(pin.length)))
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Set Security PIN",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Choose a 4-digit passcode to lock Fortis",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Invisible BasicTextField capturing numeric keyboard input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                },
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newTfv ->
                    val filtered = newTfv.text.filter { it.isDigit() }
                    if (filtered.length <= 4) {
                        textFieldValue = newTfv.copy(text = filtered, selection = TextRange(filtered.length))
                        onPinChange(filtered)
                    } else {
                        textFieldValue = TextFieldValue(text = pin, selection = TextRange(pin.length))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                cursorBrush = SolidColor(Color.Transparent),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .size(1.dp)
            )

            // Visible 4-Digit Box Cells
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
            ) {
                repeat(4) { index ->
                    val char = pin.getOrNull(index)
                    val isFilled = char != null
                    val isFocused = index == pin.length || (index == 3 && pin.length == 4)

                    Box(
                        modifier = Modifier
                            .size(60.dp, 66.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when {
                                    isFilled -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    isFocused -> MaterialTheme.colorScheme.surfaceContainerHigh
                                    else -> MaterialTheme.colorScheme.surfaceContainerLow
                                }
                            )
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = when {
                                    isFocused -> MaterialTheme.colorScheme.primary
                                    isFilled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isFilled) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        } else {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = if (pin.length == 4) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                contentDescription = null,
                tint = if (pin.length == 4) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (pin.length == 4) "4-digit PIN set" else "Enter exactly 4 digits to proceed",
                style = MaterialTheme.typography.labelMedium,
                color = if (pin.length == 4) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        if (pin.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = {
                    val updated = pin.dropLast(1)
                    onPinChange(updated)
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = "Undo digit",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Undo / Backspace", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun OnboardingFingerprintStep(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Fingerprint,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Biometric Lock",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Use fingerprint / face unlock for quick and secure access",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Enable Fingerprint Unlock",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(checked = enabled, onCheckedChange = onToggle)
            }
        }
    }
}

@Composable
private fun OnboardingSalaryDayStep(day: Int, onDayChange: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Salary Cycle Start Day",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Which day of the month do you receive your income?",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Revolving Wheel Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RevolvingNumberPicker(
                    currentValue = day,
                    range = 1..30,
                    onValueChange = onDayChange
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Cycle resets on Day $day of every month",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RevolvingNumberPicker(
    currentValue: Int,
    range: IntRange = 1..30,
    onValueChange: (Int) -> Unit
) {
    val numbers = remember(range) { range.toList() }
    val initialIndex = (currentValue - range.first).coerceIn(0, numbers.size - 1)

    val itemHeight = 44.dp
    val visibleItemsCount = 5
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState)

    val scope = rememberCoroutineScope()

    val selectedIndex by remember {
        derivedStateOf {
            val firstIndex = lazyListState.firstVisibleItemIndex
            val offset = lazyListState.firstVisibleItemScrollOffset
            val halfItemPx = 22
            if (offset > halfItemPx) (firstIndex + 1).coerceIn(0, numbers.size - 1)
            else firstIndex.coerceIn(0, numbers.size - 1)
        }
    }

    LaunchedEffect(selectedIndex) {
        val selectedNum = numbers[selectedIndex]
        if (selectedNum != currentValue) {
            onValueChange(selectedNum)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight * visibleItemsCount),
        contentAlignment = Alignment.Center
    ) {
        // Center Selection Highlight
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(itemHeight)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp)
                )
        )

        // Revolving Wheel Column
        LazyColumn(
            state = lazyListState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight * 2),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            items(numbers.size) { index ->
                val number = numbers[index]
                val distanceFromCenter = abs(index - selectedIndex)

                val alpha = when (distanceFromCenter) {
                    0 -> 1.0f
                    1 -> 0.5f
                    else -> 0.2f
                }

                val scale = when (distanceFromCenter) {
                    0 -> 1.25f
                    1 -> 0.95f
                    else -> 0.8f
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .graphicsLayer {
                            this.alpha = alpha
                            this.scaleX = scale
                            this.scaleY = scale
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            scope.launch {
                                lazyListState.animateScrollToItem(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Day $number",
                        style = if (distanceFromCenter == 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                        fontWeight = if (distanceFromCenter == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (distanceFromCenter == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingInitialAccountsStep(
    addBank: Boolean, onBankChange: (Boolean) -> Unit,
    addCard: Boolean, onCardChange: (Boolean) -> Unit,
    addCash: Boolean, onCashChange: (Boolean) -> Unit,
    addLoan: Boolean, onLoanChange: (Boolean) -> Unit,
    addFd: Boolean, onFdChange: (Boolean) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = Icons.Rounded.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Add Initial Accounts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = "Select default accounts & details to initialize", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Checkbox(checked = addBank, onCheckedChange = onBankChange)
            Text("Demo Bank Salary (₹2,45,000)")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Checkbox(checked = addCard, onCheckedChange = onCardChange)
            Text("Card A Premier Credit Card (Limit ₹1.5L)")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Checkbox(checked = addCash, onCheckedChange = onCashChange)
            Text("Cash Wallet (₹4,500)")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Checkbox(checked = addLoan, onCheckedChange = onLoanChange)
            Text("Home Loan (₹25L @ 8.5%, EMI ₹21.6k)")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Checkbox(checked = addFd, onCheckedChange = onFdChange)
            Text("Fixed Deposit (₹1L @ 7.2%, Dec 2025)")
        }
    }
}

@Composable
private fun OnboardingCategoriesPreviewStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Category,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Smart Categories",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Pre-configured for Indian personal finance",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        val categoryGroups = remember {
            listOf(
                Triple("🏠", "Housing & Utilities", listOf("Rent", "Maintenance", "Electricity", "Water")),
                Triple("🍽️", "Food & Dining", listOf("Restaurants", "Swiggy", "Zomato", "Groceries")),
                Triple("🛍️", "Shopping & Tech", listOf("Electronics", "Clothing", "Personal Care")),
                Triple("⛽", "Travel & Fuel", listOf("Petrol/Diesel", "Cab", "Metro", "Train")),
                Triple("💡", "Subscriptions", listOf("Mobile", "Broadband", "OTT Streaming")),
                Triple("💰", "Income & Investment", listOf("Salary", "Dividends", "Mutual Funds"))
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categoryGroups.size) { idx ->
                val (emoji, title, tags) = categoryGroups[idx]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tags.joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPermissionsStep(
    sms: Boolean, onSmsChange: (Boolean) -> Unit,
    notif: Boolean, onNotifChange: (Boolean) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = Icons.Rounded.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Permissions Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = "Fortis stays 100% offline", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(20.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SMS Parser Permission")
                    }
                    Switch(checked = sms, onCheckedChange = onSmsChange)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bill Reminders Permission")
                    }
                    Switch(checked = notif, onCheckedChange = onNotifChange)
                }
            }
        }
    }
}

@Composable
private fun OnboardingDoneStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "All Set!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your Fortis workspace is ready. Experience private, offline financial mastery.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    Coconut_PfaTheme {
        OnboardingScreen(onNavigate = {})
    }
}
