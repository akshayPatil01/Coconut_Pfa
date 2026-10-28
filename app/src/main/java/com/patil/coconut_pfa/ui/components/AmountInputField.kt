package com.patil.coconut_pfa.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Visual Transformation that formats numeric input strings with Indian numbering system commas.
 * Examples:
 * - "1000" -> "1,000"
 * - "100000" -> "1,00,000"
 * - "1234567.50" -> "12,34,567.50"
 */
class IndianCurrencyVisualTransformation(
    private val prefix: String = ""
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        if (rawText.isEmpty()) {
            return TransformedText(AnnotatedString(prefix), OffsetMapping.Identity)
        }

        val parts = rawText.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) "." + parts[1] else ""

        val formattedInteger = formatIndianRupeesString(integerPart)
        val transformed = "$prefix$formattedInteger$decimalPart"

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return prefix.length
                val clampedOffset = offset.coerceAtMost(rawText.length)
                val rawSub = rawText.substring(0, clampedOffset)
                val subParts = rawSub.split(".")
                val subInt = subParts[0]
                val subDec = if (subParts.size > 1) "." + subParts[1] else ""
                val formattedSubInt = formatIndianRupeesString(subInt)
                return prefix.length + formattedSubInt.length + subDec.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= prefix.length) return 0
                val adjustedOffset = offset - prefix.length
                if (adjustedOffset >= transformed.length - prefix.length) return rawText.length

                var rawIndex = 0
                var transformedIndex = 0

                val formattedInt = formattedInteger
                while (transformedIndex < adjustedOffset && rawIndex < rawText.length) {
                    if (transformedIndex < formattedInt.length && formattedInt[transformedIndex] == ',') {
                        transformedIndex++
                    } else {
                        rawIndex++
                        transformedIndex++
                    }
                }
                return rawIndex.coerceIn(0, rawText.length)
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }

    private fun formatIndianRupeesString(s: String): String {
        if (s.isEmpty()) return ""
        val isNegative = s.startsWith("-")
        val cleanDigits = if (isNegative) s.substring(1) else s
        if (cleanDigits.length <= 3) return s

        val last3 = cleanDigits.takeLast(3)
        val rest = cleanDigits.dropLast(3)

        val result = StringBuilder()
        var i = rest.length
        while (i > 0) {
            if (i >= 2) {
                if (result.isNotEmpty()) result.insert(0, ",")
                result.insert(0, rest.substring(i - 2, i))
                i -= 2
            } else {
                if (result.isNotEmpty()) result.insert(0, ",")
                result.insert(0, rest.substring(0, i))
                i = 0
            }
        }

        val formattedRest = if (result.isNotEmpty()) "$result,$last3" else last3
        return if (isNegative) "-$formattedRest" else formattedRest
    }
}

/**
 * Reusable Amount Input Text Box with live Indian currency comma formatting.
 */
@Composable
fun AmountInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "0.00",
    prefix: String = "₹ ",
    enabled: Boolean = true,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Sanitize: allow max 1 decimal point, minus sign at start, digits only
            val filtered = buildString {
                var decimalCount = 0
                newValue.forEachIndexed { index, char ->
                    if (char.isDigit()) {
                        append(char)
                    } else if (char == '.' && decimalCount == 0) {
                        decimalCount++
                        append(char)
                    } else if (char == '-' && index == 0) {
                        append(char)
                    }
                }
            }
            onValueChange(filtered)
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        prefix = { Text(prefix, style = MaterialTheme.typography.bodyLarge) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = IndianCurrencyVisualTransformation(prefix = ""),
        shape = RoundedCornerShape(16.dp)
    )
}
