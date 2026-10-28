package com.patil.coconut_pfa.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.util.MoneyFormatter
import com.patil.coconut_pfa.util.PrivacyManager
import kotlinx.coroutines.delay

/**
 * Custom Text component for displaying monetary amounts in paise.
 * Respects global [PrivacyManager.isPrivacyEnabled].
 * When privacy mode is ON, tapping the text temporarily reveals the actual amount for 5 seconds.
 */
@Composable
fun AmountText(
    amountInPaise: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    showSymbol: Boolean = true,
) {
    val isPrivacyEnabled by PrivacyManager.isPrivacyEnabled.collectAsState()
    var isTemporarilyRevealed by remember { mutableStateOf(false) }

    LaunchedEffect(isTemporarilyRevealed, isPrivacyEnabled) {
        if (isPrivacyEnabled && isTemporarilyRevealed) {
            delay(5000L)
            isTemporarilyRevealed = false
        }
    }

    LaunchedEffect(isPrivacyEnabled) {
        if (!isPrivacyEnabled) {
            isTemporarilyRevealed = false
        }
    }

    val displayText = remember(amountInPaise, isPrivacyEnabled, isTemporarilyRevealed, showSymbol) {
        if (isPrivacyEnabled && !isTemporarilyRevealed) {
            if (showSymbol) "₹ ••••" else "••••"
        } else {
            MoneyFormatter.format(
                amountInPaise = amountInPaise,
                isPrivacyEnabled = false,
                showSymbol = showSymbol,
            )
        }
    }

    Text(
        text = displayText,
        modifier = modifier.clickable(
            enabled = isPrivacyEnabled,
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
        ) {
            isTemporarilyRevealed = true
        },
        style = style,
        color = color,
        fontWeight = fontWeight,
    )
}

@Preview(showBackground = true)
@Composable
private fun AmountTextPreview() {
    Coconut_PfaTheme {
        Column {
            AmountText(
                amountInPaise = 1250000L,
                style = MaterialTheme.typography.headlineMedium
            )
            AmountText(
                amountInPaise = -45050L,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
