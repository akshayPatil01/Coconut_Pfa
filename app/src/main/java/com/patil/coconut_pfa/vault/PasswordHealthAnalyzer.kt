package com.patil.coconut_pfa.vault

import com.patil.coconut_pfa.data.local.entity.VaultItem
import com.patil.coconut_pfa.data.local.entity.VaultItemType
import com.patil.coconut_pfa.data.model.BankLoginSecret
import com.patil.coconut_pfa.data.model.LoginSecret
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlin.math.log2

data class PasswordHealthIssue(
    val item: VaultItem,
    val title: String,
    val issueType: HealthIssueType,
    val description: String
) {
    override fun toString(): String = "<redacted>"
}

enum class HealthIssueType {
    WEAK, REUSED, OLD
}

data class PasswordHealthReport(
    val weakItems: List<PasswordHealthIssue> = emptyList(),
    val reusedItems: List<PasswordHealthIssue> = emptyList(),
    val oldItems: List<PasswordHealthIssue> = emptyList()
) {
    val totalIssues: Int get() = weakItems.size + reusedItems.size + oldItems.size
    override fun toString(): String = "<redacted>"
}

object PasswordHealthAnalyzer {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val loginSecretAdapter = moshi.adapter(LoginSecret::class.java)
    private val bankLoginSecretAdapter = moshi.adapter(BankLoginSecret::class.java)
    private val loginMetaAdapter = moshi.adapter(com.patil.coconut_pfa.data.model.LoginMeta::class.java)
    private val bankLoginMetaAdapter = moshi.adapter(com.patil.coconut_pfa.data.model.BankLoginMeta::class.java)

    private const val TWELVE_MONTHS_MS = 365L * 24L * 60L * 60L * 1000L

    fun analyze(
        items: List<VaultItem>,
        secretReader: (VaultItem) -> String,
        metaReader: (VaultItem) -> String
    ): PasswordHealthReport {
        val weakList = mutableListOf<PasswordHealthIssue>()
        val reusedList = mutableListOf<PasswordHealthIssue>()
        val oldList = mutableListOf<PasswordHealthIssue>()

        val passwordToItems = mutableMapOf<String, MutableList<Pair<VaultItem, String>>>()
        val now = System.currentTimeMillis()

        for (item in items) {
            if (item.deletedAt != null) continue
            if (item.type != VaultItemType.LOGIN && item.type != VaultItemType.BANK_LOGIN) continue

            try {
                val secretJson = secretReader(item)
                val metaJson = metaReader(item)

                val title = when (item.type) {
                    VaultItemType.LOGIN -> loginMetaAdapter.fromJson(metaJson)?.title ?: "Untitled Login"
                    VaultItemType.BANK_LOGIN -> bankLoginMetaAdapter.fromJson(metaJson)?.title ?: "Untitled Bank"
                    else -> "Untitled"
                }

                val password = when (item.type) {
                    VaultItemType.LOGIN -> loginSecretAdapter.fromJson(secretJson)?.password
                    VaultItemType.BANK_LOGIN -> bankLoginSecretAdapter.fromJson(secretJson)?.loginPassword
                    else -> null
                } ?: continue

                if (password.isBlank()) continue

                val entropy = estimateEntropy(password)
                if (password.length < 12 || entropy < 60.0) {
                    weakList.add(
                        PasswordHealthIssue(
                            item = item,
                            title = title,
                            issueType = HealthIssueType.WEAK,
                            description = "Weak (${password.length} chars, ${entropy.toInt()} bits)"
                        )
                    )
                }

                val lastChanged = item.updatedAt
                if (now - lastChanged > TWELVE_MONTHS_MS) {
                    oldList.add(
                        PasswordHealthIssue(
                            item = item,
                            title = title,
                            issueType = HealthIssueType.OLD,
                            description = "Unchanged for 12+ months"
                        )
                    )
                }

                passwordToItems.getOrPut(password) { mutableListOf() }.add(Pair(item, title))

            } catch (ex: Exception) {
                // Ignore unreadable items
            }
        }

        for ((_, itemList) in passwordToItems) {
            if (itemList.size > 1) {
                for ((item, title) in itemList) {
                    reusedList.add(
                        PasswordHealthIssue(
                            item = item,
                            title = title,
                            issueType = HealthIssueType.REUSED,
                            description = "Reused across ${itemList.size} items"
                        )
                    )
                }
            }
        }

        return PasswordHealthReport(
            weakItems = weakList,
            reusedItems = reusedList,
            oldItems = oldList
        )
    }

    private fun estimateEntropy(password: String): Double {
        var hasUpper = false
        var hasLower = false
        var hasDigit = false
        var hasSymbol = false
        for (c in password) {
            when {
                c.isUpperCase() -> hasUpper = true
                c.isLowerCase() -> hasLower = true
                c.isDigit() -> hasDigit = true
                else -> hasSymbol = true
            }
        }
        var poolSize = 0
        if (hasUpper) poolSize += 26
        if (hasLower) poolSize += 26
        if (hasDigit) poolSize += 10
        if (hasSymbol) poolSize += 32
        if (poolSize == 0) poolSize = 26
        return password.length * log2(poolSize.toDouble())
    }
}
