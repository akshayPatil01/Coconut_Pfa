package com.patil.coconut_pfa.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.patil.coconut_pfa.data.local.db.CoconutDatabase
import com.patil.coconut_pfa.data.repository.TransactionRepository
import java.util.concurrent.TimeUnit

class PurgeTrashWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = CoconutDatabase.getInstance(applicationContext)
            val repository = TransactionRepository(database.txnDao())
            val cutoffMillis = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000L)
            repository.purgeOldTrash(cutoffMillis)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "PurgeTrashWorker"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<PurgeTrashWorker>(1, TimeUnit.DAYS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
