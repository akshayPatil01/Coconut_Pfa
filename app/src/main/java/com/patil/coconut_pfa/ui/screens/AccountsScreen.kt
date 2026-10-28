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
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Money
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.model.Account
import com.patil.coconut_pfa.data.model.AccountType
import com.patil.coconut_pfa.data.repository.FakeData
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.SparklineGraph
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme

@Composable
fun AccountsScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Group accounts by AccountType or specific custom groups
    val accountsList = FakeData.accounts
    val totalNetWorth = FakeData.netWorthInPaise
    val totalAssets = FakeData.totalAssetsInPaise
    val totalLiabilities = FakeData.totalLiabilitiesInPaise

    // Grouping by display groups: Bank, Cash & wallets, Credit cards, Loans, Investments & FDs
    val groups = remember(accountsList.size) {
        listOf(
            "Bank" to accountsList.filter { it.type == AccountType.BANK },
            "Cash & Wallets" to accountsList.filter { it.type == AccountType.CASH || it.type == AccountType.WALLET },
            "Credit Cards" to accountsList.filter { it.type == AccountType.CREDIT_CARD },
            "Loans" to accountsList.filter { it.type == AccountType.LOAN },
            "Investments & FDs" to accountsList.filter { it.type == AccountType.INVESTMENT || it.type == AccountType.FIXED_DEPOSIT }
        ).filter { it.second.isNotEmpty() }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigate(Screen.AddAccount) },
                icon = { Icon(Icons.Rounded.Add, contentDescription = "Add Account") },
                text = { Text("Add Account") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
                    netWorth = totalNetWorth,
                    assets = totalAssets,
                    liabilities = totalLiabilities,
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
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
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
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Net Worth",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            AmountText(
                amountInPaise = netWorth,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sparkline Graph
            SparklineGraph(
                dataPoints = FakeData.sparklineTrendPoints,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                lineColor = MaterialTheme.colorScheme.primary,
                fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Assets vs Liabilities Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Assets",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    AmountText(
                        amountInPaise = assets,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32) // Assets green
                    )
                }

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Liabilities",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    AmountText(
                        amountInPaise = liabilities,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AmountText(
                amountInPaise = groupTotal,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onAccountClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            .size(40.dp)
                            .background(
                                color = getAccountTypeContainerColor(account.type),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getAccountTypeIcon(account.type),
                            contentDescription = account.type.displayName,
                            tint = getAccountTypeIconColor(account.type),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        val subText = buildString {
                            account.bankName?.let { append(it) }
                            account.accountNumberLast4?.let {
                                if (isNotEmpty()) append(" •• ")
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
                        color = MaterialTheme.colorScheme.outline
                    )
                    AmountText(
                        amountInPaise = account.balanceInPaise,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (account.balanceInPaise < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Extra details for Credit Card or Loan
            if (account.type == AccountType.CREDIT_CARD && (account.unbilledAmountInPaise > 0 || account.dueAmountInPaise > 0)) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
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
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Due: ${account.dueDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else if (account.type == AccountType.LOAN && account.dueAmountInPaise > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
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
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            AmountText(
                                amountInPaise = account.dueAmountInPaise,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        account.dueDate?.let { dueDate ->
                            Text(
                                text = "Due: $dueDate",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
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
private fun getAccountTypeContainerColor(type: AccountType): Color {
    return when (type) {
        AccountType.BANK -> MaterialTheme.colorScheme.primaryContainer
        AccountType.CREDIT_CARD -> MaterialTheme.colorScheme.tertiaryContainer
        AccountType.CASH -> MaterialTheme.colorScheme.secondaryContainer
        AccountType.WALLET -> MaterialTheme.colorScheme.surfaceVariant
        AccountType.LOAN -> MaterialTheme.colorScheme.errorContainer
        AccountType.INVESTMENT -> MaterialTheme.colorScheme.primaryContainer
        AccountType.FIXED_DEPOSIT -> MaterialTheme.colorScheme.secondaryContainer
    }
}

@Composable
private fun getAccountTypeIconColor(type: AccountType): Color {
    return when (type) {
        AccountType.BANK -> MaterialTheme.colorScheme.onPrimaryContainer
        AccountType.CREDIT_CARD -> MaterialTheme.colorScheme.onTertiaryContainer
        AccountType.CASH -> MaterialTheme.colorScheme.onSecondaryContainer
        AccountType.WALLET -> MaterialTheme.colorScheme.onSurfaceVariant
        AccountType.LOAN -> MaterialTheme.colorScheme.onErrorContainer
        AccountType.INVESTMENT -> MaterialTheme.colorScheme.onPrimaryContainer
        AccountType.FIXED_DEPOSIT -> MaterialTheme.colorScheme.onSecondaryContainer
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountsScreenPreview() {
    Coconut_PfaTheme {
        AccountsScreen(onNavigate = {})
    }
}
