package com.patil.coconut_pfa.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PasswordHistoryItem(
    val password: String,
    val updatedAt: Long
) {
    override fun toString(): String = "<redacted>"
}

@JsonClass(generateAdapter = true)
data class CustomFieldMeta(
    val label: String,
    val hidden: Boolean
) {
    override fun toString(): String = "<redacted>"
}

@JsonClass(generateAdapter = true)
data class CustomFieldSecret(
    val value: String
) {
    override fun toString(): String = "<redacted>"
}

@JsonClass(generateAdapter = true)
data class LoginMeta(
    val title: String,
    val username: String,
    val website: String,
    val tags: List<String> = emptyList(),
    val customFields: List<CustomFieldMeta> = emptyList()
) {
    override fun toString(): String = "<redacted>"
}

@JsonClass(generateAdapter = true)
data class LoginSecret(
    val password: String,
    val notes: String = "",
    val passwordHistory: List<PasswordHistoryItem> = emptyList(),
    val customFields: List<CustomFieldSecret> = emptyList()
) {
    override fun toString(): String = "<redacted>"
}

@JsonClass(generateAdapter = true)
data class BankLoginMeta(
    val title: String,
    val bank: String,
    val customerId: String,
    val tags: List<String> = emptyList(),
    val customFields: List<CustomFieldMeta> = emptyList()
) {
    override fun toString(): String = "<redacted>"
}

@JsonClass(generateAdapter = true)
data class BankLoginSecret(
    val loginPassword: String,
    val transactionPassword: String = "",
    val notes: String = "",
    val passwordHistory: List<PasswordHistoryItem> = emptyList(),
    val customFields: List<CustomFieldSecret> = emptyList()
) {
    override fun toString(): String = "<redacted>"
}

@JsonClass(generateAdapter = true)
data class NoteMeta(
    val title: String,
    val tags: List<String> = emptyList(),
    val customFields: List<CustomFieldMeta> = emptyList()
) {
    override fun toString(): String = encryptedToString()
}

@JsonClass(generateAdapter = true)
data class NoteSecret(
    val note: String,
    val customFields: List<CustomFieldSecret> = emptyList()
) {
    override fun toString(): String = "<redacted>"
}

@JsonClass(generateAdapter = true)
data class CardMeta(
    val title: String,
    val nickname: String,
    val bank: String,
    val bankCustom: String? = null,
    val cardType: com.patil.coconut_pfa.domain.CardType,
    val network: com.patil.coconut_pfa.domain.CardNetwork,
    val last4: String,
    val expiry: String,
    val cardholderName: String,
    val colour: String,
    val notes: String = ""
) {
    override fun toString(): String = "<redacted>"
}

@JsonClass(generateAdapter = true)
data class CardSecret(
    val number: String,
    val cvv: String
) {
    override fun toString(): String = "<redacted>"
}

private fun encryptedToString(): String = "<redacted>"
