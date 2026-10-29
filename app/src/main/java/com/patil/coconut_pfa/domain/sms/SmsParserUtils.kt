package com.patil.coconut_pfa.domain.sms

object SmsParserUtils {

    fun parseAmountToPaise(amountStr: String): Long? {
        val clean = amountStr.replace(",", "").trim()
        val d = clean.toDoubleOrNull() ?: return null
        if (d < 0) return null
        return kotlin.math.round(d * 100.0).toLong()
    }

    fun detectBankInstitution(sender: String, body: String): String? {
        val upperSender = sender.uppercase().trim()
        val upperBody = body.uppercase()

        // Direct header keyword matching (handles TRAI suffixes like AX-HDFCBK-S, AD-ICICIB-A, JM-SBIINB-T)
        val institutionFromSender = when {
            upperSender.contains("HDFC") -> "HDFC Bank"
            upperSender.contains("ICICI") -> "ICICI Bank"
            upperSender.contains("SBI") -> "SBI"
            upperSender.contains("AXIS") -> "Axis Bank"
            upperSender.contains("KOTAK") -> "Kotak Mahindra Bank"
            upperSender.contains("PNB") -> "Punjab National Bank"
            upperSender.contains("BOB") -> "Bank of Baroda"
            upperSender.contains("IDFC") -> "IDFC FIRST Bank"
            upperSender.contains("YESBN") || upperSender.contains("YESBK") -> "Yes Bank"
            upperSender.contains("INDUS") -> "IndusInd Bank"
            upperSender.contains("CANBK") || upperSender.contains("CANBNK") -> "Canara Bank"
            upperSender.contains("UNIONB") || upperSender.contains("UBIOIN") -> "Union Bank of India"
            upperSender.contains("BOI") -> "Bank of India"
            upperSender.contains("FEDBK") || upperSender.contains("FEDERAL") -> "Federal Bank"
            upperSender.contains("RBL") -> "RBL Bank"
            upperSender.contains("AUBNK") || upperSender.contains("AUBANK") -> "AU Small Finance Bank"
            upperSender.contains("CITI") -> "Citi Bank"
            upperSender.contains("SCB") -> "Standard Chartered Bank"
            upperSender.contains("DBS") -> "DBS Bank"
            upperSender.contains("HSBC") -> "HSBC Bank"
            upperSender.contains("PAYTM") -> "Paytm"
            upperSender.contains("IPPB") -> "India Post Payments Bank"
            upperSender.contains("BNDHAN") || upperSender.contains("BANDHN") -> "Bandhan Bank"
            upperSender.contains("CENTRAL") || upperSender.contains("CBIN") -> "Central Bank of India"
            upperSender.contains("INDBNK") || upperSender.contains("INDIANB") -> "Indian Bank"
            upperSender.contains("UCOBNK") -> "UCO Bank"
            upperSender.contains("IOB") -> "Indian Overseas Bank"
            upperSender.contains("AMEX") -> "American Express"
            upperSender.contains("ONECRD") -> "OneCard"
            upperSender.contains("SLICE") -> "Slice"
            upperSender.contains("JUPITR") -> "Jupiter"
            upperSender.contains("FIMONY") || upperSender.contains("FIBANK") -> "Fi Money"
            upperSender.contains("CRED") -> "CRED"
            upperSender.contains("PHNPE") || upperSender.contains("PHONEPE") -> "PhonePe"
            upperSender.contains("GPAY") -> "Google Pay"
            else -> null
        }

        if (institutionFromSender != null) {
            return institutionFromSender
        }

        // Fallback 1: Check if body explicitly names a bank or financial entity
        val institutionFromBody = when {
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
            upperBody.contains("BANK OF INDIA") -> "Bank of India"
            upperBody.contains("PAYTM") -> "Paytm"
            upperBody.contains("CRED") -> "CRED"
            upperBody.contains("PHONEPE") -> "PhonePe"
            else -> null
        }

        if (institutionFromBody != null) {
            return institutionFromBody
        }

        // Fallback 2: Check if message body contains clear financial transaction indicators
        val hasTransactionVerbs = upperBody.contains("DEBIT") || upperBody.contains("CREDIT") ||
                upperBody.contains("SPENT") || upperBody.contains("PAID") || upperBody.contains("SENT TO") ||
                upperBody.contains("RECEIVED") || upperBody.contains("WITHDRAWN") || upperBody.contains("TRANSFERRED") ||
                upperBody.contains("PURCHASE OF") || upperBody.contains("TXN OF") || upperBody.contains("SWIPED")
        val hasAmount = upperBody.contains("RS") || upperBody.contains("INR") || upperBody.contains("₹")

        if (hasTransactionVerbs && hasAmount) {
            return "Bank"
        }

        return null
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

    private const val AMOUNT_PATTERN = """((?:[0-9]{1,3}(?:,[0-9]{2,3})+|[0-9]+)(?:\.[0-9]+)?)"""

    fun extractAmount(body: String): Long? {
        // 1. Leading currency: Rs. 500, INR 1,200.50, ₹ 500
        val leadingRegex = Regex("""(?i)(?:Rs\.?|INR|₹)\s*$AMOUNT_PATTERN""")
        val matchLeading = leadingRegex.find(body)
        if (matchLeading != null) {
            return parseAmountToPaise(matchLeading.groupValues[1])
        }

        // 2. Trailing currency: 500 Rs, 1200.00 INR, 500/-
        val trailingRegex = Regex("""(?i)\b$AMOUNT_PATTERN\s*(?:Rs\.?|INR|₹|/-)""")
        val matchTrailing = trailingRegex.find(body)
        if (matchTrailing != null) {
            return parseAmountToPaise(matchTrailing.groupValues[1])
        }

        return null
    }

    fun extractLast4(body: String): String? {
        // Pattern 1: A/C XX1234, Card ending 1234, A/c XXXXX123456, Card XX1234, **1234
        val patterns = listOf(
            Regex("""(?i)(?:A/C|ac|account|card|card ending|ending in|ending|XX|\*\*|X+)\s*[:.\-]?\s*([0-9]{4})\b"""),
            Regex("""[X*]{2,12}([0-9]{4})\b"""),
            Regex("""\bending\s+(?:in\s+|with\s+)?([0-9]{4})\b""", RegexOption.IGNORE_CASE),
            Regex("""\bno\.?\s*[X*]*([0-9]{4})\b""", RegexOption.IGNORE_CASE)
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
        val regex = Regex("""(?i)(?:Avl Bal|Available Balance|Avail Bal|A/c Bal|Bal:?|Available Limit|Avl Lim|Clear Bal)\s*[:.\-]?\s*(?:Rs\.?|INR|₹)?\s*$AMOUNT_PATTERN""")
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
            Regex("""(?i)\bspent\s+(?:at|on)\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|for|ref|avail|avl|bal|using|via|vpa|\.|$))"""),
            Regex("""(?i)\bused\s+(?:at|on)\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|for|ref|avail|avl|bal|using|via|vpa|\.|$))"""),
            Regex("""(?i)\bswiped\s+(?:at|on)\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|for|ref|avail|avl|bal|using|via|vpa|\.|$))"""),
            Regex("""(?i)\bpaid to\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|for|ref|avail|avl|bal|using|via|vpa|\.|$))"""),
            Regex("""(?i)\bsent to\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|for|ref|avail|avl|bal|using|via|vpa|\.|$))"""),
            Regex("""(?i)\btransfer to\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|for|ref|avail|avl|bal|a/c|using|via|vpa|\.|$))"""),
            Regex("""(?i)\bat\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|for|ref|avail|avl|bal|using|via|vpa|\.|$))"""),
            Regex("""(?i)\bto\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|for|ref|avail|avl|bal|using|via|vpa|\.|$))"""),
            Regex("""(?i)\bInfo:\s*([A-Za-z0-9\s.&'/_-]+?)(?=\s+(?:on|avail|avl|bal|ref|\.|$))"""),
            Regex("""(?i)\bby\s+([A-Za-z0-9\s.&'/_-]+?)(?=\s+(?:on|avail|avl|bal|ref|\.|$))"""),
            Regex("""(?i)\bfor\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|ref|avail|avl|bal|\.|$))"""),
            Regex("""(?i)\btowards\s+([A-Za-z0-9\s.&'-]+?)(?=\s+(?:on|ref|avail|avl|bal|\.|$))"""),
            Regex("""(?i)\bVPA\s+([A-Za-z0-9.&_-]+)""")
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
        // Remove trailing payment modes or noise words (e.g. "Swiggy via UPI", "Zomato using card")
        clean = clean.replace(Regex("""(?i)\s+(?:via|using|through)\s+(?:UPI|Card|Netbanking|A/C|Debit Card|Credit Card).*$"""), "")
        // If raw is VPA like "swiggy@icici" or "john@okaxis", extract name part before @
        if (clean.contains("@") && !clean.contains(" ")) {
            val namePart = clean.substringBefore("@").replace(".", " ").trim()
            if (namePart.isNotBlank()) {
                clean = namePart
            }
        }
        // Remove trailing legal/corporate noise suffixes like "Pvt Ltd", "Ltd", "Inc"
        clean = clean.replace(Regex("""(?i)\s+(?:Pvt\.?\s*Ltd\.?|Private\s+Limited|Ltd\.?|Inc\.?|Corp\.?|India)$"""), "")
        return clean.trim()
    }

    fun extractStatementTotal(body: String): Long? {
        val regex = Regex("""(?i)(?:Total Amt Due|Total Amount Due|Total Due|Total Due:?)\s*[:.\-]?\s*(?:Rs\.?|INR|₹)?\s*$AMOUNT_PATTERN""")
        val match = regex.find(body) ?: return null
        return parseAmountToPaise(match.groupValues[1])
    }

    fun extractMinDue(body: String): Long? {
        val regex = Regex("""(?i)(?:Min Amt Due|Min Amount Due|Min Due|Min Due:?)\s*[:.\-]?\s*(?:Rs\.?|INR|₹)?\s*$AMOUNT_PATTERN""")
        val match = regex.find(body) ?: return null
        return parseAmountToPaise(match.groupValues[1])
    }

    fun extractDueDate(body: String): String? {
        val regex = Regex("""(?i)(?:Due Date|Payment Due Date)\s*[:.\-]?\s*([0-9]{1,2}[-/\s][A-Za-z0-9]+[-/\s][0-9]{2,4})""")
        val match = regex.find(body) ?: return null
        return match.groupValues[1].trim()
    }
}