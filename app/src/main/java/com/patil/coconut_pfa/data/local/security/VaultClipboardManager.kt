package com.patil.coconut_pfa.data.local.security

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object VaultClipboardManager {
    private const val CLIP_LABEL = "Coconut Vault"
    private const val EXTRA_IS_SENSITIVE = "android.content.extra.IS_SENSITIVE"

    private val scope = CoroutineScope(Dispatchers.Main)
    private var clearJob: Job? = null
    private var lastCopiedTextHash: Int? = null

    fun copyToClipboard(context: Context, label: String, text: String, clearSeconds: Int = 30) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)

        val extras = clip.description.extras ?: PersistableBundle()
        extras.putBoolean(EXTRA_IS_SENSITIVE, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            extras.putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
        clip.description.extras = extras

        clipboard.setPrimaryClip(clip)
        lastCopiedTextHash = text.hashCode()

        clearJob?.cancel()
        if (clearSeconds > 0) {
            clearJob = scope.launch {
                delay(clearSeconds * 1000L)
                clearIfOurs(context)
            }
        }
    }

    fun clearIfOurs(context: Context) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip()) {
                val clip = clipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val currentText = clip.getItemAt(0).text?.toString()
                    if (currentText != null && currentText.hashCode() == lastCopiedTextHash) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            clipboard.clearPrimaryClip()
                        } else {
                            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            lastCopiedTextHash = null
            clearJob = null
        }
    }
}
