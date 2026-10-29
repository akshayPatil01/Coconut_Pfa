package com.patil.coconut_pfa.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.patil.coconut_pfa.ui.components.GradientFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.local.entity.BillEntity
import com.patil.coconut_pfa.data.local.entity.DbBillFrequency
import com.patil.coconut_pfa.data.local.entity.DbBillKind
import com.patil.coconut_pfa.data.model.BillingFrequency
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.theme.GradientOrangeCoralStart
import com.patil.coconut_pfa.ui.viewmodel.SubscriptionsViewModel
import com.patil.coconut_pfa.util.MoneyFormatter

@Composable
fun SubscriptionsScreen(
    onNavigate: (Screen) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SubscriptionsViewModel? = null
) {
    val uiState by viewModel?.uiState?.collectAsState()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(com.patil.coconut_pfa.ui.viewmodel.SubscriptionsUiState()) }

    val subscriptionsList = uiState.subscriptions
    val monthlyTotalPaise = uiState.monthlyTotalPaise

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            GradientFloatingActionButton(
                onClick = { onNavigate(Screen.AddBill(initialKind = "SUBSCRIPTION")) }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Subscription"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SubscriptionHeaderCard(
                    monthlyTotalPaise = monthlyTotalPaise,
                    activeCount = uiState.activeCount
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recurring Subscriptions (${subscriptionsList.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { onNavigate(Screen.AddBill(initialKind = "SUBSCRIPTION")) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Subscription",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            items(subscriptionsList, key = { it.id }) { sub ->
                val freq = when (sub.frequency) {
                    DbBillFrequency.MONTHLY -> BillingFrequency.MONTHLY
                    DbBillFrequency.QUARTERLY -> BillingFrequency.QUARTERLY
                    DbBillFrequency.YEARLY -> BillingFrequency.YEARLY
                    DbBillFrequency.EVERY_N_DAYS -> BillingFrequency.MONTHLY
                }
                val mult = when (sub.frequency) {
                    DbBillFrequency.MONTHLY -> 1.0
                    DbBillFrequency.QUARTERLY -> 1.0 / 3.0
                    DbBillFrequency.YEARLY -> 1.0 / 12.0
                    DbBillFrequency.EVERY_N_DAYS -> 1.0 / (sub.interval.toDouble().coerceAtLeast(1.0) / 30.0)
                }
                val monthlyEq = (sub.amountPaise * mult).toLong()

                SubscriptionCard(
                    bill = sub,
                    billingFrequency = freq,
                    monthlyEquivalent = monthlyEq,
                    onToggleActive = { isActive ->
                        viewModel?.toggleActive(sub.id, isActive)
                    },
                    onDelete = {
                        viewModel?.deleteBill(sub)
                    }
                )
            }
        }
    }
}

@Composable
private fun SubscriptionHeaderCard(
    monthlyTotalPaise: Long,
    activeCount: Int,
    modifier: Modifier = Modifier,
) {
    val cardBgColor = Color(0xFF121518)
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
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262A30)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Repeat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Monthly Subscription Burden",
                                style = MaterialTheme.typography.titleSmall,
                                color = labelTextColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "≈ ${MoneyFormatter.format(monthlyTotalPaise)} / month",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Tracking $activeCount active recurring payments (converted to monthly average)",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelTextColor
                )
            }
        }
    }
}

@Composable
private fun SubscriptionCard(
    bill: BillEntity,
    billingFrequency: BillingFrequency,
    monthlyEquivalent: Long,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (bill.active) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Repeat,
                            contentDescription = null,
                            tint = if (bill.active) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = bill.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val kindLabel = when (bill.kind) {
                            DbBillKind.RENT -> "Rent"
                            DbBillKind.UTILITY -> "Utility"
                            DbBillKind.INSURANCE -> "Insurance"
                            DbBillKind.SUBSCRIPTION -> "Subscription"
                            DbBillKind.EMI -> "EMI"
                            DbBillKind.OTHER -> "Other"
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = kindLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Switch(
                    checked = bill.active,
                    onCheckedChange = onToggleActive
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Price & Cycle",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AmountText(
                            amountInPaise = bill.amountPaise,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " / ${billingFrequency.displayName.lowercase()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (billingFrequency != BillingFrequency.MONTHLY) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "≈ ${MoneyFormatter.format(monthlyEquivalent)}/mo",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Next renewal: ${bill.anchorDate}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubscriptionsScreenPreview() {
    Coconut_PfaTheme {
        SubscriptionsScreen()
    }
}
