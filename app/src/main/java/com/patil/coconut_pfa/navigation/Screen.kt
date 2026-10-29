package com.patil.coconut_pfa.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations for Coconut PFA.
 */
@Serializable
sealed interface Screen {
    val title: String
    val subtitle: String? get() = null

    @Serializable
    data object Home : Screen {
        override val title: String = "Dashboard"
        override val subtitle: String = "Track your financial progress"
    }

    @Serializable
    data class Transactions(
        val initialCategory: String? = null,
        val initialCycle: String? = null,
        val initialStartDateMillis: Long? = null,
        val initialEndDateMillis: Long? = null
    ) : Screen {
        override val title: String = "Transactions"
        override val subtitle: String = "Track income and expenses"
    }

    @Serializable
    data object Payments : Screen {
        override val title: String = "Payments"
        override val subtitle: String = "Bills, subscriptions & EMIs"
    }

    @Serializable
    data object Accounts : Screen {
        override val title: String = "Accounts"
        override val subtitle: String = "Overview of assets & liabilities"
    }

    @Serializable
    data class AddTransaction(
        val txnId: String? = null,
        val duplicateTxnId: String? = null,
        val initialDateMillis: Long? = null,
        val billOccurrenceId: String? = null,
        val initialAmountPaise: Long? = null,
        val initialPayee: String? = null,
        val scanSource: Boolean = false
    ) : Screen {
        override val title: String = if (txnId != null) "Edit Transaction" else "Add Transaction"
        override val subtitle: String = "Record income, expense or transfer"
    }

    @Serializable
    data object Goals : Screen {
        override val title: String = "Financial Goals"
        override val subtitle: String = "Track progress towards your targets"
    }

    @Serializable
    data object Subscriptions : Screen {
        override val title: String = "Subscriptions"
        override val subtitle: String = "Recurring plans and auto-debits"
    }

    @Serializable
    data object SplitAndIOUs : Screen {
        override val title: String = "Split"
        override val subtitle: String = "Shared balances and group expenses"
    }

    @Serializable
    data object Reports : Screen {
        override val title: String = "Reports & Analytics"
        override val subtitle: String = "Visual insights and spending trends"
    }

    @Serializable
    data object Settings : Screen {
        override val title: String = "Settings"
        override val subtitle: String = "Preferences, security & data management"
    }

    @Serializable
    data object ReviewInbox : Screen {
        override val title: String = "SMS Review Inbox"
        override val subtitle: String = "Unprocessed automated transaction SMS"
    }

    @Serializable
    data class AccountDetail(val accountId: String) : Screen {
        override val title: String = "Account Details"
        override val subtitle: String = "Balances, cards and transaction history"
    }

    @Serializable
    data class AddAccount(val accountId: String? = null) : Screen {
        override val title: String = if (accountId == null) "Add Account" else "Edit Account"
        override val subtitle: String = "Configure bank, card, cash or loan"
    }

    @Serializable
    data class AddBill(val initialKind: String? = null) : Screen {
        override val title: String = if (initialKind == "SUBSCRIPTION") "Add Subscription" else "Add Bill"
        override val subtitle: String = "Set up recurring payment reminders"
    }

    @Serializable
    data object ImportStatement : Screen {
        override val title: String = "Import Statement"
        override val subtitle: String = "Parse bank or credit card statements"
    }

    @Serializable
    data object ReceiptScan : Screen {
        override val title: String = "Scan Receipt"
        override val subtitle: String = "Extract transaction details using OCR"
    }

    @Serializable
    data object Trash : Screen {
        override val title: String = "Trash & Recovery"
        override val subtitle: String = "Restore or permanently delete items"
    }

    @Serializable
    data object ParserTest : Screen {
        override val title: String = "SMS Parser Test"
        override val subtitle: String = "Debug SMS parsing regex & patterns"
    }

    @Serializable
    data object Onboarding : Screen {
        override val title: String = "Welcome to Fortis"
        override val subtitle: String = "Set up your personal finance manager"
    }

    @Serializable
    data object LockScreen : Screen {
        override val title: String = "App Locked"
        override val subtitle: String = "Enter your PIN to unlock"
    }

    @Serializable
    data object WelcomeWorkflow : Screen {
        override val title: String = "Welcome Workflow"
        override val subtitle: String = "Preview the new welcome experience"
    }

    @Serializable
    data class Vault(val tab: VaultTab = VaultTab.CARDS) : Screen {
        override val title: String = if (tab == VaultTab.CARDS) "Vault Cards" else "Vault Credentials"
        override val subtitle: String = "Encrypted credentials & cards"
    }
}

@Serializable
enum class VaultTab {
    CARDS,
    CREDENTIALS
}
