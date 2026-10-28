package com.patil.coconut_pfa

import com.patil.coconut_pfa.data.model.AccountType
import com.patil.coconut_pfa.data.repository.FakeData
import com.patil.coconut_pfa.util.PrivacyManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeDataTest {

    @Test
    fun testFakeAccountsAndNetWorth() {
        val accounts = FakeData.accounts
        assertTrue(accounts.isNotEmpty())
        assertTrue(accounts.any { it.type == AccountType.BANK })
        assertTrue(accounts.any { it.type == AccountType.CREDIT_CARD })
        assertTrue(accounts.any { it.type == AccountType.CASH })
        assertTrue(accounts.any { it.type == AccountType.WALLET })
        assertTrue(accounts.any { it.type == AccountType.INVESTMENT })
        assertTrue(accounts.any { it.type == AccountType.FIXED_DEPOSIT })
        assertTrue(accounts.any { it.type == AccountType.LOAN })

        val netWorth = FakeData.netWorthInPaise
        assertTrue(netWorth > 0L)
    }

    @Test
    fun testSmsReviewItemsCount() {
        val smsList = FakeData.smsReviewItems
        assertEquals(6, smsList.size)
    }

    @Test
    fun testDueSoonItemsHasOverdue() {
        val dueItems = FakeData.dueItems
        assertTrue(dueItems.isNotEmpty())
        assertTrue("At least 1 item must be overdue", dueItems.any { it.isOverdue })
    }

    @Test
    fun testGoalsAndContributions() {
        val goals = FakeData.goals
        assertTrue(goals.isNotEmpty())
        assertTrue(goals.all { it.targetAmountInPaise > 0 })
    }

    @Test
    fun testSubscriptionsList() {
        val subs = FakeData.subscriptions
        assertTrue(subs.isNotEmpty())
        assertTrue(FakeData.totalMonthlySubscriptionCostInPaise > 0L)
    }

    @Test
    fun testPersonIOUsList() {
        val people = FakeData.personIOUs
        assertTrue(people.isNotEmpty())
        assertTrue(people.any { it.netBalanceInPaise > 0 })
        assertTrue(people.any { it.netBalanceInPaise < 0 })
    }

    @Test
    fun testTrashItemsList() {
        val trash = FakeData.trashItems
        assertTrue(trash.isNotEmpty())
    }

    @Test
    fun testCycleSummaryValues() {
        val cycle = FakeData.currentCycleSummary
        assertEquals("25 Oct", cycle.startDate)
        assertEquals("24 Nov", cycle.endDate)
        assertEquals(12000000L, cycle.receivedInPaise) // ₹1,20,000
        assertEquals(4525050L, cycle.spentInPaise)     // ₹45,250.50
    }

    @Test
    fun testUnbilledCreditCardsList() {
        val unbilledCc = FakeData.unbilledCreditCards
        assertTrue(unbilledCc.isNotEmpty())
        assertTrue(unbilledCc.all { it.type == AccountType.CREDIT_CARD && it.unbilledAmountInPaise > 0 })
    }

    @Test
    fun testTransactionsGroupedByDay() {
        val txGrouped = FakeData.transactionsByDay
        assertTrue(txGrouped.isNotEmpty())
        assertTrue(txGrouped.containsKey("12 Nov 2024"))
        assertTrue(txGrouped.containsKey("11 Nov 2024"))
    }

    @Test
    fun testPrivacyManagerToggle() {
        PrivacyManager.setPrivacy(false)
        assertFalse(PrivacyManager.isEnabled())

        PrivacyManager.togglePrivacy()
        assertTrue(PrivacyManager.isEnabled())

        PrivacyManager.togglePrivacy()
        assertFalse(PrivacyManager.isEnabled())
    }
}
