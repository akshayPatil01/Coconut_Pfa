package com.patil.coconut_pfa.worker

import android.content.Context
import android.provider.Telephony
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.patil.coconut_pfa.data.local.datastore.UserSettingsDataStore
import com.patil.coconut_pfa.data.local.db.CoconutDatabase
import com.patil.coconut_pfa.domain.sms.SmsProcessingEngine
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class SmsCatchupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            scanInbox(applicationContext, sinceTimestamp = null)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "SmsCatchupWorker"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<SmsCatchupWorker>(1, TimeUnit.DAYS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        suspend fun scanInbox(context: Context, sinceTimestamp: Long? = null, force30Days: Boolean = false) {
            val appCtx = context.applicationContext
            val dataStore = UserSettingsDataStore(appCtx)
            val settings = dataStore.settingsFlow.first()

            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000L)
            val lastProcessed = if (force30Days) 0L else (sinceTimestamp ?: settings.lastSmsCatchupTimestamp)
            val cutoffTime = if (lastProcessed > 0L) lastProcessed else thirtyDaysAgo

            val db = CoconutDatabase.getInstance(appCtx)
            val smsDao = db.smsItemDao()
            val accountDao = db.accountDao()
            val merchantDao = db.merchantMemoryDao()
            val txnDao = db.txnDao()
            val statementDao = db.cardStatementDao()

            val cursor = appCtx.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.DATE),
                "${Telephony.Sms.Inbox.DATE} > ?",
                arrayOf(cutoffTime.toString()),
                "${Telephony.Sms.Inbox.DATE} ASC"
            )

            cursor?.use {
                val addressIdx = it.getColumnIndex(Telephony.Sms.Inbox.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.Inbox.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.Inbox.DATE)

                while (it.moveToNext()) {
                    val sender = if (addressIdx != -1) it.getString(addressIdx) ?: "UNKNOWN" else "UNKNOWN"
                    val body = if (bodyIdx != -1) it.getString(bodyIdx) ?: continue else continue
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else System.currentTimeMillis()

                    SmsProcessingEngine.processAndSaveSms(
                        sender = sender,
                        body = body,
                        receivedAt = date,
                        smsItemDao = smsDao,
                        accountDao = accountDao,
                        merchantMemoryDao = merchantDao,
                        txnDao = txnDao,
                        cardStatementDao = statementDao
                    )
                }
            }

            dataStore.updateLastSmsCatchupTimestamp(System.currentTimeMillis())
            SmsProcessingEngine.purgeOldBodies(smsDao)
        }
    }
}
