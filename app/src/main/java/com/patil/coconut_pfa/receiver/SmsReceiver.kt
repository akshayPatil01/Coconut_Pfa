package com.patil.coconut_pfa.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.patil.coconut_pfa.data.local.datastore.UserSettingsDataStore
import com.patil.coconut_pfa.data.local.db.CoconutDatabase
import com.patil.coconut_pfa.domain.sms.SmsProcessingEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val pendingResult = goAsync()
        val appCtx = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataStore = UserSettingsDataStore(appCtx)
                val settings = dataStore.settingsFlow.first()
                if (!settings.smsEnabled) {
                    pendingResult.finish()
                    return@launch
                }

                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return@launch
                val db = CoconutDatabase.getInstance(appCtx)
                val smsDao = db.smsItemDao()
                val accountDao = db.accountDao()
                val merchantDao = db.merchantMemoryDao()
                val txnDao = db.txnDao()
                val statementDao = db.cardStatementDao()

                for (msg in messages) {
                    val sender = msg.originatingAddress ?: "UNKNOWN"
                    val body = msg.messageBody ?: continue
                    val timestamp = msg.timestampMillis

                    SmsProcessingEngine.processAndSaveSms(
                        sender = sender,
                        body = body,
                        receivedAt = timestamp,
                        smsItemDao = smsDao,
                        accountDao = accountDao,
                        merchantMemoryDao = merchantDao,
                        txnDao = txnDao,
                        cardStatementDao = statementDao
                    )
                }

                // Purge old bodies > 30 days resolved
                SmsProcessingEngine.purgeOldBodies(smsDao)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
