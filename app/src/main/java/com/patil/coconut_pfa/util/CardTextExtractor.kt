package com.patil.coconut_pfa.util

object CardTextExtractor {
    data class ExtractedCardInfo(
        val cardNumber: String?,
        val expiryMonth: String?,
        val expiryYear: String?,
        val cardHolderName: String?
    )

    fun extract(text: String): ExtractedCardInfo {
        val cleanText = text.replace("\n", " ")
        val cardNumRegex = Regex("\\b(?:\\d[ -]*?){13,19}\\b")
        val cardNumber = cardNumRegex.find(cleanText)?.value?.filter { it.isDigit() }

        val expiryRegex = Regex("\\b(0[1-9]|1[0-2])[ /-]?(2[3-9]|[3-9][0-9])\\b")
        val expiryMatch = expiryRegex.find(cleanText)
        val expiryMonth = expiryMatch?.groupValues?.get(1)
        val expiryYear = expiryMatch?.groupValues?.get(2)

        return ExtractedCardInfo(
            cardNumber = cardNumber,
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            cardHolderName = null
        )
    }
}
