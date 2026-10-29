package com.patil.coconut_pfa.ui.screens.vault

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patil.coconut_pfa.data.local.security.VaultSession
import com.patil.coconut_pfa.data.model.Account
import com.patil.coconut_pfa.domain.CardNetwork
import com.patil.coconut_pfa.domain.CardRules
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.PaymentCardView
import com.patil.coconut_pfa.ui.viewmodel.VaultCredentialsUiState
import kotlinx.coroutines.delay
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    uiState: VaultCredentialsUiState,
    onNavigateBack: () -> Unit,
    onEdit: (String) -> Unit,
    onToggleFavourite: (String) -> Unit,
    onMoveToTrash: (String) -> Unit,
    onRevealField: (String, Int) -> Unit,
    onCopyField: (String, String, String) -> Unit,
    onOpenAccount: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val item = uiState.selectedItemDetail
    val meta = uiState.selectedCardMeta
    val secret = uiState.selectedCardSecret
    val linkedAccount = uiState.selectedCardLinkedAccount

    var isBackSide by remember { mutableStateOf(false) }

    // Check vault lock
    val isLocked = VaultSession.isLocked
    LaunchedEffect(isLocked) {
        if (isLocked) {
            onNavigateBack()
        }
    }

    if (item == null || meta == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val numberRevealed = uiState.revealedFields["card_number"] == true
    val cvvRevealed = uiState.revealedFields["card_cvv"] == true

    var numberSecondsLeft by remember { mutableIntStateOf(30) }
    LaunchedEffect(numberRevealed) {
        if (numberRevealed) {
            numberSecondsLeft = 30
            while (numberSecondsLeft > 0) {
                delay(1000L)
                numberSecondsLeft--
            }
        }
    }

    var cvvSecondsLeft by remember { mutableIntStateOf(30) }
    LaunchedEffect(cvvRevealed) {
        if (cvvRevealed) {
            cvvSecondsLeft = 30
            while (cvvSecondsLeft > 0) {
                delay(1000L)
                cvvSecondsLeft--
            }
        }
    }

    val displayNumber = if (numberRevealed && secret != null) {
        CardRules.formatNumber(secret.number, meta.network)
    } else {
        "•••• •••• •••• ${meta.last4}"
    }

    val displayCvv = if (cvvRevealed && secret != null) {
        secret.cvv
    } else {
        if (meta.network == CardNetwork.AMEX) "••••" else "•••"
    }

    val rotationY by animateFloatAsState(
        targetValue = if (isBackSide) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "card_flip"
    )
    val density = LocalDensity.current.density

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(meta.nickname.takeIf { it.isNotBlank() } ?: meta.bank) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onToggleFavourite(item.id) }) {
                        Icon(
                            imageVector = if (item.favourite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = "Favourite",
                            tint = if (item.favourite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { onEdit(item.id) }) {
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Edit Card")
                    }
                    IconButton(onClick = { onMoveToTrash(item.id) }) {
                        Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete Card")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .graphicsLayer {
                        this.rotationY = rotationY
                        cameraDistance = 8f * density
                    }
            ) {
                PaymentCardView(
                    bankName = meta.bank,
                    networkLogo = meta.network,
                    cardTypeLabel = meta.cardType.displayName,
                    nickname = meta.nickname,
                    numberText = displayNumber,
                    cardholderName = meta.cardholderName,
                    expiry = meta.expiry,
                    colour = meta.colour,
                    cvv = displayCvv,
                    isBackSide = rotationY > 90f
                )
            }

            // Buttons under card: Show details, Copy number, Flip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRevealField("card_number", 30)
                    }
                ) {
                    Icon(imageVector = if (numberRevealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (numberRevealed) "Hide Details" else "Show Details")
                }

                OutlinedButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        secret?.let { onCopyField("Card Number", it.number, "Card number copied - clears in 30 s") }
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Number")
                }

                OutlinedButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        isBackSide = !isBackSide
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.Flip, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBackSide) "Front" else "Flip")
                }
            }

            // Card Details Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Card Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Card Number row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "Card Number", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (numberRevealed) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(20.dp)) {
                                        CircularProgressIndicator(
                                            progress = { numberSecondsLeft / 30f },
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text(text = "$numberSecondsLeft", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(text = displayNumber, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onRevealField("card_number", 30)
                            }) {
                                Icon(imageVector = if (numberRevealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = "Reveal")
                            }
                            IconButton(onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                secret?.let { onCopyField("Card Number", it.number, "Card number copied - clears in 30 s") }
                            }) {
                                Icon(imageVector = Icons.Rounded.ContentCopy, contentDescription = "Copy")
                            }
                        }
                    }

                    HorizontalDivider()

                    // Expiry row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Expiry", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = meta.expiry, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCopyField("Expiry", meta.expiry, "Expiry copied - clears in 30 s")
                        }) {
                            Icon(imageVector = Icons.Rounded.ContentCopy, contentDescription = "Copy Expiry")
                        }
                    }

                    HorizontalDivider()

                    // Cardholder Name row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Cardholder Name", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = meta.cardholderName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCopyField("Cardholder Name", meta.cardholderName, "Cardholder name copied - clears in 30 s")
                        }) {
                            Icon(imageVector = Icons.Rounded.ContentCopy, contentDescription = "Copy Name")
                        }
                    }

                    HorizontalDivider()

                    // CVV row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "CVV", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (cvvRevealed) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(20.dp)) {
                                        CircularProgressIndicator(
                                            progress = { cvvSecondsLeft / 30f },
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text(text = "$cvvSecondsLeft", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(text = displayCvv, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onRevealField("card_cvv", 30)
                            }) {
                                Icon(imageVector = if (cvvRevealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = "Reveal CVV")
                            }
                            IconButton(onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                secret?.let { onCopyField("CVV", it.cvv, "CVV copied - clears in 30 s") }
                            }) {
                                Icon(imageVector = Icons.Rounded.ContentCopy, contentDescription = "Copy CVV")
                            }
                        }
                    }

                    if (meta.notes.isNotBlank()) {
                        HorizontalDivider()
                        Column {
                            Text(text = "Notes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = meta.notes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Linked CREDIT_CARD Account Panel
            linkedAccount?.let { acc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Linked Account: ${acc.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { onOpenAccount(acc.id) }) {
                                Text("Open account →")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Outstanding", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                AmountText(amountInPaise = abs(acc.balanceInPaise), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Available Credit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                AmountText(amountInPaise = acc.availableCreditInPaise, color = Color(0xFF10B981), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Unbilled This Cycle", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                AmountText(amountInPaise = acc.unbilledAmountInPaise, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Next Due Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = acc.dueDate ?: "N/A", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
