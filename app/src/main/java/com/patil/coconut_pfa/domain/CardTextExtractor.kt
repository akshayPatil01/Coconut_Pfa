package com.patil.coconut_pfa.domain

data class ScannedCardData(
    val number: String,
    val expiry: String,
    val cardholderName: String
)

object CardTextExtractor {
    fun extract(lines: List<String>): ScannedCardData? {
        var cardNumber: String? = null

        fun tryCandidate(raw: String): Boolean {
            val digits = raw.filter { it.isDigit() }
            if (digits.length in 13..19 && CardRules.luhnCheck(digits)) {
                cardNumber = digits
                return true
            }
            return false
        }

        // 1. Check individual lines
        for (line in lines) {
            if (tryCandidate(line)) break
        }

        // 2. Check adjacent line pairs (number split over 2 lines)
        if (cardNumber == null) {
            for (i in 0 until lines.size - 1) {
                val combined = "${lines[i]} ${lines[i+1]}"
                if (tryCandidate(combined)) break
            }
        }

        if (cardNumber == null) return null

        val expiry = extractExpiry(lines)
        val name = extractName(lines)

        return ScannedCardData(
            number = cardNumber,
            expiry = expiry ?: "",
            cardholderName = name ?: ""
        )
    }

    private fun extractExpiry(lines: List<String>): String? {
        val dateRegex = Regex("\\b(0?[1-9]|1[0-2])[/\\-](\\d{2}(?:\\d{2})?)\\b")
        val expiryKeywords = listOf("VALID", "THRU", "EXPIRES", "EXP", "UPTO", "GOOD")
        val foundDates = mutableListOf<Pair<Int, String>>()

        for ((index, line) in lines.withIndex()) {
            val upper = line.uppercase()
            val hasKeyword = expiryKeywords.any { upper.contains(it) }
            val matches = dateRegex.findAll(line)
            for (m in matches) {
                val month = m.groupValues[1].padStart(2, '0')
                var year = m.groupValues[2]
                if (year.length == 4) {
                    year = year.takeLast(2)
                }
                val formatted = "$month/$year"
                
                val yInt = year.toIntOrNull() ?: 0
                val mInt = month.toIntOrNull() ?: 0
                val dateValue = yInt * 12 + mInt
                
                var score = dateValue
                if (hasKeyword) score += 10000
                if (index > 0 && expiryKeywords.any { lines[index - 1].uppercase().contains(it) }) score += 5000
                if (index < lines.size - 1 && expiryKeywords.any { lines[index + 1].uppercase().contains(it) }) score += 5000

                foundDates.add(Pair(score, formatted))
            }
        }

        if (foundDates.isEmpty()) {
            for (line in lines) {
                val matches = dateRegex.findAll(line)
                for (m in matches) {
                    val month = m.groupValues[1].padStart(2, '0')
                    var year = m.groupValues[2]
                    if (year.length == 4) year = year.takeLast(2)
                    val formatted = "$month/$year"
                    val yInt = year.toIntOrNull() ?: 0
                    val mInt = month.toIntOrNull() ?: 0
                    foundDates.add(Pair(yInt * 12 + mInt, formatted))
                }
            }
        }

        if (foundDates.isNotEmpty()) {
            return foundDates.maxByOrNull { (_, exp) ->
                val parts = exp.split("/")
                val y = parts[1].toIntOrNull() ?: 0
                val mo = parts[0].toIntOrNull() ?: 0
                y * 12 + mo
            }?.second
        }

        return null
    }

    private fun extractName(lines: List<String>): String? {
        val ignoreWords = setOf(
            "VISA", "MASTERCARD", "RUPAY", "AMEX", "DINERS", "DISCOVER",
            "HDFC", "ICICI", "SBI", "AXIS", "BANK", "CREDIT", "DEBIT",
            "VALID", "THRU", "EXPIRES", "EXP", "UPTO", "GOOD", "MEMBER", "SINCE",
            "PLATINUM", "GOLD", "SIGNATURE", "WORLD", "INFINITY", "PREMIER"
        )

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed != trimmed.uppercase()) continue
            val words = trimmed.split(Regex("\\s+"))
            if (words.size in 2..4) {
                val allLetters = words.all { word -> word.all { it.isLetter() } }
                if (allLetters) {
                    val hasIgnore = words.any { it in ignoreWords }
                    if (!hasIgnore) {
                        return trimmed
                    }
                }
            }
        }
        return null
    }
}
