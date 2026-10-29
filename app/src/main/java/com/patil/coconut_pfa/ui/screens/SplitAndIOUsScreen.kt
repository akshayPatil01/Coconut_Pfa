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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import com.patil.coconut_pfa.ui.components.GradientFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.DbLedgerEntryKind
import com.patil.coconut_pfa.data.model.LedgerEntry
import com.patil.coconut_pfa.data.model.PersonIOU
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.CoconutExposedDropdownMenu
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.EmptyState
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.theme.ExpenseRed
import com.patil.coconut_pfa.ui.theme.GradientOrangeCoralStart
import com.patil.coconut_pfa.ui.theme.IncomeGreen
import com.patil.coconut_pfa.ui.viewmodel.SplitAndIOUsUiState
import com.patil.coconut_pfa.ui.viewmodel.SplitAndIOUsViewModel
import com.patil.coconut_pfa.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitAndIOUsScreen(
    onNavigate: (Screen) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SplitAndIOUsViewModel? = null
) {
    val uiState by viewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf(SplitAndIOUsUiState()) }

    val peopleList = uiState.persons.map { item ->
        PersonIOU(
            id = item.person.id,
            name = item.person.name,
            netBalanceInPaise = item.netBalancePaise,
            phone = item.person.note,
            ledger = item.entries.map { e ->
                LedgerEntry(
                    id = e.id,
                    title = e.note ?: "Shared Expense",
                    amountInPaise = kotlin.math.abs(e.amountPaise),
                    date = e.date,
                    paidByMe = e.kind != DbLedgerEntryKind.BORROWED,
                    myShareInPaise = 0L,
                    personShareInPaise = e.amountPaise,
                    note = e.note
                )
            }
        )
    }

    var selectedPersonForLedger by remember { mutableStateOf<PersonIOU?>(null) }
    var selectedPersonForSettleUp by remember { mutableStateOf<PersonIOU?>(null) }
    var selectedPersonForRecordIou by remember { mutableStateOf<PersonIOU?>(null) }
    var showAddPersonDialog by remember { mutableStateOf(false) }

    val totalOwesYou = uiState.totalOwesYouPaise
    val totalYouOwe = uiState.totalYouOwePaise

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { showAddPersonDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PersonAdd,
                        contentDescription = "Add Person"
                    )
                }
                GradientFloatingActionButton(
                    onClick = { onNavigate(Screen.AddTransaction()) }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Split Expense"
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SplitOverviewCard(totalOwesYou = totalOwesYou, totalYouOwe = totalYouOwe)
            }

            item {
                Text(
                    text = "People & Shared Expenses (${peopleList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (peopleList.isEmpty()) {
                item {
                    EmptyState(
                        title = "No Shared Expenses Yet",
                        description = "Add people or log split transactions to keep track of shared balances and group expenses.",
                        icon = Icons.AutoMirrored.Rounded.CallSplit,
                        actionLabel = "Add Person",
                        onActionClick = { showAddPersonDialog = true },
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(peopleList, key = { it.id }) { person ->
                    PersonCard(
                        person = person,
                        onClick = { selectedPersonForLedger = person },
                        onSettleUp = { selectedPersonForSettleUp = person }
                    )
                }
            }
        }
    }

    selectedPersonForLedger?.let { person ->
        PersonLedgerBottomSheet(
            person = person,
            onDismiss = { selectedPersonForLedger = null },
            onSettleUpClick = {
                selectedPersonForSettleUp = person
                selectedPersonForLedger = null
            },
            onRecordIouClick = {
                selectedPersonForRecordIou = person
                selectedPersonForLedger = null
            }
        )
    }

    selectedPersonForSettleUp?.let { person ->
        SettleUpDialog(
            person = person,
            accounts = uiState.accounts,
            onDismiss = { selectedPersonForSettleUp = null },
            onSettleUp = { accountId, amountInPaise ->
                viewModel?.settleUp(person.id, accountId, amountInPaise)
                selectedPersonForSettleUp = null
            }
        )
    }

    if (showAddPersonDialog) {
        AddPersonDialog(
            onDismiss = { showAddPersonDialog = false },
            onSave = { name, note ->
                viewModel?.savePerson(name, note)
                showAddPersonDialog = false
            }
        )
    }
}

@Composable
private fun SplitOverviewCard(
    totalOwesYou: Long,
    totalYouOwe: Long,
    modifier: Modifier = Modifier,
) {
    val netBalance = totalOwesYou - totalYouOwe
    val cardBgColor = Color(0xFF121518)
    val innerCardBgColor = Color(0xFF1C2026)
    val labelTextColor = Color(0xFF9EA3A8)
    val primaryTextColor = Color.White

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = cardBgColor,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF262A30),
                    GradientOrangeCoralStart.copy(alpha = 0.35f),
                    Color(0xFF262A30)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1B1E22),
                            cardBgColor
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                // Header Row: Split Overview & Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262A30)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.CallSplit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Split Overview",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = labelTextColor
                        )
                    }

                    // Net Status Badge
                    val netBadgeText = when {
                        netBalance > 0 -> "Owed +${MoneyFormatter.format(netBalance)}"
                        netBalance < 0 -> "Owe ${MoneyFormatter.format(kotlin.math.abs(netBalance))}"
                        else -> "Settled Up"
                    }
                    val netBadgeBg = when {
                        netBalance > 0 -> Color(0xFF12392B)
                        netBalance < 0 -> Color(0xFF3B181A)
                        else -> Color(0xFF262A30)
                    }
                    val netBadgeFg = when {
                        netBalance > 0 -> IncomeGreen
                        netBalance < 0 -> ExpenseRed
                        else -> labelTextColor
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = netBadgeBg
                    ) {
                        Text(
                            text = netBadgeText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = netBadgeFg,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Net Position Display
                Text(
                    text = "Net Position",
                    style = MaterialTheme.typography.labelMedium,
                    color = labelTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                AmountText(
                    amountInPaise = kotlin.math.abs(netBalance),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (netBalance >= 0) primaryTextColor else ExpenseRed
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Inner Container with Owed to You & You Owe details
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = innerCardBgColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // People Owe You
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(IncomeGreen, CircleShape)
                                )
                                Text(
                                    text = "People owe you",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = labelTextColor
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            AmountText(
                                amountInPaise = totalOwesYou,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp)
                                .background(Color(0xFF2E343D))
                        )

                        // You Owe People
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(ExpenseRed, CircleShape)
                                )
                                Text(
                                    text = "You owe people",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = labelTextColor
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            AmountText(
                                amountInPaise = totalYouOwe,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonCard(
    person: PersonIOU,
    onClick: () -> Unit,
    onSettleUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOwedToMe = person.netBalanceInPaise > 0
    val isSettled = person.netBalanceInPaise == 0L

    val statusColor = when {
        isSettled -> MaterialTheme.colorScheme.outline
        isOwedToMe -> Color(0xFF2E7D32)
        else -> Color(0xFFC62828)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(person.avatarColorHex).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = person.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(person.avatarColorHex)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        person.phone?.let { ph ->
                            Text(
                                text = ph,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Ledger",
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when {
                            isSettled -> "Settled up"
                            isOwedToMe -> "owes you"
                            else -> "you owe"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isSettled) {
                        Text(
                            text = "₹0.00",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    } else {
                        AmountText(
                            amountInPaise = kotlin.math.abs(person.netBalanceInPaise),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                if (!isSettled) {
                    Button(
                        onClick = onSettleUp,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Settle Up")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonLedgerBottomSheet(
    person: PersonIOU,
    onDismiss: () -> Unit,
    onSettleUpClick: () -> Unit,
    onRecordIouClick: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Shared Expenses Ledger",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRecordIouClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Record IOU")
                    }

                    if (person.netBalanceInPaise != 0L) {
                        Button(
                            onClick = onSettleUpClick,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Settle Up")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Net Status",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = when {
                            person.netBalanceInPaise > 0 -> "Owes you ${MoneyFormatter.format(person.netBalanceInPaise)}"
                            person.netBalanceInPaise < 0 -> "You owe ${MoneyFormatter.format(kotlin.math.abs(person.netBalanceInPaise))}"
                            else -> "Settled Up (₹0)"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (person.netBalanceInPaise > 0) Color(0xFF2E7D32) else if (person.netBalanceInPaise < 0) Color(0xFFC62828) else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Transaction Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (person.ledger.isEmpty()) {
                Text(
                    text = "No shared transactions found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(person.ledger) { entry ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = entry.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    AmountText(
                                        amountInPaise = entry.amountInPaise,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = entry.date,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettleUpDialog(
    person: PersonIOU,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSettleUp: (accountId: String, amountInPaise: Long) -> Unit,
) {
    val suggestedPaise = kotlin.math.abs(person.netBalanceInPaise)
    var amountText by remember { mutableStateOf((suggestedPaise / 100).toString()) }

    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    val selectedAccountName = accounts.find { it.id == selectedAccountId }?.name ?: "Select Account"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settle Up with ${person.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (person.netBalanceInPaise > 0) "${person.name} owes you ${MoneyFormatter.format(person.netBalanceInPaise)}"
                    else "You owe ${person.name} ${MoneyFormatter.format(suggestedPaise)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                CoconutExposedDropdownMenu(
                    value = selectedAccountName,
                    options = accounts,
                    onOptionSelected = { acc -> selectedAccountId = acc.id },
                    optionToString = { it.name },
                    label = "Pay From / To Account",
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Settlement Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doubleVal = amountText.toDoubleOrNull() ?: 0.0
                    val accId = selectedAccountId
                    if (doubleVal > 0 && accId != null) {
                        onSettleUp(accId, (doubleVal * 100).toLong())
                    }
                }
            ) {
                Text("Confirm Settlement")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddPersonDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, note: String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Person", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Phone (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, note.ifBlank { null })
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SplitAndIOUsScreenPreview() {
    Coconut_PfaTheme {
        SplitAndIOUsScreen()
    }
}
