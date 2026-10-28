package com.patil.coconut_pfa.domain.sms

object AxisSmsParser {
    fun parse(body: String, institution: String): ParsedSms? {
        val lower = body.lowercase()

        // 1. Card Statement
        if (lower.contains("axis bank credit card") && (lower.contains("statement") || lower.contains("total due"))) {
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
        if (lower.contains("spent on axis bank card") || lower.contains("spent on axis") || (lower.contains("axis") && lower.contains("spent"))) {
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
        if (lower.contains("debited from") || lower.contains("debited")) {
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
        if (lower.contains("credited to") || lower.contains("credited")) {
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
