package com.patil.coconut_pfa.domain

import java.util.Calendar

enum class CardType(val displayName: String) {
    CREDIT("Credit"),
    DEBIT("Debit"),
    PREPAID("Prepaid"),
    FOREX("Forex")
}

enum class CardNetwork(val displayName: String) {
    VISA("Visa"),
    MASTERCARD("Mastercard"),
    RUPAY("RuPay"),
    AMEX("Amex"),
    DINERS("Diners"),
    DISCOVER("Discover"),
    OTHER("Other")
}

object CardRules {

    fun detectNetwork(number: String): CardNetwork {
        val digits = number.filter { it.isDigit() }
        if (digits.isEmpty()) return CardNetwork.OTHER

        // Amex: 34, 37
        if (digits.length >= 2) {
            val prefix2 = digits.substring(0, 2)
            if (prefix2 == "34" || prefix2 == "37") return CardNetwork.AMEX
        }

        // Visa: 4
        if (digits.startsWith("4")) return CardNetwork.VISA

        // Mastercard: 51-55 or 2221-2720
        if (digits.length >= 2) {
            val p2 = digits.substring(0, 2).toIntOrNull() ?: 0
            if (p2 in 51..55) return CardNetwork.MASTERCARD
        }
        if (digits.length >= 4) {
            val p4 = digits.substring(0, 4).toIntOrNull() ?: 0
            if (p4 in 2221..2720) return CardNetwork.MASTERCARD
        }

        // Diners: 36, 38, 39, or 300-305
        if (digits.length >= 2) {
            val p2 = digits.substring(0, 2)
            if (p2 == "36" || p2 == "38" || p2 == "39") return CardNetwork.DINERS
        }
        if (digits.length >= 3) {
            val p3 = digits.substring(0, 3).toIntOrNull() ?: 0
            if (p3 in 300..305) return CardNetwork.DINERS
        }

        // Discover: 6011, 644-649
        if (digits.length >= 4) {
            val p4 = digits.substring(0, 4)
            if (p4 == "6011" || (p4.length >= 3 && p4.substring(0, 3).toIntOrNull() in 644..649)) {
                return CardNetwork.DISCOVER
            }
        }

        // RuPay: 60, 65, 81, 82, 508, 353, 356
        if (digits.length >= 2) {
            val p2 = digits.substring(0, 2)
            if (p2 == "60" || p2 == "65" || p2 == "81" || p2 == "82") return CardNetwork.RUPAY
        }
        if (digits.length >= 3) {
            val p3 = digits.substring(0, 3).toIntOrNull() ?: 0
            if (p3 == 508 || p3 == 353 || p3 == 356) return CardNetwork.RUPAY
        }

        return CardNetwork.OTHER
    }

    fun isValidLength(number: String, network: CardNetwork? = null): Boolean {
        val digits = number.filter { it.isDigit() }
        val net = network ?: detectNetwork(digits)
        return when (net) {
            CardNetwork.AMEX -> digits.length == 15
            CardNetwork.DINERS -> digits.length == 14
            else -> digits.length in 13..19
        }
    }

    fun luhnCheck(number: String): Boolean {
        val digits = number.filter { it.isDigit() }
        if (digits.isEmpty()) return false
        var sum = 0
        var alternate = false
        for (i in digits.length - 1 downTo 0) {
            var n = digits[i].toString().toIntOrNull() ?: return false
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n -= 9
                }
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    fun validateCvv(cvv: String, network: CardNetwork): Boolean {
        val digits = cvv.filter { it.isDigit() }
        return if (network == CardNetwork.AMEX) {
            digits.length == 4
        } else {
            digits.length == 3
        }
    }

    fun formatNumber(number: String, network: CardNetwork): String {
        val digits = number.filter { it.isDigit() }
        return when {
            network == CardNetwork.AMEX && digits.length >= 10 -> {
                val p1 = digits.take(4)
                val p2 = digits.drop(4).take(6)
                val p3 = digits.drop(10)
                listOf(p1, p2, p3).filter { it.isNotEmpty() }.joinToString(" ")
            }
            network == CardNetwork.DINERS && digits.length >= 10 -> {
                val p1 = digits.take(4)
                val p2 = digits.drop(4).take(6)
                val p3 = digits.drop(10)
                listOf(p1, p2, p3).filter { it.isNotEmpty() }.joinToString(" ")
            }
            else -> {
                digits.chunked(4).joinToString(" ")
            }
        }
    }

    fun validateExpiry(expiry: String): Boolean {
        val clean = expiry.filter { it.isDigit() }
        if (clean.length != 4) return false
        val month = clean.take(2).toIntOrNull() ?: return false
        val year = clean.drop(2).toIntOrNull() ?: return false
        if (month !in 1..12) return false

        val fullYear = 2000 + year
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH) + 1

        if (fullYear < currentYear) return false
        if (fullYear == currentYear && month < currentMonth) return false

        return true
    }
}
