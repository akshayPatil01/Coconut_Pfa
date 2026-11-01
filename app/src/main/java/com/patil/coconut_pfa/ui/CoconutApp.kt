package com.patil.coconut_pfa.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patil.coconut_pfa.ui.screens.SettingsSection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patil.coconut_pfa.data.local.datastore.UserSettings
import com.patil.coconut_pfa.data.local.datastore.UserSettingsDataStore
import com.patil.coconut_pfa.data.local.db.CoconutDatabase
import com.patil.coconut_pfa.data.local.security.LockManager
import com.patil.coconut_pfa.data.local.security.PinHasher
import com.patil.coconut_pfa.data.repository.AccountRepository
import com.patil.coconut_pfa.data.repository.BillRepository
import com.patil.coconut_pfa.data.repository.CategoryRepository
import com.patil.coconut_pfa.data.repository.NetWorthRepository
import com.patil.coconut_pfa.data.repository.PersonLedgerRepository
import com.patil.coconut_pfa.data.repository.SettingsRepository
import com.patil.coconut_pfa.data.repository.SmsRepository
import com.patil.coconut_pfa.data.repository.TransactionRepository
import com.patil.coconut_pfa.data.repository.GoalRepository
import com.patil.coconut_pfa.ui.viewmodel.AddBillViewModel
import com.patil.coconut_pfa.ui.viewmodel.GoalsViewModel
import com.patil.coconut_pfa.ui.viewmodel.HomeViewModel
import com.patil.coconut_pfa.ui.viewmodel.PaymentsViewModel
import com.patil.coconut_pfa.ui.viewmodel.ReportsViewModel
import com.patil.coconut_pfa.ui.viewmodel.SplitAndIOUsViewModel
import com.patil.coconut_pfa.ui.viewmodel.SubscriptionsViewModel
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.CoconutBottomBar
import com.patil.coconut_pfa.ui.components.CoconutNavigationDrawer
import com.patil.coconut_pfa.ui.components.CoconutTopBar
import com.patil.coconut_pfa.ui.screens.AccountDetailScreen
import com.patil.coconut_pfa.ui.screens.AccountsScreen
import com.patil.coconut_pfa.ui.screens.AddAccountScreen
import com.patil.coconut_pfa.ui.screens.AddBillScreen
import com.patil.coconut_pfa.ui.screens.AddTransactionScreen
import com.patil.coconut_pfa.ui.screens.GoalsScreen
import com.patil.coconut_pfa.ui.screens.HomeScreen
import com.patil.coconut_pfa.ui.screens.ImportStatementScreen
import com.patil.coconut_pfa.ui.screens.LockScreen
import com.patil.coconut_pfa.ui.screens.ParserTestScreen
import com.patil.coconut_pfa.ui.screens.PaymentsScreen
import com.patil.coconut_pfa.ui.screens.ReceiptScanScreen
import com.patil.coconut_pfa.ui.screens.ReportsScreen
import com.patil.coconut_pfa.ui.screens.ReviewInboxScreen
import com.patil.coconut_pfa.ui.screens.SettingsScreen
import com.patil.coconut_pfa.ui.screens.SplitAndIOUsScreen
import com.patil.coconut_pfa.ui.screens.SubscriptionsScreen
import com.patil.coconut_pfa.ui.screens.TransactionsScreen
import com.patil.coconut_pfa.ui.screens.TrashScreen
import com.patil.coconut_pfa.ui.screens.WelcomeWorkflowScreen

import com.patil.coconut_pfa.ui.viewmodel.AccountDetailViewModel
import com.patil.coconut_pfa.ui.viewmodel.AccountsViewModel
import com.patil.coconut_pfa.ui.viewmodel.AddAccountViewModel
import com.patil.coconut_pfa.ui.viewmodel.AddTransactionViewModel
import com.patil.coconut_pfa.ui.viewmodel.TransactionsViewModel
import com.patil.coconut_pfa.ui.viewmodel.TrashViewModel
import com.patil.coconut_pfa.ui.viewmodel.ReviewInboxViewModel
import com.patil.coconut_pfa.util.PrivacyManager
import com.patil.coconut_pfa.worker.BillReminderWorker
import com.patil.coconut_pfa.worker.PurgeTrashWorker
import com.patil.coconut_pfa.worker.WidgetUpdateWorker
import kotlinx.coroutines.launch

/**
 * Main application composable containing TopAppBar, NavigationDrawer, BottomBar, and screen content.
 */
@Composable
fun CoconutApp(
    modifier: Modifier = Modifier,
    navigateToScreenSignal: String? = null,
    onHandledNavigateToScreen: () -> Unit = {},
) {
    val context = LocalContext.current
    val database = remember { CoconutDatabase.getInstance(context) }
    val accountRepository = remember {
        AccountRepository(
            accountDao = database.accountDao(),
            cardDetailsDao = database.cardDetailsDao(),
            cardStatementDao = database.cardStatementDao(),
            loanDetailsDao = database.loanDetailsDao(),
            fdDetailsDao = database.fdDetailsDao(),
            valuationDao = database.valuationDao()
        )
    }
    val transactionRepository = remember { TransactionRepository(database.txnDao(), context) }
    val categoryRepository = remember { CategoryRepository(database.categoryDao()) }
    val netWorthRepository = remember { NetWorthRepository(database.netWorthSnapshotDao()) }
    val personLedgerRepository = remember { PersonLedgerRepository(database.personDao(), database.ledgerEntryDao()) }
    val goalRepository = remember { GoalRepository(database.goalDao(), database.goalContributionDao()) }
    val billRepository = remember {
        BillRepository(
            billDao = database.billDao(),
            billOccurrenceDao = database.billOccurrenceDao(),
            cardStatementDao = database.cardStatementDao(),
            accountDao = database.accountDao(),
            cardDetailsDao = database.cardDetailsDao(),
            loanDetailsDao = database.loanDetailsDao(),
            txnDao = database.txnDao()
        )
    }
    val smsRepository = remember {
        SmsRepository(
            smsItemDao = database.smsItemDao(),
            merchantMemoryDao = database.merchantMemoryDao()
        )
    }

    val userSettingsDataStore = remember { UserSettingsDataStore(context) }
    val settingsRepository = remember { SettingsRepository(userSettingsDataStore) }

    val vaultDataStore = remember { com.patil.coconut_pfa.data.local.datastore.VaultDataStore(context) }
    val vaultRepository = remember { com.patil.coconut_pfa.data.repository.VaultRepository(database.vaultKeysetDao(), database.vaultItemDao(), database) }
    val vaultViewModel: com.patil.coconut_pfa.ui.viewmodel.VaultViewModel = viewModel(
        factory = com.patil.coconut_pfa.ui.viewmodel.VaultViewModel.Factory(vaultRepository, vaultDataStore)
    )
    val vaultUiState by vaultViewModel.uiState.collectAsState()

    val userSettings by settingsRepository.settingsFlow.collectAsState(initial = UserSettings())
    val isLocked by LockManager.isLocked.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val hasColdStarted = remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: android.content.Context?, intent: android.content.Intent?) {
                if (intent?.action == android.content.Intent.ACTION_SCREEN_OFF) {
                    com.patil.coconut_pfa.data.local.security.VaultSession.lock(context)
                }
            }
        }
        context.registerReceiver(receiver, android.content.IntentFilter(android.content.Intent.ACTION_SCREEN_OFF))
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    LaunchedEffect(isLocked) {
        if (isLocked) {
            com.patil.coconut_pfa.data.local.security.VaultSession.lock(context)
        }
    }

    // Schedule WorkManager job for purging old trash & bill reminders
    LaunchedEffect(Unit) {
        PurgeTrashWorker.schedule(context)
        WidgetUpdateWorker.schedule(context)
    }

    LaunchedEffect(userSettings.reminderHour) {
        BillReminderWorker.schedule(context, userSettings.reminderHour)
    }

    LaunchedEffect(userSettings.googleAccountEmail) {
        if (!userSettings.googleAccountEmail.isNullOrBlank()) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()
            val dailyWorkRequest = androidx.work.PeriodicWorkRequestBuilder<com.patil.coconut_pfa.worker.DriveBackupWorker>(1, java.util.concurrent.TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
            androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "DriveBackupWork",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
        }
    }

    // Sync PrivacyMode default state & perform cold start lock once settings are loaded
    LaunchedEffect(userSettings.onboardingDone, userSettings.pinHash, userSettings.privacyModeDefault) {
        PrivacyManager.setPrivacy(userSettings.privacyModeDefault)
        if (userSettings.onboardingDone && !hasColdStarted.value) {
            if (userSettings.pinHash != null) {
                LockManager.lock()
            }
            hasColdStarted.value = true
        } else if (!userSettings.onboardingDone) {
            hasColdStarted.value = true
        }
    }

    // Lifecycle Observer for Auto-Lock when backgrounded for >= autoLockSeconds
    DisposableEffect(lifecycleOwner, userSettings.autoLockSeconds, userSettings.onboardingDone, userSettings.pinHash) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    LockManager.onAppBackgrounded()
                }
                Lifecycle.Event.ON_START -> {
                    LockManager.onAppForegrounded(
                        autoLockSeconds = userSettings.autoLockSeconds,
                        isOnboardingDone = userSettings.onboardingDone,
                        hasPin = userSettings.pinHash != null
                    )
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val currentDestination = backStack.lastOrNull() ?: Screen.Home
    var activeSettingsSection by remember { mutableStateOf<SettingsSection?>(null) }
    var showSettingsDebugDialog by remember { mutableStateOf(false) }

    val navigateTo: (Screen) -> Unit = { destination ->
        activeSettingsSection = null
        if (destination == Screen.Home) {
            backStack.clear()
            backStack.add(Screen.Home)
        } else {
            if (backStack.lastOrNull() != destination) {
                backStack.remove(destination)
                backStack.add(destination)
            }
        }
    }

    LaunchedEffect(navigateToScreenSignal, isLocked, userSettings.onboardingDone) {
        if (!navigateToScreenSignal.isNullOrBlank() && !isLocked && userSettings.onboardingDone) {
            when (navigateToScreenSignal) {
                "payments" -> navigateTo(Screen.Payments)
                "add_transaction" -> navigateTo(Screen.AddTransaction())
            }
            onHandledNavigateToScreen()
        }
    }

    if (backStack.size > 1) {
        BackHandler {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    val handleWipeAndReset = {
        scope.launch {
            settingsRepository.setOnboardingDone(false)
            settingsRepository.setPin(null, null)
            LockManager.unlock()
        }
    }

    val isFullScreenOverlay = !userSettings.onboardingDone || isLocked || currentDestination == Screen.Onboarding || currentDestination == Screen.WelcomeWorkflow || currentDestination == Screen.LockScreen

    com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme(themeMode = userSettings.themeMode) {
        if (isFullScreenOverlay) {
            when {
                !userSettings.onboardingDone || currentDestination == Screen.Onboarding || currentDestination == Screen.WelcomeWorkflow -> {
                    WelcomeWorkflowScreen(
                        onNavigate = navigateTo,
                        onCompleteWelcomeWorkflow = {
                            scope.launch {
                                settingsRepository.setOnboardingDone(true)
                                LockManager.unlock()
                            }
                        },
                        modifier = modifier
                    )
                }
                isLocked || currentDestination == Screen.LockScreen -> {
                    LockScreen(
                        onNavigate = navigateTo,
                        pinHash = userSettings.pinHash,
                        pinSalt = userSettings.pinSalt,
                        biometricEnabled = userSettings.biometricEnabled,
                        onUnlock = { LockManager.unlock() },
                        onWipeAndReset = { handleWipeAndReset() },
                        modifier = modifier
                    )
                }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    CoconutNavigationDrawer(
                        currentDestination = currentDestination,
                        onNavigate = { destination ->
                            scope.launch {
                                drawerState.close()
                                if (currentDestination != destination) {
                                    navigateTo(destination)
                                }
                            }
                        }
                    )
                },
                gesturesEnabled = true
            ) {
                Scaffold(
                    modifier = modifier.fillMaxSize(),
                    topBar = {
                        if (currentDestination == Screen.Settings) {
                            if (activeSettingsSection == null) {
                                CoconutTopBar(
                                    title = "Settings",
                                    subtitle = "Preferences, security & vault",
                                    onMenuClick = {
                                        scope.launch {
                                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                        }
                                    },
                                    onSettingsClick = {},
                                    navigationIcon = {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.6f)),
                                            shadowElevation = 0.5.dp,
                                            modifier = Modifier.padding(start = 12.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                                    }
                                                },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Menu,
                                                    contentDescription = "Open drawer",
                                                    tint = Color(0xFF334155),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    },
                                    titleContent = {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Settings",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(percent = 50),
                                                    color = Color(0xFFE6F9F2),
                                                    border = BorderStroke(1.dp, Color(0xFFD1FAE5))
                                                ) {
                                                    Text(
                                                        text = "Fortis Core",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF047857),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(1.dp))
                                            Text(
                                                text = "Preferences, security & vault",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    },
                                    actionsContent = {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.6f)),
                                            shadowElevation = 0.5.dp,
                                            modifier = Modifier.padding(end = 12.dp)
                                        ) {
                                            IconButton(
                                                onClick = { showSettingsDebugDialog = true },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Tune,
                                                    contentDescription = "Quick Action",
                                                    tint = Color(0xFF334155),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                )
                            } else {
                                CoconutTopBar(
                                    title = activeSettingsSection?.title.orEmpty(),
                                    subtitle = null,
                                    onMenuClick = { activeSettingsSection = null },
                                    onSettingsClick = {},
                                    navigationIcon = {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.8f)),
                                            shadowElevation = 0.5.dp,
                                            modifier = Modifier.padding(start = 12.dp)
                                        ) {
                                            IconButton(
                                                onClick = { activeSettingsSection = null },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                                    contentDescription = "Back to Settings",
                                                    tint = Color(0xFF334155),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    },
                                    titleContent = {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "SETTINGS",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF94A3B8),
                                                    letterSpacing = 1.sp
                                                )
                                                Text(
                                                    text = "/",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFCBD5E1)
                                                )
                                                val sectionTag = when (activeSettingsSection) {
                                                    SettingsSection.APPEARANCE -> "APPEARANCE"
                                                    SettingsSection.SECURITY -> "SECURITY"
                                                    SettingsSection.VAULT -> "VAULT"
                                                    SettingsSection.SALARY_CYCLE -> "SALARY CYCLE"
                                                    SettingsSection.WIDGET -> "WIDGET"
                                                    SettingsSection.CATEGORIES -> "CATEGORIES"
                                                    SettingsSection.SMS_PARSER -> "SMS PARSER"
                                                    SettingsSection.REMINDERS -> "REMINDERS"
                                                    SettingsSection.BACKUP -> "BACKUP"
                                                    SettingsSection.TOOLS -> "TOOLS"
                                                    SettingsSection.ABOUT -> "ABOUT"
                                                    null -> ""
                                                }
                                                Text(
                                                    text = sectionTag,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFFD97706),
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(1.dp))
                                            Text(
                                                text = activeSettingsSection?.title.orEmpty(),
                                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A),
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                    },
                                    actionsContent = {
                                        val badgeText = when (activeSettingsSection) {
                                            SettingsSection.APPEARANCE -> "Dynamic"
                                            SettingsSection.SECURITY -> "Active"
                                            SettingsSection.VAULT -> "Enclave"
                                            SettingsSection.SALARY_CYCLE -> "Active"
                                            SettingsSection.WIDGET -> "Live"
                                            SettingsSection.CATEGORIES -> "Taxonomy"
                                            SettingsSection.SMS_PARSER -> "Parser"
                                            SettingsSection.REMINDERS -> "Alerts"
                                            SettingsSection.BACKUP -> "Cloud"
                                            SettingsSection.TOOLS -> "Utility"
                                            SettingsSection.ABOUT -> "v1.0.4"
                                            null -> ""
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(percent = 50),
                                            color = Color(0xFFF1F5F9),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.8f)),
                                            modifier = Modifier.padding(end = 12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(Color(0xFF10B981), CircleShape)
                                                )
                                                Text(
                                                    text = badgeText,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF475569)
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        } else {
                            CoconutTopBar(
                                title = currentDestination.title,
                                subtitle = currentDestination.subtitle,
                                onMenuClick = {
                                    scope.launch {
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    }
                                },
                                onSettingsClick = {
                                    navigateTo(Screen.Settings)
                                }
                            )
                        }
                    },
                    bottomBar = {
                        CoconutBottomBar(
                            currentDestination = currentDestination,
                            onNavigateToTab = { destination ->
                                navigateTo(destination)
                            },
                            onAddClick = {
                                navigateTo(Screen.AddTransaction())
                            }
                        )
                    }
                ) { innerPadding ->
                    AnimatedContent(
                        targetState = currentDestination,
                        transitionSpec = {
                            val isInitialTopLevel = isTopLevelDestination(initialState)
                            val isTargetTopLevel = isTopLevelDestination(targetState)

                            if (isInitialTopLevel && isTargetTopLevel) {
                                // Fast, smooth crossfade for top-level tab & sidebar navigation
                                fadeIn(
                                    animationSpec = tween(
                                        180,
                                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                                    )
                                ) togetherWith fadeOut(
                                    animationSpec = tween(
                                        120,
                                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                                    )
                                )
                            } else {
                                // Smooth horizontal slide for detail/editing screens
                                (fadeIn(animationSpec = tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing)) + 
                                 slideInHorizontally(animationSpec = tween(180), initialOffsetX = { 30 })) togetherWith
                                (fadeOut(animationSpec = tween(150)) + 
                                 slideOutHorizontally(animationSpec = tween(150), targetOffsetX = { -30 }))
                            }
                        },
                        label = "ScreenTransition"
                    ) { dest ->
                        val screenModifier = Modifier.padding(innerPadding)
                        when (dest) {
                            Screen.Home -> {
                                val homeViewModel: HomeViewModel = viewModel(
                                    factory = HomeViewModel.Factory(
                                        accountRepository = accountRepository,
                                        transactionRepository = transactionRepository,
                                        netWorthRepository = netWorthRepository,
                                        personLedgerRepository = personLedgerRepository,
                                        billRepository = billRepository,
                                        smsRepository = smsRepository,
                                        categoryRepository = categoryRepository,
                                        settingsRepository = settingsRepository
                                    )
                                )
                                HomeScreen(
                                    onNavigate = navigateTo,
                                    viewModel = homeViewModel,
                                    modifier = screenModifier
                                )
                            }
                            is Screen.Transactions -> {
                                val transactionsViewModel: TransactionsViewModel = viewModel(
                                    key = "txn_${dest.initialCategory}_${dest.initialCycle}_${dest.initialStartDateMillis}",
                                    factory = TransactionsViewModel.Factory(
                                        transactionRepository = transactionRepository,
                                        accountRepository = accountRepository,
                                        categoryRepository = categoryRepository,
                                        settingsRepository = settingsRepository,
                                        smsRepository = smsRepository,
                                        initialCategory = dest.initialCategory,
                                        initialCycle = dest.initialCycle,
                                        initialStartDateMillis = dest.initialStartDateMillis,
                                        initialEndDateMillis = dest.initialEndDateMillis
                                    )
                                )
                                TransactionsScreen(
                                    onNavigate = navigateTo,
                                    modifier = screenModifier,
                                    viewModel = transactionsViewModel
                                )
                            }
                            Screen.Payments -> {
                                val paymentsViewModel: PaymentsViewModel = viewModel(
                                    factory = PaymentsViewModel.Factory(
                                        billRepository = billRepository,
                                        accountRepository = accountRepository,
                                        categoryRepository = categoryRepository,
                                        transactionRepository = transactionRepository,
                                        personLedgerRepository = personLedgerRepository
                                    )
                                )
                                PaymentsScreen(
                                    onNavigate = navigateTo,
                                    modifier = screenModifier,
                                    viewModel = paymentsViewModel
                                )
                            }
                            Screen.Accounts -> {
                                val accountsViewModel: AccountsViewModel = viewModel(
                                    factory = AccountsViewModel.Factory(
                                        accountRepository = accountRepository,
                                        transactionRepository = transactionRepository,
                                        netWorthRepository = netWorthRepository,
                                        personLedgerRepository = personLedgerRepository,
                                        settingsRepository = settingsRepository
                                    )
                                )
                                AccountsScreen(
                                    onNavigate = navigateTo,
                                    accountsViewModel = accountsViewModel,
                                    modifier = screenModifier
                                )
                            }
                            is Screen.AddTransaction -> {
                                val addTransactionViewModel: AddTransactionViewModel = viewModel(
                                    key = "add_txn_${dest.txnId ?: dest.duplicateTxnId ?: dest.billOccurrenceId ?: "new"}",
                                    factory = AddTransactionViewModel.Factory(
                                        txnId = dest.txnId,
                                        duplicateTxnId = dest.duplicateTxnId,
                                        initialDateMillis = dest.initialDateMillis,
                                        billOccurrenceId = dest.billOccurrenceId,
                                        accountRepository = accountRepository,
                                        categoryRepository = categoryRepository,
                                        transactionRepository = transactionRepository,
                                        settingsRepository = settingsRepository,
                                        billRepository = billRepository,
                                        personLedgerRepository = personLedgerRepository
                                    )
                                )
                                AddTransactionScreen(
                                    onNavigate = navigateTo,
                                    modifier = screenModifier,
                                    viewModel = addTransactionViewModel,
                                    initialAmountPaise = dest.initialAmountPaise,
                                    initialPayee = dest.initialPayee,
                                    initialDateMillisArg = dest.initialDateMillis,
                                    scanSource = dest.scanSource
                                )
                            }
                            Screen.Goals -> {
                                val goalsViewModel: GoalsViewModel = viewModel(
                                    factory = GoalsViewModel.Factory(
                                        goalRepository = goalRepository,
                                        goalContributionDao = database.goalContributionDao(),
                                        settingsRepository = settingsRepository,
                                        accountRepository = accountRepository
                                    )
                                )
                                GoalsScreen(onNavigate = navigateTo, modifier = screenModifier, viewModel = goalsViewModel)
                            }
                            Screen.Subscriptions -> {
                                val subscriptionsViewModel: SubscriptionsViewModel = viewModel(
                                    factory = SubscriptionsViewModel.Factory(billRepository = billRepository)
                                )
                                SubscriptionsScreen(onNavigate = navigateTo, modifier = screenModifier, viewModel = subscriptionsViewModel)
                            }
                            Screen.SplitAndIOUs -> {
                                val splitAndIOUsViewModel: SplitAndIOUsViewModel = viewModel(
                                    factory = SplitAndIOUsViewModel.Factory(
                                        personLedgerRepository = personLedgerRepository,
                                        accountRepository = accountRepository,
                                        transactionRepository = transactionRepository
                                    )
                                )
                                SplitAndIOUsScreen(onNavigate = navigateTo, modifier = screenModifier, viewModel = splitAndIOUsViewModel)
                            }
                            Screen.Reports -> {
                                val reportsViewModel: ReportsViewModel = viewModel(
                                    factory = ReportsViewModel.Factory(
                                        transactionRepository = transactionRepository,
                                        categoryRepository = categoryRepository,
                                        settingsRepository = settingsRepository
                                    )
                                )
                                ReportsScreen(
                                    onNavigate = navigateTo,
                                    viewModel = reportsViewModel,
                                    modifier = screenModifier
                                )
                            }
                            Screen.Settings -> SettingsScreen(
                                onNavigate = navigateTo,
                                userSettings = userSettings,
                                vaultViewModel = vaultViewModel,
                                vaultUiState = vaultUiState,
                                activeSection = activeSettingsSection,
                                onActiveSectionChange = { activeSettingsSection = it },
                                onUpdateBiometricEnabled = { enabled -> scope.launch { settingsRepository.setBiometricEnabled(enabled) } },
                                onUpdateAutoLockSeconds = { seconds -> scope.launch { settingsRepository.setAutoLockSeconds(seconds) } },
                                onUpdatePrivacyByDefault = { enabled -> scope.launch { settingsRepository.setPrivacyModeDefault(enabled) } },
                                onUpdateSalaryStartDay = { day -> scope.launch { settingsRepository.setSalaryCycleStartDay(day) } },
                                onUpdateSmsEnabled = { enabled -> scope.launch { settingsRepository.setSmsEnabled(enabled) } },
                                onUpdateShowAmountOnWidget = { enabled -> scope.launch { settingsRepository.setShowAmountOnWidget(enabled) } },
                                onUpdateThemeMode = { themeMode -> scope.launch { settingsRepository.setThemeMode(themeMode) } },
                                onUpdateToastStyle = { toastStyle -> scope.launch { settingsRepository.setToastStyle(toastStyle) } },
                                onGenerateFakeTransactions = {
                                    scope.launch {
                                        val txns = (1..10000).map { i ->
                                            com.patil.coconut_pfa.data.local.entity.TxnEntity(
                                                id = "fake_txn_$i",
                                                type = if (i % 5 == 0) com.patil.coconut_pfa.data.local.entity.DbTxnType.INCOME else com.patil.coconut_pfa.data.local.entity.DbTxnType.EXPENSE,
                                                amountPaise = (100..50000L).random(),
                                                accountId = "acc_1",
                                                categoryId = "cat_exp_groceries",
                                                payee = "Merchant $i",
                                                note = "Fake txn #$i",
                                                occurredAt = System.currentTimeMillis() - (i.toLong() * 60 * 1000L),
                                                source = com.patil.coconut_pfa.data.local.entity.DbTxnSource.MANUAL
                                            )
                                        }
                                        transactionRepository.saveTransactions(txns)
                                    }
                                },
                                modifier = screenModifier
                            )
                            Screen.ReviewInbox -> {
                                val reviewInboxViewModel: ReviewInboxViewModel = viewModel(
                                    factory = ReviewInboxViewModel.Factory(
                                        smsRepository = smsRepository,
                                        accountRepository = accountRepository,
                                        categoryRepository = categoryRepository,
                                        transactionRepository = transactionRepository
                                    )
                                )
                                ReviewInboxScreen(
                                    onNavigate = navigateTo,
                                    viewModel = reviewInboxViewModel,
                                    modifier = screenModifier
                                )
                            }
                            is Screen.AccountDetail -> {
                                val accountDetailViewModel: AccountDetailViewModel = viewModel(
                                    key = "account_detail_${dest.accountId}",
                                    factory = AccountDetailViewModel.Factory(
                                        accountId = dest.accountId,
                                        accountRepository = accountRepository,
                                        transactionRepository = transactionRepository
                                    )
                                )
                                AccountDetailScreen(
                                    accountId = dest.accountId,
                                    onNavigate = navigateTo,
                                    modifier = screenModifier,
                                    viewModel = accountDetailViewModel
                                )
                            }
                            is Screen.AddAccount -> {
                                val addAccountViewModel: AddAccountViewModel = viewModel(
                                    key = "add_account_${dest.accountId ?: "new"}",
                                    factory = AddAccountViewModel.Factory(
                                        accountId = dest.accountId,
                                        accountRepository = accountRepository
                                    )
                                )
                                AddAccountScreen(
                                    onNavigate = navigateTo,
                                    accountId = dest.accountId,
                                    modifier = screenModifier,
                                    viewModel = addAccountViewModel
                                )
                            }
                            is Screen.AddBill -> {
                                val addBillViewModel: AddBillViewModel = viewModel(
                                    factory = AddBillViewModel.Factory(
                                        billRepository = billRepository,
                                        accountRepository = accountRepository,
                                        categoryRepository = categoryRepository
                                    )
                                )
                                AddBillScreen(
                                    onNavigate = navigateTo,
                                    initialKind = dest.initialKind,
                                    modifier = screenModifier,
                                    viewModel = addBillViewModel
                                )
                            }
                            Screen.ImportStatement -> ImportStatementScreen(onNavigate = navigateTo, modifier = screenModifier)
                            Screen.ReceiptScan -> ReceiptScanScreen(onNavigate = navigateTo, modifier = screenModifier)
                            Screen.Trash -> {
                                val trashViewModel: TrashViewModel = viewModel(
                                    factory = TrashViewModel.Factory(
                                        transactionRepository = transactionRepository,
                                        accountRepository = accountRepository,
                                        categoryRepository = categoryRepository
                                    )
                                )
                                TrashScreen(
                                    onNavigate = navigateTo,
                                    modifier = screenModifier,
                                    viewModel = trashViewModel
                                )
                            }
                            Screen.ParserTest -> ParserTestScreen(onNavigate = navigateTo, modifier = screenModifier)
                            Screen.WelcomeWorkflow -> WelcomeWorkflowScreen(
                                onNavigate = navigateTo,
                                onCompleteWelcomeWorkflow = {
                                    scope.launch {
                                        settingsRepository.setOnboardingDone(true)
                                        LockManager.unlock()
                                    }
                                },
                                modifier = screenModifier
                            )

                            Screen.Onboarding -> WelcomeWorkflowScreen(
                                onNavigate = navigateTo,
                                onCompleteWelcomeWorkflow = {
                                    scope.launch {
                                        settingsRepository.setOnboardingDone(true)
                                        LockManager.unlock()
                                    }
                                },
                                modifier = screenModifier
                            )
                            Screen.LockScreen -> LockScreen(
                                onNavigate = navigateTo,
                                pinHash = userSettings.pinHash,
                                pinSalt = userSettings.pinSalt,
                                biometricEnabled = userSettings.biometricEnabled,
                                onUnlock = { LockManager.unlock() },
                                onWipeAndReset = { handleWipeAndReset() },
                                modifier = screenModifier
                            )
                            is Screen.Vault -> {
                                val vaultCredentialsViewModel: com.patil.coconut_pfa.ui.viewmodel.VaultCredentialsViewModel = viewModel(
                                    factory = com.patil.coconut_pfa.ui.viewmodel.VaultCredentialsViewModel.Factory(
                                        vaultRepository = vaultRepository,
                                        accountRepository = accountRepository,
                                        transactionRepository = transactionRepository
                                    )
                                )
                                if (vaultUiState.screenState == com.patil.coconut_pfa.ui.viewmodel.VaultScreenState.NOT_SETUP) {
                                    com.patil.coconut_pfa.ui.screens.vault.VaultSetupScreen(
                                        viewModel = vaultViewModel,
                                        uiState = vaultUiState,
                                        modifier = screenModifier
                                    )
                                } else if (vaultUiState.screenState == com.patil.coconut_pfa.ui.viewmodel.VaultScreenState.LOCKED || vaultUiState.showRecoveryMode) {
                                    if (vaultUiState.showRecoveryMode) {
                                        com.patil.coconut_pfa.ui.screens.vault.VaultRecoveryScreen(
                                            viewModel = vaultViewModel,
                                            uiState = vaultUiState,
                                            modifier = screenModifier
                                        )
                                    } else {
                                        com.patil.coconut_pfa.ui.screens.vault.VaultUnlockScreen(
                                            viewModel = vaultViewModel,
                                            uiState = vaultUiState,
                                            modifier = screenModifier
                                        )
                                    }
                                } else {
                                    com.patil.coconut_pfa.ui.screens.vault.VaultMainScreen(
                                        initialTab = dest.tab,
                                        vaultViewModel = vaultViewModel,
                                        credentialsViewModel = vaultCredentialsViewModel,
                                        onNavigateBack = { navigateTo(Screen.Home) },
                                        onNavigateToAccount = { accountId -> navigateTo(Screen.AccountDetail(accountId)) },
                                        modifier = screenModifier
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showSettingsDebugDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDebugDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = null,
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Quick Actions & Tools", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                showSettingsDebugDialog = false
                                navigateTo(Screen.WelcomeWorkflow)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Replay Welcome Workflow")
                        }
                        OutlinedButton(
                            onClick = {
                                showSettingsDebugDialog = false
                                navigateTo(Screen.LockScreen)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Show Lock Screen Overlay")
                        }
                        Button(
                            onClick = {
                                showSettingsDebugDialog = false
                                scope.launch {
                                    val txns = (1..10000).map { i ->
                                        com.patil.coconut_pfa.data.local.entity.TxnEntity(
                                            id = "fake_txn_$i",
                                            type = if (i % 5 == 0) com.patil.coconut_pfa.data.local.entity.DbTxnType.INCOME else com.patil.coconut_pfa.data.local.entity.DbTxnType.EXPENSE,
                                            amountPaise = (100..50000L).random(),
                                            accountId = "acc_1",
                                            categoryId = "cat_exp_groceries",
                                            payee = "Merchant $i",
                                            note = "Fake txn #$i",
                                            occurredAt = System.currentTimeMillis() - (i.toLong() * 60 * 1000L),
                                            source = com.patil.coconut_pfa.data.local.entity.DbTxnSource.MANUAL
                                        )
                                    }
                                    transactionRepository.saveTransactions(txns)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Generate 10,000 Fake Transactions")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsDebugDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

private fun isTopLevelDestination(screen: Screen): Boolean = when (screen) {
    Screen.Home,
    is Screen.Transactions,
    Screen.Payments,
    Screen.Accounts,
    Screen.Reports,
    Screen.Goals,
    Screen.Subscriptions,
    Screen.SplitAndIOUs,
    Screen.ReviewInbox,
    Screen.Settings,
    Screen.WelcomeWorkflow,

    is Screen.Vault -> true
    else -> false
}

