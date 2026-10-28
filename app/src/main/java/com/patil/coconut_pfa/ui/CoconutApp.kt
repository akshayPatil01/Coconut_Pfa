package com.patil.coconut_pfa.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.patil.coconut_pfa.data.repository.TransactionRepository
import com.patil.coconut_pfa.ui.viewmodel.AddBillViewModel
import com.patil.coconut_pfa.ui.viewmodel.PaymentsViewModel
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
import com.patil.coconut_pfa.ui.screens.OnboardingScreen
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
import com.patil.coconut_pfa.ui.viewmodel.AccountDetailViewModel
import com.patil.coconut_pfa.ui.viewmodel.AccountsViewModel
import com.patil.coconut_pfa.ui.viewmodel.AddAccountViewModel
import com.patil.coconut_pfa.ui.viewmodel.AddTransactionViewModel
import com.patil.coconut_pfa.ui.viewmodel.TransactionsViewModel
import com.patil.coconut_pfa.ui.viewmodel.TrashViewModel
import com.patil.coconut_pfa.util.PrivacyManager
import com.patil.coconut_pfa.worker.BillReminderWorker
import com.patil.coconut_pfa.worker.PurgeTrashWorker
import kotlinx.coroutines.launch

/**
 * Main application composable containing TopAppBar, NavigationDrawer, BottomBar, and screen content.
 */
@Composable
fun CoconutApp(
    modifier: Modifier = Modifier,
    navigateToPaymentsSignal: Boolean = false,
    onHandledNavigateToPayments: () -> Unit = {},
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
    val transactionRepository = remember { TransactionRepository(database.txnDao()) }
    val categoryRepository = remember { CategoryRepository(database.categoryDao()) }
    val netWorthRepository = remember { NetWorthRepository(database.netWorthSnapshotDao()) }
    val personLedgerRepository = remember { PersonLedgerRepository(database.personDao(), database.ledgerEntryDao()) }
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

    val userSettingsDataStore = remember { UserSettingsDataStore(context) }
    val settingsRepository = remember { SettingsRepository(userSettingsDataStore) }

    val userSettings by settingsRepository.settingsFlow.collectAsState(initial = UserSettings())
    val isLocked by LockManager.isLocked.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val hasColdStarted = remember { mutableStateOf(false) }

    // Schedule WorkManager job for purging old trash & bill reminders
    LaunchedEffect(Unit) {
        PurgeTrashWorker.schedule(context)
    }

    LaunchedEffect(userSettings.reminderHour) {
        BillReminderWorker.schedule(context, userSettings.reminderHour)
    }

    // Sync PrivacyMode default state & perform cold start lock once settings are loaded
    LaunchedEffect(userSettings.onboardingDone, userSettings.pinHash, userSettings.privacyModeDefault) {
        PrivacyManager.setPrivacy(userSettings.privacyModeDefault)
        if (userSettings.onboardingDone && userSettings.pinHash != null && !hasColdStarted.value) {
            LockManager.lock()
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

    val navigateTo: (Screen) -> Unit = { destination ->
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

    LaunchedEffect(navigateToPaymentsSignal, isLocked, userSettings.onboardingDone) {
        if (navigateToPaymentsSignal && !isLocked && userSettings.onboardingDone) {
            navigateTo(Screen.Payments)
            onHandledNavigateToPayments()
        }
    }

    if (backStack.size > 1) {
        BackHandler {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    val handleCompleteOnboarding = { pin: String, enableBiometric: Boolean, salaryDay: Int, addBank: Boolean, addCard: Boolean, addCash: Boolean, addLoan: Boolean, addFd: Boolean ->
        val salt = PinHasher.generateSalt()
        val hashB64 = PinHasher.hashPin(pin, salt)
        val saltB64 = PinHasher.saltToBase64(salt)

        scope.launch {
            settingsRepository.setPin(hashB64, saltB64)
            settingsRepository.setBiometricEnabled(enableBiometric)
            settingsRepository.setSalaryCycleStartDay(salaryDay)
            accountRepository.createOnboardingAccounts(addBank, addCard, addCash, addLoan, addFd)
            settingsRepository.setOnboardingDone(true)
            LockManager.unlock()
        }
    }

    val handleWipeAndReset = {
        scope.launch {
            settingsRepository.setOnboardingDone(false)
            settingsRepository.setPin(null, null)
            LockManager.unlock()
        }
    }

    val isFullScreenOverlay = !userSettings.onboardingDone || isLocked || currentDestination == Screen.Onboarding || currentDestination == Screen.LockScreen

    if (isFullScreenOverlay) {
        when {
            !userSettings.onboardingDone || currentDestination == Screen.Onboarding -> {
                OnboardingScreen(
                    onNavigate = navigateTo,
                    onCompleteOnboarding = { pin, bio, day, b, c, ca, l, fd ->
                        handleCompleteOnboarding(pin, bio, day, b, c, ca, l, fd)
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
                        scope.launch { drawerState.close() }
                        navigateTo(destination)
                    }
                )
            },
            gesturesEnabled = true
        ) {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                topBar = {
                    CoconutTopBar(
                        title = currentDestination.title,
                        onMenuClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        },
                        onSettingsClick = {
                            navigateTo(Screen.Settings)
                        },
                    )
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
                val screenModifier = Modifier.padding(innerPadding)
                when (val dest = currentDestination) {
                    Screen.Home -> HomeScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.Transactions -> {
                        val transactionsViewModel: TransactionsViewModel = viewModel(
                            factory = TransactionsViewModel.Factory(
                                transactionRepository = transactionRepository,
                                accountRepository = accountRepository,
                                categoryRepository = categoryRepository,
                                settingsRepository = settingsRepository
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
                                billRepository = billRepository
                            )
                        )
                        AddTransactionScreen(
                            onNavigate = navigateTo,
                            modifier = screenModifier,
                            viewModel = addTransactionViewModel
                        )
                    }
                    Screen.Goals -> GoalsScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.Subscriptions -> SubscriptionsScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.SplitAndIOUs -> SplitAndIOUsScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.Reports -> ReportsScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.Settings -> SettingsScreen(
                        onNavigate = navigateTo,
                        userSettings = userSettings,
                        onUpdateBiometricEnabled = { enabled -> scope.launch { settingsRepository.setBiometricEnabled(enabled) } },
                        onUpdateAutoLockSeconds = { seconds -> scope.launch { settingsRepository.setAutoLockSeconds(seconds) } },
                        onUpdatePrivacyByDefault = { enabled -> scope.launch { settingsRepository.setPrivacyModeDefault(enabled) } },
                        onUpdateSalaryStartDay = { day -> scope.launch { settingsRepository.setSalaryCycleStartDay(day) } },
                        onUpdateSmsEnabled = { enabled -> scope.launch { settingsRepository.setSmsEnabled(enabled) } },
                        modifier = screenModifier
                    )
                    Screen.ReviewInbox -> ReviewInboxScreen(onNavigate = navigateTo, modifier = screenModifier)
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
                    Screen.AddBill -> {
                        val addBillViewModel: AddBillViewModel = viewModel(
                            factory = AddBillViewModel.Factory(
                                billRepository = billRepository,
                                accountRepository = accountRepository,
                                categoryRepository = categoryRepository
                            )
                        )
                        AddBillScreen(
                            onNavigate = navigateTo,
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
                    Screen.Onboarding -> OnboardingScreen(
                        onNavigate = navigateTo,
                        onCompleteOnboarding = { pin, bio, day, b, c, ca, l, fd ->
                            handleCompleteOnboarding(pin, bio, day, b, c, ca, l, fd)
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
                }
            }
        }
    }
}
