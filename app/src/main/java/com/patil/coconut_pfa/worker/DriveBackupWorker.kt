package com.patil.coconut_pfa.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.patil.coconut_pfa.data.local.datastore.UserSettingsDataStore
import com.patil.coconut_pfa.data.local.security.BackupPassphraseManager
import com.patil.coconut_pfa.data.repository.DriveBackupManager
import kotlinx.coroutines.flow.first

class DriveBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val settingsDataStore = UserSettingsDataStore(context)
        val settings = settingsDataStore.settingsFlow.first()

        if (settings.googleAccountEmail.isNullOrBlank()) {
            settingsDataStore.updateLastBackupStatus("Failed: Google account not connected")
            return Result.failure()
        }

        if (!BackupPassphraseManager.hasBackupPassword(context)) {
            settingsDataStore.updateLastBackupStatus("Failed: Backup password not set")
            return Result.failure()
        }

        return try {
            DriveBackupManager.uploadBackupToDrive(context)
            Result.success()
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Unknown error"
            settingsDataStore.updateLastBackupStatus("Failed: $msg")
            if (e is java.io.IOException || e is java.net.UnknownHostException) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
