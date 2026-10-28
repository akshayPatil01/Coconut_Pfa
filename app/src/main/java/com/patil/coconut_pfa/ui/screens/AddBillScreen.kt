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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.model.BillKind
import com.patil.coconut_pfa.data.model.DueItem
import com.patil.coconut_pfa.data.model.DueTimeframe
import com.patil.coconut_pfa.data.repository.FakeData
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddBillScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var selectedKind by remember { mutableStateOf(BillKind.UTILITY) }
    var amountText by remember { mutableStateOf("") }
    var amountVaries by remember { mutableStateOf(false) }
    var frequency by remember { mutableStateOf("Monthly") }
    var dueDayText by remember { mutableStateOf("5") }
    var selectedCategory by remember { mutableStateOf("Utilities") }
    var remindDaysBefore by remember { mutableIntStateOf(3) }

    val accountsList = remember { FakeData.accounts }
    var selectedAccountId by remember { mutableStateOf(accountsList.firstOrNull()?.id ?: "") }
    var accountDropdownExpanded by remember { mutableStateOf(false) }

    val selectedAccountName = accountsList.find { it.id == selectedAccountId }?.name ?: "Select Account"
    val frequencies = listOf("Monthly", "Quarterly", "Yearly", "One-time")
    val remindOptions = listOf(1, 3, 5, 7)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Add Recurring Bill", fontWeight = FontWeight.Bold) },
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
                BillKind.entries.forEach { kind ->
                    FilterChip(
                        selected = selectedKind == kind,
                        onClick = { selectedKind = kind },
                        label = { Text(kind.displayName) },
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                frequencies.forEach { freq ->
                    FilterChip(
                        selected = frequency == freq,
                        onClick = { frequency = freq },
                        label = { Text(freq) }
                    )
                }
            }

            // Due Day of Month
            OutlinedTextField(
                value = dueDayText,
                onValueChange = { dueDayText = it.filter { char -> char.isDigit() } },
                label = { Text("Due Day of Month (1 - 31)") },
                placeholder = { Text("15") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Pay-from Account Dropdown
            Text(
                text = "Pay-From Account",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            ExposedDropdownMenuBox(
                expanded = accountDropdownExpanded,
                onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedAccountName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = accountDropdownExpanded,
                    onDismissRequest = { accountDropdownExpanded = false }
                ) {
                    accountsList.forEach { acc ->
                        DropdownMenuItem(
                            text = { Text(acc.name + (acc.bankName?.let { " ($it)" } ?: "")) },
                            onClick = {
                                selectedAccountId = acc.id
                                accountDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Category Field
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = { selectedCategory = it },
                label = { Text("Category") },
                placeholder = { Text("e.g. Utilities, Housing, Subscription") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
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
                    val amountInPaise = if (amountVaries) 0L else ((amountText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    val dueDay = dueDayText.toIntOrNull()?.coerceIn(1, 31) ?: 5
                    val newBill = DueItem(
                        id = UUID.randomUUID().toString(),
                        title = name.ifBlank { "New Bill" },
                        amountInPaise = amountInPaise,
                        dueDate = "$dueDay Nov 2024",
                        isOverdue = false,
                        kind = selectedKind,
                        timeframe = DueTimeframe.THIS_WEEK,
                        category = selectedCategory.ifBlank { "Bill" },
                        accountName = selectedAccountName,
                        isAmountVaries = amountVaries,
                        frequency = frequency,
                        dueDay = dueDay,
                        payFromAccountId = selectedAccountId,
                        remindDaysBefore = remindDaysBefore
                    )
                    FakeData.addDueItem(newBill)
                    onNavigate(Screen.Payments)
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
