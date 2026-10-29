package com.patil.coconut_pfa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patil.coconut_pfa.domain.CardNetwork
import com.patil.coconut_pfa.ui.theme.*

@Composable
fun PaymentCardView(
    bankName: String?,
    networkLogo: CardNetwork,
    cardTypeLabel: String,
    nickname: String?,
    numberText: String,
    cardholderName: String,
    expiry: String,
    colour: String,
    cvv: String? = null,
    isBackSide: Boolean = false,
    onClick: (() -> Unit)? = null,
    headerContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val brush = getCardBrush(colour)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = brush)
                .padding(20.dp)
        ) {
            if (isBackSide) {
                // Back Side
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Magnetic Stripe
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(Color.Black)
                    )

                    // Signature & CVV strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Authorized Signature",
                                color = Color.DarkGray,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(36.dp)
                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cvv ?: "•••",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Bottom info or bank
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bankName ?: "Coconut Vault",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelMedium
                        )
                        NetworkBadge(network = networkLogo)
                    }
                }
            } else {
                // Front Side
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (headerContent != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            headerContent()
                            Icon(
                                imageVector = Icons.Rounded.CreditCard,
                                contentDescription = "Chip",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                if (!nickname.isNullOrBlank()) {
                                    Text(
                                        text = nickname,
                                        color = Color.White.copy(alpha = 0.9f),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                                Text(
                                    text = bankName ?: cardTypeLabel,
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (bankName != null && cardTypeLabel.isNotBlank()) {
                                    Text(
                                        text = cardTypeLabel,
                                        color = Color.White.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Rounded.CreditCard,
                                contentDescription = "Chip",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = numberText,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "VALID THRU",
                                    color = Color.White.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = expiry.ifBlank { "12/28" },
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column {
                                Text(
                                    text = "CARDHOLDER",
                                    color = Color.White.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = cardholderName.ifBlank { "Cardholder" },
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            NetworkBadge(network = networkLogo)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkBadge(network: CardNetwork, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = Color.White.copy(alpha = 0.9f)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            when (network) {
                CardNetwork.VISA -> {
                    Text(
                        text = "VISA",
                        color = Color(0xFF1A1F71),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
                CardNetwork.MASTERCARD -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(Color.Red, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .offset(x = (-6).dp)
                                .background(Color(0xFFFF5722).copy(alpha = 0.8f), CircleShape)
                        )
                    }
                }
                CardNetwork.RUPAY -> {
                    Text(
                        text = "RuPay",
                        color = Color(0xFF0C2340),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                CardNetwork.AMEX -> {
                    Text(
                        text = "AMEX",
                        color = Color(0xFF00175A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                CardNetwork.DINERS -> {
                    Text(
                        text = "DINERS",
                        color = Color(0xFF0079BE),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
                CardNetwork.DISCOVER -> {
                    Text(
                        text = "DISCOVER",
                        color = Color(0xFFFF6600),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
                CardNetwork.OTHER -> {
                    Text(
                        text = "CARD",
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

fun getCardBrush(colour: String): Brush {
    return when (colour) {
        "GRADIENT_1", "blue" -> Brush.linearGradient(listOf(GradientBlueCyanStart, GradientBlueCyanEnd))
        "GRADIENT_2", "purple" -> Brush.linearGradient(listOf(GradientPurpleStart, GradientPurpleEnd))
        "GRADIENT_3", "orange" -> Brush.linearGradient(listOf(GradientOrangeCoralStart, GradientOrangeCoralEnd))
        "GRADIENT_4", "emerald" -> Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF10B981)))
        "GRADIENT_5", "slate" -> Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF0F172A)))
        "GRADIENT_6", "rose" -> Brush.linearGradient(listOf(Color(0xFFE11D48), Color(0xFFFB7185)))
        else -> Brush.linearGradient(listOf(GradientBlueCyanStart, GradientBlueCyanEnd))
    }
}
