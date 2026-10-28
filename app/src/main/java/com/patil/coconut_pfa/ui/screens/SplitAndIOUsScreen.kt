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
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.model.PersonIOU
import com.patil.coconut_pfa.data.repository.FakeData
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitAndIOUsScreen(
    onNavigate: (Screen) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val peopleList = FakeData.personIOUs
    var selectedPersonForLedger by remember { mutableStateOf<PersonIOU?>(null) }
    var selectedPersonForSettleUp by remember { mutableStateOf<PersonIOU?>(null) }
    var showAddSplitDialog by remember { mutableStateOf(false) }

    val totalOwesYou = peopleList.filter { it.netBalanceInPaise > 0 }.sumOf { it.netBalanceInPaise }
    val totalYouOwe = peopleList.filter { it.netBalanceInPaise < 0 }.sumOf { kotlin.math.abs(it.netBalanceInPaise) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSplitDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Split Expense"
                )
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
            // Balance Overview Card
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

            items(peopleList, key = { it.id }) { person ->
                PersonCard(
                    person = person,
                    onClick = { selectedPersonForLedger = person },
                    onSettleUp = { selectedPersonForSettleUp = person }
                )
            }
        }
    }

    // Ledger Bottom Sheet
    selectedPersonForLedger?.let { person ->
        PersonLedgerBottomSheet(
            person = person,
            onDismiss = { selectedPersonForLedger = null },
            onSettleUpClick = {
                selectedPersonForSettleUp = person
                selectedPersonForLedger = null
            }
        )
    }

    // Settle Up Dialog
    selectedPersonForSettleUp?.let { person ->
        SettleUpDialog(
            person = person,
            onDismiss = { selectedPersonForSettleUp = null },
            onSettleUp = { amountInPaise ->
                FakeData.settleUpPerson(person.id, amountInPaise)
                selectedPersonForSettleUp = null
            }
        )
    }

    // Add Split Dialog
    if (showAddSplitDialog) {
        AddSplitDialog(
            onDismiss = { showAddSplitDialog = false },
            onSave = { title, totalPaise, personName, paidByMe, personSharePaise ->
                FakeData.addSplitTransaction(
                    title = title,
                    totalAmountInPaise = totalPaise,
                    personName = personName,
                    paidByMe = paidByMe,
                    personShareInPaise = personSharePaise
                )
                showAddSplitDialog = false
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.CallSplit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Split & IOUs Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Owes You Card Section (Green)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "You are owed",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AmountText(
                            amountInPaise = totalOwesYou,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // You Owe Card Section (Red)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "You owe",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AmountText(
                            amountInPaise = totalYouOwe,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
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

                if (person.netBalanceInPaise != 0L) {
                    Button(
                        onClick = onSettleUpClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Settle Up")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Net balance badge
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
                                        text = if (entry.paidByMe) "Paid by You • ${person.name}'s share: ${MoneyFormatter.format(entry.personShareInPaise)}"
                                        else "Paid by ${person.name} • Your share: ${MoneyFormatter.format(entry.myShareInPaise)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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

@Composable
private fun SettleUpDialog(
    person: PersonIOU,
    onDismiss: () -> Unit,
    onSettleUp: (amountInPaise: Long) -> Unit,
) {
    val suggestedPaise = kotlin.math.abs(person.netBalanceInPaise)
    var amountText by remember { mutableStateOf((suggestedPaise / 100).toString()) }

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
                    if (doubleVal > 0) {
                        onSettleUp((doubleVal * 100).toLong())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSplitDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, totalPaise: Long, personName: String, paidByMe: Boolean, personSharePaise: Long) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var totalAmountText by remember { mutableStateOf("") }
    var personName by remember { mutableStateOf("") }
    var paidByMe by remember { mutableStateOf(true) }
    var isEqualSplit by remember { mutableStateOf(true) }
    var customPersonShareText by remember { mutableStateOf("") }

    val peopleNames = FakeData.personIOUs.map { it.name }
    var expandedPeopleMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Split Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Description (e.g. Dinner, Rent)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = totalAmountText,
                    onValueChange = { totalAmountText = it },
                    label = { Text("Total Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Select or Enter Person
                ExposedDropdownMenuBox(
                    expanded = expandedPeopleMenu,
                    onExpandedChange = { expandedPeopleMenu = !expandedPeopleMenu }
                ) {
                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = { Text("Split With (Person Name)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPeopleMenu) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedPeopleMenu,
                        onDismissRequest = { expandedPeopleMenu = false }
                    ) {
                        peopleNames.forEach { pName ->
                            DropdownMenuItem(
                                text = { Text(pName) },
                                onClick = {
                                    personName = pName
                                    expandedPeopleMenu = false
                                }
                            )
                        }
                    }
                }

                // Who Paid selector
                Text(
                    text = "Who Paid?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = paidByMe,
                        onClick = { paidByMe = true },
                        label = { Text("I Paid") }
                    )
                    FilterChip(
                        selected = !paidByMe,
                        onClick = { paidByMe = false },
                        label = { Text("${if (personName.isBlank()) "They" else personName} Paid") }
                    )
                }

                // Split mode (Equal vs Custom)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Equal 50/50 Split", style = MaterialTheme.typography.bodyMedium)
                    FilterChip(
                        selected = isEqualSplit,
                        onClick = { isEqualSplit = !isEqualSplit },
                        label = { Text(if (isEqualSplit) "50 / 50" else "Custom Shares") }
                    )
                }

                if (!isEqualSplit) {
                    OutlinedTextField(
                        value = customPersonShareText,
                        onValueChange = { customPersonShareText = it },
                        label = { Text("Their Share (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val totalDouble = totalAmountText.toDoubleOrNull() ?: 0.0
                    val totalPaise = (totalDouble * 100).toLong()
                    if (title.isNotBlank() && totalPaise > 0 && personName.isNotBlank()) {
                        val personSharePaise = if (isEqualSplit) {
                            totalPaise / 2
                        } else {
                            ((customPersonShareText.toDoubleOrNull() ?: 0.0) * 100).toLong()
                        }
                        onSave(title, totalPaise, personName, paidByMe, personSharePaise)
                    }
                }
            ) {
                Text("Add Split")
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
