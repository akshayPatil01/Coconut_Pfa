package com.patil.coconut_pfa.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patil.coconut_pfa.data.local.datastore.ToastStyle
import com.patil.coconut_pfa.ui.theme.GradientOrangeCoralEnd
import com.patil.coconut_pfa.ui.theme.GradientOrangeCoralStart
import com.patil.coconut_pfa.ui.theme.Orange40

/**
 * Custom Toast / Notification Card matching the modern floating banner design.
 * Supports theme-adaptive orange options and 4 customizable color themes via [ToastStyle].
 */
@Composable
fun CoconutToast(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    toastStyle: ToastStyle = ToastStyle.ORANGE_ADAPTIVE
) {
    val fullMessage = snackbarData.visuals.message
    val actionLabel = snackbarData.visuals.actionLabel

    // Parse title & subtitle
    val (title, subtitle) = rememberToastText(fullMessage)

    // Determine status badge
    val isError = fullMessage.contains("failed", ignoreCase = true) ||
            fullMessage.contains("error", ignoreCase = true) ||
            fullMessage.contains("incorrect", ignoreCase = true) ||
            fullMessage.contains("invalid", ignoreCase = true)

    val isWarning = fullMessage.contains("warning", ignoreCase = true) ||
            fullMessage.contains("required", ignoreCase = true)

    val icon = rememberToastIcon(fullMessage)

    val badgeColor = when {
        isError -> Color(0xFFEF4444)
        isWarning -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981) // Emerald Green
    }

    val badgeIcon = when {
        isError -> Icons.Rounded.Close
        isWarning -> Icons.Rounded.Info
        else -> Icons.Rounded.Check
    }

    // Resolve adaptive style if needed
    val isDarkTheme = isSystemInDarkTheme() || MaterialTheme.colorScheme.surface.red < 0.2f
    val resolvedStyle = if (toastStyle == ToastStyle.ORANGE_ADAPTIVE) {
        if (isDarkTheme) ToastStyle.SOFT_ORANGE_TINT else ToastStyle.DARK_SLATE_ORANGE
    } else {
        toastStyle
    }

    // Color styling configuration for the Toast Styles
    val cardBgColor: Color
    val titleColor: Color
    val subtitleColor: Color
    val iconBoxBgColor: Color
    val iconTint: Color
    val borderStroke: BorderStroke?
    val bottomAccentBrush: Brush
    val buttonBgColor: Color
    val buttonTextColor: Color
    val shadowElevation: Int

    val orangeGradient = Brush.horizontalGradient(
        listOf(GradientOrangeCoralStart, GradientOrangeCoralEnd)
    )

    when (resolvedStyle) {
        ToastStyle.DARK_SLATE_ORANGE, ToastStyle.ORANGE_ADAPTIVE -> {
            cardBgColor = Color(0xFF1E293B) // Dark Charcoal Slate for light background contrast
            titleColor = Color(0xFFF8FAFC) // Off-white
            subtitleColor = Color(0xFF94A3B8) // Muted Slate
            iconBoxBgColor = if (isError) Color(0xFF451A1A) else if (isWarning) Color(0xFF453010) else Color(0xFF334155)
            iconTint = if (isError) Color(0xFFF87171) else if (isWarning) Color(0xFFFBBF24) else GradientOrangeCoralEnd
            borderStroke = BorderStroke(1.dp, Color(0xFF334155))
            bottomAccentBrush = orangeGradient
            buttonBgColor = Orange40
            buttonTextColor = Color.White
            shadowElevation = 10
        }
        ToastStyle.SOFT_ORANGE_TINT -> {
            cardBgColor = Color(0xFFFFF7ED) // Soft Warm Light Tint for dark background contrast
            titleColor = Color(0xFF1C1917) // Dark Charcoal Title
            subtitleColor = Color(0xFF44403C) // Muted Text
            iconBoxBgColor = if (isError) Color(0xFFFEE2E2) else if (isWarning) Color(0xFFFEF3C7) else Color(0xFFFED7AA)
            iconTint = if (isError) Color(0xFFDC2626) else if (isWarning) Color(0xFFD97706) else Color(0xFFC2410C)
            borderStroke = BorderStroke(1.5.dp, Color(0xFFFDBA74))
            bottomAccentBrush = orangeGradient
            buttonBgColor = Orange40
            buttonTextColor = Color.White
            shadowElevation = 8
        }
        ToastStyle.DEEP_ROYAL -> {
            cardBgColor = Color(0xFF1E1B4B) // Deep Royal Indigo
            titleColor = Color(0xFFFFFFFF) // Pure White
            subtitleColor = Color(0xFFC7D2FE) // Soft Indigo
            iconBoxBgColor = if (isError) Color(0xFF451A1A) else if (isWarning) Color(0xFF453010) else Color(0xFF312E81)
            iconTint = if (isError) Color(0xFFF87171) else if (isWarning) Color(0xFFFBBF24) else Color(0xFFA5B4FC)
            borderStroke = BorderStroke(1.dp, Color(0xFF4338CA))
            bottomAccentBrush = Brush.horizontalGradient(listOf(Color(0xFF818CF8), Color(0xFFC084FC), Color.Transparent))
            buttonBgColor = Color(0xFF6366F1)
            buttonTextColor = Color.White
            shadowElevation = 12
        }
        ToastStyle.CRISP_BORDERED -> {
            cardBgColor = Color(0xFFFFFFFF) // Crisp White
            titleColor = Color(0xFF0F172A) // Dark Slate Title
            subtitleColor = Color(0xFF64748B) // Slate Muted
            iconBoxBgColor = if (isError) Color(0xFFFEE2E2) else if (isWarning) Color(0xFFFEF3C7) else Color(0xFFF1F5F9)
            iconTint = if (isError) Color(0xFFDC2626) else if (isWarning) Color(0xFFD97706) else Color(0xFF2563EB)
            borderStroke = BorderStroke(1.5.dp, Color(0xFF475569)) // Prominent dark outline!
            bottomAccentBrush = Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF3B82F6), Color.Transparent))
            buttonBgColor = Color(0xFF1E293B)
            buttonTextColor = Color.White
            shadowElevation = 12
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = shadowElevation.dp,
                shape = RoundedCornerShape(22.dp),
                clip = false
            )
            .clip(RoundedCornerShape(22.dp))
            .background(cardBgColor)
            .border(borderStroke, RoundedCornerShape(22.dp))
            .clickable(enabled = false) {}
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Box with Badge Overlay
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Main Icon Container Box
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(iconBoxBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Bottom-Right Badge Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(badgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title & Subtitle Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Optional Action Button (Pill Button)
                if (!actionLabel.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        onClick = { snackbarData.performAction() },
                        shape = CircleShape,
                        color = buttonBgColor,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = actionLabel,
                                color = buttonTextColor,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Bottom Accent Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(bottomAccentBrush)
            )
        }
    }
}

/**
 * Reusable Custom Snackbar Host that renders [CoconutToast].
 */
@Composable
fun CoconutSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    toastStyle: ToastStyle = ToastStyle.ORANGE_ADAPTIVE
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            CoconutToast(
                snackbarData = snackbarData,
                toastStyle = toastStyle
            )
        }
    )
}

/**
 * Helper function to trigger a rich toast with title and optional subtitle.
 */
suspend fun SnackbarHostState.showCoconutToast(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult {
    val formattedMessage = if (!subtitle.isNullOrBlank()) {
        "$title\n$subtitle"
    } else {
        title
    }

    return showSnackbar(
        message = formattedMessage,
        actionLabel = actionLabel,
        duration = duration
    )
}

private fun rememberToastText(message: String): Pair<String, String?> {
    return when {
        message.contains("\n") -> {
            val parts = message.split("\n", limit = 2)
            Pair(parts[0].trim(), parts.getOrNull(1)?.trim())
        }
        message.contains(" | ") -> {
            val parts = message.split(" | ", limit = 2)
            Pair(parts[0].trim(), parts.getOrNull(1)?.trim())
        }
        else -> Pair(message, null)
    }
}

private fun rememberToastIcon(message: String): ImageVector {
    val lower = message.lowercase()
    return when {
        lower.contains("export") || lower.contains("pdf") || lower.contains("report") || lower.contains("statement") -> Icons.Rounded.Description
        lower.contains("transaction") || lower.contains("account") || lower.contains("saved") || lower.contains("split") -> Icons.Rounded.AccountBalanceWallet
        lower.contains("backup") || lower.contains("drive") || lower.contains("cloud") || lower.contains("restore") -> Icons.Rounded.CloudDone
        lower.contains("pin") || lower.contains("lock") || lower.contains("password") -> Icons.Rounded.Lock
        lower.contains("bill") -> Icons.AutoMirrored.Rounded.ReceiptLong
        lower.contains("sms") || lower.contains("inbox") -> Icons.Rounded.Sms
        lower.contains("warning") || lower.contains("failed") || lower.contains("error") -> Icons.Rounded.Warning
        else -> Icons.Rounded.CheckCircle
    }
}
