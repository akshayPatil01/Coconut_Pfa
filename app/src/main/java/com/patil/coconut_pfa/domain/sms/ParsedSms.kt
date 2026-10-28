package com.patil.coconut_pfa.domain.sms

enum class ParsedSmsType {
    DEBIT,
    CREDIT,
    CARD_SPEND,
    CARD_STATEMENT,
    IGNORE
}

data class ParsedSms(
    val type: ParsedSmsType,
    val amountPaise: Long? = null,
    val last4: String? = null,
    val merchant: String? = null,
    val availableBalancePaise: Long? = null,
    val refNo: String? = null,
    val statementTotalPaise: Long? = null,
    val minDuePaise: Long? = null,
    val dueDate: String? = null,
    val institution: String? = null,
)
