package com.patil.coconut_pfa.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patil.coconut_pfa.data.model.DueItem
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.EmptyState
import com.patil.coconut_pfa.ui.components.SectionHeader
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.theme.GradientBlueCyanEnd
import com.patil.coconut_pfa.ui.theme.GradientBlueCyanStart
import com.patil.coconut_pfa.ui.theme.PastelEmerald
import com.patil.coconut_pfa.ui.theme.PastelPurple
import com.patil.coconut_pfa.ui.theme.PastelSkyBlue
import com.patil.coconut_pfa.ui.viewmodel.HomeUiState
import com.patil.coconut_pfa.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshData() },
        modifier = modifier.fillMaxSize()
    ) {
        HomeScreenContent(
            uiState = uiState,
            onNavigate = onNavigate
        )
    }
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (!uiState.hasAccounts && !uiState.isLoading) {
            // Empty state for brand new user with no accounts
            EmptyState(
                title = "No accounts added yet",
                description = "Add your first bank account, credit card, or cash wallet to get started.",
                icon = Icons.Rounded.AccountBalance,
                actionLabel = "Add your first account",
                onActionClick = { onNavigate(Screen.AddAccount()) }
            )
        } else {
            // 1. Available Balance Card Carousel
            AvailableBalanceCard(
                netWorthInPaise = uiState.netWorthInPaise
            )

            // 2. Quick Action Pills Row
            QuickActionPillsRow(onNavigate = onNavigate)

            // Optional: SMS Review Inbox Banner Card
            if (uiState.pendingSmsCount > 0) {
                SmsReviewBannerCard(
                    unreadCount = uiState.pendingSmsCount,
                    onClick = { onNavigate(Screen.ReviewInbox) }
                )
            }

            // 3. Spending Breakdown Section
            SpendingBreakdownSection()

            // 4. Recent Expenses Section
            RecentExpensesSection(onViewAll = { onNavigate(Screen.Transactions()) })

            // 5. "Due soon" Section
            DueSoonSection(
                dueItems = uiState.dueSoonItems,
                onSeeAllClick = { onNavigate(Screen.Payments) },
                onItemClick = { onNavigate(Screen.Payments) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AvailableBalanceCard(
    netWorthInPaise: Long,
    modifier: Modifier = Modifier
) {
    com.patil.coconut_pfa.ui.components.PaymentCardView(
        bankName = null,
        networkLogo = com.patil.coconut_pfa.domain.CardNetwork.MASTERCARD,
        cardTypeLabel = "",
        nickname = null,
        numberText = "•••• •••• •••• 8635",
        cardholderName = "Will Jonas",
        expiry = "12/28",
        colour = "blue",
        modifier = modifier,
        headerContent = {
            Column {
                Text(
                    text = "Available Balance",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                AmountText(
                    amountInPaise = netWorthInPaise,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    )
}

@Composable
private fun QuickActionPillsRow(onNavigate: (Screen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionPill(icon = Icons.Rounded.Add, label = "Add Expense", onClick = { onNavigate(Screen.AddTransaction()) })
        ActionPill(icon = Icons.Rounded.Category, label = "Categories", onClick = { /* Mock for now */ })
        ActionPill(icon = Icons.Rounded.PieChart, label = "Analytics", onClick = { onNavigate(Screen.Reports) })
        ActionPill(icon = Icons.Rounded.Sms, label = "SMS Review", onClick = { onNavigate(Screen.ReviewInbox) })
    }
}

@Composable
private fun ActionPill(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SpendingBreakdownSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Legend
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Spending",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                SpendLegendItem(color = PastelEmerald, label = "Groceries", amount = "₹4,500")
                Spacer(modifier = Modifier.height(12.dp))
                SpendLegendItem(color = PastelPurple, label = "Dining", amount = "₹2,100")
                Spacer(modifier = Modifier.height(12.dp))
                SpendLegendItem(color = PastelSkyBlue, label = "Transport", amount = "₹1,800")
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Canvas Donut Chart
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 24f
                    drawArc(
                        color = PastelEmerald,
                        startAngle = -90f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = PastelPurple,
                        startAngle = 90f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = PastelSkyBlue,
                        startAngle = 180f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "65%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SpendLegendItem(color: Color, label: String, amount: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RecentExpensesSection(onViewAll: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Expenses",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "View All >",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onViewAll() }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Mock Transactions
        RecentExpenseCard(
            icon = Icons.Rounded.ShoppingCart,
            iconBgColor = PastelEmerald,
            title = "Supermarket",
            timestamp = "Today, 10:45 AM",
            amount = "-₹1,250"
        )
        RecentExpenseCard(
            icon = Icons.Rounded.Restaurant,
            iconBgColor = PastelPurple,
            title = "Cafe Mocha",
            timestamp = "Yesterday, 4:20 PM",
            amount = "-₹450"
        )
        RecentExpenseCard(
            icon = Icons.Rounded.LocalGasStation,
            iconBgColor = PastelSkyBlue,
            title = "Shell Station",
            timestamp = "12 Oct, 9:00 AM",
            amount = "-₹2,000"
        )
    }
}

@Composable
private fun RecentExpenseCard(icon: ImageVector, iconBgColor: Color, title: String, timestamp: String, amount: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconBgColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Card banner for SMS Review Inbox ("N SMS to review").
 */
@Composable
private fun SmsReviewBannerCard(
    unreadCount: Int,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Inbox,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$unreadCount SMS to review",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Text(text = "$unreadCount")
                    }
                }
                Text(
                    text = "Tap to review detected bank SMS transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Review SMS",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Due soon section listing upcoming dues. Items marked [DueItem.isOverdue] are highlighted with red text/badge/card styling.
 */
@Composable
private fun DueSoonSection(
    dueItems: List<DueItem>,
    onSeeAllClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Column {
        SectionHeader(
            title = "Due soon",
            subtitle = "Bills and repayments due in 7 days or overdue",
            actionText = "See All",
            onActionClick = onSeeAllClick,
            actionIcon = Icons.AutoMirrored.Rounded.ArrowForward
        )

        if (dueItems.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onItemClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "No bills or repayments due soon",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            dueItems.forEach { due ->
                val isOverdue = due.isOverdue
                val containerColor = if (isOverdue) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
                val contentColor = if (isOverdue) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onItemClick),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    imageVector = if (isOverdue) Icons.Rounded.Warning else Icons.Rounded.Event,
                                    contentDescription = null,
                                    tint = if (isOverdue) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = due.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = contentColor
                                    )
                                    if (isOverdue) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.error
                                        ) {
                                            Text(
                                                text = "OVERDUE",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onError,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "Due: ${due.dueDate} • ${due.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isOverdue) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        AmountText(
                            amountInPaise = due.amountInPaise,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverdue) Color.Red else contentColor
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    Coconut_PfaTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                hasAccounts = true,
                netWorthInPaise = 15000000L,
                sparklineData = listOf(100f, 120f, 130f, 150f),
                pendingSmsCount = 3,
                cycleStartDateFormatted = "25 Oct",
                cycleEndDateFormatted = "24 Nov",
                receivedInPaise = 25000000L,
                spentInPaise = 8500000L,
                unbilledCreditCards = emptyList(),
                dueSoonItems = emptyList()
            ),
            onNavigate = {}
        )
    }
}
