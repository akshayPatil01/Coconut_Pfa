package com.patil.coconut_pfa.data.model

enum class CardNetwork {
    VISA, MASTERCARD, RUPAY, AMEX, DISCOVER, DINERS, UNKNOWN
}

object CardRules {
    fun detectNetwork(cardNumber: String): CardNetwork {
        val digits = cardNumber.filter { it.isDigit() }
        return when {
            digits.startsWith("4") -> CardNetwork.VISA
            digits.matches(Regex("^5[1-5].*")) || digits.matches(Regex("^2[2-7].*")) -> CardNetwork.MASTERCARD
            digits.matches(Regex("^60[6-9].*")) || digits.matches(Regex("^65[2-9].*")) || digits.matches(Regex("^508[5-9].*")) || digits.matches(Regex("^608[2-5].*")) -> CardNetwork.RUPAY
            digits.startsWith("34") || digits.startsWith("37") -> CardNetwork.AMEX
            digits.startsWith("6011") || digits.matches(Regex("^65.*")) -> CardNetwork.DISCOVER
            digits.startsWith("36") || digits.startsWith("38") || digits.startsWith("300") || digits.startsWith("305") -> CardNetwork.DINERS
            else -> CardNetwork.UNKNOWN
        }
    }

    fun luhnCheck(cardNumber: String): Boolean {
        val digits = cardNumber.filter { it.isDigit() }.map { it.toString().toInt() }
        if (digits.isEmpty()) return false
        var sum = 0
        var alternate = false
        for (i in digits.size - 1 downTo 0) {
            var n = digits[i]
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

    fun maskCardNumber(cardNumber: String): String {
        val digits = cardNumber.filter { it.isDigit() }
        if (digits.length <= 4) return digits
        val last4 = digits.takeLast(4)
        return "•••• •••• •••• $last4"
    }
}
