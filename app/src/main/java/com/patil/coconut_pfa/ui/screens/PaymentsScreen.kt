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
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.patil.coconut_pfa.ui.components.CoconutDropdownMenu
import com.patil.coconut_pfa.ui.components.CoconutDropdownMenuItem
import com.patil.coconut_pfa.ui.components.GradientExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.model.BillKind
import com.patil.coconut_pfa.data.model.CardPaymentStatus
import com.patil.coconut_pfa.data.model.DueItem
import com.patil.coconut_pfa.data.model.DueTimeframe
import com.patil.coconut_pfa.data.model.PaymentHistoryItem
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.EmptyState
import com.patil.coconut_pfa.ui.components.SectionHeader
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.viewmodel.CreditCardPaymentItem
import com.patil.coconut_pfa.ui.viewmodel.PaymentsUiState
import com.patil.coconut_pfa.ui.viewmodel.PaymentsViewModel

@Composable
fun PaymentsScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentsViewModel? = null
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Cards", "History")

    val uiState by viewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf(PaymentsUiState(isLoading = false))
    }

    var statementToEdit by remember { mutableStateOf<CreditCardPaymentItem?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            GradientExtendedFloatingActionButton(
                onClick = { onNavigate(Screen.AddBill()) },
                icon = { Icon(Icons.Rounded.Add, contentDescription = "Add Bill") },
                text = { Text("Add Bill") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary Header Row: "Due in 7 days ₹X · Overdue ₹Y" & People Chip
            PaymentsSummaryHeader(
                uiState = uiState,
                onNavigate = onNavigate
            )

            // Sub-tabs: Upcoming | Cards | History
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> UpcomingTabContent(
                    uiState = uiState,
                    onNavigate = onNavigate,
                    onSkipOccurrence = { occId -> viewModel?.skipOccurrence(occId) },
                    onDeactivateBill = { billId -> viewModel?.deactivateBill(billId) }
                )
                1 -> CardsTabContent(
                    uiState = uiState,
                    onNavigate = onNavigate,
                    onEditStatementClick = { cardItem -> statementToEdit = cardItem }
                )
                2 -> HistoryTabContent(uiState = uiState)
            }
        }
    }

    // Edit Statement Amount Dialog
    statementToEdit?.let { cardItem ->
        cardItem.statement?.let { st ->
            EditStatementDialog(
                currentAmountPaise = st.amountPaise,
                currentMinDuePaise = st.minDuePaise ?: (st.amountPaise * 5 / 100),
                onDismiss = { statementToEdit = null },
                onSave = { newAmount, newMinDue ->
                    viewModel?.updateCardStatement(st.id, newAmount, newMinDue)
                    statementToEdit = null
                }
            )
        }
    }
}

/**
 * Summary Header Row: "Due in 7 days ₹X · Overdue ₹Y" and "People: owe you ₹A · you owe ₹B" chip.
 */
@Composable
private fun PaymentsSummaryHeader(
    uiState: PaymentsUiState,
    onNavigate: (Screen) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Payment Summary",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Due in 7 days and Overdue amounts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Due in 7 days",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    AmountText(
                        amountInPaise = uiState.dueIn7DaysInPaise,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Overdue",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    AmountText(
                        amountInPaise = uiState.overdueInPaise,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // "People: owe you ₹A · you owe ₹B" chip
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Screen.SplitAndIOUs) },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.CallSplit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "People",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "owe you ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AmountText(
                            amountInPaise = uiState.peopleOweYouInPaise,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32) // Green
                        )
                        Text(
                            text = " · you owe ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AmountText(
                            amountInPaise = uiState.youOwePeopleInPaise,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Upcoming Tab Content displaying sectioned list: Overdue / This week / Later.
 */
@Composable
private fun UpcomingTabContent(
    uiState: PaymentsUiState,
    onNavigate: (Screen) -> Unit,
    onSkipOccurrence: (String) -> Unit,
    onDeactivateBill: (String) -> Unit
) {
    val overdueItems = remember(uiState.upcomingItems) {
        uiState.upcomingItems.filter { it.timeframe == DueTimeframe.OVERDUE }
    }
    val thisWeekItems = remember(uiState.upcomingItems) {
        uiState.upcomingItems.filter { it.timeframe == DueTimeframe.THIS_WEEK }
    }
    val laterItems = remember(uiState.upcomingItems) {
        uiState.upcomingItems.filter { it.timeframe == DueTimeframe.LATER }
    }

    if (uiState.upcomingItems.isEmpty()) {
        EmptyState(
            title = "No Upcoming Dues",
            description = "You have no bills, EMIs, or card payments due right now.",
            icon = Icons.Rounded.CheckCircle,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            if (overdueItems.isNotEmpty()) {
                item(key = "header_overdue") {
                    SectionHeader(
                        title = "Overdue",
                        subtitle = "Action required immediately",
                        actionText = null
                    )
                }
                items(overdueItems, key = { it.id }) { item ->
                    DueItemRow(
                        item = item,
                        onPayClick = {
                            onNavigate(Screen.AddTransaction(billOccurrenceId = item.occurrenceId ?: item.id))
                        },
                        onSkipClick = {
                            item.occurrenceId?.let { onSkipOccurrence(it) }
                        },
                        onDeactivateClick = {
                            item.billId?.let { onDeactivateBill(it) }
                        }
                    )
                }
            }

            if (thisWeekItems.isNotEmpty()) {
                item(key = "header_this_week") {
                    SectionHeader(
                        title = "This Week",
                        subtitle = "Upcoming dues in next 7 days",
                        actionText = null
                    )
                }
                items(thisWeekItems, key = { it.id }) { item ->
                    DueItemRow(
                        item = item,
                        onPayClick = {
                            onNavigate(Screen.AddTransaction(billOccurrenceId = item.occurrenceId ?: item.id))
                        },
                        onSkipClick = {
                            item.occurrenceId?.let { onSkipOccurrence(it) }
                        },
                        onDeactivateClick = {
                            item.billId?.let { onDeactivateBill(it) }
                        }
                    )
                }
            }

            if (laterItems.isNotEmpty()) {
                item(key = "header_later") {
                    SectionHeader(
                        title = "Later",
                        subtitle = "Upcoming dues later this month",
                        actionText = null
                    )
                }
                items(laterItems, key = { it.id }) { item ->
                    DueItemRow(
                        item = item,
                        onPayClick = {
                            onNavigate(Screen.AddTransaction(billOccurrenceId = item.occurrenceId ?: item.id))
                        },
                        onSkipClick = {
                            item.occurrenceId?.let { onSkipOccurrence(it) }
                        },
                        onDeactivateClick = {
                            item.billId?.let { onDeactivateBill(it) }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Single row item for a due item in Upcoming section.
 */
@Composable
private fun DueItemRow(
    item: DueItem,
    onPayClick: () -> Unit,
    onSkipClick: () -> Unit,
    onDeactivateClick: () -> Unit
) {
    val isOverdue = item.isOverdue
    val containerColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getKindIcon(item.kind),
                        contentDescription = item.kind.displayName,
                        tint = if (isOverdue) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Due: ${item.dueDate} • ${item.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    item.accountName?.let { acc ->
                        Text(
                            text = "Pay from: $acc",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AmountText(
                        amountInPaise = item.amountInPaise,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "More Options",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        CoconutDropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            CoconutDropdownMenuItem(
                                text = "Skip occurrence",
                                onClick = {
                                    menuExpanded = false
                                    onSkipClick()
                                }
                            )
                            if (item.billId != null) {
                                CoconutDropdownMenuItem(
                                    text = "Deactivate Bill",
                                    onClick = {
                                        menuExpanded = false
                                        onDeactivateClick()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onPayClick,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Pay",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Cards Tab Content showing one card per credit card with statement amount, min due, status chip, etc.
 */
@Composable
private fun CardsTabContent(
    uiState: PaymentsUiState,
    onNavigate: (Screen) -> Unit,
    onEditStatementClick: (CreditCardPaymentItem) -> Unit
) {
    if (uiState.cardItems.isEmpty()) {
        EmptyState(
            title = "No Credit Cards Found",
            description = "Add a Credit Card account to manage statements and due dates.",
            icon = Icons.Rounded.CreditCard,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.cardItems, key = { it.account.id }) { cardItem ->
                CreditCardBillCard(
                    cardItem = cardItem,
                    onRecordPaymentClick = {
                        val occId = cardItem.statement?.id
                        onNavigate(Screen.AddTransaction(billOccurrenceId = occId))
                    },
                    onEditStatementClick = { onEditStatementClick(cardItem) }
                )
            }
        }
    }
}

/**
 * Detailed card composable for Credit Card tab.
 */
@Composable
private fun CreditCardBillCard(
    cardItem: CreditCardPaymentItem,
    onRecordPaymentClick: () -> Unit,
    onEditStatementClick: () -> Unit
) {
    val card = cardItem.account

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
            // Header Row with Card Name and Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = card.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        card.last4?.let { last4 ->
                            Text(
                                text = "•• $last4",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(status = cardItem.status)
                    if (cardItem.statement != null) {
                        IconButton(
                            onClick = onEditStatementClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit Statement",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details Grid: Statement Amount, Minimum Due, Due Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Statement Due",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AmountText(
                        amountInPaise = cardItem.statementAmountPaise,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "Minimum Due",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AmountText(
                        amountInPaise = cardItem.minimumDuePaise,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = cardItem.dueDate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Unbilled this cycle banner
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
                    Text(
                        text = "Unbilled this cycle:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AmountText(
                        amountInPaise = cardItem.unbilledAmountPaise,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Record Payment Button
            OutlinedButton(
                onClick = onRecordPaymentClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Payment")
            }
        }
    }
}

@Composable
private fun StatusChip(status: CardPaymentStatus) {
    val (bgColor, textColor, icon) = when (status) {
        CardPaymentStatus.PAID -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Rounded.CheckCircle)
        CardPaymentStatus.UNPAID -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Rounded.Warning)
        CardPaymentStatus.PARTIAL -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Rounded.Event)
        CardPaymentStatus.OVERDUE -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Rounded.Warning)
    }

    Surface(
        shape = CircleShape,
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

/**
 * History Tab Content listing past bill/CC/EMI payments.
 */
@Composable
private fun HistoryTabContent(uiState: PaymentsUiState) {
    if (uiState.historyItems.isEmpty()) {
        EmptyState(
            title = "No Payment History",
            description = "Payments made for recurring bills or credit card statements will appear here.",
            icon = Icons.Rounded.Receipt,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.historyItems, key = { it.id }) { item ->
                PaymentHistoryRow(item = item)
            }
        }
    }
}

@Composable
private fun PaymentHistoryRow(item: PaymentHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getKindIcon(item.kind),
                        contentDescription = item.kind.displayName,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${item.paymentDate} • ${item.accountName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                AmountText(
                    amountInPaise = item.amountInPaise,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32) // Paid green
                )
                Text(
                    text = item.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
private fun EditStatementDialog(
    currentAmountPaise: Long,
    currentMinDuePaise: Long,
    onDismiss: () -> Unit,
    onSave: (newAmountPaise: Long, newMinDuePaise: Long) -> Unit
) {
    var amountText by remember { mutableStateOf((currentAmountPaise / 100.0).toString()) }
    var minDueText by remember { mutableStateOf((currentMinDuePaise / 100.0).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Card Statement", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Statement Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = minDueText,
                    onValueChange = { minDueText = it },
                    label = { Text("Minimum Due (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtDouble = amountText.toDoubleOrNull() ?: 0.0
                    val minDueDouble = minDueText.toDoubleOrNull() ?: 0.0
                    onSave((amtDouble * 100).toLong(), (minDueDouble * 100).toLong())
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getKindIcon(kind: BillKind): ImageVector {
    return when (kind) {
        BillKind.RENT -> Icons.Rounded.Home
        BillKind.UTILITY -> Icons.Rounded.Receipt
        BillKind.INSURANCE -> Icons.Rounded.Security
        BillKind.SUBSCRIPTION -> Icons.Rounded.Subscriptions
        BillKind.EMI -> Icons.Rounded.Payments
        BillKind.OTHER -> Icons.Rounded.Event
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentsScreenPreview() {
    Coconut_PfaTheme {
        PaymentsScreen(onNavigate = {})
    }
}
