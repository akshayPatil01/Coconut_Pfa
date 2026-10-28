package com.patil.coconut_pfa.domain.sms

object SmsParserUtils {

    fun parseAmountToPaise(amountStr: String): Long? {
        val clean = amountStr.replace(",", "").trim()
        val parts = clean.split(".")
        val rupees = parts.getOrNull(0)?.toLongOrNull() ?: return null
        val paise = when (parts.size) {
            1 -> 0L
            2 -> {
                val p = parts[1]
                when (p.length) {
                    0 -> 0L
                    1 -> (p.toLongOrNull() ?: 0L) * 10L
                    2 -> p.take(2).toLongOrNull() ?: 0L
                    else -> p.take(2).toLongOrNull() ?: 0L
                }
            }
            else -> return null
        }
        return (rupees * 100L) + paise
    }

    fun detectBankInstitution(sender: String, body: String): String? {
        val upperSender = sender.uppercase().trim()
        val upperBody = body.uppercase()

        val senderHeader = if (upperSender.contains("-")) {
            upperSender.substringAfterLast("-")
        } else {
            upperSender
        }

        return when {
            senderHeader.contains("HDFC") -> "HDFC Bank"
            senderHeader.contains("ICICI") -> "ICICI Bank"
            senderHeader.contains("SBI") -> "SBI"
            senderHeader.contains("AXIS") -> "Axis Bank"
            senderHeader.contains("KOTAK") -> "Kotak Mahindra Bank"
            senderHeader.contains("PNB") -> "Punjab National Bank"
            senderHeader.contains("BOB") -> "Bank of Baroda"
            senderHeader.contains("IDFC") -> "IDFC FIRST Bank"
            senderHeader.contains("YESBN") || senderHeader.contains("YESBK") -> "Yes Bank"
            senderHeader.contains("INDUS") -> "IndusInd Bank"
            senderHeader.contains("CANBK") || senderHeader.contains("CANBNK") -> "Canara Bank"
            senderHeader.contains("UNIONB") -> "Union Bank of India"
            senderHeader.contains("BOI") -> "Bank of India"
            senderHeader.contains("FEDBK") || senderHeader.contains("FEDERAL") -> "Federal Bank"
            senderHeader.contains("RBL") -> "RBL Bank"
            senderHeader.contains("AUBNK") -> "AU Small Finance Bank"
            senderHeader.contains("CITI") -> "Citi Bank"
            senderHeader.contains("SCB") -> "Standard Chartered Bank"
            senderHeader.contains("DBS") -> "DBS Bank"
            senderHeader.contains("HSBC") -> "HSBC Bank"

            // Fallback: Check if sender matches standard 6-char header and body explicitly names a bank
            isStandardBankSenderFormat(upperSender) -> {
                when {
                    upperBody.contains("HDFC BANK") -> "HDFC Bank"
                    upperBody.contains("ICICI BANK") -> "ICICI Bank"
                    upperBody.contains("STATE BANK OF INDIA") || upperBody.contains("SBI") -> "SBI"
                    upperBody.contains("AXIS BANK") -> "Axis Bank"
                    upperBody.contains("KOTAK BANK") || upperBody.contains("KOTAK MAHINDRA") -> "Kotak Mahindra Bank"
                    upperBody.contains("BANK OF BARODA") -> "Bank of Baroda"
                    upperBody.contains("PUNJAB NATIONAL BANK") || upperBody.contains("PNB") -> "Punjab National Bank"
                    upperBody.contains("IDFC FIRST BANK") || upperBody.contains("IDFC BANK") -> "IDFC FIRST Bank"
                    upperBody.contains("YES BANK") -> "Yes Bank"
                    upperBody.contains("INDUSIND BANK") -> "IndusInd Bank"
                    upperBody.contains("CANARA BANK") -> "Canara Bank"
                    upperBody.contains("UNION BANK") -> "Union Bank of India"
                    else -> null
                }
            }

            else -> null
        }
    }

    private fun isStandardBankSenderFormat(sender: String): Boolean {
        // e.g. "AX-BANKTX", "VM-INFOSM", "123456"
        val clean = sender.removePrefix("").trim()
        val regex = Regex("""^([A-Z]{2}-[A-Z0-9]{6}|[A-Z0-9]{6})$""")
        return regex.matches(clean)
    }

    fun isIgnoredSms(body: String): Boolean {
        val lower = body.lowercase()

        // 1. OTP / Verification Code
        val otpRegex = Regex("""\b(otp|one time password|verification code|security code|secret code|login code)\b""")
        val otpCodeRegex = Regex("""\bcode\s+\d{4,8}\b""")
        if (otpRegex.containsMatchIn(lower) || otpCodeRegex.containsMatchIn(lower)) {
            return true
        }

        // 2. Promotional / Offers / Marketing
        val promoRegex = Regex("""\b(pre-approved|apply now|eligible for|instant loan|personal loan offer|congratulations|upgrade your card|0% emi|reward points expire|cashback offer|special offer|discount coupon)\b""")

        // 3. Failed / Declined transactions
        val failedRegex = Regex("""\b(declined|failed|unsuccessful|insufficient balance|insufficient funds|could not be processed|reversed due to technical|unable to process|transaction failed)\b""")

        return promoRegex.containsMatchIn(lower) || failedRegex.containsMatchIn(lower)
    }

    fun extractAmount(body: String): Long? {
        val regex = Regex("""(?i)(?:Rs\.?|INR|₹)\s*([0-9]{1,3}(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""")
        val match = regex.find(body) ?: return null
        val amountStr = match.groupValues[1]
        return parseAmountToPaise(amountStr)
    }

    fun extractLast4(body: String): String? {
        // Pattern 1: A/C XX1234, Card ending 1234, A/c XXXXX123456, Card XX1234, **1234
        val patterns = listOf(
            Regex("""(?i)(?:A/C|ac|account|card|card ending|ending in|ending|XX|\*\*|X+)\s*[:.\-]?\s*([0-9]{4})\b"""),
            Regex("""[X*]{2,12}([0-9]{4})\b"""),
            Regex("""\bending\s+([0-9]{4})\b""", RegexOption.IGNORE_CASE),
        )

        for (pattern in patterns) {
            val match = pattern.find(body)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }

    fun extractAvailableBalance(body: String): Long? {
        val regex = Regex("""(?i)(?:Avl Bal|Available Balance|Avail Bal|A/c Bal|Bal:?|Available Limit|Avl Lim|Clear Bal)\s*[:.\-]?\s*(?:Rs\.?|INR|₹)?\s*([0-9]{1,3}(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""")
        val match = regex.find(body) ?: return null
        val amountStr = match.groupValues[1]
        return parseAmountToPaise(amountStr)
    }

    fun extractRefNo(body: String): String? {
        val patterns = listOf(
            Regex("""(?i)(?:Ref|Ref No|Reference|Txn ID|UPI Ref|UPI/)\s*[:.\-]?\s*([A-Za-z0-9]+)"""),
            Regex("""UPI/([0-9]{8,12})""")
        )

        for (pattern in patterns) {
            val match = pattern.find(body)
            if (match != null) {
                val ref = match.groupValues[1].trim()
                if (ref.length >= 4) {
                    return ref
                }
            }
        }
        return null
    }

    fun extractMerchant(body: String): String? {
        val patterns = listOf(
            Regex("""(?i)\btransfer to\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|for|ref|avail|avl|bal|a/c|using|via|vpa|\.|$))"""),
            Regex("""(?i)\bat\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|for|ref|avail|avl|bal|using|via|vpa|\.|$))"""),
            Regex("""(?i)\bto\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|for|ref|avail|avl|bal|using|via|vpa|\.|$))"""),
            Regex("""(?i)\bInfo:\s*([A-Za-z0-9\s.&'/_-]+?)(?=\s+(?:on|avail|avl|bal|ref|\.|$))"""),
            Regex("""(?i)\bby\s+([A-Za-z0-9\s.&'/_-]+?)(?=\s+(?:on|avail|avl|bal|ref|\.|$))"""),
            Regex("""(?i)\bfor\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|ref|avail|avl|bal|\.|$))"""),
            Regex("""(?i)\btowards\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|ref|avail|avl|bal|\.|$))""")
        )

        for (pattern in patterns) {
            val match = pattern.find(body)
            if (match != null) {
                val raw = match.groupValues[1].trim()
                val clean = cleanMerchantString(raw)
                if (clean.isNotBlank() && clean.length > 1) {
                    return clean
                }
            }
        }
        return null
    }

    private fun cleanMerchantString(raw: String): String {
        var clean = raw.trim()
            .trimEnd('.', ',', '-', ':', ';')
            .trim()
        // Remove trailing date-like strings if appended (e.g. "Swiggy on 12-Nov")
        clean = clean.replace(Regex("""(?i)\s+on\s+\d{1,2}[-/\s][A-Za-z0-9]+.*$"""), "")
        return clean.trim()
    }

    fun extractStatementTotal(body: String): Long? {
        val regex = Regex("""(?i)(?:Total Amt Due|Total Amount Due|Total Due|Total Due:?)\s*[:.\-]?\s*(?:Rs\.?|INR|₹)?\s*([0-9]{1,3}(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""")
        val match = regex.find(body) ?: return null
        return parseAmountToPaise(match.groupValues[1])
    }

    fun extractMinDue(body: String): Long? {
        val regex = Regex("""(?i)(?:Min Amt Due|Min Amount Due|Min Due|Min Due:?)\s*[:.\-]?\s*(?:Rs\.?|INR|₹)?\s*([0-9]{1,3}(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""")
        val match = regex.find(body) ?: return null
        return parseAmountToPaise(match.groupValues[1])
    }

    fun extractDueDate(body: String): String? {
        val regex = Regex("""(?i)(?:Due Date|Payment Due Date)\s*[:.\-]?\s*([0-9]{1,2}[-/\s][A-Za-z0-9]+[-/\s][0-9]{2,4})""")
        val match = regex.find(body) ?: return null
        return match.groupValues[1].trim()
    }
}
