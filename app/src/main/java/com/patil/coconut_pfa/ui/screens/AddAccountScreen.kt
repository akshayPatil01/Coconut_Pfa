package com.patil.coconut_pfa.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.patil.coconut_pfa.ui.components.GradientButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.CardDetailsEntity
import com.patil.coconut_pfa.data.local.entity.DbAccountType
import com.patil.coconut_pfa.data.local.entity.FdDetailsEntity
import com.patil.coconut_pfa.data.local.entity.LoanDetailsEntity
import com.patil.coconut_pfa.data.local.entity.ValuationEntity
import com.patil.coconut_pfa.data.model.AccountType
import com.patil.coconut_pfa.domain.AccountDomainEngine
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountInputField
import com.patil.coconut_pfa.ui.components.CoconutExposedDropdownMenu
import com.patil.coconut_pfa.ui.components.DatePickerField
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.viewmodel.AddAccountViewModel
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAccountScreen(
    onNavigate: (Screen) -> Unit,
    accountId: String? = null,
    modifier: Modifier = Modifier,
    viewModel: AddAccountViewModel? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val isEditMode = accountId != null

    var selectedType by remember { mutableStateOf(AccountType.BANK) }
    var accountName by remember { mutableStateOf("") }

    val bankOptions = listOf("HDFC Bank", "ICICI Bank", "SBI", "Axis Bank", "Other")
    var selectedBank by remember { mutableStateOf(bankOptions[0]) }

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
    var fdStartDate by remember { mutableStateOf(KolkataDateTimeUtils.currentDateString()) }
    var maturityDate by remember { mutableStateOf("2025-12-31") }
    var maturityAmountText by remember { mutableStateOf("") }

    var investmentValueText by remember { mutableStateOf("") }

    // Pre-fill fields in Edit Mode
    LaunchedEffect(accountId) {
        if (isEditMode && viewModel != null) {
            val entity = viewModel.getAccountToEdit()
            if (entity != null) {
                selectedType = AccountDomainEngine.mapToDomainType(entity.type)
                accountName = entity.name
                selectedBank = AccountDomainEngine.mapInstitutionName(entity.institution)
                last4Digits = entity.last4 ?: ""
                openingBalanceText = (abs(entity.openingBalancePaise) / 100.0).toString()
                includeInNetWorth = entity.includeInNetWorth

                when (entity.type) {
                    DbAccountType.CREDIT_CARD -> {
                        val card = viewModel.getCardDetailsToEdit()
                        if (card != null) {
                            creditLimitText = (card.creditLimitPaise / 100.0).toString()
                            statementDayText = card.statementDay.toString()
                            dueDayText = card.dueDay.toString()
                        }
                    }
                    DbAccountType.LOAN -> {
                        val loan = viewModel.getLoanDetailsToEdit()
                        if (loan != null) {
                            interestRateText = (loan.annualRateBps / 100.0).toString()
                            emiAmountText = (loan.emiPaise / 100.0).toString()
                            monthsLeftText = loan.tenureMonths.toString()
                        }
                    }
                    DbAccountType.FD -> {
                        val fd = viewModel.getFdDetailsToEdit()
                        if (fd != null) {
                            principalText = (fd.principalPaise / 100.0).toString()
                            interestRateText = (fd.annualRateBps / 100.0).toString()
                            fdStartDate = fd.startDate
                            maturityDate = fd.maturityDate
                            maturityAmountText = fd.maturityAmountPaise?.let { (it / 100.0).toString() } ?: ""
                        }
                    }
                    DbAccountType.INVESTMENT -> {
                        investmentValueText = (abs(entity.openingBalancePaise) / 100.0).toString()
                    }
                    else -> {}
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Account" else "Add Account", fontWeight = FontWeight.Bold) },
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
            // 1. Account Type Picker Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Account Type",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
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
                }
            }

            // 2. Main Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Basic Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("Account Name") },
                        placeholder = { Text("e.g. Salary Account, Regalia Card, Car Loan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    if (selectedType == AccountType.BANK || selectedType == AccountType.CREDIT_CARD || selectedType == AccountType.LOAN || selectedType == AccountType.FIXED_DEPOSIT) {
                        CoconutExposedDropdownMenu(
                            value = selectedBank,
                            options = bankOptions,
                            onOptionSelected = { selectedBank = it },
                            label = "Bank / Provider",
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = last4Digits,
                            onValueChange = { if (it.length <= 4) last4Digits = it.filter { char -> char.isDigit() } },
                            label = { Text("Last 4 Digits (Optional)") },
                            placeholder = { Text("1234") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    // Opening Balance with Comma Formatting
                    AmountInputField(
                        value = openingBalanceText,
                        onValueChange = { openingBalanceText = it },
                        label = when (selectedType) {
                            AccountType.LOAN -> "Loan Outstanding Amount"
                            AccountType.CREDIT_CARD -> "Current Credit Card Outstanding"
                            AccountType.INVESTMENT -> "Initial Investment Value"
                            else -> "Opening Balance"
                        },
                        placeholder = "0.00"
                    )
                }
            }

            // 3. Type-Specific Details Card
            if (selectedType != AccountType.BANK && selectedType != AccountType.CASH && selectedType != AccountType.WALLET) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "${selectedType.displayName} Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        when (selectedType) {
                            AccountType.CREDIT_CARD -> {
                                AmountInputField(
                                    value = creditLimitText,
                                    onValueChange = { creditLimitText = it },
                                    label = "Credit Limit",
                                    placeholder = "100000"
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
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    OutlinedTextField(
                                        value = dueDayText,
                                        onValueChange = { dueDayText = it.filter { char -> char.isDigit() } },
                                        label = { Text("Due Day (1-31)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            }

                            AccountType.LOAN -> {
                                AmountInputField(
                                    value = emiAmountText,
                                    onValueChange = { emiAmountText = it },
                                    label = "Monthly EMI Amount",
                                    placeholder = "15000"
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
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    OutlinedTextField(
                                        value = monthsLeftText,
                                        onValueChange = { monthsLeftText = it.filter { char -> char.isDigit() } },
                                        label = { Text("Tenure (Months)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            }

                            AccountType.FIXED_DEPOSIT -> {
                                AmountInputField(
                                    value = principalText,
                                    onValueChange = { principalText = it },
                                    label = "Principal Deposit Amount",
                                    placeholder = "200000"
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
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    AmountInputField(
                                        value = maturityAmountText,
                                        onValueChange = { maturityAmountText = it },
                                        label = "Maturity Amount",
                                        placeholder = "214200",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    DatePickerField(
                                        value = fdStartDate,
                                        onDateSelected = { fdStartDate = it },
                                        label = "Start Date",
                                        modifier = Modifier.weight(1f)
                                    )
                                    DatePickerField(
                                        value = maturityDate,
                                        onDateSelected = { maturityDate = it },
                                        label = "Maturity Date",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            AccountType.INVESTMENT -> {
                                AmountInputField(
                                    value = investmentValueText,
                                    onValueChange = { investmentValueText = it },
                                    label = "Current Market Value",
                                    placeholder = "500000"
                                )
                            }

                            else -> {}
                        }
                    }
                }
            }

            // 4. Preferences Card (Net Worth Switch)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Include in Net Worth",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save / Update Action Button
            GradientButton(
                onClick = {
                    coroutineScope.launch {
                        val targetAccountId = accountId ?: UUID.randomUUID().toString()
                        val rawBal = ((openingBalanceText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                        val openingBal = when (selectedType) {
                            AccountType.CREDIT_CARD, AccountType.LOAN -> -abs(rawBal)
                            else -> abs(rawBal)
                        }

                        val dbType = AccountDomainEngine.mapToDbType(selectedType)
                        val dbInst = AccountDomainEngine.mapInstitutionToEnum(selectedBank)

                        val accountEntity = AccountEntity(
                            id = targetAccountId,
                            name = accountName.ifBlank { "${selectedType.displayName} Account" },
                            type = dbType,
                            institution = dbInst,
                            last4 = last4Digits.ifBlank { null },
                            openingBalancePaise = openingBal,
                            openingDate = KolkataDateTimeUtils.currentDateString(),
                            icon = when (selectedType) {
                                AccountType.BANK -> "account_balance"
                                AccountType.CREDIT_CARD -> "credit_card"
                                AccountType.CASH -> "payments"
                                AccountType.WALLET -> "account_balance_wallet"
                                AccountType.LOAN -> "payments"
                                AccountType.INVESTMENT -> "show_chart"
                                AccountType.FIXED_DEPOSIT -> "savings"
                            },
                            colour = 0xFF1E88E5L,
                            includeInNetWorth = includeInNetWorth
                        )

                        var cardDetails: CardDetailsEntity? = null
                        var loanDetails: LoanDetailsEntity? = null
                        var fdDetails: FdDetailsEntity? = null
                        var valuation: ValuationEntity? = null

                        when (selectedType) {
                            AccountType.CREDIT_CARD -> {
                                cardDetails = CardDetailsEntity(
                                    accountId = targetAccountId,
                                    creditLimitPaise = ((creditLimitText.toDoubleOrNull() ?: 0.0) * 100).toLong(),
                                    statementDay = statementDayText.toIntOrNull() ?: 15,
                                    dueDay = dueDayText.toIntOrNull() ?: 5
                                )
                            }
                            AccountType.LOAN -> {
                                loanDetails = LoanDetailsEntity(
                                    accountId = targetAccountId,
                                    principalPaise = abs(rawBal),
                                    annualRateBps = ((interestRateText.toDoubleOrNull() ?: 0.0) * 100).toInt(),
                                    tenureMonths = monthsLeftText.toIntOrNull() ?: 12,
                                    emiPaise = ((emiAmountText.toDoubleOrNull() ?: 0.0) * 100).toLong(),
                                    startDate = KolkataDateTimeUtils.currentDateString(),
                                    emiDay = dueDayText.toIntOrNull() ?: 10
                                )
                            }
                            AccountType.FIXED_DEPOSIT -> {
                                fdDetails = FdDetailsEntity(
                                    accountId = targetAccountId,
                                    principalPaise = ((principalText.toDoubleOrNull() ?: 0.0) * 100).toLong(),
                                    annualRateBps = ((interestRateText.toDoubleOrNull() ?: 0.0) * 100).toInt(),
                                    startDate = fdStartDate,
                                    maturityDate = maturityDate,
                                    maturityAmountPaise = ((maturityAmountText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                                )
                            }
                            AccountType.INVESTMENT -> {
                                if (investmentValueText.isNotBlank()) {
                                    valuation = ValuationEntity(
                                        id = UUID.randomUUID().toString(),
                                        accountId = targetAccountId,
                                        date = KolkataDateTimeUtils.currentDateString(),
                                        valuePaise = ((investmentValueText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                                    )
                                }
                            }
                            else -> {}
                        }

                        viewModel?.saveAccount(
                            accountEntity = accountEntity,
                            cardDetails = cardDetails,
                            loanDetails = loanDetails,
                            fdDetails = fdDetails,
                            valuation = valuation,
                            isEditMode = isEditMode
                        )

                        onNavigate(Screen.Accounts)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isEditMode) "Update Account" else "Save Account",
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
