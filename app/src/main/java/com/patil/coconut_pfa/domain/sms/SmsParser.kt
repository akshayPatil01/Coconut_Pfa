package com.patil.coconut_pfa.domain.sms

object SmsParser {

    /**
     * Parses an incoming SMS message.
     *
     * @param sender The SMS sender ID (e.g., "AX-HDFCBK", "AD-ICICIB", "SBIINB", "AXISBK")
     * @param body The SMS body text
     * @param receivedAt The epoch timestamp in milliseconds when the SMS was received
     * @return ParsedSms if the sender is a recognized bank ID and the message is parseable, null otherwise.
     */
    fun parse(sender: String, body: String, receivedAt: Long): ParsedSms? {
        val institution = SmsParserUtils.detectBankInstitution(sender, body) ?: return null

        if (SmsParserUtils.isIgnoredSms(body)) {
            return ParsedSms(
                type = ParsedSmsType.IGNORE,
                institution = institution,
            )
        }

        val bankParsed = when (institution) {
            "HDFC Bank" -> HdfcSmsParser.parse(body, institution)
            "ICICI Bank" -> IciciSmsParser.parse(body, institution)
            "SBI" -> SbiSmsParser.parse(body, institution)
            "Axis Bank" -> AxisSmsParser.parse(body, institution)
            else -> null
        }

        return bankParsed ?: GenericSmsParser.parse(body, institution)
    }
}
