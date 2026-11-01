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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Widgets
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    VAULT(
        title = "Vault Settings",
        subtitle = "Master password, fingerprint unlock, key rotation & recovery code",
        icon = Icons.Rounded.Lock
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
    vaultViewModel: com.patil.coconut_pfa.ui.viewmodel.VaultViewModel? = null,
    vaultUiState: com.patil.coconut_pfa.ui.viewmodel.VaultUiState? = null,
    activeSection: SettingsSection? = null,
    onActiveSectionChange: (SettingsSection?) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isPrivacyEnabled by PrivacyManager.isPrivacyEnabled.collectAsState()

    var internalActiveSection by remember { mutableStateOf<SettingsSection?>(null) }
    val currentActiveSection = activeSection ?: internalActiveSection
    val setActiveSection: (SettingsSection?) -> Unit = { section ->
        internalActiveSection = section
        onActiveSectionChange(section)
    }

    BackHandler(enabled = currentActiveSection != null) {
        setActiveSection(null)
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
            // Sub-section Header Bar (shown only when viewing a specific settings section without parent topBar back arrow)
            if (activeSection == null && internalActiveSection != null) {
                val s = internalActiveSection!!
                val tagText = when (s) {
                    SettingsSection.APPEARANCE -> "APPEARANCE"
                    SettingsSection.SECURITY -> "SECURITY"
                    SettingsSection.VAULT -> "VAULT"
                    SettingsSection.SALARY_CYCLE -> "SALARY CYCLE"
                    SettingsSection.WIDGET -> "WIDGET"
                    SettingsSection.CATEGORIES -> "CATEGORIES"
                    SettingsSection.SMS_PARSER -> "SMS PARSER"
                    SettingsSection.REMINDERS -> "REMINDERS"
                    SettingsSection.BACKUP -> "BACKUP"
                    SettingsSection.TOOLS -> "TOOLS"
                    SettingsSection.ABOUT -> "ABOUT"
                }
                val badgeText = when (s) {
                    SettingsSection.APPEARANCE -> "Dynamic"
                    SettingsSection.SECURITY -> "Active"
                    SettingsSection.VAULT -> "Enclave"
                    SettingsSection.SALARY_CYCLE -> "Active"
                    SettingsSection.WIDGET -> "Live"
                    SettingsSection.CATEGORIES -> "Taxonomy"
                    SettingsSection.SMS_PARSER -> "Parser"
                    SettingsSection.REMINDERS -> "Alerts"
                    SettingsSection.BACKUP -> "Cloud"
                    SettingsSection.TOOLS -> "Utility"
                    SettingsSection.ABOUT -> "v1.0.4"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.8f)),
                            shadowElevation = 0.5.dp
                        ) {
                            IconButton(
                                onClick = { setActiveSection(null) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back to Settings",
                                    tint = Color(0xFF334155),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "SETTINGS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "/",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFCBD5E1)
                                )
                                Text(
                                    text = tagText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFD97706),
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = s.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = currentActiveSection,
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
                                SettingsSection.VAULT -> if (com.patil.coconut_pfa.data.local.security.VaultSession.isUnlocked) "Unlocked" else "Locked"
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
                                onClick = { setActiveSection(s) }
                            )
                        }
                    }
                } else {
                    // SUB-OPTIONS VIEW for the selected section
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF8F9FC)),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (section) {
                            SettingsSection.APPEARANCE -> {
                                item {
                                    SubTabBentoBanner(
                                        tagText = "Adaptive Obsidian Engine",
                                        headline = "Real-Time Fluid Theme",
                                        description = "Dynamically mirrors system display schedule with soft OLED blacks and warm ambient accents.",
                                        statusIcon = Icons.Rounded.AutoMode,
                                        statusLabel = "SYNCED",
                                        statusDotColor = Color(0xFFFBBF24),
                                        statusIconTint = Color(0xFFFBBF24)
                                    )
                                }
                                item {
                                    SubTabCardContainer {
                                        // 1. Theme Mode
                                        SubTabSectionHeader(
                                            title = "Theme Mode",
                                            subtitle = "Auto-adapts to your environment",
                                            icon = Icons.Rounded.LightMode
                                        )
                                        SubTabSegmentedSwitch(
                                            options = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK),
                                            selectedOption = userSettings.themeMode,
                                            onOptionSelected = { onUpdateThemeMode(it) },
                                            labelProvider = { mode ->
                                                when (mode) {
                                                    ThemeMode.SYSTEM -> "System"
                                                    ThemeMode.LIGHT -> "Light"
                                                    ThemeMode.DARK -> "Dark"
                                                }
                                            }
                                        )

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        // 2. In-App Toast Style
                                        SubTabSectionHeader(
                                            title = "In-App Toast Style",
                                            subtitle = "Dark Slate in Light Mode / Soft Warm Tint in Dark Mode",
                                            icon = Icons.Rounded.NotificationsActive
                                        )

                                        // Featured Active Toast Style Card (Gold Adaptive)
                                        val isGoldAdaptive = userSettings.toastStyle == ToastStyle.ORANGE_ADAPTIVE
                                        Surface(
                                            onClick = { onUpdateToastStyle(ToastStyle.ORANGE_ADAPTIVE) },
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isGoldAdaptive) Color(0xFFFFFBEB) else Color(0xFFF8FAFC),
                                            border = BorderStroke(
                                                if (isGoldAdaptive) 2.dp else 1.dp,
                                                if (isGoldAdaptive) Color(0xFFF59E0B) else Color(0xFFE2E8F0)
                                            ),
                                            shadowElevation = if (isGoldAdaptive) 0.5.dp else 0.dp,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(text = "✨", fontSize = 14.sp)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(
                                                            text = "Gold Adaptive",
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF0F172A)
                                                        )
                                                        Spacer(modifier = Modifier.height(1.dp))
                                                        Text(
                                                            text = "Recommended for high visibility",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = Color(0xFFD97706)
                                                        )
                                                    }
                                                }

                                                if (isGoldAdaptive) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = Color(0xFFF59E0B),
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.Check,
                                                                contentDescription = "Selected",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Toast Preset Options 2x2 Grid
                                        val presetList = listOf(
                                            Pair(ToastStyle.DARK_SLATE_ORANGE, Pair("Dark Slate", Color(0xFF1E293B))),
                                            Pair(ToastStyle.SOFT_ORANGE_TINT, Pair("Soft Gold", Color(0xFFF59E0B))),
                                            Pair(ToastStyle.DEEP_ROYAL, Pair("Deep Royal", Color(0xFF4F46E5))),
                                            Pair(ToastStyle.CRISP_BORDERED, Pair("Crisp Bordered", Color.White))
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                presetList.take(2).forEach { (style, meta) ->
                                                    val (label, dotColor) = meta
                                                    val isSelected = userSettings.toastStyle == style
                                                    Surface(
                                                        onClick = { onUpdateToastStyle(style) },
                                                        shape = RoundedCornerShape(14.dp),
                                                        color = if (isSelected) Color(0xFFFFFBEB) else Color(0xFFF8FAFC),
                                                        border = BorderStroke(
                                                            if (isSelected) 2.dp else 1.dp,
                                                            if (isSelected) Color(0xFFF59E0B) else Color(0xFFE2E8F0)
                                                        ),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 12.dp, vertical = 11.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(10.dp)
                                                                    .background(dotColor, CircleShape)
                                                                    .then(
                                                                        if (dotColor == Color.White) Modifier.border(1.dp, Color(0xFF64748B), CircleShape) else Modifier
                                                                    )
                                                            )
                                                            Text(
                                                                text = label,
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                                color = Color(0xFF0F172A),
                                                                maxLines = 1,
                                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                presetList.drop(2).take(2).forEach { (style, meta) ->
                                                    val (label, dotColor) = meta
                                                    val isSelected = userSettings.toastStyle == style
                                                    Surface(
                                                        onClick = { onUpdateToastStyle(style) },
                                                        shape = RoundedCornerShape(14.dp),
                                                        color = if (isSelected) Color(0xFFFFFBEB) else Color(0xFFF8FAFC),
                                                        border = BorderStroke(
                                                            if (isSelected) 2.dp else 1.dp,
                                                            if (isSelected) Color(0xFFF59E0B) else Color(0xFFE2E8F0)
                                                        ),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 12.dp, vertical = 11.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(10.dp)
                                                                    .background(dotColor, CircleShape)
                                                                    .then(
                                                                        if (dotColor == Color.White) Modifier.border(1.dp, Color(0xFF64748B), CircleShape) else Modifier
                                                                    )
                                                            )
                                                            Text(
                                                                text = label,
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                                color = Color(0xFF0F172A),
                                                                maxLines = 1,
                                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        // 3. Accent & Tint Palette (Signature Fortis Tokens)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Active Accent Tint",
                                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "Fortis Primary",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }

                                        val accents = listOf(
                                            Pair("Gold", Color(0xFFF59E0B)),
                                            Pair("Mint", Color(0xFF10B981)),
                                            Pair("Butter", Color(0xFFFBBF24)),
                                            Pair("Slate", Color(0xFF334155))
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            accents.forEachIndexed { i, (name, color) ->
                                                val isActive = i == 0 // Gold is active signature
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (isActive) Color(0xFFFFFBEB) else Color(0xFFF8FAFC),
                                                    border = BorderStroke(
                                                        if (isActive) 2.dp else 1.dp,
                                                        if (isActive) Color(0xFFF59E0B) else Color(0xFFE2E8F0)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 10.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .background(color, CircleShape)
                                                                .then(
                                                                    if (isActive) Modifier.border(2.dp, Color.White, CircleShape) else Modifier
                                                                )
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = name,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                                                            color = if (isActive) Color(0xFF0F172A) else Color(0xFF64748B)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        // 4. Interactive Demo Action Button
                                        SubTabBlackButton(
                                            text = "Generate Test Notification",
                                            icon = Icons.Rounded.NotificationsActive,
                                            onClick = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Export Complete\noctober-report.pdf · 2.4 MB",
                                                        actionLabel = "Open"
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            SettingsSection.SECURITY -> {
                                item {
                                    SubTabBentoBanner(
                                        tagText = "Fortis Defense Enclave",
                                        headline = "Hardware & Biometric Shield",
                                        description = "Military-grade sandbox security. Multi-layer PIN hashing, hardware biometric gating, and customizable auto-lock intervals.",
                                        statusIcon = Icons.Rounded.Shield,
                                        statusLabel = if (pinLockEnabled || fingerprintEnabled) "ARMED" else "UNPROTECTED",
                                        statusDotColor = if (pinLockEnabled || fingerprintEnabled) Color(0xFF10B981) else Color(0xFFF59E0B),
                                        statusIconTint = if (pinLockEnabled || fingerprintEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    )
                                }
                                item {
                                    SubTabCardContainer {
                                        SubTabSectionHeader(
                                            title = "Authentication Credentials",
                                            subtitle = "Control who can unlock Fortis on this device",
                                            icon = Icons.Rounded.Security
                                        )
                                        SubTabSwitchRow(
                                            title = "4-Digit PIN Protection",
                                            subtitle = "Require PIN verification whenever app is launched",
                                            checked = pinLockEnabled,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    showNewPinDialog = true
                                                } else {
                                                    scope.launch {
                                                        settingsDataStore.updatePin(null, null)
                                                        pinLockEnabled = false
                                                        snackbarHostState.showSnackbar("PIN protection disabled.")
                                                    }
                                                }
                                            }
                                        )
                                        SubTabSwitchRow(
                                            title = "Fingerprint / Biometric Unlock",
                                            subtitle = "Fast authentication using device biometric sensors",
                                            checked = fingerprintEnabled,
                                            onCheckedChange = { checked ->
                                                fingerprintEnabled = checked
                                                onUpdateBiometricEnabled(checked)
                                            }
                                        )

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        SubTabSectionHeader(
                                            title = "Auto-Lock Policy",
                                            subtitle = "App automatically locks when left unattended in background",
                                            icon = Icons.Rounded.Schedule
                                        )
                                        val lockTimeouts = listOf("Immediately", "After 1 minute", "After 5 minutes", "After 15 minutes")
                                        SubTabSegmentedSwitch(
                                            options = lockTimeouts,
                                            selectedOption = autoLockTimeout,
                                            onOptionSelected = { selected ->
                                                autoLockTimeout = selected
                                                onUpdateAutoLockSeconds(timeoutLabelToSeconds(selected))
                                            },
                                            labelProvider = { opt ->
                                                when (opt) {
                                                    "Immediately" -> "Instant"
                                                    "After 1 minute" -> "1 min"
                                                    "After 5 minutes" -> "5 min"
                                                    "After 15 minutes" -> "15 min"
                                                    else -> opt
                                                }
                                            }
                                        )

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        SubTabSectionHeader(
                                            title = "Screen Privacy & Masking",
                                            subtitle = "Obscure account balances and transaction totals from observers",
                                            icon = Icons.Rounded.Tune
                                        )
                                        SubTabSwitchRow(
                                            title = "Privacy Mode (Mask Figures)",
                                            subtitle = "Replaces numbers with '••••' across all screens",
                                            checked = isPrivacyEnabled,
                                            onCheckedChange = { PrivacyManager.togglePrivacy() }
                                        )
                                        SubTabSwitchRow(
                                            title = "Privacy Mode by Default",
                                            subtitle = "Always boot Fortis in masked privacy mode",
                                            checked = privacyByDefault,
                                            onCheckedChange = { checked ->
                                                privacyByDefault = checked
                                                onUpdatePrivacyByDefault(checked)
                                            }
                                        )

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        SubTabBlackButton(
                                            text = "Lock App Now",
                                            icon = Icons.Rounded.Lock,
                                            onClick = { onNavigate(Screen.LockScreen) }
                                        )
                                    }
                                }
                            }

                            SettingsSection.VAULT -> {
                                item {
                                    VaultSettingsSection(
                                        vaultViewModel = vaultViewModel,
                                        vaultUiState = vaultUiState,
                                        snackbarHostState = snackbarHostState
                                    )
                                }
                            }

                            SettingsSection.SALARY_CYCLE -> {
                                item {
                                    SubTabBentoBanner(
                                        tagText = "Smart Cash Flow Engine",
                                        headline = "Payday & Accounting Cycle",
                                        description = "Align monthly analytics, budget burn rates, and spending summaries directly to the exact day your salary hits your account.",
                                        statusIcon = Icons.Rounded.Schedule,
                                        statusLabel = "DAY $salaryStartDay OF MONTH",
                                        statusDotColor = Color(0xFFF59E0B),
                                        statusIconTint = Color(0xFFF59E0B)
                                    )
                                }
                                item {
                                    SubTabCardContainer {
                                        SubTabSectionHeader(
                                            title = "Salary Cycle Start Day",
                                            subtitle = "Select when your monthly accounting period begins",
                                            icon = Icons.Rounded.Schedule
                                        )

                                        val popularSalaryDays = listOf(1, 25, 28, 30)
                                        SubTabSegmentedSwitch(
                                            options = popularSalaryDays,
                                            selectedOption = if (salaryStartDay in popularSalaryDays) salaryStartDay else 25,
                                            onOptionSelected = { day ->
                                                salaryStartDay = day
                                                onUpdateSalaryStartDay(day)
                                            },
                                            labelProvider = { "Day $it" }
                                        )

                                        SubTabDropdownRow(
                                            title = "Custom Calendar Day",
                                            currentValue = "Day $salaryStartDay of the month",
                                            options = (1..31).map { "Day $it of the month" },
                                            onOptionSelected = { selected ->
                                                val day = selected.replace("Day ", "").replace(" of the month", "").toIntOrNull() ?: 25
                                                salaryStartDay = day
                                                onUpdateSalaryStartDay(day)
                                            }
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = Color(0xFFFFFBEB),
                                            border = BorderStroke(1.dp, Color(0xFFFEF3C7)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Info,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(18.dp).offset(y = 2.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = "How Salary Cycle Works",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF92400E)
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    val endDay = if (salaryStartDay == 1) 31 else salaryStartDay - 1
                                                    Text(
                                                        text = "Budgets and monthly charts will compute expenses from Day $salaryStartDay of the month through Day $endDay of the following month.",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                                        color = Color(0xFFB45309)
                                                    )
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        SubTabBlackButton(
                                            text = "Apply Salary Cycle (Day $salaryStartDay)",
                                            icon = Icons.Rounded.Check,
                                            onClick = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Salary cycle successfully set to Day $salaryStartDay")
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            SettingsSection.WIDGET -> {
                                item {
                                    SubTabBentoBanner(
                                        tagText = "Glanceable Live Engine",
                                        headline = "Android Home Widget",
                                        description = "Instant glance at net worth and quick expense shortcuts right from your device's home screen without opening the app.",
                                        statusIcon = Icons.Rounded.Widgets,
                                        statusLabel = if (userSettings.showAmountOnWidget) "VISIBLE" else "MASKED",
                                        statusDotColor = if (userSettings.showAmountOnWidget) Color(0xFFF59E0B) else Color(0xFF10B981),
                                        statusIconTint = if (userSettings.showAmountOnWidget) Color(0xFFF59E0B) else Color(0xFF10B981)
                                    )
                                }
                                item {
                                    SubTabCardContainer {
                                        SubTabSectionHeader(
                                            title = "Widget Display Privacy",
                                            subtitle = "Manage how sensitive numbers appear on your launcher",
                                            icon = Icons.Rounded.Widgets
                                        )

                                        SubTabSwitchRow(
                                            title = "Show Balance On Widget",
                                            subtitle = "Display total net worth on the home screen widget",
                                            checked = userSettings.showAmountOnWidget,
                                            onCheckedChange = { checked ->
                                                onUpdateShowAmountOnWidget(checked)
                                                scope.launch {
                                                    com.patil.coconut_pfa.widget.CoconutWidget.updateAll(context)
                                                }
                                            }
                                        )

                                        if (userSettings.showAmountOnWidget) {
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = Color(0xFFFFFBEB),
                                                border = BorderStroke(1.dp, Color(0xFFFEF3C7)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(14.dp),
                                                    verticalAlignment = Alignment.Top,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Shield,
                                                        contentDescription = null,
                                                        tint = Color(0xFFD97706),
                                                        modifier = Modifier.size(18.dp).offset(y = 2.dp)
                                                    )
                                                    Column {
                                                        Text(
                                                            text = "Privacy Notice",
                                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF92400E)
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "Your net worth will be visible on your home screen without requiring biometric or PIN unlocking.",
                                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                                            color = Color(0xFFB45309)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        SubTabBlackButton(
                                            text = "Force Sync Widget Now",
                                            icon = Icons.Rounded.Widgets,
                                            onClick = {
                                                scope.launch {
                                                    com.patil.coconut_pfa.widget.CoconutWidget.updateAll(context)
                                                    snackbarHostState.showSnackbar("Home widget refreshed successfully!")
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            SettingsSection.CATEGORIES -> {
                                item {
                                    SubTabBentoBanner(
                                        tagText = "Transaction Taxonomy",
                                        headline = "Category & Sub-Category System",
                                        description = "Organize personal finances with custom categories, budget thresholds, sub-category groupings, and visual color icons.",
                                        statusIcon = Icons.Rounded.Category,
                                        statusLabel = "HIERARCHICAL",
                                        statusDotColor = Color(0xFFF59E0B),
                                        statusIconTint = Color(0xFFF59E0B)
                                    )
                                }
                                item {
                                    SubTabCardContainer {
                                        SubTabSectionHeader(
                                            title = "Taxonomy Overview",
                                            subtitle = "Manage expense and income classifications",
                                            icon = Icons.Rounded.Category
                                        )

                                        val showcaseCategories = listOf(
                                            Pair("🍽️ Dining", "Food & Drinks"),
                                            Pair("🏠 Housing", "Rent & Utilities"),
                                            Pair("🛍️ Shopping", "Retail & Gear"),
                                            Pair("⚡ Utilities", "Bills & Power"),
                                            Pair("💼 Salary", "Income Feed"),
                                            Pair("🚗 Transport", "Fuel & Commute")
                                        )

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            showcaseCategories.chunked(2).forEach { rowPair ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    rowPair.forEach { (name, sub) ->
                                                        Surface(
                                                            shape = RoundedCornerShape(14.dp),
                                                            color = Color(0xFFF8FAFC),
                                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp)) {
                                                                Text(
                                                                    text = name,
                                                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFF0F172A)
                                                                )
                                                                Text(
                                                                    text = sub,
                                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                                                    color = Color(0xFF64748B)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        SubTabNavigationRow(
                                            title = "Open Full Category Editor",
                                            subtitle = "Add, edit, delete and arrange categories & sub-categories",
                                            icon = Icons.Rounded.Tune,
                                            badgeText = "Editor",
                                            onClick = { showCategoriesDialog = true }
                                        )

                                        SubTabBlackButton(
                                            text = "Launch Category Manager",
                                            icon = Icons.Rounded.Category,
                                            onClick = { showCategoriesDialog = true }
                                        )
                                    }
                                }
                            }

                            SettingsSection.SMS_PARSER -> {
                                item {
                                    SubTabBentoBanner(
                                        tagText = "Offline Local Neural Parser",
                                        headline = "Automated Bank Transaction Feed",
                                        description = "100% on-device SMS parsing engine. Real-time extraction of debit/credit alerts from HDFC, SBI, ICICI, Axis and UPI with zero cloud leakage.",
                                        statusIcon = Icons.Rounded.Sms,
                                        statusLabel = if (smsAutoDetectEnabled) "ACTIVE LISTENER" else "STANDBY",
                                        statusDotColor = if (smsAutoDetectEnabled) Color(0xFF10B981) else Color(0xFF94A3B8),
                                        statusIconTint = if (smsAutoDetectEnabled) Color(0xFF10B981) else Color(0xFF94A3B8)
                                    )
                                }
                                item {
                                    SubTabCardContainer {
                                        SubTabSectionHeader(
                                            title = "Real-Time Ingestion",
                                            subtitle = "Listen for new bank and card transactional SMS messages",
                                            icon = Icons.Rounded.Sms
                                        )

                                        SubTabSwitchRow(
                                            title = "SMS Auto-Detection",
                                            subtitle = "Automatically parses incoming bank alerts as they arrive",
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

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        SubTabSectionHeader(
                                            title = "Historical Scan & Sandbox",
                                            subtitle = "Catchup missed alerts and test extraction algorithms",
                                            icon = Icons.Rounded.Tune
                                        )

                                        SubTabNavigationRow(
                                            title = "Scan Last 30 Days Inbox",
                                            subtitle = "Retroactively parse bank messages received in past month",
                                            icon = Icons.Rounded.Schedule,
                                            badgeText = "Batch Ingest",
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

                                        SubTabNavigationRow(
                                            title = "SMS Parser Test Sandbox",
                                            subtitle = "Paste sample bank messages to test token parsing rules",
                                            icon = Icons.Rounded.BugReport,
                                            badgeText = "Diagnostic",
                                            onClick = { onNavigate(Screen.ParserTest) }
                                        )

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        SubTabBlackButton(
                                            text = "Open Parser Test Sandbox",
                                            icon = Icons.Rounded.PlayArrow,
                                            onClick = { onNavigate(Screen.ParserTest) }
                                        )
                                    }
                                }
                            }

                            SettingsSection.REMINDERS -> {
                                item {
                                    SubTabBentoBanner(
                                        tagText = "Scheduled Notification Guard",
                                        headline = "Bill & Due Date Alerts",
                                        description = "Timely offline notifications before due dates. Protect your credit rating with smart reminders that never reveal sensitive monetary amounts.",
                                        statusIcon = Icons.Rounded.Alarm,
                                        statusLabel = if (hasNotificationPermission) "ACTIVE" else "PERMISSION REQ.",
                                        statusDotColor = if (hasNotificationPermission) Color(0xFF10B981) else Color(0xFFEF4444),
                                        statusIconTint = if (hasNotificationPermission) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                }
                                item {
                                    SubTabCardContainer {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = Color(0xFFFEF2F2),
                                                border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Notifications,
                                                            contentDescription = null,
                                                            tint = Color(0xFFEF4444),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "Notification Permission Required",
                                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF991B1B)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Fortis requires permission to post reminders about upcoming bills, due today items, and overdue payments.\n\nPrivacy Guarantee: Payment amounts are NEVER displayed in notifications.",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                                        color = Color(0xFFB91C1C)
                                                    )
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    SubTabBlackButton(
                                                        text = "Grant Notification Permission",
                                                        icon = Icons.Rounded.Notifications,
                                                        onClick = { showPermissionRationaleDialog = true }
                                                    )
                                                }
                                            }
                                            HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)
                                        }

                                        SubTabSectionHeader(
                                            title = "Daily Alert Schedule",
                                            subtitle = "Choose the exact hour when Fortis scans and alerts you",
                                            icon = Icons.Rounded.Alarm
                                        )

                                        val quickReminderTimes = listOf("08:00 AM", "09:00 AM", "12:00 PM", "08:00 PM")
                                        SubTabSegmentedSwitch(
                                            options = quickReminderTimes,
                                            selectedOption = if (reminderTimeLabel in quickReminderTimes) reminderTimeLabel else "09:00 AM",
                                            onOptionSelected = { selected ->
                                                reminderTimeLabel = selected
                                                val hour = labelToHour(selected)
                                                onUpdateReminderHour(hour)
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                                    showPermissionRationaleDialog = true
                                                }
                                            },
                                            labelProvider = { it }
                                        )

                                        SubTabDropdownRow(
                                            title = "Specific Notification Time",
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

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        SubTabSectionHeader(
                                            title = "Advance Warning Lead Time",
                                            subtitle = "How many days ahead to start displaying bill notifications",
                                            icon = Icons.Rounded.Schedule
                                        )

                                        val leadOptions = listOf(1, 2, 3, 7)
                                        SubTabSegmentedSwitch(
                                            options = leadOptions,
                                            selectedOption = if (reminderDaysBefore in leadOptions) reminderDaysBefore else 3,
                                            onOptionSelected = { days ->
                                                reminderDaysBefore = days
                                                onUpdateDefaultReminderDays(days)
                                            },
                                            labelProvider = { "$it ${if (it == 1) "day" else "days"} prior" }
                                        )

                                        SubTabDropdownRow(
                                            title = "Custom Lead Time",
                                            currentValue = "$reminderDaysBefore days before",
                                            options = listOf("1 day before", "2 days before", "3 days before", "7 days before", "14 days before"),
                                            onOptionSelected = { selected ->
                                                val days = selected.split(" ").firstOrNull()?.toIntOrNull() ?: 3
                                                reminderDaysBefore = days
                                                onUpdateDefaultReminderDays(days)
                                            }
                                        )

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        SubTabBlackButton(
                                            text = "Simulate Bill Reminder",
                                            icon = Icons.Rounded.NotificationsActive,
                                            onClick = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Bill reminder alert active at $reminderTimeLabel ($reminderDaysBefore days prior)")
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            SettingsSection.BACKUP -> {
                                item {
                                    SubTabBentoBanner(
                                        tagText = "Encrypted Cloud & Local Vault",
                                        headline = "Zero-Knowledge Data Backup",
                                        description = "AES-256 encrypted database snapshots. Sync securely to your personal Google Drive or export encrypted .cbk files locally.",
                                        statusIcon = Icons.Rounded.CloudSync,
                                        statusLabel = if (userSettings.googleAccountEmail != null) "DRIVE CONNECTED" else "OFFLINE ONLY",
                                        statusDotColor = if (userSettings.googleAccountEmail != null) Color(0xFF10B981) else Color(0xFFF59E0B),
                                        statusIconTint = if (userSettings.googleAccountEmail != null) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    )
                                }
                                item {
                                    SubTabCardContainer {
                                        SubTabSectionHeader(
                                            title = "Google Drive Cloud Vault",
                                            subtitle = "Encrypted backup storage synced to your Google account",
                                            icon = Icons.Rounded.CloudSync
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = if (userSettings.googleAccountEmail != null) "Google Account Connected" else "Google Account",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                    Text(
                                                        text = userSettings.googleAccountEmail ?: "Not connected (Required for Drive backup)",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = if (userSettings.googleAccountEmail != null) Color(0xFF10B981) else Color(0xFF64748B)
                                                    )
                                                }
                                                Surface(
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
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (userSettings.googleAccountEmail != null) Color(0xFFFEF2F2) else Color(0xFF0F172A),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (userSettings.googleAccountEmail != null) Color(0xFFFEE2E2) else Color(0xFF0F172A)
                                                    )
                                                ) {
                                                    Text(
                                                        text = if (userSettings.googleAccountEmail != null) "Disconnect" else "Connect",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (userSettings.googleAccountEmail != null) Color(0xFFEF4444) else Color.White,
                                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (hasBackupPwd) Color(0xFFFFFBEB) else Color(0xFFFEF2F2),
                                            border = BorderStroke(1.dp, if (hasBackupPwd) Color(0xFFFEF3C7) else Color(0xFFFEE2E2)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text(
                                                    text = if (hasBackupPwd) "Backup Password: Set & Secured in Keystore" else "Backup Password: Not set (required for encrypted backups)",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (hasBackupPwd) Color(0xFFD97706) else Color(0xFFEF4444)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Last backup: ${if (userSettings.lastBackupAt != null) java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.US).format(java.util.Date(userSettings.lastBackupAt)) else "Never"}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                    color = Color(0xFF64748B)
                                                )
                                                if (!userSettings.lastBackupStatus.isNullOrBlank()) {
                                                    Text(
                                                        text = "Status: ${userSettings.lastBackupStatus}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                        color = if (userSettings.lastBackupStatus == "Success") Color(0xFF10B981) else Color(0xFFEF4444)
                                                    )
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                            Surface(
                                                onClick = {
                                                    newBackupPasswordInput = ""
                                                    confirmBackupPasswordInput = ""
                                                    showSetPasswordDialog = true
                                                },
                                                shape = RoundedCornerShape(14.dp),
                                                color = Color(0xFFF8FAFC),
                                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(46.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = if (hasBackupPwd) "Change Password" else "Set Password",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                }
                                            }

                                            Surface(
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
                                                shape = RoundedCornerShape(14.dp),
                                                color = Color(0xFF0F172A),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(46.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = if (isUploadingDrive) "Backing up..." else "Backup Now",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        Surface(
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
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(46.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = if (isLoadingDriveList) "Loading Drive Backups..." else "Restore from Google Drive",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF0F172A)
                                                )
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        SubTabSectionHeader(
                                            title = "Local Encrypted File Export",
                                            subtitle = "Save or load encrypted .cbk files directly on device storage",
                                            icon = Icons.Rounded.Description
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                            Surface(
                                                onClick = {
                                                    val fileName = "coconut-${java.text.SimpleDateFormat("yyyyMMdd-HHmm", java.util.Locale.getDefault()).format(java.util.Date())}.cbk"
                                                    exportLauncher.launch(fileName)
                                                },
                                                shape = RoundedCornerShape(14.dp),
                                                color = Color(0xFFF8FAFC),
                                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(46.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "Export File",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                }
                                            }
                                            Surface(
                                                onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                                                shape = RoundedCornerShape(14.dp),
                                                color = Color(0xFFF8FAFC),
                                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(46.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "Restore File",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            SettingsSection.TOOLS -> {
                                item {
                                    SubTabBentoBanner(
                                        tagText = "Data Management Suite",
                                        headline = "Financial Utilities & Ingestion",
                                        description = "High-precision optical receipt scanning, CSV bank statement parsing, and 30-day trash recovery enclave.",
                                        statusIcon = Icons.Rounded.Tune,
                                        statusLabel = "3 TOOLS READY",
                                        statusDotColor = Color(0xFF10B981),
                                        statusIconTint = Color(0xFF10B981)
                                    )
                                }
                                item {
                                    SubTabCardContainer {
                                        SubTabSectionHeader(
                                            title = "Document & Optical Ingestion",
                                            subtitle = "Import bank records and scan merchant receipts",
                                            icon = Icons.Rounded.Description
                                        )

                                        SubTabNavigationRow(
                                            title = "Import Bank Statement",
                                            subtitle = "CSV & Excel column mapper with multi-row validation",
                                            icon = Icons.Rounded.Description,
                                            badgeText = "CSV/XLSX",
                                            onClick = { onNavigate(Screen.ImportStatement) }
                                        )

                                        SubTabNavigationRow(
                                            title = "Receipt Scan (OCR)",
                                            subtitle = "Extract total, vendor & date directly from photo bills",
                                            icon = Icons.Rounded.QrCodeScanner,
                                            badgeText = "Camera OCR",
                                            onClick = { onNavigate(Screen.ReceiptScan) }
                                        )

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        SubTabSectionHeader(
                                            title = "Recycle & Storage",
                                            subtitle = "Recover deleted items or purge permanent records",
                                            icon = Icons.Rounded.DeleteSweep
                                        )

                                        SubTabNavigationRow(
                                            title = "Trash & Data Recovery",
                                            subtitle = "Recover deleted transactions, accounts, and bill reminders",
                                            icon = Icons.Rounded.DeleteSweep,
                                            badgeText = "Recovery",
                                            onClick = { onNavigate(Screen.Trash) }
                                        )
                                    }
                                }
                            }

                            SettingsSection.ABOUT -> {
                                item {
                                    SubTabBentoBanner(
                                        tagText = "Fortis Financial Core",
                                        headline = "Architecture & Privacy Specs",
                                        description = "100% offline, zero trackers, military-grade client-side encryption. Crafted with modern Jetpack Compose & Material 3.",
                                        statusIcon = Icons.Rounded.Info,
                                        statusLabel = "v1.0.0 STABLE",
                                        statusDotColor = Color(0xFF10B981),
                                        statusIconTint = Color(0xFF10B981)
                                    )
                                }
                                item {
                                    SubTabCardContainer {
                                        SubTabSectionHeader(
                                            title = "Fortis v1.0.0",
                                            subtitle = "Offline Personal Finance Assistant",
                                            icon = Icons.Rounded.Info
                                        )

                                        val features = listOf(
                                            Pair("🛡️ 100% Offline & Private", "Your financial records never leave this device. Zero analytics or server sync."),
                                            Pair("🔑 Hardware Keystore Encryption", "Protected with AES-256 GCM backed by Android Hardware Keystore."),
                                            Pair("⚡ Fluid Reactive Architecture", "Built entirely with Jetpack Compose and modern unidirectional data flow.")
                                        )

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            features.forEach { (title, desc) ->
                                                Surface(
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = Color(0xFFF8FAFC),
                                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Text(
                                                            text = title,
                                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF0F172A)
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = desc,
                                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                                            color = Color(0xFF64748B)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { showDebugMenu = !showDebugMenu }
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(Color(0xFFFFFBEB), RoundedCornerShape(10.dp))
                                                        .border(1.dp, Color(0xFFFEF3C7), RoundedCornerShape(10.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.BugReport,
                                                        contentDescription = null,
                                                        tint = Color(0xFFD97706),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = "Developer & Diagnostic Suite",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                    Text(
                                                        text = "Stress testing, workflows & lock overlays",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                            }

                                            Icon(
                                                imageVector = if (showDebugMenu) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                                contentDescription = null,
                                                tint = Color(0xFF64748B)
                                            )
                                        }

                                        AnimatedVisibility(visible = showDebugMenu) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 10.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Surface(
                                                    onClick = { onNavigate(Screen.WelcomeWorkflow) },
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = Color(0xFFF8FAFC),
                                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(48.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = "Replay Welcome Workflow",
                                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = Color(0xFF0F172A)
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    onClick = { onNavigate(Screen.LockScreen) },
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = Color(0xFFF8FAFC),
                                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(48.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxSize(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(imageVector = Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0F172A))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "Show Lock Screen Overlay",
                                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = Color(0xFF0F172A)
                                                        )
                                                    }
                                                }

                                                SubTabBlackButton(
                                                    text = "Generate 10,000 Fake Transactions",
                                                    icon = Icons.Rounded.BugReport,
                                                    onClick = {
                                                        onGenerateFakeTransactions()
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Generated 10,000 fake transactions successfully!")
                                                        }
                                                    }
                                                )
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
private fun SubTabBentoBanner(
    tagText: String,
    headline: String,
    description: String,
    statusIcon: ImageVector,
    statusLabel: String,
    modifier: Modifier = Modifier,
    statusDotColor: Color = Color(0xFFFBBF24),
    statusIconTint: Color = Color(0xFFFBBF24)
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0E1726),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0E1726),
                            Color(0xFF111C30),
                            Color(0xFF0B1320)
                        )
                    )
                )
                .clip(RoundedCornerShape(24.dp))
        ) {
            // Ambient warm gold glow at top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 24.dp, y = (-20).dp)
                    .size(130.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFF59E0B).copy(alpha = 0.14f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Ambient emerald glow at bottom-left
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-20).dp, y = 20.dp)
                    .size(110.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF10B981).copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = Color.White.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(statusDotColor, CircleShape)
                            )
                            Text(
                                text = tagText.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE68A),
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = headline,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.2).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = Color(0xFF94A3B8),
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier.size(48.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusIconTint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = statusLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFCBD5E1),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubTabCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F3F6)),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
private fun SubTabSectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconBgColor: Color = Color(0xFFFFFBEB),
    iconBorderColor: Color = Color(0xFFFEF3C7),
    iconTint: Color = Color(0xFFD97706)
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = iconBgColor,
            border = BorderStroke(1.dp, iconBorderColor),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color(0xFF64748B),
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun SubTabBlackButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = Color(0xFF0F172A),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF0F172A).copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.1).sp
        )
    }
}

@Composable
private fun <T> SubTabSegmentedSwitch(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                Surface(
                    onClick = { onOptionSelected(option) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF0F172A) else Color.Transparent,
                    shadowElevation = if (isSelected) 1.dp else 0.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (isSelected) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = labelProvider(option),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else Color(0xFF64748B),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubTabSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Color(0xFF64748B),
                lineHeight = 15.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0F172A),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE2E8F0),
                uncheckedBorderColor = Color(0xFFCBD5E1)
            )
        )
    }
}

@Composable
private fun SubTabNavigationRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    badgeText: String? = null
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.8f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                if (icon != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFEF3C7)),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color(0xFF64748B),
                        lineHeight = 15.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFEF3C7))
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubTabDropdownRow(
    title: String,
    currentValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color(0xFF64748B)
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentValue,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(18.dp),
                containerColor = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VaultSettingsSection(
    vaultViewModel: com.patil.coconut_pfa.ui.viewmodel.VaultViewModel?,
    vaultUiState: com.patil.coconut_pfa.ui.viewmodel.VaultUiState?,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionState by com.patil.coconut_pfa.data.local.security.VaultSession.state.collectAsState()
    val isUnlocked = sessionState is com.patil.coconut_pfa.data.local.security.VaultState.Unlocked

    val uiState = vaultUiState ?: vaultViewModel?.uiState?.collectAsState()?.value ?: com.patil.coconut_pfa.ui.viewmodel.VaultUiState()

    var showChangeMasterPwdDialog by remember { mutableStateOf(false) }
    var showNewRecoveryCodePrompt by remember { mutableStateOf(false) }
    var showRotateKeyPrompt by remember { mutableStateOf(false) }
    var masterPwdForUnlock by remember { mutableStateOf("") }
    var isPwdVisible by remember { mutableStateOf(false) }

    val launchBiometricEncryptionPrompt = { onCipherReady: (javax.crypto.Cipher) -> Unit ->
        val activity = context as? androidx.fragment.app.FragmentActivity
        if (activity != null) {
            try {
                val cryptoObject = com.patil.coconut_pfa.data.local.security.BiometricKeyManager.createEncryptCryptoObject(activity)
                val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
                val prompt = androidx.biometric.BiometricPrompt(activity, executor, object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                        val resCipher = result.cryptoObject?.cipher
                        if (resCipher != null) {
                            activity.runOnUiThread {
                                onCipherReady(resCipher)
                            }
                        }
                    }
                })

                val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Enable Fingerprint Unlock")
                    .setSubtitle("Confirm fingerprint to wrap your Vault Key")
                    .setNegativeButtonText("Cancel")
                    .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build()

                prompt.authenticate(promptInfo, cryptoObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            vaultViewModel?.clearMessages()
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            vaultViewModel?.clearMessages()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SubTabBentoBanner(
            tagText = "Zero-Knowledge Enclave",
            headline = if (isUnlocked) "Vault Enclave Unlocked" else "Cryptographic Enclave Locked",
            description = "Zero-knowledge AES-256 GCM hardware encryption protects your payment cards, credentials and private records.",
            statusIcon = if (isUnlocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
            statusLabel = if (isUnlocked) "ACTIVE" else "LOCKED",
            statusDotColor = if (isUnlocked) Color(0xFF10B981) else Color(0xFFE11D48),
            statusIconTint = if (isUnlocked) Color(0xFF10B981) else Color(0xFFE11D48)
        )

        SubTabCardContainer {
            if (!isUnlocked) {
                SubTabSectionHeader(
                    title = "Master Key Unlock",
                    subtitle = "Enter master password to access encrypted vault enclave",
                    icon = Icons.Rounded.Lock
                )
                OutlinedTextField(
                    value = masterPwdForUnlock,
                    onValueChange = { masterPwdForUnlock = it },
                    label = { Text("Master Password") },
                    singleLine = true,
                    visualTransformation = if (isPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPwdVisible = !isPwdVisible }) {
                            Icon(
                                imageVector = if (isPwdVisible) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                SubTabBlackButton(
                    text = "Unlock Vault Enclave",
                    icon = Icons.Rounded.LockOpen,
                    enabled = masterPwdForUnlock.isNotBlank(),
                    onClick = {
                        vaultViewModel?.unlockWithMasterPassword(masterPwdForUnlock)
                        masterPwdForUnlock = ""
                    }
                )
            } else {
                SubTabSectionHeader(
                    title = "Master Credentials",
                    subtitle = "Master key lifecycle and biometric wrapping",
                    icon = Icons.Rounded.Key
                )
                SubTabNavigationRow(
                    title = "Change Master Password*",
                    subtitle = "Re-wrap Vault Key with new master password",
                    icon = Icons.Rounded.Key,
                    badgeText = "Security",
                    onClick = { showChangeMasterPwdDialog = true }
                )
                SubTabSwitchRow(
                    title = "Fingerprint Unlock",
                    subtitle = "Unlock vault using device biometric sensor",
                    checked = uiState.deviceSettings.vaultBioEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            launchBiometricEncryptionPrompt { cipher ->
                                vaultViewModel?.toggleFingerprint(true, cipher)
                            }
                        } else {
                            vaultViewModel?.toggleFingerprint(false)
                        }
                    }
                )

                HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                SubTabSectionHeader(
                    title = "Enclave Lock Policies",
                    subtitle = "Background inactivity and clipboard sanitization",
                    icon = Icons.Rounded.Schedule
                )
                val backgroundLockLabel = when (uiState.deviceSettings.vaultBackgroundLockSeconds) {
                    0 -> "Immediately"
                    30 -> "30 seconds"
                    60 -> "1 minute"
                    300 -> "5 minutes"
                    else -> "${uiState.deviceSettings.vaultBackgroundLockSeconds} seconds"
                }
                SubTabDropdownRow(
                    title = "Lock After Leaving App",
                    subtitle = "Re-locks enclave when navigating outside Fortis",
                    currentValue = backgroundLockLabel,
                    options = listOf("Immediately", "30 seconds", "1 minute", "5 minutes"),
                    onOptionSelected = { selected ->
                        val seconds = when (selected) {
                            "Immediately" -> 0
                            "30 seconds" -> 30
                            "1 minute" -> 60
                            "5 minutes" -> 300
                            else -> 30
                        }
                        vaultViewModel?.updateBackgroundLockSeconds(seconds)
                    }
                )

                val clipboardClearLabel = when (uiState.deviceSettings.clipboardClearSeconds) {
                    15 -> "15 seconds"
                    30 -> "30 seconds"
                    60 -> "60 seconds"
                    90 -> "90 seconds"
                    else -> "${uiState.deviceSettings.clipboardClearSeconds} seconds"
                }
                SubTabDropdownRow(
                    title = "Clear Clipboard After",
                    subtitle = "Automatically purge copied card and password data",
                    currentValue = clipboardClearLabel,
                    options = listOf("15 seconds", "30 seconds", "60 seconds", "90 seconds"),
                    onOptionSelected = { selected ->
                        val seconds = when (selected) {
                            "15 seconds" -> 15
                            "30 seconds" -> 30
                            "60 seconds" -> 60
                            "90 seconds" -> 90
                            else -> 30
                        }
                        vaultViewModel?.updateClipboardClearSeconds(seconds)
                    }
                )

                HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                SubTabSectionHeader(
                    title = "Cryptographic Enclave Lifecycle",
                    subtitle = "Emergency recovery code and key rotation",
                    icon = Icons.Rounded.Shield
                )
                SubTabNavigationRow(
                    title = "Generate New Recovery Code*",
                    subtitle = "Invalidates old code and issues fresh 16-char emergency code",
                    icon = Icons.Rounded.Description,
                    onClick = { showNewRecoveryCodePrompt = true }
                )
                SubTabNavigationRow(
                    title = "Rotate Vault Key*",
                    subtitle = "Re-encrypts all vault items in 1 atomic database transaction",
                    icon = Icons.Rounded.CloudSync,
                    onClick = { showRotateKeyPrompt = true }
                )

                HorizontalDivider(color = Color(0xFFF1F3F6), thickness = 1.dp)

                SubTabBlackButton(
                    text = "Lock Vault Enclave Now",
                    icon = Icons.Rounded.Lock,
                    onClick = { com.patil.coconut_pfa.data.local.security.VaultSession.lock() }
                )
            }
        }
    }

    // Change Master Password Dialog
    if (showChangeMasterPwdDialog) {
        var currentPwdInput by remember { mutableStateOf("") }
        var newPwdInput by remember { mutableStateOf("") }
        var confirmPwdInput by remember { mutableStateOf("") }
        var rotateIfExposed by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showChangeMasterPwdDialog = false },
            title = { Text("Change Master Password*", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currentPwdInput,
                        onValueChange = { currentPwdInput = it },
                        label = { Text("Current Master Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPwdInput,
                        onValueChange = { newPwdInput = it },
                        label = { Text("New Master Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPwdInput,
                        onValueChange = { confirmPwdInput = it },
                        label = { Text("Confirm New Master Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = rotateIfExposed,
                            onCheckedChange = { rotateIfExposed = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "I think my old password was exposed (also rotates Vault Key & re-encrypts items)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uiState.deviceSettings.vaultBioEnabled) {
                            launchBiometricEncryptionPrompt { cipher ->
                                vaultViewModel?.changeMasterPasswordSettings(
                                    currentPassword = currentPwdInput,
                                    newPassword = newPwdInput,
                                    confirmPassword = confirmPwdInput,
                                    rotateIfExposed = rotateIfExposed,
                                    cipherToReWrapBio = cipher
                                )
                                showChangeMasterPwdDialog = false
                            }
                        } else {
                            vaultViewModel?.changeMasterPasswordSettings(
                                currentPassword = currentPwdInput,
                                newPassword = newPwdInput,
                                confirmPassword = confirmPwdInput,
                                rotateIfExposed = rotateIfExposed
                            )
                            showChangeMasterPwdDialog = false
                        }
                    },
                    enabled = currentPwdInput.isNotBlank() && newPwdInput.length >= 12 && newPwdInput == confirmPwdInput
                ) {
                    Text("Change Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeMasterPwdDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // New Recovery Code Prompt Dialog
    if (showNewRecoveryCodePrompt) {
        var currentPwdInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewRecoveryCodePrompt = false },
            title = { Text("Generate New Recovery Code*", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your current master password to generate a new recovery code. Your old recovery code will stop working immediately.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = currentPwdInput,
                        onValueChange = { currentPwdInput = it },
                        label = { Text("Current Master Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vaultViewModel?.generateNewRecoveryCodeSettings(currentPwdInput)
                        showNewRecoveryCodePrompt = false
                    },
                    enabled = currentPwdInput.isNotBlank()
                ) {
                    Text("Generate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewRecoveryCodePrompt = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rotate Vault Key Prompt Dialog
    if (showRotateKeyPrompt) {
        var currentPwdInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRotateKeyPrompt = false },
            title = { Text("Rotate Vault Key*", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This action will generate a brand-new Vault Key and re-encrypt all stored credentials and cards in a single database transaction. It will also generate a new recovery code.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = currentPwdInput,
                        onValueChange = { currentPwdInput = it },
                        label = { Text("Current Master Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uiState.deviceSettings.vaultBioEnabled) {
                            launchBiometricEncryptionPrompt { cipher ->
                                vaultViewModel?.rotateVaultKeySettings(currentPwdInput, cipher)
                                showRotateKeyPrompt = false
                            }
                        } else {
                            vaultViewModel?.rotateVaultKeySettings(currentPwdInput)
                            showRotateKeyPrompt = false
                        }
                    },
                    enabled = currentPwdInput.isNotBlank()
                ) {
                    Text("Rotate Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRotateKeyPrompt = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // New Recovery Code Display Dialog
    if (uiState.showNewRecoveryCodeDialog && uiState.newlyGeneratedRecoveryCode != null) {
        var hasWrittenDown by remember { mutableStateOf(false) }
        var confirmLastGroup by remember { mutableStateOf("") }
        val recoveryCode = uiState.newlyGeneratedRecoveryCode!!
        val expectedLastGroup = recoveryCode.split("-").last()

        AlertDialog(
            onDismissRequest = {},
            title = { Text("New Recovery Code Generated", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Write down this recovery code in a secure location. The previous recovery code no longer works.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = recoveryCode,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Checkbox(
                            checked = hasWrittenDown,
                            onCheckedChange = { hasWrittenDown = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("I have written down this recovery code", style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedTextField(
                        value = confirmLastGroup,
                        onValueChange = { if (it.length <= 4) confirmLastGroup = it.uppercase() },
                        label = { Text("Confirm last 4 characters ($expectedLastGroup)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { vaultViewModel?.dismissNewRecoveryCodeDialog() },
                    enabled = hasWrittenDown && confirmLastGroup == expectedLastGroup
                ) {
                    Text("Done")
                }
            }
        )
    }

    // Delete Older Backups Offer Dialog
    if (uiState.showDeleteBackupsOffer) {
        AlertDialog(
            onDismissRequest = { vaultViewModel?.dismissDeleteBackupsOffer() },
            title = { Text("Delete Older Backups on Drive?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "You updated your master password / Vault Key. Older backups on Google Drive can still be decrypted with your previous master password.\n\nWould you like to clear older backups?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        vaultViewModel?.dismissDeleteBackupsOffer()
                        scope.launch {
                            snackbarHostState.showSnackbar("Older Drive backups deleted.")
                        }
                    }
                ) {
                    Text("Delete Older Backups")
                }
            },
            dismissButton = {
                TextButton(onClick = { vaultViewModel?.dismissDeleteBackupsOffer() }) {
                    Text("Keep Backups")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    Coconut_PfaTheme {
        SettingsScreen(onNavigate = {})
    }
}
