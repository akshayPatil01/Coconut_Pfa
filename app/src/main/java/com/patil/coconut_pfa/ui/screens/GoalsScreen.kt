package com.patil.coconut_pfa.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.local.entity.GoalEntity
import com.patil.coconut_pfa.data.model.Goal
import com.patil.coconut_pfa.data.model.GoalContribution
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountInputField
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.CoconutExposedDropdownMenu
import com.patil.coconut_pfa.ui.components.DatePickerField
import com.patil.coconut_pfa.ui.components.GradientButton
import com.patil.coconut_pfa.ui.components.GradientFloatingActionButton
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.theme.IncomeGreen
import com.patil.coconut_pfa.ui.viewmodel.GoalsUiState
import com.patil.coconut_pfa.ui.viewmodel.GoalsViewModel
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import com.patil.coconut_pfa.util.MoneyFormatter
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val GOAL_CATEGORY_OPTIONS = listOf(
    "Savings", "Travel", "Emergency Fund", "Vehicle", "Home", "Gadget", "Investment", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onNavigate: (Screen) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: GoalsViewModel? = null
) {
    val uiState by viewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf(GoalsUiState()) }

    val goalsList = uiState.goals.map { item ->
        Goal(
            id = item.goal.id,
            title = item.goal.name,
            targetAmountInPaise = item.goal.targetPaise,
            currentAmountInPaise = item.currentAmountPaise,
            targetDate = item.goal.targetDate,
            perCycleNeededInPaise = item.perCycleNeededPaise,
            category = item.goal.icon,
            contributions = item.contributions.map { c ->
                GoalContribution(
                    id = c.id,
                    amountInPaise = c.amountPaise,
                    date = c.date,
                    sourceAccountName = c.note ?: "Contribution",
                    note = c.note
                )
            }
        )
    }

    var selectedGoalForDetail by remember { mutableStateOf<Goal?>(null) }
    var selectedGoalForContribution by remember { mutableStateOf<Goal?>(null) }
    var goalToEdit by remember { mutableStateOf<GoalEntity?>(null) }
    var showAddGoalSheet by remember { mutableStateOf(false) }
    var goalToDelete by remember { mutableStateOf<GoalEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            GradientFloatingActionButton(
                onClick = {
                    goalToEdit = null
                    showAddGoalSheet = true
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Goal"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Charcoal Black Hero Overview Card
            item {
                GoalSummaryCard(goals = goalsList)
            }

            item {
                Text(
                    text = "Active Financial Goals (${goalsList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(goalsList, key = { it.id }) { goal ->
                GoalCard(
                    goal = goal,
                    onClick = { selectedGoalForDetail = goal },
                    onAddContribution = { selectedGoalForContribution = goal },
                    onEditGoal = {
                        val entity = uiState.goals.find { it.goal.id == goal.id }?.goal
                            ?: GoalEntity(
                                id = goal.id,
                                name = goal.title,
                                targetPaise = goal.targetAmountInPaise,
                                targetDate = goal.targetDate,
                                icon = goal.category
                            )
                        goalToEdit = entity
                        showAddGoalSheet = true
                    }
                )
            }
        }
    }

    // Detail Bottom Sheet
    selectedGoalForDetail?.let { goal ->
        GoalDetailBottomSheet(
            goal = goal,
            onDismiss = { selectedGoalForDetail = null },
            onAddContributionClick = {
                selectedGoalForContribution = goal
                selectedGoalForDetail = null
            },
            onEditGoalClick = {
                val entity = uiState.goals.find { it.goal.id == goal.id }?.goal
                    ?: GoalEntity(
                        id = goal.id,
                        name = goal.title,
                        targetPaise = goal.targetAmountInPaise,
                        targetDate = goal.targetDate,
                        icon = goal.category
                    )
                goalToEdit = entity
                selectedGoalForDetail = null
                showAddGoalSheet = true
            },
            onDeleteContribution = { contributionId ->
                viewModel?.deleteContribution(contributionId)
                // Refresh local goal instance in detail bottom sheet
                selectedGoalForDetail = selectedGoalForDetail?.copy(
                    contributions = selectedGoalForDetail?.contributions?.filter { it.id != contributionId } ?: emptyList()
                )
            }
        )
    }

    // Add Contribution Bottom Sheet
    selectedGoalForContribution?.let { goal ->
        AddContributionBottomSheet(
            goal = goal,
            availableAccountNames = uiState.accounts.map { it.name }.ifEmpty { listOf("Bank Account", "Cash") },
            onDismiss = { selectedGoalForContribution = null },
            onSaveContribution = { amountInPaise, _, note ->
                viewModel?.addContribution(
                    goalId = goal.id,
                    amountPaise = amountInPaise,
                    note = note
                )
                selectedGoalForContribution = null
            }
        )
    }

    // Add / Edit Goal Bottom Sheet
    if (showAddGoalSheet) {
        AddEditGoalBottomSheet(
            goalToEdit = goalToEdit,
            onDismiss = {
                showAddGoalSheet = false
                goalToEdit = null
            },
            onSaveGoal = { id, title, targetPaise, targetDate, initialPaise, category ->
                viewModel?.saveGoal(
                    id = id,
                    name = title,
                    targetPaise = targetPaise,
                    targetDate = targetDate,
                    icon = category,
                    initialAmountPaise = initialPaise
                )
                showAddGoalSheet = false
                goalToEdit = null
            },
            onDeleteGoal = { entity ->
                goalToDelete = entity
                showAddGoalSheet = false
                goalToEdit = null
            }
        )
    }

    // Delete Goal Confirmation Dialog
    goalToDelete?.let { entity ->
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            title = { Text("Delete Goal", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${entity.name}' and all its logged contributions? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel?.deleteGoal(entity)
                        goalToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Hero summary card styled in Charcoal Black matching the Total Net Worth card on the Accounts tab.
 */
@Composable
private fun GoalSummaryCard(
    goals: List<Goal>,
    modifier: Modifier = Modifier,
) {
    val totalTarget = goals.sumOf { it.targetAmountInPaise }
    val totalCurrent = goals.sumOf { it.currentAmountInPaise }
    val totalPerCycle = goals.sumOf { it.perCycleNeededInPaise }
    val overallProgress = if (totalTarget > 0) (totalCurrent.toFloat() / totalTarget.toFloat()).coerceIn(0f, 1f) else 0f

    val cardBgColor = Color(0xFF121518)
    val labelTextColor = Color(0xFF9EA3A8)
    val primaryTextColor = Color.White

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = cardBgColor,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF262A30),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
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
            Column(modifier = Modifier.padding(20.dp)) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Savings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Goals Overview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                            Text(
                                text = "${goals.size} active targets",
                                style = MaterialTheme.typography.bodySmall,
                                color = labelTextColor
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF12392B)
                    ) {
                        Text(
                            text = "${(overallProgress * 100).toInt()}% Saved",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { overallProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color(0xFF262A30)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Saved vs Target vs Per Cycle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Saved",
                            style = MaterialTheme.typography.labelSmall,
                            color = labelTextColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        AmountText(
                            amountInPaise = totalCurrent,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Target",
                            style = MaterialTheme.typography.labelSmall,
                            color = labelTextColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        AmountText(
                            amountInPaise = totalTarget,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Per Cycle Needed",
                            style = MaterialTheme.typography.labelSmall,
                            color = labelTextColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "≈ ${MoneyFormatter.format(totalPerCycle)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    onAddContribution: () -> Unit,
    onEditGoal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title, Category & Edit Action
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = goal.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditGoal) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Goal",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Details",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { goal.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Saved vs Target Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AmountText(
                        amountInPaise = goal.currentAmountInPaise,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " / ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    AmountText(
                        amountInPaise = goal.targetAmountInPaise,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "${(goal.progressFraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Target Date & Per Cycle Needed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Target: ${goal.targetDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${MoneyFormatter.format(goal.perCycleNeededInPaise)}/cycle",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            OutlinedButton(
                onClick = onAddContribution,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Add Contribution")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalDetailBottomSheet(
    goal: Goal,
    onDismiss: () -> Unit,
    onAddContributionClick: () -> Unit,
    onEditGoalClick: () -> Unit,
    onDeleteContribution: (String) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Target Date: ${goal.targetDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onEditGoalClick,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    Button(
                        onClick = onAddContributionClick,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Contribute")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Breakdown card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Current Saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        AmountText(amountInPaise = goal.currentAmountInPaise, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        AmountText(amountInPaise = goal.remainingAmountInPaise, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Per Cycle", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(MoneyFormatter.format(goal.perCycleNeededInPaise), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Contribution History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (goal.contributions.isEmpty()) {
                Text(
                    text = "No contributions logged yet. Tap 'Contribute' to start saving!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(goal.contributions) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.sourceAccountName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${item.date}${if (!item.note.isNull_or_blank_safe()) " • ${item.note}" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AmountText(
                                        amountInPaise = item.amountInPaise,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onDeleteContribution(item.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = "Delete Contribution",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun String?.isNull_or_blank_safe(): Boolean {
    return this == null || this.trim().isEmpty()
}

/**
 * Themed Bottom Sheet for Creating or Editing a Goal.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddEditGoalBottomSheet(
    goalToEdit: GoalEntity? = null,
    onDismiss: () -> Unit,
    onSaveGoal: (id: String?, title: String, targetPaise: Long, targetDate: String, initialPaise: Long, category: String) -> Unit,
    onDeleteGoal: ((GoalEntity) -> Unit)? = null
) {
    val isEditMode = goalToEdit != null
    var title by remember { mutableStateOf(goalToEdit?.name ?: "") }
    var targetAmountText by remember {
        mutableStateOf(
            if (goalToEdit != null) (goalToEdit.targetPaise / 100.0).toString() else ""
        )
    }
    var initialAmountText by remember { mutableStateOf("") }
    var targetDate by remember {
        mutableStateOf(
            goalToEdit?.targetDate?.ifEmpty { KolkataDateTimeUtils.currentLocalDate().plusMonths(6).format(KolkataDateTimeUtils.DATE_FORMATTER) }
                ?: KolkataDateTimeUtils.currentLocalDate().plusMonths(6).format(KolkataDateTimeUtils.DATE_FORMATTER)
        )
    }
    var selectedCategory by remember { mutableStateOf(goalToEdit?.icon ?: "Savings") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditMode) "Edit Goal" else "Create New Goal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (isEditMode && goalToEdit != null && onDeleteGoal != null) {
                    IconButton(
                        onClick = { onDeleteGoal(goalToEdit) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete Goal",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Goal Title") },
                placeholder = { Text("e.g., Emergency Fund, New Bike") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            // Target Amount Field with Indian Currency formatting
            AmountInputField(
                value = targetAmountText,
                onValueChange = { targetAmountText = it },
                label = "Target Amount"
            )

            // Initial Saved Amount (Only for Create Goal)
            if (!isEditMode) {
                AmountInputField(
                    value = initialAmountText,
                    onValueChange = { initialAmountText = it },
                    label = "Initial Saved Amount (Optional)"
                )
            }

            // Target Date Selector using DatePickerField
            DatePickerField(
                value = targetDate,
                onDateSelected = { selectedDate ->
                    targetDate = selectedDate
                },
                label = "Target Date"
            )

            // Category Choice Chips (Horizontal Scroll)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GOAL_CATEGORY_OPTIONS.forEach { categoryName ->
                        val isSelected = selectedCategory == categoryName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = categoryName },
                            label = { Text(categoryName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Auto-Calculated Needed Per Month/Cycle Live Preview Card
            val targetPaise = MoneyFormatter.parseToPaise(targetAmountText)
            val initialPaise = MoneyFormatter.parseToPaise(initialAmountText)
            val remainingPaise = (targetPaise - initialPaise).coerceAtLeast(0L)

            if (targetPaise > 0) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estimated Remaining",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            AmountText(
                                amountInPaise = remainingPaise,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Target Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = targetDate,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            GradientButton(
                onClick = {
                    if (title.isNotBlank() && targetPaise > 0) {
                        onSaveGoal(
                            goalToEdit?.id,
                            title,
                            targetPaise,
                            targetDate,
                            initialPaise,
                            selectedCategory
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isEditMode) "Save Changes" else "Create Goal",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Themed Bottom Sheet for Adding a Contribution to a Goal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddContributionBottomSheet(
    goal: Goal,
    availableAccountNames: List<String>,
    onDismiss: () -> Unit,
    onSaveContribution: (amountInPaise: Long, accountName: String, note: String?) -> Unit,
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf(availableAccountNames.firstOrNull() ?: "Bank Account") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "Add Contribution",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Goal: ${goal.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Amount Input
            AmountInputField(
                value = amountText,
                onValueChange = { amountText = it },
                label = "Contribution Amount"
            )

            // Source Account Selector
            CoconutExposedDropdownMenu(
                value = selectedAccount,
                options = availableAccountNames,
                onOptionSelected = { selectedAccount = it },
                label = "From Account",
                modifier = Modifier.fillMaxWidth()
            )

            // Optional Note
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note (Optional)") },
                placeholder = { Text("e.g. Monthly salary savings") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            GradientButton(
                onClick = {
                    val amountPaise = MoneyFormatter.parseToPaise(amountText)
                    if (amountPaise > 0) {
                        onSaveContribution(
                            amountPaise,
                            selectedAccount,
                            noteText.ifBlank { null }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Add Contribution",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GoalsScreenPreview() {
    Coconut_PfaTheme {
        GoalsScreen()
    }
}
