package com.patil.coconut_pfa.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.patil.coconut_pfa.ui.components.CoconutSnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.local.entity.DbBillFrequency
import com.patil.coconut_pfa.data.local.entity.DbBillKind
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.CoconutExposedDropdownMenu
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.viewmodel.AddBillUiState
import com.patil.coconut_pfa.ui.viewmodel.AddBillViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddBillScreen(
    onNavigate: (Screen) -> Unit,
    initialKind: String? = null,
    modifier: Modifier = Modifier,
    viewModel: AddBillViewModel? = null
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf(AddBillUiState(isLoading = false))
    }

    var name by remember { mutableStateOf("") }
    var selectedKind by remember {
        mutableStateOf(
            if (initialKind == "SUBSCRIPTION") DbBillKind.SUBSCRIPTION else DbBillKind.UTILITY
        )
    }
    var amountText by remember { mutableStateOf("") }
    var amountVaries by remember { mutableStateOf(false) }
    var selectedFrequency by remember { mutableStateOf(DbBillFrequency.MONTHLY) }
    var dueDayText by remember { mutableStateOf("5") }
    var remindDaysBefore by remember { mutableIntStateOf(3) }

    var selectedAccountId by remember { mutableStateOf<String?>(null) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.accounts, uiState.categories) {
        if (selectedAccountId == null && uiState.accounts.isNotEmpty()) {
            selectedAccountId = uiState.accounts.firstOrNull()?.id
        }
        if (selectedCategoryId == null && uiState.categories.isNotEmpty()) {
            val defaultCat = uiState.categories.firstOrNull { it.name.contains("Bill", ignoreCase = true) || it.name.contains("Utility", ignoreCase = true) }
                ?: uiState.categories.firstOrNull()
            selectedCategoryId = defaultCat?.id
        }
    }

    val selectedAccountName = uiState.accounts.find { it.id == selectedAccountId }?.name ?: "Select Pay-From Account"
    val selectedCategoryName = uiState.categories.find { it.id == selectedCategoryId }?.name ?: "Select Category"

    val remindOptions = listOf(1, 3, 5, 7)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { CoconutSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (initialKind == "SUBSCRIPTION") "Add Subscription" else "Add Recurring Bill", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Screen.Payments) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bill Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Bill Name") },
                placeholder = { Text("e.g. Electricity, Rent, Broadband") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Bill Kind Picker
            Text(
                text = "Bill Kind",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DbBillKind.entries.forEach { kind ->
                    val displayName = when (kind) {
                        DbBillKind.RENT -> "Rent"
                        DbBillKind.UTILITY -> "Utility"
                        DbBillKind.INSURANCE -> "Insurance"
                        DbBillKind.SUBSCRIPTION -> "Subscription"
                        DbBillKind.EMI -> "EMI"
                        DbBillKind.OTHER -> "Other"
                    }
                    FilterChip(
                        selected = selectedKind == kind,
                        onClick = { selectedKind = kind },
                        label = { Text(displayName) },
                        leadingIcon = if (selectedKind == kind) {
                            { Icon(Icons.Rounded.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            // Amount & Amount Varies Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !amountVaries,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Varies",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Switch(
                        checked = amountVaries,
                        onCheckedChange = { amountVaries = it }
                    )
                }
            }

            // Frequency Chips
            Text(
                text = "Frequency",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DbBillFrequency.entries.forEach { freq ->
                    val freqLabel = when (freq) {
                        DbBillFrequency.MONTHLY -> "Monthly"
                        DbBillFrequency.QUARTERLY -> "Quarterly"
                        DbBillFrequency.YEARLY -> "Yearly"
                        DbBillFrequency.EVERY_N_DAYS -> "Custom Days"
                    }
                    FilterChip(
                        selected = selectedFrequency == freq,
                        onClick = { selectedFrequency = freq },
                        label = { Text(freqLabel) }
                    )
                }
            }

            // Due Day of Month
            OutlinedTextField(
                value = dueDayText,
                onValueChange = { dueDayText = it.filter { char -> char.isDigit() } },
                label = { Text("Due Day of Month (1 - 31)") },
                placeholder = { Text("5") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Pay-from Account Dropdown
            CoconutExposedDropdownMenu(
                value = selectedAccountName,
                options = uiState.accounts,
                onOptionSelected = { acc -> selectedAccountId = acc.id },
                optionToString = { it.name },
                label = "Pay-From Account",
                modifier = Modifier.fillMaxWidth()
            )

            // Category Dropdown
            CoconutExposedDropdownMenu(
                value = selectedCategoryName,
                options = uiState.categories,
                onOptionSelected = { cat -> selectedCategoryId = cat.id },
                optionToString = { it.name },
                label = "Category",
                modifier = Modifier.fillMaxWidth()
            )

            // Remind N Days Before Selector
            Text(
                text = "Remind Me Before",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                remindOptions.forEach { days ->
                    FilterChip(
                        selected = remindDaysBefore == days,
                        onClick = { remindDaysBefore = days },
                        label = { Text("$days Days") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Bill Action Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Please enter a bill name") }
                        return@Button
                    }
                    val amountInPaise = if (amountVaries) 0L else ((amountText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    val dueDay = dueDayText.toIntOrNull()?.coerceIn(1, 31) ?: 5

                    scope.launch {
                        val success = viewModel?.saveBill(
                            name = name,
                            kind = selectedKind,
                            amountPaise = amountInPaise,
                            isVariable = amountVaries,
                            frequency = selectedFrequency,
                            dueDay = dueDay,
                            payFromAccountId = selectedAccountId,
                            categoryId = selectedCategoryId,
                            remindDaysBefore = remindDaysBefore
                        ) ?: true

                        if (success) {
                            onNavigate(Screen.Payments)
                        } else {
                            snackbarHostState.showSnackbar("Failed to save bill")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Save Bill",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddBillScreenPreview() {
    Coconut_PfaTheme {
        AddBillScreen(onNavigate = {})
    }
}
