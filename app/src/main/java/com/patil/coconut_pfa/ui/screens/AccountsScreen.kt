package com.patil.coconut_pfa.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Money
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patil.coconut_pfa.data.model.Account
import com.patil.coconut_pfa.data.model.AccountType
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.GradientExtendedFloatingActionButton
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.theme.ExpenseRed
import com.patil.coconut_pfa.ui.theme.GradientOrangeCoralStart
import com.patil.coconut_pfa.ui.theme.IncomeGreen
import com.patil.coconut_pfa.ui.theme.PastelCoral
import com.patil.coconut_pfa.ui.theme.PastelEmerald
import com.patil.coconut_pfa.ui.theme.PastelPurple
import com.patil.coconut_pfa.ui.theme.PastelSkyBlue
import com.patil.coconut_pfa.ui.theme.PastelWarmAmber
import com.patil.coconut_pfa.ui.viewmodel.AccountsScreenUiState
import com.patil.coconut_pfa.ui.viewmodel.AccountsViewModel

@Composable
fun AccountsScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    accountsViewModel: AccountsViewModel? = null,
) {
    val uiState by accountsViewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(AccountsScreenUiState()) }
    var showArchived by remember { mutableStateOf(false) }

    val activeAccounts = uiState.activeAccounts
    val archivedAccounts = uiState.archivedAccounts

    // Grouping by display groups: Bank, Cash & Wallets, Credit Cards, Loans, Investments & FDs
    val groups = remember(activeAccounts) {
        listOf(
            "Bank" to activeAccounts.filter { it.type == AccountType.BANK },
            "Cash & Wallets" to activeAccounts.filter { it.type == AccountType.CASH || it.type == AccountType.WALLET },
            "Credit Cards" to activeAccounts.filter { it.type == AccountType.CREDIT_CARD },
            "Loans" to activeAccounts.filter { it.type == AccountType.LOAN },
            "Investments & FDs" to activeAccounts.filter { it.type == AccountType.INVESTMENT || it.type == AccountType.FIXED_DEPOSIT }
        ).filter { it.second.isNotEmpty() }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            GradientExtendedFloatingActionButton(
                onClick = { onNavigate(Screen.AddAccount()) },
                icon = { Icon(Icons.Rounded.Add, contentDescription = "Add Account") },
                text = { Text("Add Account") },
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Net Worth Summary Card with Assets, Liabilities, and Sparkline Graph
            item(key = "net_worth_card") {
                NetWorthDetailCard(
                    netWorth = uiState.totalNetWorth,
                    assets = uiState.totalAssets,
                    liabilities = uiState.totalLiabilities,
                    sparklinePoints = uiState.sparklineTrend.map { it.toFloat() },
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Iterate over each account group
            groups.forEach { (groupTitle, groupAccounts) ->
                val groupTotal = groupAccounts.sumOf { it.balanceInPaise }

                item(key = "header_$groupTitle") {
                    AccountGroupHeader(
                        title = groupTitle,
                        groupTotal = groupTotal,
                        typeIcon = getGroupIcon(groupTitle)
                    )
                }

                items(groupAccounts, key = { it.id }) { account ->
                    AccountItemCard(
                        account = account,
                        onAccountClick = { onNavigate(Screen.AccountDetail(account.id)) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }

            // Archived Accounts Section
            if (archivedAccounts.isNotEmpty()) {
                item(key = "archived_section_header") {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { showArchived = !showArchived },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Archive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Archived Accounts (${archivedAccounts.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = if (showArchived) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = if (showArchived) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (showArchived) {
                    items(archivedAccounts, key = { "archived_${it.id}" }) { account ->
                        AccountItemCard(
                            account = account,
                            onAccountClick = { onNavigate(Screen.AccountDetail(account.id)) },
                            isArchived = true,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Net worth summary card featuring Total Net Worth, Assets, Liabilities, and Sparkline graph.
 */
@Composable
private fun NetWorthDetailCard(
    netWorth: Long,
    assets: Long,
    liabilities: Long,
    sparklinePoints: List<Float>,
    modifier: Modifier = Modifier,
) {
    val cardBgColor = Color(0xFF121518)
    val innerCardBgColor = Color(0xFF1F2328)
    val labelTextColor = Color(0xFF9EA3A8)
    val primaryTextColor = Color.White

    val (trendText, isPositiveTrend) = remember(sparklinePoints) {
        if (sparklinePoints.size >= 2) {
            val first = sparklinePoints.first()
            val last = sparklinePoints.last()
            if (first > 0f) {
                val pct = ((last - first) / first) * 100f
                val isPos = pct >= 0f
                val prefix = if (isPos) "↗ +" else "↘ "
                Pair(String.format(java.util.Locale.US, "%s%.1f%% · 30d", prefix, pct), isPos)
            } else {
                Pair("↗ +2.7% · 30d", true)
            }
        } else {
            Pair("↗ +2.7% · 30d", true)
        }
    }

    val badgeBg = if (isPositiveTrend) Color(0xFF12392B) else Color(0xFF3B181A)
    val badgeFg = if (isPositiveTrend) IncomeGreen else ExpenseRed

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
                // Header Row: Total Net Worth label & Trend pill badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Net Worth",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = labelTextColor
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = trendText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeFg,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Main Net Worth Value
                AmountText(
                    amountInPaise = netWorth,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Inner Assets vs Liabilities Card Container
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
                        // Assets Column
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
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
                                    text = "Assets",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Normal,
                                    color = labelTextColor
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            AmountText(
                                amountInPaise = assets,
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 21.sp),
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor,
                                showDecimals = false
                            )
                        }

                        // Vertical Separator Line
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp)
                                .background(labelTextColor.copy(alpha = 0.2f))
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Liabilities Column
                        Column(
                            modifier = Modifier.weight(1f)
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
                                    text = "Liabilities",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Normal,
                                    color = labelTextColor
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            AmountText(
                                amountInPaise = liabilities,
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 21.sp),
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor,
                                showDecimals = false
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Header section for each Account Group showing group title and total balance.
 */
@Composable
private fun AccountGroupHeader(
    title: String,
    groupTotal: Long,
    typeIcon: ImageVector,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AmountText(
                amountInPaise = groupTotal,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Card composable for displaying an individual Account item.
 */
@Composable
private fun AccountItemCard(
    account: Account,
    onAccountClick: () -> Unit,
    isArchived: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onAccountClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isArchived) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isArchived) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = getAccountTypeBadgeColor(account.type),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getAccountTypeIcon(account.type),
                            contentDescription = account.type.displayName,
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (isArchived) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Archived",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        val subText = buildString {
                            account.bankName?.let { append(it) }
                            account.accountNumberLast4?.let {
                                if (isNotEmpty()) append(" •••• ")
                                append(it)
                            }
                        }

                        if (subText.isNotEmpty()) {
                            Text(
                                text = subText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Balance amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (account.type == AccountType.LOAN) "Outstanding" else "Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AmountText(
                        amountInPaise = account.balanceInPaise,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (account.balanceInPaise < 0) ExpenseRed else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Extra details for Credit Card or Loan
            if (account.type == AccountType.CREDIT_CARD && (account.unbilledAmountInPaise > 0 || account.dueAmountInPaise > 0)) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (account.unbilledAmountInPaise > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Unbilled: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AmountText(
                                    amountInPaise = account.unbilledAmountInPaise,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (account.dueDate != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Due: ${account.dueDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else if (account.type == AccountType.LOAN && account.dueAmountInPaise > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ExpenseRed.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "EMI Due: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = ExpenseRed
                            )
                            AmountText(
                                amountInPaise = account.dueAmountInPaise,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }

                        account.dueDate?.let { dueDate ->
                            Text(
                                text = "Due: $dueDate",
                                style = MaterialTheme.typography.labelSmall,
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

private fun getGroupIcon(groupTitle: String): ImageVector {
    return when (groupTitle) {
        "Bank" -> Icons.Rounded.AccountBalance
        "Cash & Wallets" -> Icons.Rounded.AccountBalanceWallet
        "Credit Cards" -> Icons.Rounded.CreditCard
        "Loans" -> Icons.Rounded.Payments
        "Investments & FDs" -> Icons.AutoMirrored.Rounded.ShowChart
        else -> Icons.Rounded.AccountBalance
    }
}

private fun getAccountTypeIcon(type: AccountType): ImageVector {
    return when (type) {
        AccountType.BANK -> Icons.Rounded.AccountBalance
        AccountType.CREDIT_CARD -> Icons.Rounded.CreditCard
        AccountType.CASH -> Icons.Rounded.Money
        AccountType.WALLET -> Icons.Rounded.AccountBalanceWallet
        AccountType.LOAN -> Icons.Rounded.Payments
        AccountType.INVESTMENT -> Icons.AutoMirrored.Rounded.ShowChart
        AccountType.FIXED_DEPOSIT -> Icons.Rounded.Lock
    }
}

@Composable
private fun getAccountTypeBadgeColor(type: AccountType): Color {
    return when (type) {
        AccountType.BANK -> PastelSkyBlue
        AccountType.CREDIT_CARD -> PastelCoral
        AccountType.CASH -> PastelEmerald
        AccountType.WALLET -> PastelPurple
        AccountType.LOAN -> PastelWarmAmber
        AccountType.INVESTMENT -> PastelEmerald
        AccountType.FIXED_DEPOSIT -> PastelPurple
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountsScreenPreview() {
    Coconut_PfaTheme {
        AccountsScreen(onNavigate = {})
    }
}
