package com.patil.coconut_pfa.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Switch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.CategoryEntity
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.model.TransactionType
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.viewmodel.AddTransactionUiState
import com.patil.coconut_pfa.ui.viewmodel.AddTransactionViewModel
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddTransactionViewModel? = null,
    initialAmountPaise: Long? = null,
    initialPayee: String? = null,
    initialDateMillisArg: Long? = null,
    scanSource: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf(AddTransactionUiState(isLoading = false))
    }

    // Tab state (0: Expense, 1: Income, 2: Transfer, 3: Refund)
    var selectedTabItem by remember { mutableStateOf(0) }
    val tabs = remember { listOf("Expense", "Income", "Transfer", "Refund") }

    // Form states
    var rawAmountInput by remember { mutableStateOf("0") }
    var payee by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isSplitEnabled by remember { mutableStateOf(false) }
    var paidByMe by remember { mutableStateOf(true) }
    var selectedPersonId by remember { mutableStateOf<String?>(null) }
    var personDropdownExpanded by remember { mutableStateOf(false) }
    var myShareInput by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedCategoryName by remember { mutableStateOf("Groceries") }
    var selectedOccurredAt by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Selected Accounts
    var fromAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var toAccount by remember { mutableStateOf<AccountEntity?>(null) }

    // Dropdown expansion states
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var payeeDropdownExpanded by remember { mutableStateOf(false) }
    var fromAccountExpanded by remember { mutableStateOf(false) }
    var toAccountExpanded by remember { mutableStateOf(false) }

    // Pre-populate logic when data is loaded from Room / ViewModel
    var initialized by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading, uiState.accounts, uiState.categories, uiState.initialTxn) {
        if (!uiState.isLoading && !initialized) {
            val tx = uiState.initialTxn
            val accounts = uiState.accounts
            val categories = uiState.categories

            if (accounts.isNotEmpty()) {
                fromAccount = accounts.firstOrNull { it.id == tx?.accountId }
                    ?: accounts.firstOrNull { it.id == uiState.lastUsedAccountId }
                    ?: accounts.firstOrNull()

                toAccount = accounts.firstOrNull { it.id == tx?.toAccountId }
                    ?: accounts.getOrNull(1)
                    ?: accounts.firstOrNull()
            }

            if (tx != null) {
                // Editing or duplicating existing transaction
                selectedTabItem = when (tx.type) {
                    DbTxnType.EXPENSE -> 0
                    DbTxnType.INCOME -> 1
                    DbTxnType.TRANSFER -> 2
                    DbTxnType.REFUND -> 3
                    else -> 0
                }
                val amountDouble = tx.amountPaise / 100.0
                rawAmountInput = if (tx.amountPaise % 100 == 0L) {
                    (tx.amountPaise / 100).toString()
                } else {
                    String.format("%.2f", amountDouble)
                }
                payee = tx.payee ?: ""
                note = tx.note ?: ""
                selectedOccurredAt = tx.occurredAt

                if (tx.categoryId != null) {
                    val foundCat = categories.firstOrNull { it.id == tx.categoryId }
                    selectedCategoryId = foundCat?.id ?: tx.categoryId
                    selectedCategoryName = foundCat?.name ?: "General"
                }
            } else {
                // New transaction - preset defaults or scan pre-fill
                if (initialAmountPaise != null && initialAmountPaise > 0L) {
                    val amountDouble = initialAmountPaise / 100.0
                    rawAmountInput = if (initialAmountPaise % 100 == 0L) {
                        (initialAmountPaise / 100).toString()
                    } else {
                        String.format("%.2f", amountDouble)
                    }
                }
                if (!initialPayee.isNullOrBlank()) {
                    payee = initialPayee
                }
                if (initialDateMillisArg != null && initialDateMillisArg > 0L) {
                    selectedOccurredAt = initialDateMillisArg
                }
                if (scanSource) {
                    note = "Scanned Receipt"
                }

                val defaultCat = categories.firstOrNull { it.id == uiState.lastUsedCategoryId }
                    ?: categories.firstOrNull()
                selectedCategoryId = defaultCat?.id
                selectedCategoryName = defaultCat?.name ?: "Groceries"
            }
            initialized = true
        }
    }

    val currentType = when (selectedTabItem) {
        0 -> TransactionType.EXPENSE
        1 -> TransactionType.INCOME
        2 -> TransactionType.TRANSFER
        3 -> TransactionType.REFUND
        else -> TransactionType.EXPENSE
    }

    val availableCategories = remember(selectedTabItem, uiState.categories) {
        if (uiState.categories.isNotEmpty()) {
            uiState.categories
        } else {
            // Fallback default list if categories not loaded yet
            when (selectedTabItem) {
                1 -> listOf("Salary", "Freelance", "Investment", "Bonus", "Gift", "Other Income").map { CategoryEntity(id = it, name = it, kind = com.patil.coconut_pfa.data.local.entity.DbCategoryKind.INCOME, icon = "payments", colour = 0xFF4CAF50L) }
                2 -> listOf("Transfer", "Self Transfer", "Settlement", "Credit Card Bill").map { CategoryEntity(id = it, name = it, kind = com.patil.coconut_pfa.data.local.entity.DbCategoryKind.EXPENSE, icon = "swap_horiz", colour = 0xFF1E88E5L) }
                3 -> listOf("Shopping Refund", "Cashback", "Reimbursement", "Other Refund").map { CategoryEntity(id = it, name = it, kind = com.patil.coconut_pfa.data.local.entity.DbCategoryKind.INCOME, icon = "redeem", colour = 0xFF009688L) }
                else -> listOf("Groceries", "Dining", "Shopping", "Fuel", "Utilities", "Bills", "Rent", "Entertainment", "Health").map { CategoryEntity(id = it, name = it, kind = com.patil.coconut_pfa.data.local.entity.DbCategoryKind.EXPENSE, icon = "shopping_cart", colour = 0xFFE91E63L) }
            }
        }
    }

    val selectedDateText = remember(selectedOccurredAt) {
        val date = KolkataDateTimeUtils.epochMillisToLocalDate(selectedOccurredAt)
        val today = KolkataDateTimeUtils.currentLocalDate()
        when (date) {
            today -> "Today, ${date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}"
            today.minusDays(1) -> "Yesterday, ${date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}"
            else -> date.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Action Bar / Close & Scan buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate(Screen.Home) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Close / Back"
                    )
                }

                Text(
                    text = if (uiState.initialTxn != null) "Edit Transaction" else "Add Transaction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // "Scan" Button
                OutlinedButton(
                    onClick = {
                        onNavigate(Screen.ReceiptScan)
                    },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QrCodeScanner,
                        contentDescription = "Scan",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Scan", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Tabs: Expense | Income | Transfer | Refund
            TabRow(
                selectedTabIndex = selectedTabItem,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabItem == index,
                        onClick = {
                            selectedTabItem = index
                            val matchingCat = when (index) {
                                1 -> availableCategories.firstOrNull { it.name.equals("Salary", ignoreCase = true) } ?: availableCategories.firstOrNull()
                                2 -> availableCategories.firstOrNull { it.name.equals("Transfer", ignoreCase = true) } ?: availableCategories.firstOrNull()
                                3 -> availableCategories.firstOrNull { it.name.contains("Refund", ignoreCase = true) || it.name.contains("Cashback", ignoreCase = true) } ?: availableCategories.firstOrNull()
                                else -> availableCategories.firstOrNull { it.name.equals("Groceries", ignoreCase = true) } ?: availableCategories.firstOrNull()
                            }
                            matchingCat?.let {
                                selectedCategoryId = it.id
                                selectedCategoryName = it.name
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabItem == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Prominent Amount Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (currentType) {
                        TransactionType.INCOME -> MaterialTheme.colorScheme.primaryContainer
                        TransactionType.REFUND -> MaterialTheme.colorScheme.tertiaryContainer
                        TransactionType.TRANSFER -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${currentType.displayName} Amount",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "₹ ",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (currentType) {
                                TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                                TransactionType.REFUND -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Text(
                            text = rawAmountInput.ifEmpty { "0" },
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (currentType) {
                                TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                                TransactionType.REFUND -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom On-Screen Numeric Keypad
            NumericKeypad(
                onKeyPress = { key ->
                    rawAmountInput = handleKeypadPress(rawAmountInput, key)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Form Fields Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Account Selector Field(s)
                if (currentType == TransactionType.TRANSFER) {
                    // Show "From Account" AND "To Account" pickers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // From Account
                        Box(modifier = Modifier.weight(1f)) {
                            AccountPickerField(
                                label = "From Account",
                                accountName = fromAccount?.name ?: "Select Account",
                                onClick = { fromAccountExpanded = true }
                            )
                            DropdownMenu(
                                expanded = fromAccountExpanded,
                                onDismissRequest = { fromAccountExpanded = false }
                            ) {
                                uiState.accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(acc.name) },
                                        onClick = {
                                            fromAccount = acc
                                            fromAccountExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // To Account
                        Box(modifier = Modifier.weight(1f)) {
                            AccountPickerField(
                                label = "To Account",
                                accountName = toAccount?.name ?: "Select Account",
                                onClick = { toAccountExpanded = true }
                            )
                            DropdownMenu(
                                expanded = toAccountExpanded,
                                onDismissRequest = { toAccountExpanded = false }
                            ) {
                                uiState.accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(acc.name) },
                                        onClick = {
                                            toAccount = acc
                                            toAccountExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Single Account picker
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AccountPickerField(
                            label = "Account",
                            accountName = fromAccount?.name ?: "Select Account",
                            onClick = { fromAccountExpanded = true }
                        )
                        DropdownMenu(
                            expanded = fromAccountExpanded,
                            onDismissRequest = { fromAccountExpanded = false }
                        ) {
                            uiState.accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text(acc.name) },
                                    onClick = {
                                        fromAccount = acc
                                        fromAccountExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Category Picker (Chips & Dropdown)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Box {
                            Text(
                                text = "More...",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { categoryDropdownExpanded = true }
                            )
                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                availableCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            selectedCategoryId = cat.id
                                            selectedCategoryName = cat.name
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableCategories.take(6).forEach { cat ->
                            val isSelected = selectedCategoryId == cat.id || selectedCategoryName == cat.name
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategoryId = cat.id
                                    selectedCategoryName = cat.name
                                },
                                label = { Text(cat.name) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                // Payee text field with suggestions dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = payee,
                        onValueChange = {
                            payee = it
                            payeeDropdownExpanded = uiState.distinctPayees.any { p -> p.contains(it, ignoreCase = true) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Payee / Merchant / Description") },
                        placeholder = { Text("e.g. Starbucks, Supermarket, John") },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    if (payeeDropdownExpanded && uiState.distinctPayees.isNotEmpty()) {
                        val matchingPayees = uiState.distinctPayees.filter { it.contains(payee, ignoreCase = true) }.take(5)
                        if (matchingPayees.isNotEmpty()) {
                            DropdownMenu(
                                expanded = payeeDropdownExpanded,
                                onDismissRequest = { payeeDropdownExpanded = false }
                            ) {
                                matchingPayees.forEach { suggestion ->
                                    DropdownMenuItem(
                                        text = { Text(suggestion) },
                                        onClick = {
                                            payee = suggestion
                                            payeeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Note text field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note (optional)") },
                    placeholder = { Text("e.g. Lunch with team, Groceries restock") },
                    leadingIcon = { Icon(Icons.Rounded.EditNote, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                // Date & Time Picker Field
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            scope.launch {
                                snackbarHostState.showSnackbar("Date: $selectedDateText")
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Date & Time",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedDateText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // "Split with friends" Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.CallSplit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Split with friends",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Divide cost with contacts or group",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isSplitEnabled,
                        onCheckedChange = { isSplitEnabled = it }
                    )
                }

                if (isSplitEnabled && selectedTabItem == 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Split Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        val selectedPersonName = uiState.persons.find { it.id == selectedPersonId }?.name ?: "Select Person"
                        ExposedDropdownMenuBox(
                            expanded = personDropdownExpanded,
                            onExpandedChange = { personDropdownExpanded = !personDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedPersonName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Split With Person") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = personDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = personDropdownExpanded,
                                onDismissRequest = { personDropdownExpanded = false }
                            ) {
                                if (uiState.persons.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No persons found. Add in Split & IOUs tab.") },
                                        onClick = { personDropdownExpanded = false }
                                    )
                                } else {
                                    uiState.persons.forEach { person ->
                                        DropdownMenuItem(
                                            text = { Text(person.name) },
                                            onClick = {
                                                selectedPersonId = person.id
                                                personDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Who Paid?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = paidByMe,
                                onClick = { paidByMe = true },
                                label = { Text("I Paid") }
                            )
                            FilterChip(
                                selected = !paidByMe,
                                onClick = { paidByMe = false },
                                label = { Text("Friend Paid") }
                            )
                        }

                        OutlinedTextField(
                            value = myShareInput,
                            onValueChange = { myShareInput = it },
                            label = { Text("My Share Amount (₹)") },
                            placeholder = { Text("e.g. 500 (Leave blank for 50/50)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // "Save" Button with validation & DB call
                Button(
                    onClick = {
                        val amountDouble = rawAmountInput.toDoubleOrNull() ?: 0.0
                        val amountPaise = (amountDouble * 100).toLong()

                        if (amountPaise <= 0L) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter a valid amount greater than 0") }
                            return@Button
                        }
                        if (fromAccount == null && !(isSplitEnabled && selectedTabItem == 0 && !paidByMe)) {
                            scope.launch { snackbarHostState.showSnackbar("Please select an account") }
                            return@Button
                        }

                        scope.launch {
                            val success = if (isSplitEnabled && selectedTabItem == 0) {
                                val pId = selectedPersonId
                                if (pId.isNullOrBlank()) {
                                    snackbarHostState.showSnackbar("Please select a person to split with")
                                    return@launch
                                }
                                val myShareDouble = myShareInput.toDoubleOrNull()
                                val mySharePaise = if (myShareDouble != null && myShareDouble > 0) {
                                    (myShareDouble * 100).toLong()
                                } else {
                                    amountPaise / 2
                                }

                                viewModel?.saveSplitTransaction(
                                    title = payee.ifBlank { "Split Expense" },
                                    totalAmountPaise = amountPaise,
                                    mySharePaise = mySharePaise,
                                    paidByMe = paidByMe,
                                    personId = pId,
                                    accountId = fromAccount?.id,
                                    categoryId = selectedCategoryId ?: selectedCategoryName,
                                    occurredAt = selectedOccurredAt
                                ) ?: true
                            } else {
                                val dbType = when (selectedTabItem) {
                                    0 -> DbTxnType.EXPENSE
                                    1 -> DbTxnType.INCOME
                                    2 -> DbTxnType.TRANSFER
                                    3 -> DbTxnType.REFUND
                                    else -> DbTxnType.EXPENSE
                                }
                                if (dbType == DbTxnType.TRANSFER) {
                                    if (toAccount == null || fromAccount?.id == toAccount?.id) {
                                        snackbarHostState.showSnackbar("From and To accounts must be different")
                                        return@launch
                                    }
                                } else {
                                    if (selectedCategoryId == null && selectedCategoryName.isBlank()) {
                                        snackbarHostState.showSnackbar("Please select a category")
                                        return@launch
                                    }
                                }

                                viewModel?.saveTransaction(
                                    type = dbType,
                                    amountPaise = amountPaise,
                                    accountId = fromAccount?.id,
                                    toAccountId = toAccount?.id,
                                    categoryId = selectedCategoryId ?: selectedCategoryName,
                                    payee = payee,
                                    note = note,
                                    occurredAt = selectedOccurredAt,
                                    scanSource = scanSource
                                ) ?: true
                            }

                            if (success) {
                                snackbarHostState.showSnackbar("Transaction saved successfully!")
                                onNavigate(Screen.Transactions())
                            } else {
                                snackbarHostState.showSnackbar("Failed to save transaction. Check inputs.")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Save Transaction",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Reusable Account Picker field widget.
 */
@Composable
private fun AccountPickerField(
    label: String,
    accountName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = accountName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Custom On-screen Numeric Keypad composable containing buttons 0-9, 00, decimal point, Backspace, Clear.
 */
@Composable
private fun NumericKeypad(
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keys = remember {
        listOf(
            listOf("1", "2", "3", "C"),
            listOf("4", "5", "6", "⌫"),
            listOf("7", "8", "9", "00"),
            listOf(".", "0")
        )
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        val weight = if (key == "0" && row.size == 2) 3f else 1f
                        KeypadButton(
                            key = key,
                            onClick = { onKeyPress(key) },
                            modifier = Modifier.weight(weight)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAction = key == "C" || key == "⌫"
    val containerColor = if (isAction) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }
    val contentColor = if (isAction) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = containerColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (key == "⌫") {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = "Backspace",
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = key,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}

/**
 * Handles numeric keypad press events safely for amount formatting.
 */
private fun handleKeypadPress(current: String, key: String): String {
    return when (key) {
        "C" -> "0"
        "⌫" -> {
            if (current.length <= 1) "0" else current.dropLast(1)
        }
        "." -> {
            if (current.contains(".")) current else "$current."
        }
        "00" -> {
            if (current == "0") "0"
            else if (current.contains(".") && current.substringAfter(".").length >= 2) current
            else if (current.contains(".") && current.substringAfter(".").length == 1) "${current}0"
            else "${current}00"
        }
        else -> { // "0".."9"
            if (current == "0") key
            else if (current.contains(".") && current.substringAfter(".").length >= 2) current
            else "$current$key"
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTransactionScreenPreview() {
    Coconut_PfaTheme {
        AddTransactionScreen(onNavigate = {})
    }
}
