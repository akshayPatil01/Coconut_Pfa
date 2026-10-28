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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.model.Transaction
import com.patil.coconut_pfa.data.model.TransactionType
import com.patil.coconut_pfa.data.repository.FakeData
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.EmptyState
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme

@Composable
fun TransactionsScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<TransactionType?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedAccount by remember { mutableStateOf<String?>(null) }
    var selectedCycle by remember { mutableStateOf("Current Cycle") }

    // Dropdown visibility states
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var cycleDropdownExpanded by remember { mutableStateOf(false) }

    val accountMap = remember { FakeData.accounts.associateBy { it.id } }

    // Filter transactions based on search query and chip filters
    val filteredTransactionsByDay = remember(
        searchQuery,
        selectedType,
        selectedCategory,
        selectedAccount,
        selectedCycle
    ) {
        FakeData.transactionsByDay.mapValues { (_, txList) ->
            txList.filter { tx ->
                val matchesSearch = searchQuery.isBlank() ||
                        tx.payee.contains(searchQuery, ignoreCase = true) ||
                        tx.category.contains(searchQuery, ignoreCase = true) ||
                        (tx.note?.contains(searchQuery, ignoreCase = true) == true)

                val matchesType = selectedType == null || tx.type == selectedType
                val matchesCategory = selectedCategory == null || tx.category.equals(selectedCategory, ignoreCase = true)
                val matchesAccount = selectedAccount == null || tx.accountId == selectedAccount || tx.targetAccountId == selectedAccount

                matchesSearch && matchesType && matchesCategory && matchesAccount
            }
        }.filterValues { it.isNotEmpty() }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Search Bar
        PaddingValues(horizontal = 16.dp, vertical = 8.dp).let { padding ->
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                placeholder = { Text("Search payee, category, note...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Rounded.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Filter Chips Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Cycle Filter Chip
            item {
                Box {
                    FilterChip(
                        selected = selectedCycle != "All Time",
                        onClick = { cycleDropdownExpanded = true },
                        label = { Text(selectedCycle) }
                    )
                    DropdownMenu(
                        expanded = cycleDropdownExpanded,
                        onDismissRequest = { cycleDropdownExpanded = false }
                    ) {
                        listOf("Current Cycle", "Previous Cycle", "All Time").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedCycle = option
                                    cycleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 2. Type Filter Chip
            item {
                Box {
                    FilterChip(
                        selected = selectedType != null,
                        onClick = { typeDropdownExpanded = true },
                        label = { Text(selectedType?.displayName ?: "Type: All") }
                    )
                    DropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Types") },
                            onClick = {
                                selectedType = null
                                typeDropdownExpanded = false
                            }
                        )
                        TransactionType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 3. Category Filter Chip
            item {
                Box {
                    val categories = remember {
                        FakeData.transactions.map { it.category }.distinct()
                    }
                    FilterChip(
                        selected = selectedCategory != null,
                        onClick = { categoryDropdownExpanded = true },
                        label = { Text(selectedCategory ?: "Category: All") }
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                selectedCategory = null
                                categoryDropdownExpanded = false
                            }
                        )
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 4. Account Filter Chip
            item {
                Box {
                    FilterChip(
                        selected = selectedAccount != null,
                        onClick = { accountDropdownExpanded = true },
                        label = {
                            val name = selectedAccount?.let { accountMap[it]?.name } ?: "Account: All"
                            Text(name)
                        }
                    )
                    DropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Accounts") },
                            onClick = {
                                selectedAccount = null
                                accountDropdownExpanded = false
                            }
                        )
                        FakeData.accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedAccount = acc.id
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main List Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // "5 SMS to review" Banner at Top of Feed
            item {
                if (FakeData.smsReviewItems.isNotEmpty()) {
                    SmsBannerInTransactions(
                        unreadCount = FakeData.smsReviewItems.size,
                        onClick = { onNavigate(Screen.ReviewInbox) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            if (filteredTransactionsByDay.isEmpty()) {
                item {
                    EmptyState(
                        title = "No Matching Transactions",
                        description = "Try adjusting your search query or filter chips.",
                        icon = Icons.Rounded.Search
                    )
                }
            } else {
                filteredTransactionsByDay.forEach { (dateLabel, txList) ->
                    // Group Header with Day Summary
                    item(key = "header_$dateLabel") {
                        DayHeader(dateLabel = dateLabel, txList = txList)
                    }

                    // Transaction Items
                    items(txList, key = { it.id }) { tx ->
                        TransactionItemRow(
                            transaction = tx,
                            accountMap = accountMap,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top banner on Transactions Feed alerting user of unreviewed SMS.
 */
@Composable
private fun SmsBannerInTransactions(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Inbox,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$unreadCount SMS to review",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Text(text = "$unreadCount")
                    }
                }
                Text(
                    text = "Tap to review auto-detected bank transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Daily header showing date label and total spent/received in the day.
 */
@Composable
private fun DayHeader(
    dateLabel: String,
    txList: List<Transaction>,
) {
    val daySpent = txList
        .filter { it.type == TransactionType.EXPENSE || it.type == TransactionType.TRANSFER }
        .sumOf { it.amountInPaise }

    val dayReceived = txList
        .filter { it.type == TransactionType.INCOME || it.type == TransactionType.REFUND }
        .sumOf { it.amountInPaise }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (daySpent > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Spent: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AmountText(
                            amountInPaise = daySpent,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (dayReceived > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Recv: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AmountText(
                            amountInPaise = dayReceived,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Transaction item card with category icon, payee, account details, note, split badge, and [AmountText].
 */
@Composable
private fun TransactionItemRow(
    transaction: Transaction,
    accountMap: Map<String, com.patil.coconut_pfa.data.model.Account>,
    modifier: Modifier = Modifier,
) {
    val account = accountMap[transaction.accountId]
    val targetAccount = transaction.targetAccountId?.let { accountMap[it] }

    val isPositive = transaction.type == TransactionType.INCOME || transaction.type == TransactionType.REFUND
    val amountColor = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon inside colored circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = getCategoryContainerColor(transaction.type),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(transaction.category, transaction.type),
                    contentDescription = transaction.category,
                    tint = getCategoryIconColor(transaction.type),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.payee,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (transaction.isSplit) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.CallSplit,
                            contentDescription = "Split transaction",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Account & Target account text
                val accountDisplay = when (transaction.type) {
                    TransactionType.TRANSFER, TransactionType.SETTLEMENT -> {
                        val fromName = account?.name ?: "Account"
                        val toName = targetAccount?.name ?: "Target Account"
                        "$fromName ↔ $toName"
                    }
                    else -> account?.name ?: transaction.category
                }

                Text(
                    text = accountDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Amount Text
            Column(horizontalAlignment = Alignment.End) {
                val prefix = if (isPositive) "+ " else ""
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPositive) {
                        Text(
                            text = prefix,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = amountColor
                        )
                    }
                    AmountText(
                        amountInPaise = transaction.amountInPaise,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                }

                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun getCategoryContainerColor(type: TransactionType): Color {
    return when (type) {
        TransactionType.INCOME -> MaterialTheme.colorScheme.primaryContainer
        TransactionType.REFUND -> MaterialTheme.colorScheme.tertiaryContainer
        TransactionType.TRANSFER, TransactionType.SETTLEMENT -> MaterialTheme.colorScheme.secondaryContainer
        TransactionType.EXPENSE, TransactionType.ADJUSTMENT -> MaterialTheme.colorScheme.surfaceVariant
    }
}

@Composable
private fun getCategoryIconColor(type: TransactionType): Color {
    return when (type) {
        TransactionType.INCOME -> MaterialTheme.colorScheme.onPrimaryContainer
        TransactionType.REFUND -> MaterialTheme.colorScheme.onTertiaryContainer
        TransactionType.TRANSFER, TransactionType.SETTLEMENT -> MaterialTheme.colorScheme.onSecondaryContainer
        TransactionType.EXPENSE, TransactionType.ADJUSTMENT -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun getCategoryIcon(category: String, type: TransactionType): ImageVector {
    return when (type) {
        TransactionType.TRANSFER, TransactionType.SETTLEMENT -> Icons.Rounded.SwapHoriz
        else -> when (category.lowercase()) {
            "groceries" -> Icons.Rounded.ShoppingCart
            "dining" -> Icons.Rounded.Restaurant
            "salary", "income" -> Icons.Rounded.Payments
            "shopping" -> Icons.Rounded.ShoppingBag
            "fuel" -> Icons.Rounded.LocalGasStation
            "utilities", "bills", "emi" -> Icons.AutoMirrored.Rounded.ReceiptLong
            "credit card payment" -> Icons.Rounded.CreditCard
            else -> Icons.Rounded.Category
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsScreenPreview() {
    Coconut_PfaTheme {
        TransactionsScreen(onNavigate = {})
    }
}
