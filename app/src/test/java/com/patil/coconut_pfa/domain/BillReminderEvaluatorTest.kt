package com.patil.coconut_pfa.domain

import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.BillEntity
import com.patil.coconut_pfa.data.local.entity.BillOccurrenceEntity
import com.patil.coconut_pfa.data.local.entity.CardStatementEntity
import com.patil.coconut_pfa.data.local.entity.DbAccountInstitution
import com.patil.coconut_pfa.data.local.entity.DbAccountType
import com.patil.coconut_pfa.data.local.entity.DbBillFrequency
import com.patil.coconut_pfa.data.local.entity.DbBillKind
import com.patil.coconut_pfa.data.local.entity.DbBillOccurrenceStatus
import com.patil.coconut_pfa.data.local.entity.DbCardStatementSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BillReminderEvaluatorTest {

    private val today = LocalDate.of(2025, 10, 20)

    private val sampleBill = BillEntity(
        id = "bill_elec",
        name = "Electricity Bill",
        kind = DbBillKind.UTILITY,
        amountPaise = 150000L,
        frequency = DbBillFrequency.MONTHLY,
        anchorDate = "2025-10-23",
        reminderDaysBefore = 3
    )

    private val sampleCardAccount = AccountEntity(
        id = "acc_card_1",
        name = "HDFC Regalia",
        type = DbAccountType.CREDIT_CARD,
        institution = DbAccountInstitution.HDFC,
        last4 = "4321",
        openingBalancePaise = 0L,
        openingDate = "2025-01-01",
        icon = "credit_card",
        colour = 0xFF1E88E5L
    )

    private val sampleStatement = CardStatementEntity(
        id = "stmt_1",
        accountId = "acc_card_1",
        periodStart = "2025-09-01",
        periodEnd = "2025-09-30",
        amountPaise = 2500000L,
        dueDate = "2025-10-20",
        source = DbCardStatementSource.AUTO
    )

    @Test
    fun testOccurrenceDueInNDaysGeneratesNotificationWithoutAmount() {
        // Due in 3 days (2025-10-23 relative to 2025-10-20)
        val occ = BillOccurrenceEntity(
            id = "occ_1",
            billId = sampleBill.id,
            dueDate = "2025-10-23",
            amountPaise = 150000L,
            status = DbBillOccurrenceStatus.UPCOMING
        )

        val notifications = BillReminderEvaluator.evaluateReminders(
            occurrences = listOf(occ),
            bills = listOf(sampleBill),
            statements = emptyList(),
            accounts = emptyList(),
            defaultReminderDays = 3,
            today = today
        )

        assertEquals(1, notifications.size)
        val notif = notifications.first()
        assertEquals("Electricity Bill", notif.title)
        assertTrue(notif.dueMessage.contains("Due on 23 Oct 2025"))
        // Strict privacy requirement: NO amount in title or dueMessage!
        assertFalse(notif.title.contains("1500"))
        assertFalse(notif.dueMessage.contains("1500"))
        assertFalse(notif.title.contains("₹"))
        assertFalse(notif.dueMessage.contains("₹"))
    }

    @Test
    fun testOccurrenceDueTodayGeneratesDueTodayNotificationWithoutAmount() {
        val occ = BillOccurrenceEntity(
            id = "occ_today",
            billId = sampleBill.id,
            dueDate = "2025-10-20",
            amountPaise = 150000L,
            status = DbBillOccurrenceStatus.UPCOMING
        )

        val notifications = BillReminderEvaluator.evaluateReminders(
            occurrences = listOf(occ),
            bills = listOf(sampleBill),
            statements = emptyList(),
            accounts = emptyList(),
            defaultReminderDays = 3,
            today = today
        )

        assertEquals(1, notifications.size)
        val notif = notifications.first()
        assertEquals("Electricity Bill", notif.title)
        assertTrue(notif.dueMessage.contains("Due today"))
        assertFalse(notif.dueMessage.contains("1500"))
    }

    @Test
    fun testOccurrenceOverdueGeneratesOverdueNotificationWithoutAmount() {
        val occ = BillOccurrenceEntity(
            id = "occ_overdue",
            billId = sampleBill.id,
            dueDate = "2025-10-15",
            amountPaise = 150000L,
            status = DbBillOccurrenceStatus.UPCOMING
        )

        val notifications = BillReminderEvaluator.evaluateReminders(
            occurrences = listOf(occ),
            bills = listOf(sampleBill),
            statements = emptyList(),
            accounts = emptyList(),
            defaultReminderDays = 3,
            today = today
        )

        assertEquals(1, notifications.size)
        val notif = notifications.first()
        assertEquals("Electricity Bill", notif.title)
        assertTrue(notif.dueMessage.contains("Overdue"))
        assertFalse(notif.dueMessage.contains("1500"))
    }

    @Test
    fun testOccurrenceFarInFutureNotNotified() {
        // Due in 10 days, but reminderDaysBefore = 3
        val occ = BillOccurrenceEntity(
            id = "occ_future",
            billId = sampleBill.id,
            dueDate = "2025-10-30",
            amountPaise = 150000L,
            status = DbBillOccurrenceStatus.UPCOMING
        )

        val notifications = BillReminderEvaluator.evaluateReminders(
            occurrences = listOf(occ),
            bills = listOf(sampleBill),
            statements = emptyList(),
            accounts = emptyList(),
            defaultReminderDays = 3,
            today = today
        )

        assertTrue(notifications.isEmpty())
    }

    @Test
    fun testPaidAndSkippedOccurrencesIgnored() {
        val paidOcc = BillOccurrenceEntity(
            id = "occ_paid",
            billId = sampleBill.id,
            dueDate = "2025-10-20",
            amountPaise = 150000L,
            status = DbBillOccurrenceStatus.PAID
        )
        val skippedOcc = BillOccurrenceEntity(
            id = "occ_skipped",
            billId = sampleBill.id,
            dueDate = "2025-10-20",
            amountPaise = 150000L,
            status = DbBillOccurrenceStatus.SKIPPED
        )

        val notifications = BillReminderEvaluator.evaluateReminders(
            occurrences = listOf(paidOcc, skippedOcc),
            bills = listOf(sampleBill),
            statements = emptyList(),
            accounts = emptyList(),
            defaultReminderDays = 3,
            today = today
        )

        assertTrue(notifications.isEmpty())
    }

    @Test
    fun testCreditCardStatementNotification() {
        val occ = BillOccurrenceEntity(
            id = "occ_stmt",
            statementId = sampleStatement.id,
            dueDate = "2025-10-20",
            amountPaise = 2500000L,
            status = DbBillOccurrenceStatus.UPCOMING
        )

        val notifications = BillReminderEvaluator.evaluateReminders(
            occurrences = listOf(occ),
            bills = emptyList(),
            statements = listOf(sampleStatement),
            accounts = listOf(sampleCardAccount),
            defaultReminderDays = 3,
            today = today
        )

        assertEquals(1, notifications.size)
        val notif = notifications.first()
        assertEquals("HDFC Regalia Statement", notif.title)
        assertTrue(notif.dueMessage.contains("Due today"))
        assertFalse(notif.dueMessage.contains("25000"))
    }
}
