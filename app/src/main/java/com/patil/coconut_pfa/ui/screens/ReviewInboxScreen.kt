package com.patil.coconut_pfa.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.CategoryEntity
import com.patil.coconut_pfa.data.local.entity.SmsItemEntity
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.CoconutExposedDropdownMenu
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.EmptyState
import com.patil.coconut_pfa.ui.viewmodel.ReviewInboxViewModel
import com.patil.coconut_pfa.util.MoneyFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReviewInboxScreen(
    onNavigate: (Screen) -> Unit,
    viewModel: ReviewInboxViewModel,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()

    var selectedSmsForEdit by remember { mutableStateOf<SmsItemEntity?>(null) }
    var selectedSmsForAccountPick by remember { mutableStateOf<SmsItemEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate(Screen.Home) }) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "SMS Review Inbox",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.pendingSmsList.size} unread • Real-time SMS Detection",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (uiState.pendingSmsList.any { it.matchedAccountId != null && it.suggestedCategoryId != null }) {
                    Button(
                        onClick = {
                            viewModel.confirmAllMatched()
                            scope.launch {
                                snackbarHostState.showSnackbar("Confirmed all matched transactions!")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Confirm All Matched", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (uiState.pendingSmsList.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        title = "Inbox Clean!",
                        description = "All parsed SMS transactions have been confirmed or processed.",
                        icon = Icons.Rounded.Inbox,
                        actionLabel = "Back to Home",
                        onActionClick = { onNavigate(Screen.Home) }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.pendingSmsList, key = { it.id }) { sms ->
                        val matchedAcc = sms.matchedAccountId?.let { uiState.accountMap[it] }
                        val suggestedCat = sms.suggestedCategoryId?.let { uiState.categoryMap[it] }
                        val matchingManualTxn = viewModel.findMatchingManualTxn(sms, uiState.activeTxns)

                        if (sms.amountPaise == null || sms.amountPaise <= 0L) {
                            UnparsedSmsCard(
                                item = sms,
                                onTapToFill = { onNavigate(Screen.AddTransaction()) },
                                onDismiss = { viewModel.ignoreSms(sms) }
                            )
                        } else {
                            SmsReviewItemCard(
                                item = sms,
                                matchedAccount = matchedAcc,
                                suggestedCategory = suggestedCat,
                                matchingManualTxn = matchingManualTxn,
                                onConfirm = {
                                    if (matchedAcc != null && suggestedCat != null) {
                                        viewModel.confirmSms(sms, matchedAcc.id, suggestedCat.id, sms.merchant)
                                        scope.launch { snackbarHostState.showSnackbar("Transaction confirmed & added") }
                                    } else {
                                        selectedSmsForAccountPick = sms
                                    }
                                },
                                onLinkToExisting = { txn ->
                                    viewModel.linkToExisting(sms, txn.id)
                                    scope.launch { snackbarHostState.showSnackbar("Linked to existing manual transaction") }
                                },
                                onPickAccount = {
                                    selectedSmsForAccountPick = sms
                                },
                                onEdit = {
                                    selectedSmsForEdit = sms
                                },
                                onIgnore = {
                                    viewModel.ignoreSms(sms)
                                    scope.launch { snackbarHostState.showSnackbar("SMS ignored") }
                                },
                                onDuplicate = {
                                    viewModel.markDuplicate(sms)
                                    scope.launch { snackbarHostState.showSnackbar("Marked as duplicate") }
                                },
                                onReconcile = { balance ->
                                    onNavigate(Screen.AccountDetail(matchedAcc?.id ?: ""))
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Dialog
    selectedSmsForEdit?.let { sms ->
        val defaultAcc = sms.matchedAccountId?.let { uiState.accountMap[it] }
        val defaultCat = sms.suggestedCategoryId?.let { uiState.categoryMap[it] }
        EditSmsTransactionDialog(
            item = sms,
            accounts = uiState.accounts,
            categories = uiState.categories,
            initialAccount = defaultAcc,
            initialCategory = defaultCat,
            onDismiss = { selectedSmsForEdit = null },
            onSave = { accId, catId, payee ->
                viewModel.confirmSms(sms, accId, catId, payee)
                selectedSmsForEdit = null
                scope.launch { snackbarHostState.showSnackbar("Transaction confirmed & updated") }
            }
        )
    }

    // Account Selection Dialog when account not matched
    selectedSmsForAccountPick?.let { sms ->
        AccountSelectionDialog(
            accounts = uiState.accounts,
            onDismiss = { selectedSmsForAccountPick = null },
            onAccountSelected = { acc ->
                if (!sms.last4.isNullOrBlank() && acc.last4.isNullOrBlank()) {
                    viewModel.saveLast4(acc.id, sms.last4)
                }
                val defaultCatId = uiState.categories.firstOrNull()?.id ?: ""
                viewModel.confirmSms(sms, acc.id, defaultCatId, sms.merchant)
                selectedSmsForAccountPick = null
                scope.launch { snackbarHostState.showSnackbar("Account assigned & transaction confirmed") }
            }
        )
    }
}

@Composable
private fun SmsReviewItemCard(
    item: SmsItemEntity,
    matchedAccount: AccountEntity?,
    suggestedCategory: CategoryEntity?,
    matchingManualTxn: com.patil.coconut_pfa.data.local.entity.TxnEntity?,
    onConfirm: () -> Unit,
    onLinkToExisting: (com.patil.coconut_pfa.data.local.entity.TxnEntity) -> Unit,
    onPickAccount: () -> Unit,
    onEdit: () -> Unit,
    onIgnore: () -> Unit,
    onDuplicate: () -> Unit,
    onReconcile: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(item.receivedAt) { dateFormatter.format(Date(item.receivedAt)) }
    val isIncome = item.parsedType == "CREDIT"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sms,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.sender,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = if (isIncome) "CREDIT" else "DEBIT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body text snippet (if available)
            item.body?.let { body ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Account & Category Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        modifier = Modifier.clickable { onPickAccount() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalance,
                            contentDescription = null,
                            tint = if (matchedAccount != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = matchedAccount?.name ?: "No account matched — Tap to assign",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (matchedAccount != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = CircleShape,
                        color = if (suggestedCategory != null) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = "Category: ${suggestedCategory?.name ?: "Select category"}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (suggestedCategory != null) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    AmountText(
                        amountInPaise = item.amountPaise ?: 0L,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                }
            }

            // If manual transaction matches within ±1 day -> "Link to existing"
            matchingManualTxn?.let { manualTxn ->
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onLinkToExisting(manualTxn) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Icon(imageVector = Icons.Rounded.Link, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Link to existing manual txn (${MoneyFormatter.format(manualTxn.amountPaise)})",
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Balance Reconcile Hint
            item.availableBalancePaise?.let { bankBal ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReconcile(bankBal) }
                ) {
                    Text(
                        text = "💡 Bank says balance ₹${MoneyFormatter.format(bankBal)} — Tap to Reconcile",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirm")
                    }

                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onIgnore, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ignore", color = MaterialTheme.colorScheme.error)
                    }

                    TextButton(onClick = onDuplicate, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Duplicate")
                    }
                }
            }
        }
    }
}

@Composable
private fun UnparsedSmsCard(
    item: SmsItemEntity,
    onTapToFill: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Rounded.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Couldn't read — tap to fill in", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "Dismiss")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            item.body?.let { body ->
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                    Text(text = body, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            Button(
                onClick = onTapToFill,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Tap to Fill in Details Manually")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSmsTransactionDialog(
    item: SmsItemEntity,
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    initialAccount: AccountEntity?,
    initialCategory: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (accountId: String, categoryId: String, payee: String) -> Unit,
) {
    var selectedAccount by remember { mutableStateOf(initialAccount ?: accounts.firstOrNull()) }
    var selectedCategory by remember { mutableStateOf(initialCategory ?: categories.firstOrNull()) }
    var payee by remember { mutableStateOf(item.merchant ?: "Merchant") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Detected Transaction", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Amount: ${MoneyFormatter.format(item.amountPaise ?: 0L)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Account Dropdown
                CoconutExposedDropdownMenu(
                    value = selectedAccount?.name ?: "Select Account",
                    options = accounts,
                    onOptionSelected = { selectedAccount = it },
                    optionToString = { it.name },
                    label = "Account",
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown
                CoconutExposedDropdownMenu(
                    value = selectedCategory?.name ?: "Select Category",
                    options = categories,
                    onOptionSelected = { selectedCategory = it },
                    optionToString = { it.name },
                    label = "Category",
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = payee,
                    onValueChange = { payee = it },
                    label = { Text("Merchant / Payee Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedAccount != null && selectedCategory != null) {
                        onSave(selectedAccount!!.id, selectedCategory!!.id, payee)
                    }
                },
                enabled = selectedAccount != null && selectedCategory != null
            ) {
                Text("Confirm & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AccountSelectionDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onAccountSelected: (AccountEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Bank Account", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { acc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAccountSelected(acc) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Rounded.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = acc.name, fontWeight = FontWeight.Bold)
                                Text(text = "${acc.institution.name} • Last 4: ${acc.last4 ?: "None"}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
