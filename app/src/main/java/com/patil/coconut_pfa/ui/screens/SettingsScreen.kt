package com.patil.coconut_pfa.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.common.AccountPicker
import com.patil.coconut_pfa.data.local.datastore.ThemeMode
import com.patil.coconut_pfa.data.local.datastore.ToastStyle
import com.patil.coconut_pfa.data.local.datastore.UserSettings
import com.patil.coconut_pfa.data.local.datastore.UserSettingsDataStore
import com.patil.coconut_pfa.data.local.security.BackupPassphraseManager
import com.patil.coconut_pfa.data.local.security.PinHasher
import com.patil.coconut_pfa.data.repository.BackupManager
import com.patil.coconut_pfa.data.repository.BackupSummary
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.CoconutDropdownMenuItem
import com.patil.coconut_pfa.ui.components.CoconutSnackbarHost
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.util.PrivacyManager
import kotlinx.coroutines.launch

/**
 * Main Settings Sections available in Fortis
 */
enum class SettingsSection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    APPEARANCE(
        title = "Appearance",
        subtitle = "Theme mode, toast style & notification preview",
        icon = Icons.Rounded.LightMode
    ),
    SECURITY(
        title = "Security & Privacy",
        subtitle = "PIN protection, biometrics, auto-lock & privacy mode",
        icon = Icons.Rounded.Security
    ),
    SALARY_CYCLE(
        title = "Salary Cycle Settings",
        subtitle = "Start day of monthly salary cycle",
        icon = Icons.Rounded.Schedule
    ),
    WIDGET(
        title = "Widget",
        subtitle = "Home screen widget visibility settings",
        icon = Icons.Rounded.Info
    ),
    CATEGORIES(
        title = "Categories & Sub-Categories",
        subtitle = "Manage expense & income categories",
        icon = Icons.Rounded.Category
    ),
    SMS_PARSER(
        title = "SMS & Bank Parser",
        subtitle = "Auto-detection, SMS inbox scan & parser test",
        icon = Icons.Rounded.Sms
    ),
    REMINDERS(
        title = "Bill Reminders",
        subtitle = "Daily reminder time & notification days",
        icon = Icons.Rounded.Alarm
    ),
    BACKUP(
        title = "Backup & Recovery",
        subtitle = "Google Drive sync, passphrase & local files",
        icon = Icons.Rounded.CloudSync
    ),
    TOOLS(
        title = "Tools & Data Management",
        subtitle = "Bank statement import, receipt scan & trash",
        icon = Icons.Rounded.Description
    ),
    ABOUT(
        title = "About & Developer Options",
        subtitle = "App info, onboarding replay & debug tools",
        icon = Icons.Rounded.BugReport
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (Screen) -> Unit,
    userSettings: UserSettings = UserSettings(),
    onUpdateBiometricEnabled: (Boolean) -> Unit = {},
    onUpdateAutoLockSeconds: (Int) -> Unit = {},
    onUpdatePrivacyByDefault: (Boolean) -> Unit = {},
    onUpdateSalaryStartDay: (Int) -> Unit = {},
    onUpdateSmsEnabled: (Boolean) -> Unit = {},
    onUpdateReminderHour: (Int) -> Unit = {},
    onUpdateDefaultReminderDays: (Int) -> Unit = {},
    onUpdateShowAmountOnWidget: (Boolean) -> Unit = {},
    onUpdateThemeMode: (ThemeMode) -> Unit = {},
    onUpdateToastStyle: (ToastStyle) -> Unit = {},
    onGenerateFakeTransactions: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isPrivacyEnabled by PrivacyManager.isPrivacyEnabled.collectAsState()

    var activeSection by remember { mutableStateOf<SettingsSection?>(null) }

    BackHandler(enabled = activeSection != null) {
        activeSection = null
    }

    var hasBackupPwd by remember { mutableStateOf(BackupPassphraseManager.hasBackupPassword(context)) }
    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var newBackupPasswordInput by remember { mutableStateOf("") }
    var confirmBackupPasswordInput by remember { mutableStateOf("") }

    val settingsDataStore = remember { UserSettingsDataStore(context) }
    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val email = result.data?.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME)
            if (email != null) {
                scope.launch {
                    settingsDataStore.updateGoogleAccountEmail(email)
                    snackbarHostState.showSnackbar("Connected to Google account: $email")
                }
            }
        }
    }

    var showDriveBackupsDialog by remember { mutableStateOf(false) }
    var driveBackupsList by remember { mutableStateOf<List<com.patil.coconut_pfa.data.repository.DriveBackupSummary>?>(null) }
    var selectedDriveFileId by remember { mutableStateOf<String?>(null) }
    var showDisconnectGoogleDialog by remember { mutableStateOf(false) }
    var isUploadingDrive by remember { mutableStateOf(false) }
    var isLoadingDriveList by remember { mutableStateOf(false) }

    var showRestorePasswordDialog by remember { mutableStateOf(false) }
    var selectedRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var restorePasswordInput by remember { mutableStateOf("") }
    var restoreSummary by remember { mutableStateOf<BackupSummary?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showNewPinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    BackupManager.exportBackup(context, uri)
                    snackbarHostState.showSnackbar("Encrypted backup exported successfully!")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Backup failed: ${e.localizedMessage}")
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedRestoreUri = uri
            restorePasswordInput = ""
            showRestorePasswordDialog = true
        }
    }

    fun secondsToTimeoutLabel(seconds: Int): String = when (seconds) {
        0 -> "Immediately"
        300 -> "After 5 minutes"
        900 -> "After 15 minutes"
        else -> "After 1 minute"
    }

    fun timeoutLabelToSeconds(label: String): Int = when (label) {
        "Immediately" -> 0
        "After 5 minutes" -> 300
        "After 15 minutes" -> 900
        else -> 60
    }

    fun hourToLabel(hour: Int): String = when (hour) {
        8 -> "08:00 AM"
        9 -> "09:00 AM"
        10 -> "10:00 AM"
        12 -> "12:00 PM"
        18 -> "06:00 PM"
        20 -> "08:00 PM"
        21 -> "09:00 PM"
        else -> String.format(java.util.Locale.US, "%02d:00", hour)
    }

    fun labelToHour(label: String): Int = when (label) {
        "08:00 AM" -> 8
        "09:00 AM" -> 9
        "10:00 AM" -> 10
        "12:00 PM" -> 12
        "06:00 PM" -> 18
        "08:00 PM" -> 20
        "09:00 PM" -> 21
        else -> 9
    }

    var pinLockEnabled by remember(userSettings.pinHash) { mutableStateOf(userSettings.pinHash != null) }
    var fingerprintEnabled by remember(userSettings.biometricEnabled) { mutableStateOf(userSettings.biometricEnabled) }
    var autoLockTimeout by remember(userSettings.autoLockSeconds) { mutableStateOf(secondsToTimeoutLabel(userSettings.autoLockSeconds)) }
    var privacyByDefault by remember(userSettings.privacyModeDefault) { mutableStateOf(userSettings.privacyModeDefault) }

    var salaryStartDay by remember(userSettings.salaryCycleStartDay) { mutableIntStateOf(userSettings.salaryCycleStartDay) }
    var smsAutoDetectEnabled by remember(userSettings.smsEnabled) { mutableStateOf(userSettings.smsEnabled) }

    var reminderTimeLabel by remember(userSettings.reminderHour) { mutableStateOf(hourToLabel(userSettings.reminderHour)) }
    var reminderDaysBefore by remember(userSettings.defaultReminderDays) { mutableIntStateOf(userSettings.defaultReminderDays) }

    var showCategoriesDialog by remember { mutableStateOf(false) }
    var showDebugMenu by remember { mutableStateOf(false) }
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    var hasSmsPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showRestrictedSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            scope.launch { snackbarHostState.showSnackbar("Notification permission granted! Bill reminders enabled.") }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Notification permission is required to post bill reminders.") }
        }
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.all { it }
        hasSmsPermissions = granted
        if (granted) {
            smsAutoDetectEnabled = true
            onUpdateSmsEnabled(true)
            scope.launch { snackbarHostState.showSnackbar("SMS permissions granted! Auto-detection enabled.") }
        } else {
            smsAutoDetectEnabled = false
            onUpdateSmsEnabled(false)
            showRestrictedSettingsDialog = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { CoconutSnackbarHost(hostState = snackbarHostState, toastStyle = userSettings.toastStyle) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (activeSection != null) {
                        activeSection = null
                    } else {
                        onNavigate(Screen.Home)
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = if (activeSection != null) "Back to Settings" else "Back to Dashboard"
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = activeSection?.title ?: "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (activeSection == null) {
                        Text(
                            text = "Preferences, security & app options",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = activeSection,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "SettingsSectionTransition"
            ) { section ->
                if (section == null) {
                    // MAIN OPTIONS VIEW (Top-level clean list)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(SettingsSection.entries.size) { index ->
                            val s = SettingsSection.entries[index]
                            val badge = when (s) {
                                SettingsSection.APPEARANCE -> when (userSettings.themeMode) {
                                    ThemeMode.SYSTEM -> "System"
                                    ThemeMode.LIGHT -> "Light"
                                    ThemeMode.DARK -> "Dark"
                                }
                                SettingsSection.SECURITY -> if (pinLockEnabled) "PIN Active" else "Off"
                                SettingsSection.SALARY_CYCLE -> "Day $salaryStartDay"
                                SettingsSection.WIDGET -> if (userSettings.showAmountOnWidget) "Show" else "Hidden"
                                SettingsSection.CATEGORIES -> "Manage"
                                SettingsSection.SMS_PARSER -> if (smsAutoDetectEnabled) "Auto ON" else "Off"
                                SettingsSection.REMINDERS -> reminderTimeLabel
                                SettingsSection.BACKUP -> if (userSettings.googleAccountEmail != null) "Drive Linked" else "Off"
                                SettingsSection.TOOLS -> "3 Tools"
                                SettingsSection.ABOUT -> "v1.0.0"
                            }

                            MainSettingOptionCard(
                                section = s,
                                badgeText = badge,
                                onClick = { activeSection = s }
                            )
                        }
                    }
                } else {
                    // SUB-OPTIONS VIEW for the selected section
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (section) {
                            SettingsSection.APPEARANCE -> {
                                item {
                                    SettingsCategoryCard(title = "Appearance & Customization", icon = Icons.Rounded.LightMode) {
                                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                            Text(
                                                text = "Theme Mode",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                ThemeOptionChip(
                                                    text = "System",
                                                    selected = userSettings.themeMode == ThemeMode.SYSTEM,
                                                    onClick = { onUpdateThemeMode(ThemeMode.SYSTEM) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                ThemeOptionChip(
                                                    text = "Light",
                                                    selected = userSettings.themeMode == ThemeMode.LIGHT,
                                                    onClick = { onUpdateThemeMode(ThemeMode.LIGHT) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                ThemeOptionChip(
                                                    text = "Dark",
                                                    selected = userSettings.themeMode == ThemeMode.DARK,
                                                    onClick = { onUpdateThemeMode(ThemeMode.DARK) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "In-App Toast Style",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = userSettings.toastStyle.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                ThemeOptionChip(
                                                    text = "🍊 Orange Adaptive (Recommended)",
                                                    selected = userSettings.toastStyle == ToastStyle.ORANGE_ADAPTIVE,
                                                    onClick = { onUpdateToastStyle(ToastStyle.ORANGE_ADAPTIVE) },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    ThemeOptionChip(
                                                        text = "🌑 Dark Slate",
                                                        selected = userSettings.toastStyle == ToastStyle.DARK_SLATE_ORANGE,
                                                        onClick = { onUpdateToastStyle(ToastStyle.DARK_SLATE_ORANGE) },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    ThemeOptionChip(
                                                        text = "📙 Soft Orange",
                                                        selected = userSettings.toastStyle == ToastStyle.SOFT_ORANGE_TINT,
                                                        onClick = { onUpdateToastStyle(ToastStyle.SOFT_ORANGE_TINT) },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    ThemeOptionChip(
                                                        text = "👑 Deep Royal",
                                                        selected = userSettings.toastStyle == ToastStyle.DEEP_ROYAL,
                                                        onClick = { onUpdateToastStyle(ToastStyle.DEEP_ROYAL) },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    ThemeOptionChip(
                                                        text = "◽ Crisp Bordered",
                                                        selected = userSettings.toastStyle == ToastStyle.CRISP_BORDERED,
                                                        onClick = { onUpdateToastStyle(ToastStyle.CRISP_BORDERED) },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Export Complete\noctober-report.pdf · 2.4 MB",
                                                            actionLabel = "Open"
                                                        )
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.NotificationsActive,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Generate Test Notification",
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            SettingsSection.SECURITY -> {
                                item {
                                    SettingsCategoryCard(title = "Security & Privacy", icon = Icons.Rounded.Security) {
                                        SettingsSwitchRow(
                                            title = "PIN Protection",
                                            subtitle = "Require 4-digit PIN to open app",
                                            checked = pinLockEnabled,
                                            onCheckedChange = { checked -> pinLockEnabled = checked }
                                        )
                                        SettingsSwitchRow(
                                            title = "Fingerprint / Biometric",
                                            subtitle = "Use device biometric unlock",
                                            checked = fingerprintEnabled,
                                            onCheckedChange = { checked ->
                                                fingerprintEnabled = checked
                                                onUpdateBiometricEnabled(checked)
                                            }
                                        )
                                        SettingsDropdownRow(
                                            title = "Auto-lock Timeout",
                                            currentValue = autoLockTimeout,
                                            options = listOf("Immediately", "After 1 minute", "After 5 minutes", "After 15 minutes"),
                                            onOptionSelected = { selected ->
                                                autoLockTimeout = selected
                                                onUpdateAutoLockSeconds(timeoutLabelToSeconds(selected))
                                            }
                                        )
                                        SettingsSwitchRow(
                                            title = "Privacy Mode (Mask Balances)",
                                            subtitle = "Hide sensitive numbers on screen",
                                            checked = isPrivacyEnabled,
                                            onCheckedChange = { PrivacyManager.togglePrivacy() }
                                        )
                                        SettingsSwitchRow(
                                            title = "Privacy Mode by Default",
                                            subtitle = "Always launch app in privacy mode",
                                            checked = privacyByDefault,
                                            onCheckedChange = { checked ->
                                                privacyByDefault = checked
                                                onUpdatePrivacyByDefault(checked)
                                            }
                                        )
                                    }
                                }
                            }

                            SettingsSection.SALARY_CYCLE -> {
                                item {
                                    SettingsCategoryCard(title = "Salary Cycle Settings", icon = Icons.Rounded.Schedule) {
                                        SettingsDropdownRow(
                                            title = "Salary Cycle Start Day",
                                            currentValue = "Day $salaryStartDay of the month",
                                            options = (1..31).map { "Day $it of the month" },
                                            onOptionSelected = { selected ->
                                                val day = selected.replace("Day ", "").replace(" of the month", "").toIntOrNull() ?: 25
                                                salaryStartDay = day
                                                onUpdateSalaryStartDay(day)
                                            }
                                        )
                                    }
                                }
                            }

                            SettingsSection.WIDGET -> {
                                item {
                                    SettingsCategoryCard(title = "Widget Settings", icon = Icons.Rounded.Info) {
                                        SettingsSwitchRow(
                                            title = "Show amount on widget",
                                            subtitle = "Warning: This amount will be visible on your home screen without unlocking the app.",
                                            checked = userSettings.showAmountOnWidget,
                                            onCheckedChange = { checked ->
                                                onUpdateShowAmountOnWidget(checked)
                                                scope.launch {
                                                    com.patil.coconut_pfa.widget.CoconutWidget.updateAll(context)
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            SettingsSection.CATEGORIES -> {
                                item {
                                    SettingsCategoryCard(title = "Categories & Sub-Categories", icon = Icons.Rounded.Category) {
                                        SettingsNavigationRow(
                                            title = "Manage Categories",
                                            subtitle = "Dining, Housing, Shopping, Utilities, Salary",
                                            onClick = { showCategoriesDialog = true }
                                        )
                                    }
                                }
                            }

                            SettingsSection.SMS_PARSER -> {
                                item {
                                    SettingsCategoryCard(title = "SMS & Bank Parser", icon = Icons.Rounded.Sms) {
                                        SettingsSwitchRow(
                                            title = "Enable SMS Auto-Detection",
                                            subtitle = "Read bank & card SMS alerts automatically",
                                            checked = smsAutoDetectEnabled,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    smsPermissionLauncher.launch(
                                                        arrayOf(
                                                            Manifest.permission.RECEIVE_SMS,
                                                            Manifest.permission.READ_SMS
                                                        )
                                                    )
                                                } else {
                                                    smsAutoDetectEnabled = false
                                                    onUpdateSmsEnabled(false)
                                                }
                                            }
                                        )
                                        SettingsNavigationRow(
                                            title = "Scan Last 30 Days Inbox",
                                            subtitle = "One-time scan of past SMS transactions",
                                            onClick = {
                                                if (hasSmsPermissions) {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Scanning last 30 days of SMS inbox...")
                                                        com.patil.coconut_pfa.worker.SmsCatchupWorker.scanInbox(context, force30Days = true)
                                                        snackbarHostState.showSnackbar("Scan complete! Check SMS Review Inbox.")
                                                    }
                                                } else {
                                                    smsPermissionLauncher.launch(
                                                        arrayOf(
                                                            Manifest.permission.RECEIVE_SMS,
                                                            Manifest.permission.READ_SMS
                                                        )
                                                    )
                                                }
                                            }
                                        )
                                        SettingsNavigationRow(
                                            title = "SMS Parser Test Screen",
                                            subtitle = "Paste raw SMS text to test live parsing logic",
                                            onClick = { onNavigate(Screen.ParserTest) }
                                        )
                                    }
                                }
                            }

                            SettingsSection.REMINDERS -> {
                                item {
                                    SettingsCategoryCard(title = "Bill Reminders", icon = Icons.Rounded.Alarm) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Notifications,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.error,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "Notification Permission Needed",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Fortis requires notification permission to alert you about bills due in N days, due today, and overdue payments.\n\nPrivacy Note: Payment amounts are NEVER included in notifications.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Button(
                                                        onClick = { showPermissionRationaleDialog = true },
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("Grant Notification Permission")
                                                    }
                                                }
                                            }
                                        }

                                        SettingsDropdownRow(
                                            title = "Daily Reminder Time",
                                            currentValue = reminderTimeLabel,
                                            options = listOf("08:00 AM", "09:00 AM", "10:00 AM", "12:00 PM", "06:00 PM", "08:00 PM", "09:00 PM"),
                                            onOptionSelected = { selected ->
                                                reminderTimeLabel = selected
                                                val hour = labelToHour(selected)
                                                onUpdateReminderHour(hour)
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                                    showPermissionRationaleDialog = true
                                                }
                                            }
                                        )
                                        SettingsDropdownRow(
                                            title = "Default Days Before Due",
                                            currentValue = "$reminderDaysBefore days before",
                                            options = listOf("1 day before", "2 days before", "3 days before", "7 days before", "14 days before"),
                                            onOptionSelected = { selected ->
                                                val days = selected.split(" ").firstOrNull()?.toIntOrNull() ?: 3
                                                reminderDaysBefore = days
                                                onUpdateDefaultReminderDays(days)
                                            }
                                        )
                                    }
                                }
                            }

                            SettingsSection.BACKUP -> {
                                item {
                                    SettingsCategoryCard(title = "Backup & Recovery", icon = Icons.Rounded.CloudSync) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = if (userSettings.googleAccountEmail != null) "Google Account Connected" else "Google Account",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = userSettings.googleAccountEmail ?: "Not connected (Required for Drive backup)",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = if (userSettings.googleAccountEmail != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                                Button(
                                                    onClick = {
                                                        if (userSettings.googleAccountEmail != null) {
                                                            showDisconnectGoogleDialog = true
                                                        } else {
                                                            val intent = AccountPicker.newChooseAccountIntent(
                                                                null, null, arrayOf("com.google"), false, null, null, null, null
                                                            )
                                                            accountPickerLauncher.launch(intent)
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(if (userSettings.googleAccountEmail != null) "Disconnect" else "Connect")
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))
                                            HorizontalDivider()
                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text(
                                                text = if (hasBackupPwd) "Backup Password: Set & Secured in Keystore" else "Backup Password: Not set (required for encrypted backups)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (hasBackupPwd) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                            )
                                            Text(
                                                text = "Last backup: ${if (userSettings.lastBackupAt != null) java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.US).format(java.util.Date(userSettings.lastBackupAt)) else "Never"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                            if (!userSettings.lastBackupStatus.isNullOrBlank()) {
                                                Text(
                                                    text = "Status: ${userSettings.lastBackupStatus}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (userSettings.lastBackupStatus == "Success") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                OutlinedButton(
                                                    onClick = {
                                                        newBackupPasswordInput = ""
                                                        confirmBackupPasswordInput = ""
                                                        showSetPasswordDialog = true
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(if (hasBackupPwd) "Change Password" else "Set Password")
                                                }
                                                Button(
                                                    onClick = {
                                                        if (userSettings.googleAccountEmail.isNullOrBlank()) {
                                                            scope.launch { snackbarHostState.showSnackbar("Please connect your Google account first.") }
                                                        } else if (!hasBackupPwd) {
                                                            showSetPasswordDialog = true
                                                        } else {
                                                            scope.launch {
                                                                isUploadingDrive = true
                                                                try {
                                                                    com.patil.coconut_pfa.data.repository.DriveBackupManager.uploadBackupToDrive(context)
                                                                    snackbarHostState.showSnackbar("Backup uploaded to Google Drive successfully!")
                                                                } catch (e: Exception) {
                                                                    snackbarHostState.showSnackbar("Backup failed: ${e.localizedMessage}")
                                                                } finally {
                                                                    isUploadingDrive = false
                                                                }
                                                            }
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.weight(1f),
                                                    enabled = !isUploadingDrive
                                                ) {
                                                    Text(if (isUploadingDrive) "Backing up..." else "Backup Now")
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    if (userSettings.googleAccountEmail.isNullOrBlank()) {
                                                        scope.launch { snackbarHostState.showSnackbar("Please connect your Google account first.") }
                                                    } else if (!hasBackupPwd) {
                                                        showSetPasswordDialog = true
                                                    } else {
                                                        scope.launch {
                                                            isLoadingDriveList = true
                                                            try {
                                                                val list = com.patil.coconut_pfa.data.repository.DriveBackupManager.listDriveBackups(context)
                                                                driveBackupsList = list
                                                                selectedDriveFileId = null
                                                                selectedRestoreUri = null
                                                                showDriveBackupsDialog = true
                                                            } catch (e: Exception) {
                                                                snackbarHostState.showSnackbar("Failed to list Drive backups: ${e.localizedMessage}")
                                                            } finally {
                                                                isLoadingDriveList = false
                                                            }
                                                        }
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled = !isLoadingDriveList
                                            ) {
                                                Text(if (isLoadingDriveList) "Loading Drive Backups..." else "Restore from Google Drive")
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val fileName = "coconut-${java.text.SimpleDateFormat("yyyyMMdd-HHmm", java.util.Locale.getDefault()).format(java.util.Date())}.cbk"
                                                        exportLauncher.launch(fileName)
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Export File")
                                                }
                                                OutlinedButton(
                                                    onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Restore File")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            SettingsSection.TOOLS -> {
                                item {
                                    SettingsCategoryCard(title = "Tools & Data Management", icon = Icons.Rounded.Description) {
                                        SettingsNavigationRow(
                                            title = "Import Bank Statement",
                                            subtitle = "CSV/Excel column mapping & row review",
                                            icon = Icons.Rounded.Description,
                                            onClick = { onNavigate(Screen.ImportStatement) }
                                        )
                                        SettingsNavigationRow(
                                            title = "Receipt Scan",
                                            subtitle = "Extract amount & merchant from receipt photos",
                                            icon = Icons.Rounded.QrCodeScanner,
                                            onClick = { onNavigate(Screen.ReceiptScan) }
                                        )
                                        SettingsNavigationRow(
                                            title = "Trash & Data Recovery",
                                            subtitle = "Recover deleted transactions & bills",
                                            icon = Icons.Rounded.DeleteSweep,
                                            onClick = { onNavigate(Screen.Trash) }
                                        )
                                    }
                                }
                            }

                            SettingsSection.ABOUT -> {
                                item {
                                    SettingsCategoryCard(title = "About Fortis", icon = Icons.Rounded.Info) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Fortis v1.0.0",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "100% Offline & Private Personal Finance Assistant.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { showDebugMenu = !showDebugMenu },
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.BugReport,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.tertiary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Developer & Debug Menu",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.tertiary
                                                    )
                                                }

                                                Icon(
                                                    imageVector = if (showDebugMenu) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary
                                                )
                                            }

                                            AnimatedVisibility(visible = showDebugMenu) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 12.dp),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = { onNavigate(Screen.Onboarding) },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Text("Replay Onboarding Flow")
                                                    }

                                                    Button(
                                                        onClick = { onNavigate(Screen.LockScreen) },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Show Lock Screen Overlay")
                                                    }

                                                    Button(
                                                        onClick = {
                                                            onGenerateFakeTransactions()
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("Generated 10,000 fake transactions successfully!")
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Icon(imageVector = Icons.Rounded.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Generate 10,000 Fake Transactions")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Permission Rationale Dialog
    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionRationaleDialog = false },
            title = { Text("Enable Bill Reminders", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Fortis uses daily notifications at your set reminder time to alert you about upcoming bills, due today items, and overdue payments.\n\n" +
                            "Privacy Guarantee: For your complete privacy, notification text only displays the bill name and due date — NEVER the amount."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionRationaleDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationaleDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    // Restricted Settings Dialog
    if (showRestrictedSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showRestrictedSettingsDialog = false },
            title = { Text("Allow Restricted Settings", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Android has restricted SMS permissions for this app.\n\n" +
                            "To enable SMS auto-detection:\n" +
                            "1. Open device App Info (Settings → Apps → Fortis).\n" +
                            "2. Tap the three dots (⋮) in the top-right corner.\n" +
                            "3. Select 'Allow restricted settings'.\n" +
                            "4. Return here and enable SMS Auto-Detection."
                )
            },
            confirmButton = {
                Button(onClick = { showRestrictedSettingsDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }

    // Categories Dialog
    if (showCategoriesDialog) {
        CategoriesDialog(onDismiss = { showCategoriesDialog = false })
    }

    // Disconnect Google Account Dialog
    if (showDisconnectGoogleDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectGoogleDialog = false },
            title = { Text("Disconnect Google Account") },
            text = { Text("Are you sure you want to disconnect your Google account? Automatic daily backups to Drive will stop.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            settingsDataStore.updateGoogleAccountEmail(null)
                            showDisconnectGoogleDialog = false
                            snackbarHostState.showSnackbar("Google account disconnected.")
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Disconnect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectGoogleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Google Drive Backups List Dialog
    if (showDriveBackupsDialog && driveBackupsList != null) {
        AlertDialog(
            onDismissRequest = { showDriveBackupsDialog = false },
            title = { Text("Google Drive Backups") },
            text = {
                Column {
                    if (driveBackupsList!!.isEmpty()) {
                        Text("No backups found in Google Drive app folder.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("Select a backup to restore:", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.height(220.dp)) {
                            items(driveBackupsList!!.size) { index ->
                                val backup = driveBackupsList!![index]
                                val sizeKb = backup.size / 1024
                                val dateStr = try {
                                    val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                                    parser.timeZone = java.util.TimeZone.getTimeZone("UTC")
                                    val date = parser.parse(backup.createdTime) ?: java.util.Date(backup.createdTime.toLongOrNull() ?: System.currentTimeMillis())
                                    java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.US).format(date)
                                } catch (_: Exception) {
                                    backup.name
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            selectedDriveFileId = backup.id
                                            selectedRestoreUri = null
                                            showDriveBackupsDialog = false
                                            restorePasswordInput = ""
                                            showRestorePasswordDialog = true
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = dateStr, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "Size: $sizeKb KB • File: ${backup.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDriveBackupsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Set Backup Password Dialog
    if (showSetPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showSetPasswordDialog = false },
            title = { Text(if (hasBackupPwd) "Change Backup Password" else "Set Backup Password") },
            text = {
                Column {
                    Text("Enter a strong password to encrypt/decrypt your local backups.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newBackupPasswordInput,
                        onValueChange = { newBackupPasswordInput = it },
                        label = { Text("Backup Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmBackupPasswordInput,
                        onValueChange = { confirmBackupPasswordInput = it },
                        label = { Text("Confirm Password") },
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
                        if (newBackupPasswordInput.isNotEmpty() && newBackupPasswordInput == confirmBackupPasswordInput) {
                            BackupPassphraseManager.setBackupPassword(context, newBackupPasswordInput)
                            hasBackupPwd = true
                            showSetPasswordDialog = false
                            scope.launch { snackbarHostState.showSnackbar("Backup password saved successfully!") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Passwords do not match or are empty.") }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Restore Password Dialog
    if (showRestorePasswordDialog && (selectedRestoreUri != null || selectedDriveFileId != null)) {
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
                                if (selectedDriveFileId != null) {
                                    val tempFile = java.io.File.createTempFile("coconut-inspect", ".cbk", context.cacheDir)
                                    try {
                                        val email = userSettings.googleAccountEmail!!
                                        val token = com.patil.coconut_pfa.data.repository.DriveBackupManager.getAccessToken(context, email)
                                        val client = okhttp3.OkHttpClient()
                                        val request = okhttp3.Request.Builder()
                                            .url("https://www.googleapis.com/drive/v3/files/$selectedDriveFileId?alt=media")
                                            .header("Authorization", "Bearer $token")
                                            .build()
                                        client.newCall(request).execute().use { response ->
                                            if (!response.isSuccessful) throw RuntimeException("Download failed (${response.code})")
                                            response.body.byteStream().use { input ->
                                                java.io.FileOutputStream(tempFile).use { output ->
                                                    input.copyTo(output)
                                                }
                                            }
                                        }
                                        val uri = Uri.fromFile(tempFile)
                                        val summary = BackupManager.inspectBackup(context, uri, restorePasswordInput)
                                        restoreSummary = summary
                                        showRestorePasswordDialog = false
                                        showRestoreConfirmDialog = true
                                    } finally {
                                        if (tempFile.exists()) tempFile.delete()
                                    }
                                } else if (selectedRestoreUri != null) {
                                    val summary = BackupManager.inspectBackup(context, selectedRestoreUri!!, restorePasswordInput)
                                    restoreSummary = summary
                                    showRestorePasswordDialog = false
                                    showRestoreConfirmDialog = true
                                }
                            } catch (e: Exception) {
                                val msg = e.localizedMessage ?: "Unknown error"
                                snackbarHostState.showSnackbar("Incorrect password or invalid backup: $msg")
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
                    Text("Backup created on: ${java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.US).format(java.util.Date(s.createdAt))}", fontWeight = FontWeight.Bold)
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
                                if (selectedDriveFileId != null) {
                                    com.patil.coconut_pfa.data.repository.DriveBackupManager.downloadAndRestoreBackup(context, selectedDriveFileId!!, restorePasswordInput)
                                } else if (selectedRestoreUri != null) {
                                    BackupManager.restoreBackup(context, selectedRestoreUri!!, restorePasswordInput)
                                }
                                showRestoreConfirmDialog = false
                                selectedDriveFileId = null
                                selectedRestoreUri = null
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
                                snackbarHostState.showSnackbar("New PIN set successfully!")
                            }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Please enter a valid 4-digit PIN.") }
                        }
                    },
                    enabled = newPinInput.length == 4
                ) {
                    Text("Save PIN")
                }
            }
        )
    }
}

@Composable
private fun MainSettingOptionCard(
    section: SettingsSection,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = section.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!badgeText.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun SettingsCategoryCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdownRow(
    title: String,
    currentValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = currentValue, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                options.forEach { opt ->
                    CoconutDropdownMenuItem(
                        text = opt,
                        isSelected = opt == currentValue,
                        onClick = {
                            onOptionSelected(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            icon?.let {
                Icon(imageVector = it, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun CategoriesDialog(onDismiss: () -> Unit) {
    val categoriesMap = mapOf(
        "Dining" to listOf("Restaurants", "Fast Food", "Coffee & Tea", "Delivery"),
        "Housing" to listOf("Rent", "Utilities", "Maintenance", "Furniture"),
        "Shopping" to listOf("Clothing", "Electronics", "Online Stores"),
        "Transportation" to listOf("Fuel", "Cab / Uber", "Public Transport", "Vehicle Repair")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Categories & Sub-Categories", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categoriesMap.forEach { (cat, subList) ->
                    Column {
                        Text(text = cat, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = subList.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun ThemeOptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    Coconut_PfaTheme {
        SettingsScreen(onNavigate = {})
    }
}
