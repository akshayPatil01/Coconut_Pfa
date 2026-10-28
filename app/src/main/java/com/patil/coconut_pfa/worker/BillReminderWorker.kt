package com.patil.coconut_pfa.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.patil.coconut_pfa.data.local.datastore.UserSettingsDataStore
import com.patil.coconut_pfa.data.local.db.CoconutDatabase
import com.patil.coconut_pfa.domain.BillReminderEvaluator
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import com.patil.coconut_pfa.util.NotificationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class BillReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = CoconutDatabase.getInstance(applicationContext)
            val settingsDataStore = UserSettingsDataStore(applicationContext)
            val userSettings = settingsDataStore.settingsFlow.first()

            val occurrences = database.billOccurrenceDao().getUpcomingOccurrencesFlow().firstOrNull() ?: emptyList()
            val bills = database.billDao().getActiveBillsFlow().firstOrNull() ?: emptyList()
            val statements = database.cardStatementDao().getAllStatementsFlow().firstOrNull() ?: emptyList()
            val accounts = database.accountDao().getActiveAccountsFlow().firstOrNull() ?: emptyList()

            val notifications = BillReminderEvaluator.evaluateReminders(
                occurrences = occurrences,
                bills = bills,
                statements = statements,
                accounts = accounts,
                defaultReminderDays = userSettings.defaultReminderDays,
                today = KolkataDateTimeUtils.currentLocalDate()
            )

            for (notif in notifications) {
                NotificationHelper.postBillReminderNotification(
                    context = applicationContext,
                    occurrenceId = notif.occurrenceId,
                    title = notif.title,
                    dueMessage = notif.dueMessage
                )
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "BillReminderWorker"

        fun schedule(context: Context, reminderHour: Int) {
            val now = LocalDateTime.now(KolkataDateTimeUtils.ZONE_KOLKATA)
            var target = now.withHour(reminderHour.coerceIn(0, 23)).withMinute(0).withSecond(0).withNano(0)
            if (now >= target) {
                target = target.plusDays(1)
            }
            val initialDelayMillis = Duration.between(now, target).toMillis().coerceAtLeast(0L)

            val workRequest = PeriodicWorkRequestBuilder<BillReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                workRequest
            )
        }
    }
}
