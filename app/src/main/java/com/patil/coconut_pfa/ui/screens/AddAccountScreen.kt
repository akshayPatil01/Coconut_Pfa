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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.model.Account
import com.patil.coconut_pfa.data.model.AccountType
import com.patil.coconut_pfa.data.model.CardPaymentStatus
import com.patil.coconut_pfa.data.repository.FakeData
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAccountScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedType by remember { mutableStateOf(AccountType.BANK) }
    var accountName by remember { mutableStateOf("") }

    val bankOptions = listOf("HDFC Bank", "ICICI Bank", "SBI", "Axis Bank", "Other")
    var selectedBank by remember { mutableStateOf(bankOptions[0]) }
    var bankDropdownExpanded by remember { mutableStateOf(false) }

    var last4Digits by remember { mutableStateOf("") }
    var openingBalanceText by remember { mutableStateOf("") }
    var includeInNetWorth by remember { mutableStateOf(true) }

    // Type-specific fields
    var creditLimitText by remember { mutableStateOf("") }
    var statementDayText by remember { mutableStateOf("15") }
    var dueDayText by remember { mutableStateOf("5") }

    var interestRateText by remember { mutableStateOf("") }
    var emiAmountText by remember { mutableStateOf("") }
    var monthsLeftText by remember { mutableStateOf("12") }

    var principalText by remember { mutableStateOf("") }
    var fdStartDate by remember { mutableStateOf("01 Nov 2024") }
    var maturityDate by remember { mutableStateOf("01 Nov 2025") }
    var maturityAmountText by remember { mutableStateOf("") }

    var investmentValueText by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Add Account", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Screen.Accounts) }) {
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
            // 1. Account Type Picker (First)
            Text(
                text = "Account Type",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccountType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.displayName) },
                        leadingIcon = if (selectedType == type) {
                            { Icon(Icons.Rounded.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            // 2. Account Name
            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("Account Name") },
                placeholder = { Text("e.g. Salary Account, Regalia Card, Car Loan") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 3. Bank Picker (for relevant types)
            if (selectedType == AccountType.BANK || selectedType == AccountType.CREDIT_CARD || selectedType == AccountType.LOAN || selectedType == AccountType.FIXED_DEPOSIT) {
                Text(
                    text = "Bank / Provider",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                ExposedDropdownMenuBox(
                    expanded = bankDropdownExpanded,
                    onExpandedChange = { bankDropdownExpanded = !bankDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedBank,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false }
                    ) {
                        bankOptions.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank) },
                                onClick = {
                                    selectedBank = bank
                                    bankDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Last 4 digits (optional)
                OutlinedTextField(
                    value = last4Digits,
                    onValueChange = { if (it.length <= 4) last4Digits = it.filter { char -> char.isDigit() } },
                    label = { Text("Last 4 Digits (Optional)") },
                    placeholder = { Text("1234") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 4. Opening Balance
            OutlinedTextField(
                value = openingBalanceText,
                onValueChange = { openingBalanceText = it },
                label = {
                    Text(
                        when (selectedType) {
                            AccountType.LOAN -> "Loan Outstanding Amount (₹)"
                            AccountType.CREDIT_CARD -> "Current Credit Card Outstanding (₹)"
                            AccountType.INVESTMENT -> "Initial Investment Value (₹)"
                            else -> "Opening Balance (₹)"
                        }
                    )
                },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 5. Type-Specific Fields
            when (selectedType) {
                AccountType.CREDIT_CARD -> {
                    OutlinedTextField(
                        value = creditLimitText,
                        onValueChange = { creditLimitText = it },
                        label = { Text("Credit Limit (₹)") },
                        placeholder = { Text("100000") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = statementDayText,
                            onValueChange = { statementDayText = it.filter { char -> char.isDigit() } },
                            label = { Text("Statement Day (1-31)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = dueDayText,
                            onValueChange = { dueDayText = it.filter { char -> char.isDigit() } },
                            label = { Text("Due Day (1-31)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                AccountType.LOAN -> {
                    OutlinedTextField(
                        value = emiAmountText,
                        onValueChange = { emiAmountText = it },
                        label = { Text("Monthly EMI Amount (₹)") },
                        placeholder = { Text("15000") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = interestRateText,
                            onValueChange = { interestRateText = it },
                            label = { Text("Interest Rate (%)") },
                            placeholder = { Text("10.5") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = monthsLeftText,
                            onValueChange = { monthsLeftText = it.filter { char -> char.isDigit() } },
                            label = { Text("Tenure (Months)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                AccountType.FIXED_DEPOSIT -> {
                    OutlinedTextField(
                        value = principalText,
                        onValueChange = { principalText = it },
                        label = { Text("Principal Deposit Amount (₹)") },
                        placeholder = { Text("200000") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = interestRateText,
                            onValueChange = { interestRateText = it },
                            label = { Text("Interest Rate (%)") },
                            placeholder = { Text("7.1") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = maturityAmountText,
                            onValueChange = { maturityAmountText = it },
                            label = { Text("Maturity Amount (₹)") },
                            placeholder = { Text("214200") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = fdStartDate,
                            onValueChange = { fdStartDate = it },
                            label = { Text("Start Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = maturityDate,
                            onValueChange = { maturityDate = it },
                            label = { Text("Maturity Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                AccountType.INVESTMENT -> {
                    OutlinedTextField(
                        value = investmentValueText,
                        onValueChange = { investmentValueText = it },
                        label = { Text("Current Market Value (₹)") },
                        placeholder = { Text("500000") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                else -> {}
            }

            // 6. Include in Net Worth Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Include in Net Worth",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Add balance to overall net worth calculation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = includeInNetWorth,
                    onCheckedChange = { includeInNetWorth = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Account Action Button
            Button(
                onClick = {
                    val rawBal = (openingBalanceText.toDoubleOrNull() ?: 0.0) * 100
                    val balanceInPaise = when (selectedType) {
                        AccountType.CREDIT_CARD, AccountType.LOAN -> -rawBal.toLong()
                        else -> rawBal.toLong()
                    }

                    val newAccount = Account(
                        id = UUID.randomUUID().toString(),
                        name = accountName.ifBlank { "${selectedType.displayName} Account" },
                        type = selectedType,
                        balanceInPaise = balanceInPaise,
                        accountNumberLast4 = last4Digits.ifBlank { null },
                        bankName = selectedBank,
                        includeInNetWorth = includeInNetWorth,
                        creditLimitInPaise = ((creditLimitText.toDoubleOrNull() ?: 0.0) * 100).toLong(),
                        statementDay = statementDayText.toIntOrNull(),
                        dueDate = "$dueDayText Nov 2024",
                        cardStatus = CardPaymentStatus.UNPAID,
                        emiAmountInPaise = ((emiAmountText.toDoubleOrNull() ?: 0.0) * 100).toLong(),
                        interestRate = interestRateText.toDoubleOrNull() ?: 0.0,
                        monthsLeft = monthsLeftText.toIntOrNull() ?: 12,
                        principalInPaise = ((principalText.toDoubleOrNull() ?: 0.0) * 100).toLong(),
                        fdInterestRate = interestRateText.toDoubleOrNull() ?: 0.0,
                        fdStartDate = fdStartDate,
                        maturityDate = maturityDate,
                        maturityAmountInPaise = ((maturityAmountText.toDoubleOrNull() ?: 0.0) * 100).toLong(),
                        currentValueInPaise = if (investmentValueText.isNotBlank()) {
                            ((investmentValueText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                        } else balanceInPaise
                    )
                    FakeData.addAccount(newAccount)
                    onNavigate(Screen.Accounts)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Save Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddAccountScreenPreview() {
    Coconut_PfaTheme {
        AddAccountScreen(onNavigate = {})
    }
}
