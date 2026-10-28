package com.patil.coconut_pfa.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations for Coconut PFA.
 */
@Serializable
sealed interface Screen {
    val title: String

    @Serializable
    data object Home : Screen {
        override val title: String = "Coconut PFA"
    }

    @Serializable
    data object Transactions : Screen {
        override val title: String = "Transactions"
    }

    @Serializable
    data object Payments : Screen {
        override val title: String = "Payments"
    }

    @Serializable
    data object Accounts : Screen {
        override val title: String = "Accounts"
    }

    @Serializable
    data object AddTransaction : Screen {
        override val title: String = "Add Transaction"
    }

    @Serializable
    data object Goals : Screen {
        override val title: String = "Financial Goals"
    }

    @Serializable
    data object Subscriptions : Screen {
        override val title: String = "Subscriptions"
    }

    @Serializable
    data object SplitAndIOUs : Screen {
        override val title: String = "Split & IOUs"
    }

    @Serializable
    data object Reports : Screen {
        override val title: String = "Reports & Analytics"
    }

    @Serializable
    data object Settings : Screen {
        override val title: String = "Settings"
    }

    @Serializable
    data object ReviewInbox : Screen {
        override val title: String = "SMS Review Inbox"
    }

    @Serializable
    data class AccountDetail(val accountId: String) : Screen {
        override val title: String = "Account Details"
    }

    @Serializable
    data class AddAccount(val accountId: String? = null) : Screen {
        override val title: String = if (accountId == null) "Add Account" else "Edit Account"
    }

    @Serializable
    data object AddBill : Screen {
        override val title: String = "Add Bill"
    }

    @Serializable
    data object ImportStatement : Screen {
        override val title: String = "Import Statement"
    }

    @Serializable
    data object ReceiptScan : Screen {
        override val title: String = "Scan Receipt"
    }

    @Serializable
    data object Trash : Screen {
        override val title: String = "Trash & Data Recovery"
    }

    @Serializable
    data object ParserTest : Screen {
        override val title: String = "SMS Parser Test"
    }

    @Serializable
    data object Onboarding : Screen {
        override val title: String = "Welcome to Coconut PFA"
    }

    @Serializable
    data object LockScreen : Screen {
        override val title: String = "App Locked"
    }
}
