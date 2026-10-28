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
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
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
import com.patil.coconut_pfa.ui.components.CoconutDropdownMenu
import com.patil.coconut_pfa.ui.components.CoconutDropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.CategoryEntity
import com.patil.coconut_pfa.data.local.entity.DbTxnSource
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.repository.FakeData
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.EmptyState
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.theme.GradientOrangeCoralStart
import com.patil.coconut_pfa.ui.theme.PastelCoral
import com.patil.coconut_pfa.ui.theme.PastelEmerald
import com.patil.coconut_pfa.ui.theme.PastelPurple
import com.patil.coconut_pfa.ui.theme.PastelSkyBlue
import com.patil.coconut_pfa.ui.viewmodel.DayGroupedTransactions
import com.patil.coconut_pfa.ui.viewmodel.TransactionsUiState
import com.patil.coconut_pfa.ui.viewmodel.TransactionsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PillChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = if (isSelected) GradientOrangeCoralStart else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel? = null
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf(TransactionsUiState(isLoading = false))
    }

    // Dropdown visibility states
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var cycleDropdownExpanded by remember { mutableStateOf(false) }
    var sourceDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Search Bar
            PaddingValues(horizontal = 16.dp, vertical = 8.dp).let { padding ->
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel?.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding),
                    placeholder = { Text("Search payee, category, note, amount...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel?.updateSearchQuery("") }) {
                                Icon(imageVector = Icons.Rounded.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = GradientOrangeCoralStart,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
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
                        PillChip(
                            isSelected = uiState.selectedCycle != "All Time",
                            onClick = { cycleDropdownExpanded = true },
                            text = uiState.selectedCycle
                        )
                        CoconutDropdownMenu(
                            expanded = cycleDropdownExpanded,
                            onDismissRequest = { cycleDropdownExpanded = false }
                        ) {
                            listOf("Current Cycle", "Previous Cycle", "All Time").forEach { option ->
                                CoconutDropdownMenuItem(
                                    text = option,
                                    isSelected = option == uiState.selectedCycle,
                                    onClick = {
                                        viewModel?.updateSelectedCycle(option)
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
                        PillChip(
                            isSelected = uiState.selectedType != null,
                            onClick = { typeDropdownExpanded = true },
                            text = uiState.selectedType?.name ?: "Type: All"
                        )
                        CoconutDropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false }
                        ) {
                            CoconutDropdownMenuItem(
                                text = "All Types",
                                isSelected = uiState.selectedType == null,
                                onClick = {
                                    viewModel?.updateSelectedType(null)
                                    typeDropdownExpanded = false
                                }
                            )
                            DbTxnType.entries.forEach { type ->
                                CoconutDropdownMenuItem(
                                    text = type.name,
                                    isSelected = uiState.selectedType == type,
                                    onClick = {
                                        viewModel?.updateSelectedType(type)
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
                        val selectedCatName = uiState.selectedCategory?.let { catId ->
                            uiState.categoryMap[catId]?.name ?: catId
                        } ?: "Category: All"
                        PillChip(
                            isSelected = uiState.selectedCategory != null,
                            onClick = { categoryDropdownExpanded = true },
                            text = selectedCatName
                        )
                        CoconutDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            CoconutDropdownMenuItem(
                                text = "All Categories",
                                isSelected = uiState.selectedCategory == null,
                                onClick = {
                                    viewModel?.updateSelectedCategory(null)
                                    categoryDropdownExpanded = false
                                }
                            )
                            uiState.categoryMap.values.distinctBy { it.name }.forEach { cat ->
                                CoconutDropdownMenuItem(
                                    text = cat.name,
                                    isSelected = uiState.selectedCategory == cat.id,
                                    onClick = {
                                        viewModel?.updateSelectedCategory(cat.id)
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
                        val selectedAccName = uiState.selectedAccount?.let { accId ->
                            uiState.accountMap[accId]?.name ?: accId
                        } ?: "Account: All"
                        PillChip(
                            isSelected = uiState.selectedAccount != null,
                            onClick = { accountDropdownExpanded = true },
                            text = selectedAccName
                        )
                        CoconutDropdownMenu(
                            expanded = accountDropdownExpanded,
                            onDismissRequest = { accountDropdownExpanded = false }
                        ) {
                            CoconutDropdownMenuItem(
                                text = "All Accounts",
                                isSelected = uiState.selectedAccount == null,
                                onClick = {
                                    viewModel?.updateSelectedAccount(null)
                                    accountDropdownExpanded = false
                                }
                            )
                            uiState.accountMap.values.forEach { acc ->
                                CoconutDropdownMenuItem(
                                    text = acc.name,
                                    isSelected = uiState.selectedAccount == acc.id,
                                    onClick = {
                                        viewModel?.updateSelectedAccount(acc.id)
                                        accountDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 5. Source Filter Chip
                item {
                    Box {
                        PillChip(
                            isSelected = uiState.selectedSource != null,
                            onClick = { sourceDropdownExpanded = true },
                            text = uiState.selectedSource?.name ?: "Source: All"
                        )
                        CoconutDropdownMenu(
                            expanded = sourceDropdownExpanded,
                            onDismissRequest = { sourceDropdownExpanded = false }
                        ) {
                            CoconutDropdownMenuItem(
                                text = "All Sources",
                                isSelected = uiState.selectedSource == null,
                                onClick = {
                                    viewModel?.updateSelectedSource(null)
                                    sourceDropdownExpanded = false
                                }
                            )
                            DbTxnSource.entries.forEach { src ->
                                CoconutDropdownMenuItem(
                                    text = src.name,
                                    isSelected = uiState.selectedSource == src,
                                    onClick = {
                                        viewModel?.updateSelectedSource(src)
                                        sourceDropdownExpanded = false
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
                // SMS Review Banner at Top of Feed
                item {
                    if (FakeData.smsReviewItems.isNotEmpty()) {
                        SmsBannerInTransactions(
                            unreadCount = FakeData.smsReviewItems.size,
                            onClick = { onNavigate(Screen.ReviewInbox) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                if (uiState.groupedTransactions.isEmpty() && !uiState.isLoading) {
                    item {
                        EmptyState(
                            title = "No Transactions Found",
                            description = "Try adjusting your search query or filter chips.",
                            icon = Icons.Rounded.Search
                        )
                    }
                } else {
                    uiState.groupedTransactions.forEach { dayGroup ->
                        // Group Header with Day Summary
                        item(key = "header_${dayGroup.dateLabel}") {
                            DayHeaderEntity(dayGroup = dayGroup)
                        }

                        // Transaction Items with Swipe Gestures
                        items(dayGroup.transactions, key = { it.id }) { tx ->
                            SwipeableTransactionItem(
                                tx = tx,
                                accountMap = uiState.accountMap,
                                categoryMap = uiState.categoryMap,
                                onTap = { onNavigate(Screen.AddTransaction(txnId = tx.id)) },
                                onDuplicate = { onNavigate(Screen.AddTransaction(duplicateTxnId = tx.id)) },
                                onDelete = {
                                    viewModel?.softDeleteTransaction(tx.id)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Transaction deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel?.restoreTransaction(tx.id)
                                        }
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Swipeable wrapper using Material3 SwipeToDismissBox.
 * Swipe left -> Delete (soft delete with Undo)
 * Swipe right -> Duplicate (opens Add pre-filled with today's date)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTransactionItem(
    tx: TxnEntity,
    accountMap: Map<String, AccountEntity>,
    categoryMap: Map<String, CategoryEntity>,
    onTap: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> { // Swipe left -> Delete
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> { // Swipe right -> Duplicate
                    onDuplicate()
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Duplicate",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Duplicate",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        },
        content = {
            TransactionEntityItemRow(
                tx = tx,
                accountMap = accountMap,
                categoryMap = categoryMap,
                onClick = onTap
            )
        }
    )
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
private fun DayHeaderEntity(
    dayGroup: DayGroupedTransactions
) {
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
                text = dayGroup.dateLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (dayGroup.daySpentPaise > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Spent: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AmountText(
                            amountInPaise = dayGroup.daySpentPaise,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (dayGroup.dayReceivedPaise > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Recv: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AmountText(
                            amountInPaise = dayGroup.dayReceivedPaise,
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
 * Individual Transaction item card with category icon, payee, account details, note, and [AmountText].
 */
@Composable
private fun TransactionEntityItemRow(
    tx: TxnEntity,
    accountMap: Map<String, AccountEntity>,
    categoryMap: Map<String, CategoryEntity>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val account = accountMap[tx.accountId]
    val targetAccount = tx.toAccountId?.let { accountMap[it] }
    val category = tx.categoryId?.let { categoryMap[it] }

    val isPositive = tx.type == DbTxnType.INCOME || tx.type == DbTxnType.REFUND
    val amountColor = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    val categoryName = category?.name ?: tx.categoryId ?: "General"
    
    val timeString = remember(tx.occurredAt) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(tx.occurredAt))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon inside colored rounded box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = getDbCategoryContainerColor(tx.type),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getDbCategoryIcon(categoryName, tx.type),
                    contentDescription = categoryName,
                    tint = getDbCategoryIconColor(tx.type),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tx.payee ?: categoryName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Account & Target account text
                val accountDisplay = when (tx.type) {
                    DbTxnType.TRANSFER, DbTxnType.SETTLEMENT -> {
                        val fromName = account?.name ?: "Account"
                        val toName = targetAccount?.name ?: "Target Account"
                        "$fromName ↔ $toName"
                    }
                    else -> account?.name ?: categoryName
                }

                Text(
                    text = accountDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!tx.note.isNullOrBlank()) {
                    Text(
                        text = tx.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Amount Text & Time
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
                        amountInPaise = tx.amountPaise,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                }

                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun getDbCategoryContainerColor(type: DbTxnType): Color {
    return when (type) {
        DbTxnType.INCOME -> PastelEmerald
        DbTxnType.REFUND -> PastelSkyBlue
        DbTxnType.TRANSFER, DbTxnType.SETTLEMENT -> PastelPurple
        DbTxnType.EXPENSE, DbTxnType.ADJUSTMENT -> PastelCoral
    }
}

@Composable
private fun getDbCategoryIconColor(type: DbTxnType): Color {
    return when (type) {
        DbTxnType.INCOME -> Color(0xFF064E3B) // Dark Emerald
        DbTxnType.REFUND -> Color(0xFF0C4A6E) // Dark Sky
        DbTxnType.TRANSFER, DbTxnType.SETTLEMENT -> Color(0xFF4C1D95) // Dark Purple
        DbTxnType.EXPENSE, DbTxnType.ADJUSTMENT -> Color(0xFF7F1D1D) // Dark Red
    }
}

private fun getDbCategoryIcon(category: String, type: DbTxnType): ImageVector {
    return when (type) {
        DbTxnType.TRANSFER, DbTxnType.SETTLEMENT -> Icons.Rounded.SwapHoriz
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
