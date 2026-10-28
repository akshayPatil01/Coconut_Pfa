package com.patil.coconut_pfa.domain.sms

import com.patil.coconut_pfa.data.local.dao.AccountDao
import com.patil.coconut_pfa.data.local.dao.CardStatementDao
import com.patil.coconut_pfa.data.local.dao.MerchantMemoryDao
import com.patil.coconut_pfa.data.local.dao.SmsItemDao
import com.patil.coconut_pfa.data.local.dao.TxnDao
import com.patil.coconut_pfa.data.local.entity.CardStatementEntity
import com.patil.coconut_pfa.data.local.entity.DbCardStatementSource
import com.patil.coconut_pfa.data.local.entity.DbSmsSource
import com.patil.coconut_pfa.data.local.entity.DbSmsStatus
import com.patil.coconut_pfa.data.local.entity.SmsItemEntity
import java.security.MessageDigest
import java.util.UUID
import kotlin.math.abs

object SmsProcessingEngine {

    fun hashBody(body: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(body.trim().toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun processAndSaveSms(
        sender: String,
        body: String,
        receivedAt: Long,
        smsItemDao: SmsItemDao,
        accountDao: AccountDao,
        merchantMemoryDao: MerchantMemoryDao,
        txnDao: TxnDao,
        cardStatementDao: CardStatementDao
    ): SmsItemEntity? {
        val bodyHash = hashBody(body)
        val existing = smsItemDao.getSmsByHash(bodyHash)
        if (existing != null) {
            return existing
        }

        val parsed = SmsParser.parse(sender, body, receivedAt) ?: return null

        if (parsed.type == ParsedSmsType.IGNORE) {
            val ignoredEntity = SmsItemEntity(
                sender = sender,
                receivedAt = receivedAt,
                body = body,
                bodyHash = bodyHash,
                parsedType = parsed.type.name,
                status = DbSmsStatus.IGNORED,
                source = DbSmsSource.SMS,
                resolvedAt = System.currentTimeMillis()
            )
            smsItemDao.insert(ignoredEntity)
            return ignoredEntity
        }

        val accounts = accountDao.getAllAccountsList()
        val matchedAccount = accounts.firstOrNull { acc ->
            val matchesInstitution = parsed.institution == null ||
                    acc.institution.name.equals(parsed.institution.replace(" Bank", "").replace(" ", ""), ignoreCase = true) ||
                    acc.name.equals(parsed.institution, ignoreCase = true)
            val matchesLast4 = parsed.last4 == null || acc.last4 == parsed.last4
            matchesInstitution && matchesLast4
        } ?: accounts.firstOrNull { acc ->
            parsed.last4 != null && acc.last4 == parsed.last4
        }

        // Handle CARD_STATEMENT
        if (parsed.type == ParsedSmsType.CARD_STATEMENT) {
            if (matchedAccount != null) {
                val stmtTotal = parsed.statementTotalPaise ?: 0L
                val minDue = parsed.minDuePaise ?: 0L
                val dueDate = parsed.dueDate ?: ""
                val periodEnd = dueDate.ifBlank { System.currentTimeMillis().toString() }

                val statement = CardStatementEntity(
                    id = UUID.randomUUID().toString(),
                    accountId = matchedAccount.id,
                    periodStart = "",
                    periodEnd = periodEnd,
                    amountPaise = stmtTotal,
                    minDuePaise = minDue,
                    dueDate = dueDate,
                    source = DbCardStatementSource.SMS
                )
                cardStatementDao.insert(statement)
            }

            val stmtEntity = SmsItemEntity(
                sender = sender,
                receivedAt = receivedAt,
                body = body,
                bodyHash = bodyHash,
                parsedType = parsed.type.name,
                amountPaise = parsed.statementTotalPaise,
                last4 = parsed.last4,
                refNo = parsed.refNo,
                matchedAccountId = matchedAccount?.id,
                status = DbSmsStatus.CONFIRMED,
                source = DbSmsSource.SMS,
                resolvedAt = System.currentTimeMillis(),
                txnId = null
            )
            smsItemDao.insert(stmtEntity)
            return stmtEntity
        }

        // Suggested Category from MerchantMemory
        var suggestedCategoryId: String? = null
        val merchant = parsed.merchant
        if (!merchant.isNullOrBlank()) {
            val merchantKey = merchant.lowercase().replace(Regex("[0-9]"), "").trim()
            val memory = merchantMemoryDao.getByMerchantKey(merchantKey)
            if (memory != null) {
                suggestedCategoryId = memory.categoryId
            }
        }

        // Duplicate checks
        var status = DbSmsStatus.PENDING
        if (matchedAccount != null && parsed.amountPaise != null) {
            val recentSmsList = smsItemDao.getPendingSmsList()
            val isDuplicate = recentSmsList.any { other ->
                val sameAcc = other.matchedAccountId == matchedAccount.id
                val sameAmount = other.amountPaise == parsed.amountPaise
                val within10Mins = abs(other.receivedAt - receivedAt) <= 10 * 60 * 1000L
                val sameRef = !parsed.refNo.isNullOrBlank() && other.refNo == parsed.refNo
                (sameAcc && sameAmount && within10Mins) || sameRef
            }
            if (isDuplicate) {
                status = DbSmsStatus.DUPLICATE
            }
        }

        val smsEntity = SmsItemEntity(
            sender = sender,
            receivedAt = receivedAt,
            body = body,
            bodyHash = bodyHash,
            parsedType = parsed.type.name,
            amountPaise = parsed.amountPaise,
            last4 = parsed.last4,
            merchant = parsed.merchant,
            availableBalancePaise = parsed.availableBalancePaise,
            refNo = parsed.refNo,
            matchedAccountId = matchedAccount?.id,
            suggestedCategoryId = suggestedCategoryId,
            status = status,
            source = DbSmsSource.SMS
        )

        smsItemDao.insert(smsEntity)
        return smsEntity
    }

    suspend fun purgeOldBodies(smsItemDao: SmsItemDao) {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000L)
        smsItemDao.purgeOldBodies(thirtyDaysAgo)
    }
}
