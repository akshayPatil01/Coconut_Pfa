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
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.model.Account
import com.patil.coconut_pfa.data.model.AccountType
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.SparklineGraph
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.theme.ExpenseRed
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
            ExtendedFloatingActionButton(
                onClick = { onNavigate(Screen.AddAccount()) },
                icon = { Icon(Icons.Rounded.Add, contentDescription = "Add Account") },
                text = { Text("Add Account") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Net Worth",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "Real-time",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AmountText(
                amountInPaise = netWorth,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sparkline Graph
            SparklineGraph(
                dataPoints = sparklinePoints,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                lineColor = MaterialTheme.colorScheme.primary,
                fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Assets vs Liabilities Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Assets pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = IncomeGreen.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Assets",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        AmountText(
                            amountInPaise = assets,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                }

                // Liabilities pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = ExpenseRed.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Liabilities",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        AmountText(
                            amountInPaise = liabilities,
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
