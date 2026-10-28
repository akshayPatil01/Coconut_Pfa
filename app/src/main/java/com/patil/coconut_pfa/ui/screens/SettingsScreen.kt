package com.patil.coconut_pfa.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.SectionHeader
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.util.PrivacyManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isPrivacyEnabled by PrivacyManager.isPrivacyEnabled.collectAsState()

    // Setting State Variables
    var pinLockEnabled by remember { mutableStateOf(true) }
    var fingerprintEnabled by remember { mutableStateOf(true) }
    var autoLockTimeout by remember { mutableStateOf("After 1 minute") }
    var privacyByDefault by remember { mutableStateOf(false) }

    var salaryStartDay by remember { mutableIntStateOf(25) }

    var smsAutoDetectEnabled by remember { mutableStateOf(true) }

    var reminderTime by remember { mutableStateOf("09:00 PM") }
    var reminderDaysBefore by remember { mutableIntStateOf(3) }

    var showCategoriesDialog by remember { mutableStateOf(false) }
    var showDebugMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Title Row
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate(Screen.Home) }) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // SECTION 1: SECURITY
            item {
                SettingsCategoryCard(title = "Security & Privacy", icon = Icons.Rounded.Security) {
                    SettingsSwitchRow(
                        title = "PIN Protection",
                        subtitle = "Require 4-digit PIN to open app",
                        checked = pinLockEnabled,
                        onCheckedChange = { pinLockEnabled = it }
                    )
                    SettingsSwitchRow(
                        title = "Fingerprint / Biometric",
                        subtitle = "Use device biometric unlock",
                        checked = fingerprintEnabled,
                        onCheckedChange = { fingerprintEnabled = it }
                    )
                    SettingsDropdownRow(
                        title = "Auto-lock Timeout",
                        currentValue = autoLockTimeout,
                        options = listOf("Immediately", "After 1 minute", "After 5 minutes", "After 15 minutes"),
                        onOptionSelected = { autoLockTimeout = it }
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
                        onCheckedChange = { privacyByDefault = it }
                    )
                }
            }

            // SECTION 2: SALARY CYCLE
            item {
                SettingsCategoryCard(title = "Salary Cycle Settings", icon = Icons.Rounded.Schedule) {
                    SettingsDropdownRow(
                        title = "Salary Cycle Start Day",
                        currentValue = "Day $salaryStartDay of the month",
                        options = (1..31).map { "Day $it of the month" },
                        onOptionSelected = { selected ->
                            val day = selected.replace("Day ", "").replace(" of the month", "").toIntOrNull() ?: 25
                            salaryStartDay = day
                        }
                    )
                }
            }

            // SECTION 3: CATEGORIES
            item {
                SettingsCategoryCard(title = "Categories & Sub-Categories", icon = Icons.Rounded.Category) {
                    SettingsNavigationRow(
                        title = "Manage Categories",
                        subtitle = "Dining, Housing, Shopping, Utilities, Salary",
                        onClick = { showCategoriesDialog = true }
                    )
                }
            }

            // SECTION 4: SMS & BANKS
            item {
                SettingsCategoryCard(title = "SMS & Bank Parser", icon = Icons.Rounded.Sms) {
                    SettingsSwitchRow(
                        title = "Enable SMS Auto-Detection",
                        subtitle = "Read bank & card SMS alerts automatically",
                        checked = smsAutoDetectEnabled,
                        onCheckedChange = { smsAutoDetectEnabled = it }
                    )
                    SettingsNavigationRow(
                        title = "SMS Parser Test Screen",
                        subtitle = "Paste raw SMS text to test live parsing logic",
                        onClick = { onNavigate(Screen.ParserTest) }
                    )
                }
            }

            // SECTION 5: REMINDERS
            item {
                SettingsCategoryCard(title = "Bill Reminders", icon = Icons.Rounded.Alarm) {
                    SettingsDropdownRow(
                        title = "Daily Reminder Time",
                        currentValue = reminderTime,
                        options = listOf("08:00 AM", "12:00 PM", "06:00 PM", "09:00 PM"),
                        onOptionSelected = { reminderTime = it }
                    )
                    SettingsDropdownRow(
                        title = "Default Days Before Due",
                        currentValue = "$reminderDaysBefore days before",
                        options = listOf("1 day before", "2 days before", "3 days before", "7 days before"),
                        onOptionSelected = { selected ->
                            reminderDaysBefore = selected.take(1).toIntOrNull() ?: 3
                        }
                    )
                }
            }

            // SECTION 6: BACKUP & SYNC
            item {
                SettingsCategoryCard(title = "Backup & Recovery", icon = Icons.Rounded.CloudSync) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Google Account: connected as demo.user@gmail.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Last backup: Today at 10:15 AM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    scope.launch { snackbarHostState.showSnackbar("Encrypted backup created successfully!") }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Backup Now")
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch { snackbarHostState.showSnackbar("Data restored from backup!") }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Restore")
                            }
                        }
                    }
                }
            }

            // SECTION 7: TOOLS & UTILITIES (Import Statement, Receipt Scan, Trash)
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

            // SECTION 8: ABOUT & DEBUG MENU
            item {
                SettingsCategoryCard(title = "About Coconut PFA", icon = Icons.Rounded.Info) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Coconut PFA v1.0.0",
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

                        // Debug Menu Toggle
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
                            }
                        }
                    }
                }
            }
        }
    }

    // Categories Dialog
    if (showCategoriesDialog) {
        CategoriesDialog(onDismiss = { showCategoriesDialog = false })
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
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
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

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    Coconut_PfaTheme {
        SettingsScreen(onNavigate = {})
    }
}
