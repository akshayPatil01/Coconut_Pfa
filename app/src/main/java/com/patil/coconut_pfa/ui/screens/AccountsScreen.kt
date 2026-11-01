package com.patil.coconut_pfa.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.SendToMobile
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CorporateFare
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.North
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.RealEstateAgent
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patil.coconut_pfa.data.model.Account
import com.patil.coconut_pfa.data.model.AccountType
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.theme.PlusJakartaSans
import com.patil.coconut_pfa.ui.viewmodel.AccountsScreenUiState
import com.patil.coconut_pfa.ui.viewmodel.AccountsViewModel
import com.patil.coconut_pfa.util.MoneyFormatter

/**
 * Redesigned Accounts Screen strictly mirroring Google Stitch "Fortis - Accounts Tab (Light Mode)".
 * Features:
 * - Interactive Subheader / Quick Bar with live beacon & account count
 * - Obsidian Net Worth Card with Bézier sparkline, beacon, and telemetry
 * - 5 Crisp Light Mode Category Cards (Bank, Credit Card, Cash/Wallet, Loan, Investment/FD)
 * - Collapsible Archived Accounts Accordion
 * - High-contrast Obsidian Floating Action Button
 */
@Composable
fun AccountsScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    accountsViewModel: AccountsViewModel? = null,
) {
    val uiState by accountsViewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(AccountsScreenUiState()) }

    var showArchived by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val activeAccounts = uiState.activeAccounts
    val archivedAccounts = uiState.archivedAccounts

    val filteredActive = remember(activeAccounts, searchQuery) {
        if (searchQuery.isBlank()) {
            activeAccounts
        } else {
            activeAccounts.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    (it.bankName?.contains(searchQuery, ignoreCase = true) == true) ||
                    (it.accountNumberLast4?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    val bankAccounts = remember(filteredActive) { filteredActive.filter { it.type == AccountType.BANK } }
    val creditCards = remember(filteredActive) { filteredActive.filter { it.type == AccountType.CREDIT_CARD } }
    val cashAndWallets = remember(filteredActive) { filteredActive.filter { it.type == AccountType.CASH || it.type == AccountType.WALLET } }
    val loanAccounts = remember(filteredActive) { filteredActive.filter { it.type == AccountType.LOAN } }
    val investmentsAndFds = remember(filteredActive) { filteredActive.filter { it.type == AccountType.INVESTMENT || it.type == AccountType.FIXED_DEPOSIT } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF8F9FA),
        floatingActionButton = {
            AccountsFab(
                onClick = { onNavigate(Screen.AddAccount()) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA)),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Interactive & Sticky Quick Bar / Subheader
            item(key = "quick_bar") {
                AccountsQuickBar(
                    connectedCount = activeAccounts.size,
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    onToggleSearch = { isSearchActive = !isSearchActive },
                    onSearchQueryChange = { searchQuery = it }
                )
            }

            // Hero Net Worth Obsidian Card
            item(key = "hero_net_worth_card") {
                HeroNetWorthCard(
                    netWorthPaise = uiState.totalNetWorth,
                    totalAssetsPaise = uiState.totalAssets,
                    totalLiabilitiesPaise = uiState.totalLiabilities,
                    sparklinePoints = uiState.sparklineTrend.map { it.toFloat() },
                    activeDuesCount = creditCards.size + loanAccounts.size,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // Category 1: Bank Accounts
            if (bankAccounts.isNotEmpty() || searchQuery.isBlank()) {
                val bankTotal = bankAccounts.sumOf { it.balanceInPaise }
                item(key = "header_bank") {
                    CategoryHeader(
                        icon = Icons.Rounded.AccountBalance,
                        iconBg = Color(0xFFFAE0AA).copy(alpha = 0.4f),
                        iconTint = Color(0xFF75591C),
                        title = "Bank Accounts",
                        count = bankAccounts.size,
                        totalAmountPaise = bankTotal
                    )
                }

                items(bankAccounts, key = { it.id }) { account ->
                    val isPrimary = bankAccounts.firstOrNull()?.id == account.id
                    BankAccountCard(
                        account = account,
                        isPrimary = isPrimary,
                        onReconcile = { onNavigate(Screen.AccountDetail(account.id)) },
                        onStatements = { onNavigate(Screen.ImportStatement) },
                        onDetails = { onNavigate(Screen.AccountDetail(account.id)) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }

            // Category 2: Credit Cards
            if (creditCards.isNotEmpty() || searchQuery.isBlank()) {
                val cardDueTotal = creditCards.sumOf { it.dueAmountInPaise.takeIf { due -> due > 0 } ?: it.balanceInPaise }
                item(key = "header_credit_cards") {
                    CategoryHeader(
                        icon = Icons.Rounded.CreditCard,
                        iconBg = Color(0xFFFDE8E7),
                        iconTint = Color(0xFFBA1A1A),
                        title = "Credit Cards",
                        count = creditCards.size,
                        totalAmountPaise = cardDueTotal,
                        amountSuffix = "Due",
                        amountColor = Color(0xFFBA1A1A)
                    )
                }

                items(creditCards, key = { it.id }) { card ->
                    CreditCardItem(
                        account = card,
                        onPayBill = { onNavigate(Screen.Payments) },
                        onCardClick = { onNavigate(Screen.AccountDetail(card.id)) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }

            // Category 3: Cash & Wallets
            if (cashAndWallets.isNotEmpty() || searchQuery.isBlank()) {
                val cashTotal = cashAndWallets.sumOf { it.balanceInPaise }
                item(key = "header_cash_wallets") {
                    CategoryHeader(
                        icon = Icons.Rounded.AccountBalanceWallet,
                        iconBg = Color(0xFFD3F7E9),
                        iconTint = Color(0xFF006651),
                        title = "Cash & Wallets",
                        count = cashAndWallets.size,
                        totalAmountPaise = cashTotal
                    )
                }

                items(cashAndWallets, key = { it.id }) { account ->
                    CashWalletItem(
                        account = account,
                        onAdjust = { onNavigate(Screen.AccountDetail(account.id)) },
                        onItemClick = { onNavigate(Screen.AccountDetail(account.id)) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }

            // Category 4: Loans & Mortgages
            if (loanAccounts.isNotEmpty() || searchQuery.isBlank()) {
                val loanTotal = loanAccounts.sumOf { it.loanOutstandingInPaise }
                item(key = "header_loans") {
                    CategoryHeader(
                        icon = Icons.Rounded.RealEstateAgent,
                        iconBg = Color(0xFFFAE0AA).copy(alpha = 0.4f),
                        iconTint = Color(0xFF75591C),
                        title = "Loans & Mortgages",
                        count = loanAccounts.size,
                        totalAmountPaise = loanTotal
                    )
                }

                items(loanAccounts, key = { it.id }) { loan ->
                    LoanItemCard(
                        account = loan,
                        onLoanClick = { onNavigate(Screen.AccountDetail(loan.id)) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }

            // Category 5: Investments & Fixed Deposits
            if (investmentsAndFds.isNotEmpty() || searchQuery.isBlank()) {
                val invTotal = investmentsAndFds.sumOf { it.balanceInPaise }
                item(key = "header_investments") {
                    CategoryHeader(
                        icon = Icons.AutoMirrored.Rounded.TrendingUp,
                        iconBg = Color(0xFFD3F7E9),
                        iconTint = Color(0xFF006651),
                        title = "Investments & FDs",
                        count = investmentsAndFds.size,
                        totalAmountPaise = invTotal
                    )
                }

                items(investmentsAndFds, key = { it.id }) { inv ->
                    InvestmentAndFdCard(
                        account = inv,
                        onItemClick = { onNavigate(Screen.AccountDetail(inv.id)) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }

            // Auxiliary Section: Archived Accounts Accordion
            if (archivedAccounts.isNotEmpty()) {
                item(key = "archived_accounts_accordion") {
                    ArchivedAccountsCard(
                        archivedAccounts = archivedAccounts,
                        isExpanded = showArchived,
                        onToggleExpand = { showArchived = !showArchived },
                        onAccountClick = { onNavigate(Screen.AccountDetail(it.id)) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// SUBHEADER & QUICK BAR
// =========================================================================

@Composable
private fun AccountsQuickBar(
    connectedCount: Int,
    isSearchActive: Boolean,
    searchQuery: String,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Connected accounts indicator & Synced badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF006651).copy(alpha = pulseAlpha), CircleShape)
                )

                Text(
                    text = "$connectedCount Connected Accounts",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF444749)
                    )
                )

                Text(
                    text = "•",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        color = Color(0xFF74787A)
                    )
                )

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFD3F7E9),
                    modifier = Modifier.padding(start = 2.dp)
                ) {
                    Text(
                        text = "All Synced",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF006651)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Right: Circular Action Buttons (Search & Tune)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE1E3E5)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onToggleSearch)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Rounded.Close else Icons.Rounded.Search,
                            contentDescription = "Search and Filter",
                            tint = Color(0xFF191C1D),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE1E3E5)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { /* Tune/manage action */ }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Manage Accounts",
                            tint = Color(0xFF191C1D),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Animated Search Input Bar
        AnimatedVisibility(
            visible = isSearchActive,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE1E3E5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = Color(0xFF74787A),
                        modifier = Modifier.size(18.dp)
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            color = Color(0xFF191C1D)
                        ),
                        cursorBrush = SolidColor(Color(0xFF006651)),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search by account name, bank or last 4...",
                                    style = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 13.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF74787A),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onSearchQueryChange("") }
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// HERO NET WORTH CARD (OBSIDIAN DARK)
// =========================================================================

@Composable
private fun HeroNetWorthCard(
    netWorthPaise: Long,
    totalAssetsPaise: Long,
    totalLiabilitiesPaise: Long,
    sparklinePoints: List<Float>,
    activeDuesCount: Int,
    modifier: Modifier = Modifier,
) {
    val cardBg = Color(0xFF0E0E10)
    val textLight = Color(0xFFE5E1E4)
    val textMuted = Color(0xFFBDC9C3)
    val mintTeal = Color(0xFF7CE5C4)

    val (trendText, isPositiveTrend) = remember(sparklinePoints) {
        if (sparklinePoints.size >= 2) {
            val first = sparklinePoints.first()
            val last = sparklinePoints.last()
            if (first > 0f) {
                val pct = ((last - first) / first) * 100f
                val isPos = pct >= 0f
                val prefix = if (isPos) "+" else ""
                Pair(String.format(java.util.Locale.US, "%s%.1f%% • 30d", prefix, pct), isPos)
            } else {
                Pair("+2.7% • 30d", true)
            }
        } else {
            Pair("+2.7% • 30d", true)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = cardBg
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
        ) {
            // Background Ambient Topographic Wave & Donut Canvas
            Canvas(
                modifier = Modifier
                    .matchParentSize()
            ) {
                val strokeColor = textLight.copy(alpha = 0.12f)
                val w = size.width
                val h = size.height

                // Wave 1
                val path1 = Path().apply {
                    moveTo(-20f, h * 0.25f)
                    cubicTo(w * 0.2f, h * 0.44f, w * 0.4f, h * 0.1f, w * 0.65f, h * 0.28f)
                    cubicTo(w * 0.85f, h * 0.44f, w * 0.95f, h * 0.22f, w + 20f, h * 0.3f)
                }
                drawPath(path1, strokeColor, style = Stroke(width = 1.5.dp.toPx()))

                // Wave 2
                val path2 = Path().apply {
                    moveTo(-20f, h * 0.4f)
                    cubicTo(w * 0.22f, h * 0.62f, w * 0.45f, h * 0.28f, w * 0.68f, h * 0.47f)
                    cubicTo(w * 0.85f, h * 0.62f, w * 0.95f, h * 0.4f, w + 20f, h * 0.5f)
                }
                drawPath(path2, strokeColor, style = Stroke(width = 1.5.dp.toPx()))

                // Decorative Donut Ring (Top Right)
                drawCircle(
                    color = Color(0xFFFFC3C0).copy(alpha = 0.18f),
                    radius = 42.dp.toPx(),
                    center = Offset(w * 0.9f, h * 0.16f),
                    style = Stroke(width = 7.dp.toPx())
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header: Total Net Worth & 30d Trend pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Net Worth",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textMuted,
                            letterSpacing = 0.2.sp
                        )
                    )

                    Surface(
                        shape = CircleShape,
                        color = if (isPositiveTrend) mintTeal.copy(alpha = 0.12f) else Color(0xFFFFB4AB).copy(alpha = 0.12f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = if (isPositiveTrend) mintTeal else Color(0xFFFFB4AB),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = trendText,
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isPositiveTrend) mintTeal else Color(0xFFFFB4AB)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Main Net Worth Value
                AmountText(
                    amountInPaise = netWorthPaise,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = textLight
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Inline Sparkline Telemetry Wave & Glowing Coordinate Beacon
                SparklineTelemetryWave(
                    points = sparklinePoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Assets vs Liabilities Split Capsule
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF201F21).copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Total Assets Column
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(mintTeal, CircleShape)
                                )
                                Text(
                                    text = "TOTAL ASSETS",
                                    style = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.8.sp,
                                        color = textMuted
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            AmountText(
                                amountInPaise = totalAssetsPaise,
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = textLight,
                                showDecimals = false
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NorthEast,
                                    contentDescription = null,
                                    tint = mintTeal,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "+4.1% MoM",
                                    style = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = mintTeal
                                    )
                                )
                            }
                        }

                        // Vertical Separator
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .width(1.dp)
                                .background(Color(0xFF353437))
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Liabilities Column
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(Color(0xFFFFB3AF), CircleShape)
                                )
                                Text(
                                    text = "LIABILITIES",
                                    style = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.8.sp,
                                        color = textMuted
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            AmountText(
                                amountInPaise = totalLiabilitiesPaise,
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = textLight,
                                showDecimals = false
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$activeDuesCount active dues",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = textMuted
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas-rendered Bézier Telemetry Sparkline with glow gradient and active coordinate beacon.
 */
@Composable
private fun SparklineTelemetryWave(
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val mintTeal = Color(0xFF7CE5C4)

    val infiniteTransition = rememberInfiniteTransition(label = "beacon_ping")
    val pingRadius by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ping_radius"
    )
    val pingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ping_alpha"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Generate smooth sample telemetry coordinates
        val strokePath = Path().apply {
            moveTo(0f, h * 0.78f)
            cubicTo(w * 0.12f, h * 0.70f, w * 0.22f, h * 0.46f, w * 0.44f, h * 0.58f)
            cubicTo(w * 0.58f, h * 0.65f, w * 0.65f, h * 0.28f, w * 0.75f, h * 0.38f)
            cubicTo(w * 0.84f, h * 0.44f, w * 0.92f, h * 0.15f, w, h * 0.12f)
        }

        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        // Draw Glow Gradient Fill under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    mintTeal.copy(alpha = 0.32f),
                    mintTeal.copy(alpha = 0.0f)
                ),
                startY = 0f,
                endY = h
            )
        )

        // Draw Spline Wave Line
        drawPath(
            path = strokePath,
            color = mintTeal,
            style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        // Active Coordinate Beacon (focal coordinate around 68% width)
        val beaconX = w * 0.68f
        val beaconY = h * 0.32f

        // Animated Outer Ping Ring
        drawCircle(
            color = mintTeal.copy(alpha = pingAlpha),
            radius = pingRadius.dp.toPx(),
            center = Offset(beaconX, beaconY),
            style = Stroke(width = 2.dp.toPx())
        )

        // Solid White Core
        drawCircle(
            color = Color.White,
            radius = 5.dp.toPx(),
            center = Offset(beaconX, beaconY)
        )
    }
}

// =========================================================================
// CATEGORY HEADER
// =========================================================================

@Composable
private fun CategoryHeader(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    count: Int,
    totalAmountPaise: Long,
    amountSuffix: String? = null,
    amountColor: Color = Color(0xFF191C1D),
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = title,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191C1D)
                )
            )

            Surface(
                shape = CircleShape,
                color = Color(0xFFE8EAED)
            ) {
                Text(
                    text = count.toString(),
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF444749)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AmountText(
                amountInPaise = totalAmountPaise,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = amountColor
            )
            if (!amountSuffix.isNullOrBlank()) {
                Text(
                    text = amountSuffix,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                )
            }
        }
    }
}

// =========================================================================
// 1. BANK ACCOUNT CARD
// =========================================================================

@Composable
private fun BankAccountCard(
    account: Account,
    isPrimary: Boolean,
    onReconcile: () -> Unit,
    onStatements: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onDetails),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Row: Bank Icon, Account Name & Last4, Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFE6F7F2), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CorporateFare,
                            contentDescription = null,
                            tint = Color(0xFF006651),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = account.name,
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF191C1D)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (isPrimary) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFD3F7E9)
                                ) {
                                    Text(
                                        text = "PRIMARY",
                                        style = TextStyle(
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp,
                                            color = Color(0xFF006651)
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        val subText = buildString {
                            account.bankName?.let { append(it) } ?: append("Bank")
                            append(" •••• ")
                            append(account.accountNumberLast4 ?: "••••")
                        }
                        Text(
                            text = subText,
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                color = Color(0xFF5F6368)
                            )
                        )
                    }
                }

                // Balance presentation
                Column(horizontalAlignment = Alignment.End) {
                    AmountText(
                        amountInPaise = account.balanceInPaise,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1D)
                        )
                    )
                    Text(
                        text = "Available balance",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF006651)
                        )
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F3F4))

            // Bottom Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF4F5F7),
                        modifier = Modifier.clickable(onClick = onReconcile)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Sync,
                                contentDescription = null,
                                tint = Color(0xFF191C1D),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Reconcile",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF191C1D)
                                )
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF4F5F7),
                        modifier = Modifier.clickable(onClick = onStatements)
                    ) {
                        Text(
                            text = "Statements",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF5F6368)
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.clickable(onClick = onDetails)
                ) {
                    Text(
                        text = "Details",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF75591C)
                        )
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF75591C),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// 2. CREDIT CARD ITEM
// =========================================================================

@Composable
private fun CreditCardItem(
    account: Account,
    onPayBill: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalDues = account.dueAmountInPaise.takeIf { it > 0 } ?: account.balanceInPaise

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle Topographic Card Canvas Background
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeColor = Color(0xFF191C1D).copy(alpha = 0.05f)
                val w = size.width
                val h = size.height

                val path = Path().apply {
                    moveTo(-10f, h * 0.3f)
                    cubicTo(w * 0.25f, h * 0.55f, w * 0.5f, h * 0.15f, w * 0.75f, h * 0.45f)
                    cubicTo(w * 0.9f, h * 0.6f, w * 0.95f, h * 0.35f, w + 10f, h * 0.45f)
                }
                drawPath(path, strokeColor, style = Stroke(width = 1.5.dp.toPx()))

                drawCircle(
                    color = Color(0xFF75591C).copy(alpha = 0.06f),
                    radius = 38.dp.toPx(),
                    center = Offset(w * 0.92f, h * 0.25f),
                    style = Stroke(width = 5.dp.toPx())
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Row: Card Name & Last4, Network Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = account.name.uppercase(),
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF5F6368)
                            )
                        )
                        Text(
                            text = "•••• ${account.accountNumberLast4 ?: "8912"}",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF191C1D)
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0F2F5),
                        border = BorderStroke(1.dp, Color(0xFFE1E3E5))
                    ) {
                        Text(
                            text = if (account.name.contains("Master", ignoreCase = true)) "MC" else "VISA",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF191C1D)
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Middle Row: Current Dues, Limits, and Pay Bill Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Total Current Dues",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = Color(0xFF5F6368)
                            )
                        )

                        AmountText(
                            amountInPaise = totalDues,
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = Color(0xFFBA1A1A)
                        )

                        val limitStr = if (account.creditLimitInPaise > 0) MoneyFormatter.format(account.creditLimitInPaise, showDecimals = false) else "₹3,00,000"
                        val unbilledStr = if (account.unbilledAmountInPaise > 0) MoneyFormatter.format(account.unbilledAmountInPaise, showDecimals = false) else "₹12,400"

                        Text(
                            text = "Limit: $limitStr • Unbilled: $unbilledStr",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                color = Color(0xFF5F6368)
                            )
                        )
                    }

                    // Pay Bill Action Pill
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF251A00),
                        shadowElevation = 4.dp,
                        modifier = Modifier.clickable(onClick = onPayBill)
                    ) {
                        Text(
                            text = "Pay Bill",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFAE0AA)
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Dues Alert Callout Banner
                val dueDateStr = account.dueDate ?: "04 Nov"
                val minDuePaise = account.minimumDueInPaise.takeIf { it > 0 } ?: (totalDues / 10).coerceAtLeast(0L)
                val minDueStr = MoneyFormatter.format(minDuePaise, showDecimals = false)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFF1F0),
                    border = BorderStroke(1.dp, Color(0xFFFFDAD6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = Color(0xFFBA1A1A),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Due in 4 days ($dueDateStr) • Min $minDueStr",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF410002)
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFBA1A1A), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. CASH & WALLETS ITEM
// =========================================================================

@Composable
private fun CashWalletItem(
    account: Account,
    onAdjust: () -> Unit,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPhysicalCash = account.type == AccountType.CASH

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isPhysicalCash) Color(0xFFF0F2F5) else Color(0xFFE6F7F2),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPhysicalCash) Icons.Rounded.Payments else Icons.AutoMirrored.Rounded.SendToMobile,
                        contentDescription = null,
                        tint = if (isPhysicalCash) Color(0xFF191C1D) else Color(0xFF006651),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = account.name,
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF191C1D)
                            )
                        )

                        if (!isPhysicalCash) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFD3F7E9)
                            ) {
                                Text(
                                    text = "Auto-topup",
                                    style = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF006651)
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isPhysicalCash) (account.bankName ?: "Manual Log • Pocket") else "UPI Digital • Instant",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Color(0xFF5F6368)
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                AmountText(
                    amountInPaise = account.balanceInPaise,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF191C1D)
                    )
                )

                if (isPhysicalCash) {
                    Text(
                        text = "+ Adjust",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006651)
                        ),
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clickable(onClick = onAdjust)
                    )
                } else {
                    Text(
                        text = "Ready for scan",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Color(0xFF5F6368)
                        )
                    )
                }
            }
        }
    }
}

// =========================================================================
// 4. LOANS & MORTGAGES CARD
// =========================================================================

@Composable
private fun LoanItemCard(
    account: Account,
    onLoanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onLoanClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFFAE0AA).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = null,
                            tint = Color(0xFF75591C),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = account.name,
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF191C1D)
                                )
                            )

                            val rate = if (account.interestRate > 0) account.interestRate else 8.4
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFAE0AA).copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "$rate% Floating",
                                    style = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF55451C)
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        val last4 = account.accountNumberLast4 ?: "9901"
                        Text(
                            text = "A/C: •••• $last4",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                color = Color(0xFF5F6368)
                            )
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    AmountText(
                        amountInPaise = account.loanOutstandingInPaise,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1D)
                        ),
                        showDecimals = false
                    )
                    Text(
                        text = "Remaining principal",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Color(0xFF5F6368)
                        )
                    )
                }
            }

            // Progress bar for repayment
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "36 of 120 EMIs paid",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Color(0xFF5F6368)
                        )
                    )
                    Text(
                        text = "30% Complete",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF75591C)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0xFFE8EAED), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.30f)
                            .fillMaxHeight()
                            .background(Color(0xFFCAB380), CircleShape)
                    )
                }
            }

            // EMI Alert Strip
            val emiPaise = account.emiAmountInPaise.takeIf { it > 0 } ?: 2485000L
            val emiStr = MoneyFormatter.format(emiPaise, showDecimals = false)
            val emiDate = account.dueDate ?: "10 Nov"

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF4F5F7),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EventRepeat,
                            contentDescription = null,
                            tint = Color(0xFF006651),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Upcoming EMI: $emiStr on $emiDate",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF191C1D)
                            )
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFD3F7E9)
                    ) {
                        Text(
                            text = "Auto-Debit On",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006651)
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 5. INVESTMENTS & FIXED DEPOSITS CARD
// =========================================================================

@Composable
private fun InvestmentAndFdCard(
    account: Account,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFd = account.type == AccountType.FIXED_DEPOSIT

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isFd) Color(0xFFFAE0AA).copy(alpha = 0.3f) else Color(0xFFE6F7F2),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFd) Icons.Rounded.LockClock else Icons.Rounded.PieChart,
                        contentDescription = null,
                        tint = if (isFd) Color(0xFF75591C) else Color(0xFF006651),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = account.name,
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF191C1D)
                            )
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isFd) Color(0xFFFAE0AA).copy(alpha = 0.5f) else Color(0xFFD3F7E9)
                        ) {
                            val badgeLabel = if (isFd) {
                                val rate = if (account.fdInterestRate > 0) account.fdInterestRate else 7.1
                                "$rate% p.a."
                            } else {
                                "MF"
                            }
                            Text(
                                text = badgeLabel,
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFd) Color(0xFF55451C) else Color(0xFF006651)
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val subtitle = if (isFd) {
                        val matDate = account.maturityDate ?: "15 Mar 2027"
                        "Matures $matDate"
                    } else {
                        val fundName = account.bankName ?: "UTI Mutual Fund"
                        "$fundName • SIP Active"
                    }
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Color(0xFF5F6368)
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                AmountText(
                    amountInPaise = account.balanceInPaise,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191C1D)
                    ),
                    showDecimals = false
                )

                if (isFd) {
                    val matPaise = account.maturityAmountInPaise.takeIf { it > 0 } ?: (account.balanceInPaise * 123 / 100)
                    val matStr = MoneyFormatter.format(matPaise, showDecimals = false)
                    Text(
                        text = "At maturity: $matStr",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Color(0xFF5F6368)
                        )
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.North,
                            contentDescription = null,
                            tint = Color(0xFF006651),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "+18.4% (+₹3,85,000)",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006651)
                            )
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 6. ARCHIVED ACCOUNTS ACCORDION
// =========================================================================

@Composable
private fun ArchivedAccountsCard(
    archivedAccounts: List<Account>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAccountClick: (Account) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleExpand
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Inventory2,
                        contentDescription = null,
                        tint = Color(0xFF5F6368),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Archived Accounts (${archivedAccounts.size})",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF191C1D)
                        )
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color(0xFF5F6368)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFF1F3F4))
                    Spacer(modifier = Modifier.height(10.dp))

                    archivedAccounts.forEach { account ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAccountClick(account) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = account.name,
                                    style = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF191C1D)
                                    )
                                )
                                Text(
                                    text = if (account.type == AccountType.CREDIT_CARD) "Deactivated" else "Closed",
                                    style = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.sp,
                                        color = Color(0xFF5F6368)
                                    )
                                )
                            }

                            Text(
                                text = if (account.balanceInPaise == 0L) "Settled" else MoneyFormatter.format(account.balanceInPaise),
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF191C1D)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 7. FLOATING ACTION BUTTON (OBSIDIAN PILL)
// =========================================================================

@Composable
private fun AccountsFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color(0xFF191C1D)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add Account",
                tint = Color(0xFFFAE0AA),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Add Account",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFAE0AA)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountsScreenPreview() {
    Coconut_PfaTheme {
        AccountsScreen(onNavigate = {})
    }
}
