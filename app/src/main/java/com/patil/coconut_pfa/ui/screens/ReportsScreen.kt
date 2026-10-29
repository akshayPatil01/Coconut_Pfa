package com.patil.coconut_pfa.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarViewMonth
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.model.CategorySpendItem
import com.patil.coconut_pfa.data.model.CycleSpendItem
import com.patil.coconut_pfa.data.model.DailySpendHeatmapItem
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.theme.GradientOrangeCoralEnd
import com.patil.coconut_pfa.ui.theme.GradientOrangeCoralStart
import com.patil.coconut_pfa.ui.theme.GradientPurpleEnd
import com.patil.coconut_pfa.ui.theme.GradientPurpleStart
import com.patil.coconut_pfa.ui.theme.PastelCoral
import com.patil.coconut_pfa.ui.theme.PastelEmerald
import com.patil.coconut_pfa.ui.theme.PastelPurple
import com.patil.coconut_pfa.ui.theme.PastelSkyBlue
import com.patil.coconut_pfa.ui.viewmodel.ReportsUiState
import com.patil.coconut_pfa.ui.viewmodel.ReportsViewModel
import com.patil.coconut_pfa.util.MoneyFormatter
import com.patil.coconut_pfa.util.PrivacyManager
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigate: (Screen) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel? = null
) {
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(ReportsUiState()) }
    val isPrivacyEnabled by PrivacyManager.isPrivacyEnabled.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Premium Widget 1: Month & Calendar Selector
        item {
            MonthCalendarSelectorWidget()
        }

        // Premium Widget 2: Side-by-Side Balance Summary Cards
        item {
            SideBySideBalanceCards(isPrivacyEnabled = isPrivacyEnabled)
        }

        // Premium Widget 3: Expense Breakdown Section
        item {
            PremiumExpenseBreakdownSection(
                categories = uiState.categorySpendItems,
                isPrivacyEnabled = isPrivacyEnabled,
                onCategoryClick = { categoryItem ->
                    onNavigate(
                        Screen.Transactions(
                            initialCategory = categoryItem.categoryName,
                            initialCycle = uiState.selectedCycle?.label
                        )
                    )
                }
            )
        }

        // Section 4: Multi-Cycle Bar Chart (6 vs 12 Toggle)
        item {
            CycleBarChartCard(
                history = if (uiState.showTwelveCycles) uiState.cycleSpendHistory12 else uiState.cycleSpendHistory6,
                showTwelveCycles = uiState.showTwelveCycles,
                onToggle12Cycles = { viewModel?.toggle12Cycles(it) },
                isPrivacyEnabled = isPrivacyEnabled
            )
        }

        // Section 5: Daily Spend Calendar Heatmap
        item {
            DailySpendHeatmapCard(
                heatmapItems = uiState.dailyHeatmapItems,
                selectedDay = uiState.selectedHeatmapDay,
                onDayClick = { viewModel?.selectHeatmapDay(it) },
                onViewDayTransactions = { dayItem ->
                    val zoneId = ZoneId.of("Asia/Kolkata")
                    val date = uiState.selectedCycle?.period?.startDate?.withDayOfMonth(dayItem.dayOfMonth) ?: java.time.LocalDate.now()
                    val startMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val endMillis = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
                    onNavigate(
                        Screen.Transactions(
                            initialStartDateMillis = startMillis,
                            initialEndDateMillis = endMillis,
                            initialCycle = uiState.selectedCycle?.label
                        )
                    )
                },
                isPrivacyEnabled = isPrivacyEnabled
            )
        }
    }
}

@Composable
fun MonthCalendarSelectorWidget() {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* Handle previous month */ },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = "January 2026",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            IconButton(
                onClick = { /* Handle next month */ },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Days Row
        val days = listOf("S", "M", "T", "W", "T", "F", "S")
        val dates = listOf("10", "11", "12", "13", "14", "15", "16")
        val selectedIndex = 3 // Wednesday (Mock selected day)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEachIndexed { index, day ->
                val isSelected = index == selectedIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .padding(vertical = 16.dp, horizontal = 12.dp)
                        .clickable { /* Select Day */ }
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = dates[index],
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SideBySideBalanceCards(isPrivacyEnabled: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Card - Salary
        Card(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(0.9f),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GradientPurpleStart, GradientPurpleEnd)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .padding(8.dp)
                    )
                    Column {
                        Text(
                            text = "Total Salary",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPrivacyEnabled) "****" else "$4,228.76",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "********1965",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Right Card - Expense
        Card(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(0.9f),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GradientOrangeCoralStart, GradientOrangeCoralEnd)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CreditCard,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .padding(8.dp)
                    )
                    Column {
                        Text(
                            text = "Total Expense",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPrivacyEnabled) "****" else "$4,543.98",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "********1965",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PremiumExpenseBreakdownSection(
    categories: List<CategorySpendItem>,
    isPrivacyEnabled: Boolean,
    onCategoryClick: (CategorySpendItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Use mock data if real data is empty so the beautiful UI is still visible
    val displayCategories = if (categories.isEmpty()) {
        listOf(
            CategorySpendItem("Food", 204479L, 45f, PastelPurple),
            CategorySpendItem("Transport", 113599L, 25f, PastelEmerald),
            CategorySpendItem("Shopping", 90879L, 20f, PastelCoral),
            CategorySpendItem("Bills", 45439L, 10f, PastelSkyBlue)
        )
    } else categories

    val totalSpend = displayCategories.sumOf { it.amountInPaise }
    val categoriesDesc = displayCategories.joinToString { "${if (isPrivacyEnabled) "Protected" else it.categoryName}: ${it.percentage}%" }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Expense breakdown chart. Total spend ${MoneyFormatter.format(totalSpend, isPrivacyEnabled)}. Categories: $categoriesDesc"
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Expense Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Canvas Donut Chart
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    val strokeWidth = 40.dp.toPx()

                    displayCategories.forEach { item ->
                        val sweepAngle = (item.percentage / 100f) * 360f
                        drawArc(
                            color = item.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 3f, // Small gap for style
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }
                
                // Center text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Spend",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AmountText(
                        amountInPaise = totalSpend,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Callout Pills (Wrap layout)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                displayCategories.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = item.color.copy(alpha = 0.2f),
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable { onCategoryClick(item) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(item.color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPrivacyEnabled) "••••" else item.categoryName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${item.percentage.toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Multi-Cycle Bar Chart composable with Canvas rendering, 6/12 cycle toggle, and TalkBack description.
 */
@Composable
private fun CycleBarChartCard(
    history: List<CycleSpendItem>,
    showTwelveCycles: Boolean,
    onToggle12Cycles: (Boolean) -> Unit,
    isPrivacyEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val barColorExpense = MaterialTheme.colorScheme.error
    val barColorIncome = Color(0xFF2E7D32)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Income vs expense history bar chart for ${history.size} cycles. Showing income and expense trends."
            },
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
                    Icon(
                        imageVector = Icons.Rounded.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Income vs Expense History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = !showTwelveCycles,
                        onClick = { onToggle12Cycles(false) },
                        label = { Text("6 Cycles") }
                    )
                    FilterChip(
                        selected = showTwelveCycles,
                        onClick = { onToggle12Cycles(true) },
                        label = { Text("12 Cycles") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxAmount = history.maxOfOrNull { maxOf(it.incomeInPaise, it.expenseInPaise) } ?: 10000000L

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val width = size.width
                val height = size.height
                val bottomPadding = 40f
                val chartHeight = height - bottomPadding

                val itemCount = if (history.isNotEmpty()) history.size else 1
                val groupWidth = width / itemCount

                history.forEachIndexed { index, item ->
                    val xCenter = index * groupWidth + groupWidth / 2f
                    val barW = (groupWidth * 0.35f).coerceAtMost(24f)

                    val incomeRatio = item.incomeInPaise.toFloat() / maxAmount.toFloat()
                    val expenseRatio = item.expenseInPaise.toFloat() / maxAmount.toFloat()

                    val incomeH = incomeRatio * chartHeight
                    val expenseH = expenseRatio * chartHeight

                    drawRoundRect(
                        color = barColorIncome,
                        topLeft = Offset(xCenter - barW - 2f, chartHeight - incomeH),
                        size = Size(barW, incomeH),
                        cornerRadius = CornerRadius(4f, 4f)
                    )

                    drawRoundRect(
                        color = barColorExpense,
                        topLeft = Offset(xCenter + 2f, chartHeight - expenseH),
                        size = Size(barW, expenseH),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                history.forEach { item ->
                    Text(
                        text = if (isPrivacyEnabled) "••" else item.cycleLabel.take(4),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(barColorIncome))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Income", style = MaterialTheme.typography.labelMedium)

                Spacer(modifier = Modifier.width(20.dp))

                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(barColorExpense))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Expense", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/**
 * Calendar Heatmap composable displaying daily spending squares, tap to view transactions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailySpendHeatmapCard(
    heatmapItems: List<DailySpendHeatmapItem>,
    selectedDay: DailySpendHeatmapItem?,
    onDayClick: (DailySpendHeatmapItem) -> Unit,
    onViewDayTransactions: (DailySpendHeatmapItem) -> Unit,
    isPrivacyEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Daily spending calendar heatmap showing spending intensity across ${heatmapItems.size} days of the cycle."
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.CalendarViewMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daily Spending Heatmap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Darker green = higher spending on that day of cycle",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 7
            ) {
                heatmapItems.forEach { dayItem ->
                    val color = Color(0xFF2E7D32).copy(alpha = dayItem.intensity)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                            .clickable { onDayClick(dayItem) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayItem.dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (dayItem.intensity > 0.5f) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            selectedDay?.let { item ->
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Spend on ${if (isPrivacyEnabled) "••••" else item.dateLabel}:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            AmountText(
                                amountInPaise = item.amountInPaise,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Button(
                            onClick = { onViewDayTransactions(item) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("View Transactions")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportsScreenPreview() {
    Coconut_PfaTheme {
        ReportsScreen()
    }
}
