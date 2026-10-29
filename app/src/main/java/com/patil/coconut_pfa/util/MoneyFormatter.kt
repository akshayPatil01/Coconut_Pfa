package com.patil.coconut_pfa.util

import java.lang.StringBuilder
import kotlin.math.abs

/**
 * Utility for formatting currency amounts in Indian Rupees (₹) with proper digit grouping.
 * All monetary amounts in the system MUST be stored and passed as [Long] paise.
 */
object MoneyFormatter {

    /**
     * Formats an amount given in paise into Indian currency format.
     * Examples:
     * - 10000000L -> "₹1,00,000"
     * - 125050L -> "₹1,250.50"
     * - -50000L -> "-₹500"
     * - isPrivacyEnabled = true -> "₹••••••"
     *
     * @param amountInPaise The money amount in paise (1 Rupee = 100 paise).
     * @param isPrivacyEnabled If true, returns a masked representation ("₹••••••").
     * @param showSymbol Whether to include the '₹' symbol. Default is true.
     * @param showDecimals Whether to include decimal paise values. Default is true.
     * @return Formatted currency string.
     */
    fun format(
        amountInPaise: Long,
        isPrivacyEnabled: Boolean = false,
        showSymbol: Boolean = true,
        showDecimals: Boolean = true,
    ): String {
        if (isPrivacyEnabled) {
            return if (showSymbol) "₹••••••" else "••••••"
        }

        val isNegative = amountInPaise < 0
        val absPaise = abs(amountInPaise)
        val rupees = absPaise / 100
        val paise = absPaise % 100

        val formattedRupees = formatIndianRupees(rupees)

        val formattedPaise = if (showDecimals && paise > 0) {
            val paiseString = if (paise < 10) "0$paise" else paise.toString()
            ".$paiseString"
        } else {
            ""
        }

        val prefix = if (isNegative) "-" else ""
        val symbolStr = if (showSymbol) "₹" else ""

        return "$prefix$symbolStr$formattedRupees$formattedPaise"
    }

    /**
     * Formats rupee integer part using Indian numbering system (comma after first 3 digits from right,
     * then every 2 digits).
     */
    private fun formatIndianRupees(rupees: Long): String {
        val s = rupees.toString()
        if (s.length <= 3) return s

        val last3 = s.takeLast(3)
        val rest = s.dropLast(3)

        val result = StringBuilder()
        var i = rest.length
        while (i > 0) {
            if (i >= 2) {
                if (result.isNotEmpty()) {
                    result.insert(0, ",")
                }
                result.insert(0, rest.substring(i - 2, i))
                i -= 2
            } else {
                if (result.isNotEmpty()) {
                    result.insert(0, ",")
                }
                result.insert(0, rest.substring(0, i))
                i = 0
            }
        }

        return "$result,$last3"
    }

    /**
     * Helper to convert Rupees (Long) into paise.
     */
    fun rupeesToPaise(rupees: Long): Long = rupees * 100L

    /**
     * Helper to convert Rupees (Double) into paise safely rounding to closest Long.
     */
    fun rupeesToPaise(rupees: Double): Long = kotlin.math.round(rupees * 100.0).toLong()

    /**
     * Parses a currency string into Long paise.
     */
    fun parseToPaise(rupeeString: String): Long {
        val clean = rupeeString.trim().replace(",", "").replace("₹", "").replace("−", "-")
        if (clean.isEmpty()) return 0L
        val parts = clean.split(".")
        val rupees = parts[0].toLongOrNull() ?: 0L
        val isNeg = (rupees < 0) || clean.startsWith("-")
        val absRupees = abs(rupees)
        val paise = if (parts.size > 1) {
            val pStr = parts[1].padEnd(2, '0').take(2)
            pStr.toLongOrNull() ?: 0L
        } else {
            0L
        }
        val total = absRupees * 100L + paise
        return if (isNeg) -total else total
    }
}
