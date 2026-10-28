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
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarViewMonth
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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

    var expandedCycleMenu by remember { mutableStateOf(false) }

    val selectedCycleLabel = if (isPrivacyEnabled) "••••" else (uiState.selectedCycle?.label ?: "Current Cycle")

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cycle Picker Dropdown
        item {
            ExposedDropdownMenuBox(
                expanded = expandedCycleMenu,
                onExpandedChange = { expandedCycleMenu = !expandedCycleMenu }
            ) {
                OutlinedTextField(
                    value = selectedCycleLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Selected Salary Cycle") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCycleMenu) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedCycleMenu,
                    onDismissRequest = { expandedCycleMenu = false }
                ) {
                    uiState.availableCycles.forEach { cycleOpt ->
                        DropdownMenuItem(
                            text = { Text(if (isPrivacyEnabled) "••••" else cycleOpt.label) },
                            onClick = {
                                viewModel?.selectCycle(cycleOpt)
                                expandedCycleMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Section 1: Category Donut Chart
        item {
            CategoryDonutChartCard(
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

        // Section 2: Multi-Cycle Bar Chart (6 vs 12 Toggle)
        item {
            CycleBarChartCard(
                history = if (uiState.showTwelveCycles) uiState.cycleSpendHistory12 else uiState.cycleSpendHistory6,
                showTwelveCycles = uiState.showTwelveCycles,
                onToggle12Cycles = { viewModel?.toggle12Cycles(it) },
                isPrivacyEnabled = isPrivacyEnabled
            )
        }

        // Section 3: Daily Spend Calendar Heatmap
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

/**
 * Category Donut Chart composable with Canvas rendering, Legend, and tap-to-filter.
 */
@Composable
private fun CategoryDonutChartCard(
    categories: List<CategorySpendItem>,
    isPrivacyEnabled: Boolean,
    onCategoryClick: (CategorySpendItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalSpend = categories.sumOf { it.amountInPaise }
    val categoriesDesc = categories.joinToString { "${if (isPrivacyEnabled) "Protected" else it.categoryName}: ${it.percentage}%" }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Category spending breakdown chart. Total spend ${MoneyFormatter.format(totalSpend, isPrivacyEnabled)}. Categories: $categoriesDesc"
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Category Spending Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Canvas Donut Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    var startAngle = -90f
                    val strokeWidth = 32.dp.toPx()

                    categories.forEach { item ->
                        val sweepAngle = (item.percentage / 100f) * 360f
                        drawArc(
                            color = item.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 2f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )
                        startAngle += sweepAngle
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Spend",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AmountText(
                        amountInPaise = totalSpend,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Legend (Tap category to filter transactions)
            if (categories.isEmpty()) {
                Text(
                    text = "No expenses recorded for this cycle.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    categories.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategoryClick(item) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(item.color)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isPrivacyEnabled) "••••" else item.categoryName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = item.color.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${item.percentage}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = item.color,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                AmountText(
                                    amountInPaise = item.amountInPaise,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
