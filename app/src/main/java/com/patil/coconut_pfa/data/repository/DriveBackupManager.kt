package com.patil.coconut_pfa.data.repository

import android.accounts.Account
import android.content.Context
import android.net.Uri
import com.google.android.gms.auth.GoogleAuthUtil
import com.patil.coconut_pfa.data.local.datastore.UserSettingsDataStore
import com.patil.coconut_pfa.data.local.security.BackupPassphraseManager
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class DriveBackupSummary(
    val id: String,
    val name: String,
    val size: Long,
    val createdTime: String,
    val isPinned: Boolean
)

object DriveBackupManager {
    private val client by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val api: DriveApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(DriveApiService::class.java)
    }

    suspend fun getAccessToken(context: Context, email: String): String {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val account = Account(email, "com.google")
                GoogleAuthUtil.getToken(context, account, "oauth2:https://www.googleapis.com/auth/drive.appdata")
            } catch (e: Exception) {
                throw RuntimeException("Google authentication expired or failed: ${e.localizedMessage}")
            }
        }
    }

    suspend fun uploadPinnedPreVaultBackupToDrive(context: Context, preVaultBytes: ByteArray): String {
        val settingsDataStore = UserSettingsDataStore(context)
        val settings = settingsDataStore.settingsFlow.first()
        val email = settings.googleAccountEmail ?: throw IllegalStateException("Google account not connected")

        val token = getAccessToken(context, email)
        val authHeader = "Bearer $token"

        val timestampStr = android.text.format.DateFormat.format("yyyyMMdd-HHmm", java.util.Date()).toString()
        val fileName = "coconut-$timestampStr.cbk"

        val jsonMetadata = "{\"name\":\"$fileName\",\"parents\":[\"appDataFolder\"],\"appProperties\":{\"pinned\":\"true\",\"kind\":\"pre-vault\"}}"
        val metadataPart = MultipartBody.Part.createFormData(
            "metadata", null, jsonMetadata.toRequestBody("application/json; charset=UTF-8".toMediaTypeOrNull())
        )
        val requestFile = preVaultBytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
        val mediaPart = MultipartBody.Part.createFormData("file", fileName, requestFile)

        val uploadResponse = api.uploadFile(authHeader, metadataPart, mediaPart)
        if (!uploadResponse.isSuccessful) {
            val errorBody = uploadResponse.errorBody()?.string() ?: "Unknown error"
            throw RuntimeException("Pinned pre-vault upload failed (${uploadResponse.code()}): $errorBody")
        }
        return fileName
    }

    suspend fun uploadBackupToDrive(context: Context): String {
        val settingsDataStore = UserSettingsDataStore(context)
        val settings = settingsDataStore.settingsFlow.first()
        val email = settings.googleAccountEmail ?: throw IllegalStateException("Google account not connected")

        if (!BackupPassphraseManager.hasBackupPassword(context)) {
            val err = "Backup password not set. Please set a backup password first."
            settingsDataStore.updateLastBackupStatus("Failed: $err")
            throw IllegalStateException(err)
        }

        val tempFile = File.createTempFile("coconut-backup", ".cbk", context.cacheDir)
        try {
            val uri = Uri.fromFile(tempFile)
            BackupManager.exportBackup(context, uri)

            val token = getAccessToken(context, email)
            val authHeader = "Bearer $token"

            val timestampStr = android.text.format.DateFormat.format("yyyyMMdd-HHmm", java.util.Date()).toString()
            val fileName = "coconut-$timestampStr.cbk"

            val jsonMetadata = "{\"name\":\"$fileName\",\"parents\":[\"appDataFolder\"]}"
            val metadataPart = MultipartBody.Part.createFormData(
                "metadata", null, jsonMetadata.toRequestBody("application/json; charset=UTF-8".toMediaTypeOrNull())
            )
            val requestFile = tempFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val mediaPart = MultipartBody.Part.createFormData("file", fileName, requestFile)

            val uploadResponse = api.uploadFile(authHeader, metadataPart, mediaPart)
            if (!uploadResponse.isSuccessful) {
                val errorBody = uploadResponse.errorBody()?.string() ?: "Unknown error"
                throw RuntimeException("Upload failed (${uploadResponse.code()}) : $errorBody")
            }

            pruneOldBackups(authHeader)

            settingsDataStore.updateLastBackupStatus("Success")
            return fileName
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Unknown error"
            settingsDataStore.updateLastBackupStatus("Failed: $msg")
            throw e
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    private suspend fun pruneOldBackups(authHeader: String) {
        try {
            val response = api.listFiles(authHeader)
            if (response.isSuccessful) {
                val files = response.body()?.files.orEmpty()
                val nonPinnedFiles = files.filter { it.appProperties?.get("pinned") != "true" }
                if (nonPinnedFiles.size > 7) {
                    val filesToDelete = nonPinnedFiles.drop(7)
                    for (file in filesToDelete) {
                        api.deleteFile(authHeader, file.id)
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    suspend fun listDriveBackups(context: Context): List<DriveBackupSummary> {
        val settingsDataStore = UserSettingsDataStore(context)
        val settings = settingsDataStore.settingsFlow.first()
        val email = settings.googleAccountEmail ?: throw IllegalStateException("Google account not connected")

        val token = getAccessToken(context, email)
        val authHeader = "Bearer $token"

        val response = api.listFiles(authHeader)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            throw RuntimeException("Failed to list backups (${response.code()}): $errorBody")
        }

        val files = response.body()?.files.orEmpty()
        return files.map { file ->
            DriveBackupSummary(
                id = file.id,
                name = file.name,
                size = file.size?.toLongOrNull() ?: 0L,
                createdTime = file.createdTime ?: "",
                isPinned = file.appProperties?.get("pinned") == "true"
            )
        }
    }

    suspend fun deleteDriveFile(context: Context, fileId: String) {
        val settingsDataStore = UserSettingsDataStore(context)
        val settings = settingsDataStore.settingsFlow.first()
        val email = settings.googleAccountEmail ?: throw IllegalStateException("Google account not connected")
        val token = getAccessToken(context, email)
        val authHeader = "Bearer $token"
        val response = api.deleteFile(authHeader, fileId)
        if (!response.isSuccessful) {
            throw RuntimeException("Failed to delete backup file (${response.code()})")
        }
    }

    suspend fun downloadAndRestoreBackup(context: Context, fileId: String, password: String) {
        val settingsDataStore = UserSettingsDataStore(context)
        val settings = settingsDataStore.settingsFlow.first()
        val email = settings.googleAccountEmail ?: throw IllegalStateException("Google account not connected")

        val token = getAccessToken(context, email)
        val authHeader = "Bearer $token"

        val response = api.downloadFile(authHeader, fileId)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            throw RuntimeException("Download failed (${response.code()}): $errorBody")
        }

        val body = response.body() ?: throw IllegalStateException("Empty response body from Drive download")
        val tempFile = File.createTempFile("coconut-restore", ".cbk", context.cacheDir)
        try {
            FileOutputStream(tempFile).use { output ->
                body.byteStream().use { input ->
                    input.copyTo(output)
                }
            }

            val uri = Uri.fromFile(tempFile)
            BackupManager.restoreBackup(context, uri, password)
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Unknown error"
            if (msg.contains("mac", true) || msg.contains("cipher", true) || msg.contains("decrypt", true) || msg.contains("bad padding", true) || msg.contains("pad", true) || msg.contains("tag mismatch", true)) {
                throw IllegalArgumentException("Incorrect backup password.")
            }
            throw e
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }
}
