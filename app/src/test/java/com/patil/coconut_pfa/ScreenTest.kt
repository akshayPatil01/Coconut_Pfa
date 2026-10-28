package com.patil.coconut_pfa

import com.patil.coconut_pfa.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenTest {

    @Test
    fun screenTitles_areCorrect() {
        assertEquals("Coconut PFA", Screen.Home.title)
        assertEquals("Transactions", Screen.Transactions.title)
        assertEquals("Payments", Screen.Payments.title)
        assertEquals("Accounts", Screen.Accounts.title)
        assertEquals("Add Transaction", Screen.AddTransaction().title)
        assertEquals("Financial Goals", Screen.Goals.title)
        assertEquals("Subscriptions", Screen.Subscriptions.title)
        assertEquals("Split & IOUs", Screen.SplitAndIOUs.title)
        assertEquals("Reports & Analytics", Screen.Reports.title)
        assertEquals("Settings", Screen.Settings.title)
        assertEquals("SMS Review Inbox", Screen.ReviewInbox.title)
        assertEquals("Account Details", Screen.AccountDetail("acc_1").title)
        assertEquals("Add Account", Screen.AddAccount().title)
        assertEquals("Add Bill", Screen.AddBill.title)
    }
}
