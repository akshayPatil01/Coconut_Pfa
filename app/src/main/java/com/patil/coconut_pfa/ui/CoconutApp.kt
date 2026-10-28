package com.patil.coconut_pfa.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch

/**
 * Main application composable containing TopAppBar, NavigationDrawer, BottomBar, and screen content.
 */
@Composable
fun CoconutApp(
    modifier: Modifier = Modifier,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
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

    if (backStack.size > 1) {
        BackHandler {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    val isFullScreenOverlay = currentDestination == Screen.Onboarding || currentDestination == Screen.LockScreen

    if (isFullScreenOverlay) {
        when (currentDestination) {
            Screen.Onboarding -> OnboardingScreen(onNavigate = navigateTo, modifier = modifier)
            Screen.LockScreen -> LockScreen(onNavigate = navigateTo, modifier = modifier)
            else -> Unit
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
                            navigateTo(Screen.AddTransaction)
                        }
                    )
                }
            ) { innerPadding ->
                val screenModifier = Modifier.padding(innerPadding)
                when (val dest = currentDestination) {
                    Screen.Home -> HomeScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.Transactions -> TransactionsScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.Payments -> PaymentsScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.Accounts -> AccountsScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.AddTransaction -> AddTransactionScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.Goals -> GoalsScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.Subscriptions -> SubscriptionsScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.SplitAndIOUs -> SplitAndIOUsScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.Reports -> ReportsScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.Settings -> SettingsScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.ReviewInbox -> ReviewInboxScreen(onNavigate = navigateTo, modifier = screenModifier)
                    is Screen.AccountDetail -> AccountDetailScreen(accountId = dest.accountId, onNavigate = navigateTo, modifier = screenModifier)
                    Screen.AddAccount -> AddAccountScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.AddBill -> AddBillScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.ImportStatement -> ImportStatementScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.ReceiptScan -> ReceiptScanScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.Trash -> TrashScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.ParserTest -> ParserTestScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.Onboarding -> OnboardingScreen(onNavigate = navigateTo, modifier = screenModifier)
                    Screen.LockScreen -> LockScreen(onNavigate = navigateTo, modifier = screenModifier)
                }
            }
        }
    }
}
