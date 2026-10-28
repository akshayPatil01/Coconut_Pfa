package com.patil.coconut_pfa.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.model.Account
import com.patil.coconut_pfa.data.model.AccountType
import com.patil.coconut_pfa.data.model.EmiScheduleItem
import com.patil.coconut_pfa.data.model.Transaction
import com.patil.coconut_pfa.data.repository.FakeData
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.SectionHeader
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountId: String,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accountList = FakeData.accounts
    val account = accountList.find { it.id == accountId } ?: accountList.first()

    var showReconcileDialog by remember { mutableStateOf(false) }
    var showUpdateValueDialog by remember { mutableStateOf(false) }

    val accountTransactions = remember(accountId) {
        FakeData.transactions.filter { it.accountId == accountId || it.targetAccountId == accountId }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(account.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = account.type.displayName + (account.bankName?.let { " • $it" } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Screen.Accounts) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Card & Reconcile Button
            item(key = "balance_header") {
                AccountBalanceHeaderCard(
                    account = account,
                    onReconcileClick = { showReconcileDialog = true }
                )
            }

            // Type-Specific Panel
            item(key = "type_specific_panel") {
                TypeSpecificPanel(
                    account = account,
                    onUpdateValueClick = { showUpdateValueDialog = true }
                )
            }

            // Account Transactions Section
            item(key = "transactions_header") {
                SectionHeader(
                    title = "Account Transactions",
                    subtitle = "${accountTransactions.size} recorded transactions"
                )
            }

            if (accountTransactions.isEmpty()) {
                item(key = "no_tx") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "No recent transactions found for this account.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(accountTransactions, key = { it.id }) { tx ->
                    AccountTransactionRow(tx = tx)
                }
            }
        }
    }

    // Reconcile Balance Dialog
    if (showReconcileDialog) {
        ReconcileDialog(
            currentBalanceInPaise = account.balanceInPaise,
            onDismiss = { showReconcileDialog = false },
            onConfirm = { newBalanceInPaise ->
                FakeData.updateAccountBalance(account.id, newBalanceInPaise)
                showReconcileDialog = false
            }
        )
    }

    // Update Investment Value Dialog
    if (showUpdateValueDialog) {
        UpdateValueDialog(
            currentValueInPaise = account.currentValueInPaise,
            onDismiss = { showUpdateValueDialog = false },
            onConfirm = { newValueInPaise ->
                FakeData.updateAccountBalance(account.id, newValueInPaise)
                showUpdateValueDialog = false
            }
        )
    }
}

/**
 * Account Balance Header Card displaying balance and "Reconcile" button.
 */
@Composable
private fun AccountBalanceHeaderCard(
    account: Account,
    onReconcileClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (account.type == AccountType.LOAN) "Outstanding Loan Balance" else "Current Balance",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AmountText(
                        amountInPaise = account.balanceInPaise,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (account.balanceInPaise < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                OutlinedButton(
                    onClick = onReconcileClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reconcile")
                }
            }

            if (account.accountNumberLast4 != null || account.bankName != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${account.bankName ?: ""} •• ${account.accountNumberLast4 ?: ""}".trim(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Type-Specific Panel rendering fields dynamically depending on `AccountType`.
 */
@Composable
private fun TypeSpecificPanel(
    account: Account,
    onUpdateValueClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "${account.type.displayName} Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            when (account.type) {
                AccountType.CREDIT_CARD -> CreditCardPanel(account = account)
                AccountType.LOAN -> LoanPanel(account = account)
                AccountType.FIXED_DEPOSIT -> FixedDepositPanel(account = account)
                AccountType.INVESTMENT -> InvestmentPanel(account = account, onUpdateValueClick = onUpdateValueClick)
                else -> GeneralAccountPanel(account = account)
            }
        }
    }
}

@Composable
private fun CreditCardPanel(account: Account) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DetailItem(label = "Credit Limit", valueInPaise = account.creditLimitInPaise)
            DetailItem(label = "Outstanding", valueInPaise = abs(account.balanceInPaise), isError = true)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DetailItem(label = "Available Credit", valueInPaise = account.availableCreditInPaise, isGreen = true)
            DetailTextItem(label = "Statement Day", value = "${account.statementDay ?: "15"}th of month")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DetailTextItem(label = "Next Due Date", value = account.dueDate ?: "N/A")
            DetailItem(label = "Minimum Due", valueInPaise = account.minimumDueInPaise)
        }
    }
}

@Composable
private fun LoanPanel(account: Account) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DetailItem(label = "Loan Outstanding", valueInPaise = account.loanOutstandingInPaise, isError = true)
            DetailItem(label = "Monthly EMI", valueInPaise = account.emiAmountInPaise)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DetailTextItem(label = "Interest Rate", value = "${account.interestRate}% p.a.")
            DetailTextItem(label = "Months Left", value = "${account.monthsLeft} months")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "EMI Schedule",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        if (account.emiSchedule.isEmpty()) {
            Text(
                text = "No EMI schedule details available.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                account.emiSchedule.forEach { item ->
                    EmiScheduleRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun EmiScheduleRow(item: EmiScheduleItem) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (item.isPaid) Icons.Rounded.CheckCircle else Icons.Rounded.Pending,
                    contentDescription = null,
                    tint = if (item.isPaid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "#${item.installmentNo} - Due: ${item.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Prin: ₹${item.principalInPaise / 100} • Int: ₹${item.interestInPaise / 100}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AmountText(
                amountInPaise = item.emiAmountInPaise,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FixedDepositPanel(account: Account) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DetailItem(label = "Principal Amount", valueInPaise = account.principalInPaise)
            DetailTextItem(label = "Interest Rate", value = "${account.fdInterestRate}% p.a.")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DetailTextItem(label = "Start Date", value = account.fdStartDate ?: "N/A")
            DetailTextItem(label = "Maturity Date", value = account.maturityDate ?: "N/A")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DetailItem(label = "Maturity Amount", valueInPaise = account.maturityAmountInPaise, isGreen = true)
        }
    }
}

@Composable
private fun InvestmentPanel(
    account: Account,
    onUpdateValueClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Current Value",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AmountText(
                    amountInPaise = account.currentValueInPaise,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            Button(
                onClick = onUpdateValueClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Update Value")
            }
        }
    }
}

@Composable
private fun GeneralAccountPanel(account: Account) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DetailTextItem(label = "Account Type", value = account.type.displayName)
            DetailTextItem(label = "Net Worth Status", value = if (account.includeInNetWorth) "Included" else "Excluded")
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    valueInPaise: Long,
    isError: Boolean = false,
    isGreen: Boolean = false,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AmountText(
            amountInPaise = valueInPaise,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = when {
                isError -> MaterialTheme.colorScheme.error
                isGreen -> Color(0xFF2E7D32)
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun DetailTextItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AccountTransactionRow(tx: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.payee.ifBlank { tx.category },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${tx.category} • ${tx.type.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AmountText(
                amountInPaise = tx.amountInPaise,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ReconcileDialog(
    currentBalanceInPaise: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    var textValue by remember { mutableStateOf((currentBalanceInPaise / 100.0).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reconcile Account Balance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Enter the actual current balance as shown in your bank/card statement.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text("Real Balance (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newBal = ((textValue.toDoubleOrNull() ?: 0.0) * 100).toLong()
                onConfirm(newBal)
            }) {
                Text("Update Balance")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun UpdateValueDialog(
    currentValueInPaise: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    var textValue by remember { mutableStateOf((currentValueInPaise / 100.0).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Market Value") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Enter the updated total market value of your portfolio.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text("Market Value (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newVal = ((textValue.toDoubleOrNull() ?: 0.0) * 100).toLong()
                onConfirm(newVal)
            }) {
                Text("Save Value")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun AccountDetailScreenPreview() {
    Coconut_PfaTheme {
        AccountDetailScreen(accountId = "acc_1", onNavigate = {})
    }
}
