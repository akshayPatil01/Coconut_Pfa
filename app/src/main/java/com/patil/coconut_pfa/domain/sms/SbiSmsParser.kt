package com.patil.coconut_pfa.domain.sms

object SbiSmsParser {
    fun parse(body: String, institution: String): ParsedSms? {
        val lower = body.lowercase()

        // 1. Card Statement
        if (lower.contains("sbi card") && (lower.contains("statement") || lower.contains("total amt due"))) {
            val totalDue = SmsParserUtils.extractStatementTotal(body)
            val minDue = SmsParserUtils.extractMinDue(body)
            val dueDate = SmsParserUtils.extractDueDate(body)
            val last4 = SmsParserUtils.extractLast4(body)
            return ParsedSms(
                type = ParsedSmsType.CARD_STATEMENT,
                statementTotalPaise = totalDue,
                minDuePaise = minDue,
                dueDate = dueDate,
                last4 = last4,
                institution = institution,
            )
        }

        // 2. Card Spend
        if (lower.contains("spent on sbi credit card") || lower.contains("spent on sbi card") || (lower.contains("sbi card") && lower.contains("spent"))) {
            val amount = SmsParserUtils.extractAmount(body)
            val last4 = SmsParserUtils.extractLast4(body)
            val merchant = SmsParserUtils.extractMerchant(body)
            return ParsedSms(
                type = ParsedSmsType.CARD_SPEND,
                amountPaise = amount,
                last4 = last4,
                merchant = merchant,
                institution = institution
            )
        }

        // 3. Debit
        if (lower.contains("debited") || lower.contains("debit") || lower.contains("paid") || lower.contains("sent")) {
            val amount = SmsParserUtils.extractAmount(body)
            val last4 = SmsParserUtils.extractLast4(body)
            val merchant = SmsParserUtils.extractMerchant(body)
            val bal = SmsParserUtils.extractAvailableBalance(body)
            val refNo = SmsParserUtils.extractRefNo(body)
            return ParsedSms(
                type = ParsedSmsType.DEBIT,
                amountPaise = amount,
                last4 = last4,
                merchant = merchant,
                availableBalancePaise = bal,
                refNo = refNo,
                institution = institution
            )
        }

        // 4. Credit
        if (lower.contains("credited") || lower.contains("credit") || lower.contains("received") || lower.contains("deposited")) {
            val amount = SmsParserUtils.extractAmount(body)
            val last4 = SmsParserUtils.extractLast4(body)
            val merchant = SmsParserUtils.extractMerchant(body)
            val bal = SmsParserUtils.extractAvailableBalance(body)
            val refNo = SmsParserUtils.extractRefNo(body)
            return ParsedSms(
                type = ParsedSmsType.CREDIT,
                amountPaise = amount,
                last4 = last4,
                merchant = merchant,
                availableBalancePaise = bal,
                refNo = refNo,
                institution = institution
            )
        }

        return null
    }
}
