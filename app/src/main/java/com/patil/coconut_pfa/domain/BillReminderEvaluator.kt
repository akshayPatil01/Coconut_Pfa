package com.patil.coconut_pfa.domain

import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.BillEntity
import com.patil.coconut_pfa.data.local.entity.BillOccurrenceEntity
import com.patil.coconut_pfa.data.local.entity.CardStatementEntity
import com.patil.coconut_pfa.data.local.entity.DbBillOccurrenceStatus
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ReminderNotification(
    val occurrenceId: String,
    val title: String,
    val dueMessage: String
)

object BillReminderEvaluator {

    private val DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy")

    fun evaluateReminders(
        occurrences: List<BillOccurrenceEntity>,
        bills: List<BillEntity>,
        statements: List<CardStatementEntity>,
        accounts: List<AccountEntity>,
        defaultReminderDays: Int,
        today: LocalDate = KolkataDateTimeUtils.currentLocalDate()
    ): List<ReminderNotification> {
        val billMap = bills.associateBy { it.id }
        val statementMap = statements.associateBy { it.id }
        val accountMap = accounts.associateBy { it.id }

        val upcomingOccurrences = occurrences.filter { it.status == DbBillOccurrenceStatus.UPCOMING }

        val result = mutableListOf<ReminderNotification>()

        for (occ in upcomingOccurrences) {
            val occDate = try {
                LocalDate.parse(occ.dueDate, KolkataDateTimeUtils.DATE_FORMATTER)
            } catch (e: Exception) {
                continue
            }

            var title = "Bill Payment"
            var reminderDaysBefore = defaultReminderDays

            when {
                occ.billId != null -> {
                    val bill = billMap[occ.billId]
                    title = bill?.name ?: "Recurring Bill"
                    reminderDaysBefore = bill?.reminderDaysBefore ?: defaultReminderDays
                }
                occ.statementId != null -> {
                    val statement = statementMap[occ.statementId]
                    val cardAcc = accountMap[statement?.accountId]
                    title = "${cardAcc?.name ?: "Credit Card"} Statement"
                    reminderDaysBefore = defaultReminderDays
                }
                occ.loanAccountId != null -> {
                    val loanAcc = accountMap[occ.loanAccountId]
                    title = "${loanAcc?.name ?: "Loan"} EMI"
                    reminderDaysBefore = defaultReminderDays
                }
            }

            val isOverdue = occDate < today
            val isDueToday = occDate == today
            val isDueSoon = occDate > today && occDate <= today.plusDays(reminderDaysBefore.toLong())

            if (!isOverdue && !isDueToday && !isDueSoon) {
                continue
            }

            val formattedDueDate = try {
                occDate.format(DISPLAY_DATE_FORMATTER)
            } catch (e: Exception) {
                occ.dueDate
            }

            val dueMessage = when {
                isOverdue -> "Overdue (due $formattedDueDate)"
                isDueToday -> "Due today ($formattedDueDate)"
                else -> "Due on $formattedDueDate"
            }

            result.add(
                ReminderNotification(
                    occurrenceId = occ.id,
                    title = title,
                    dueMessage = dueMessage
                )
            )
        }

        return result
    }
}
