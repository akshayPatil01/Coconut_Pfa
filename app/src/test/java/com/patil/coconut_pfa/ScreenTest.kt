package com.patil.coconut_pfa

import com.patil.coconut_pfa.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenTest {

    @Test
    fun screenTitlesAndSubtitles_areCorrect() {
        assertEquals("Dashboard", Screen.Home.title)
        assertEquals("Track your financial progress", Screen.Home.subtitle)

        assertEquals("Transactions", Screen.Transactions().title)
        assertEquals("Track income and expenses", Screen.Transactions().subtitle)

        assertEquals("Payments", Screen.Payments.title)
        assertEquals("Bills, subscriptions & EMIs", Screen.Payments.subtitle)

        assertEquals("Accounts", Screen.Accounts.title)
        assertEquals("Overview of assets & liabilities", Screen.Accounts.subtitle)

        assertEquals("Add Transaction", Screen.AddTransaction().title)
        assertEquals("Record income, expense or transfer", Screen.AddTransaction().subtitle)

        assertEquals("Financial Goals", Screen.Goals.title)
        assertEquals("Track progress towards your targets", Screen.Goals.subtitle)

        assertEquals("Subscriptions", Screen.Subscriptions.title)
        assertEquals("Recurring plans and auto-debits", Screen.Subscriptions.subtitle)

        assertEquals("Split & IOUs", Screen.SplitAndIOUs.title)
        assertEquals("Shared balances and group expenses", Screen.SplitAndIOUs.subtitle)

        assertEquals("Reports & Analytics", Screen.Reports.title)
        assertEquals("Visual insights and spending trends", Screen.Reports.subtitle)

        assertEquals("Settings", Screen.Settings.title)
        assertEquals("Preferences, security & data management", Screen.Settings.subtitle)

        assertEquals("SMS Review Inbox", Screen.ReviewInbox.title)
        assertEquals("Unprocessed automated transaction SMS", Screen.ReviewInbox.subtitle)

        assertEquals("Account Details", Screen.AccountDetail("acc_1").title)
        assertEquals("Balances, cards and transaction history", Screen.AccountDetail("acc_1").subtitle)

        assertEquals("Add Account", Screen.AddAccount().title)
        assertEquals("Configure bank, card, cash or loan", Screen.AddAccount().subtitle)

        assertEquals("Add Bill", Screen.AddBill().title)
        assertEquals("Set up recurring payment reminders", Screen.AddBill().subtitle)
    }
}
