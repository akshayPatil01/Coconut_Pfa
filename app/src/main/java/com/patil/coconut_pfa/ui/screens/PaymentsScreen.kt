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
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.model.Account
import com.patil.coconut_pfa.data.model.BillKind
import com.patil.coconut_pfa.data.model.CardPaymentStatus
import com.patil.coconut_pfa.data.model.DueItem
import com.patil.coconut_pfa.data.model.DueTimeframe
import com.patil.coconut_pfa.data.model.PaymentHistoryItem
import com.patil.coconut_pfa.data.repository.FakeData
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.SectionHeader
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme

@Composable
fun PaymentsScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Cards", "History")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigate(Screen.AddBill) },
                icon = { Icon(Icons.Rounded.Add, contentDescription = "Add Bill") },
                text = { Text("Add Bill") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary Header Row: "Due in 7 days ₹X · Overdue ₹Y" & People Chip
            PaymentsSummaryHeader(onNavigate = onNavigate)

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
                0 -> UpcomingTabContent(onNavigate = onNavigate)
                1 -> CardsTabContent(onNavigate = onNavigate)
                2 -> HistoryTabContent()
            }
        }
    }
}

/**
 * Summary Header Row: "Due in 7 days ₹X · Overdue ₹Y" and "People: owe you ₹A · you owe ₹B" chip.
 */
@Composable
private fun PaymentsSummaryHeader(
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
                        amountInPaise = FakeData.dueIn7DaysInPaise,
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
                        amountInPaise = FakeData.overdueInPaise,
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
                            amountInPaise = FakeData.peopleOweYouInPaise,
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
                            amountInPaise = FakeData.youOwePeopleInPaise,
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
    onNavigate: (Screen) -> Unit,
) {
    val overdueItems = remember(FakeData.dueItems.size) {
        FakeData.dueItems.filter { it.timeframe == DueTimeframe.OVERDUE }
    }
    val thisWeekItems = remember(FakeData.dueItems.size) {
        FakeData.dueItems.filter { it.timeframe == DueTimeframe.THIS_WEEK }
    }
    val laterItems = remember(FakeData.dueItems.size) {
        FakeData.dueItems.filter { it.timeframe == DueTimeframe.LATER }
    }

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
                DueItemRow(item = item, onPayClick = { onNavigate(Screen.AddTransaction()) })
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
                DueItemRow(item = item, onPayClick = { onNavigate(Screen.AddTransaction()) })
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
                DueItemRow(item = item, onPayClick = { onNavigate(Screen.AddTransaction()) })
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
) {
    val isOverdue = item.isOverdue
    val containerColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface

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
                        text = "Due: ${item.dueDate} • ${item.kind.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                AmountText(
                    amountInPaise = item.amountInPaise,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )

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
    onNavigate: (Screen) -> Unit,
) {
    val cards = remember { FakeData.creditCardAccounts }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(cards, key = { it.id }) { card ->
            CreditCardBillCard(
                card = card,
                onRecordPaymentClick = { onNavigate(Screen.AddTransaction()) }
            )
        }
    }
}

/**
 * Detailed card composable for Credit Card tab.
 */
@Composable
private fun CreditCardBillCard(
    card: Account,
    onRecordPaymentClick: () -> Unit,
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
                        card.accountNumberLast4?.let { last4 ->
                            Text(
                                text = "${card.bankName ?: ""} •• $last4",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                StatusChip(status = card.cardStatus)
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
                        amountInPaise = card.dueAmountInPaise,
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
                        amountInPaise = card.minimumDueInPaise,
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
                        text = card.dueDate ?: "N/A",
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
                        amountInPaise = card.unbilledAmountInPaise,
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
private fun HistoryTabContent() {
    val history = remember { FakeData.paymentHistory }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(history, key = { it.id }) { item ->
            PaymentHistoryRow(item = item)
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
