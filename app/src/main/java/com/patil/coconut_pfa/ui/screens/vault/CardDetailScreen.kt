package com.patil.coconut_pfa.ui.screens.vault

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.model.CardMeta
import com.patil.coconut_pfa.data.model.CardSecret
import com.patil.coconut_pfa.ui.components.PaymentCardView
import com.patil.coconut_pfa.ui.viewmodel.VaultCredentialsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    uiState: VaultCredentialsUiState,
    onNavigateBack: () -> Unit,
    onEdit: (String) -> Unit,
    onToggleFavourite: (String) -> Unit,
    onMoveToTrash: (String) -> Unit,
    onRevealField: (String, Int) -> Unit,
    onCopyField: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val item = uiState.selectedItemDetail
    val meta = uiState.selectedCardMeta
    val secret = uiState.selectedCardSecret

    var isBackSide by remember { mutableStateOf(false) }

    if (item == null || meta == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val numberRevealed = uiState.revealedFields["card_number"] == true
    val cvvRevealed = uiState.revealedFields["card_cvv"] == true

    val displayNumber = if (numberRevealed && secret != null) {
        com.patil.coconut_pfa.domain.CardRules.formatNumber(secret.number, meta.network)
    } else {
        "•••• •••• •••• ${meta.last4}"
    }

    val displayCvv = if (cvvRevealed && secret != null) {
        secret.cvv
    } else {
        "•••"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(meta.nickname.ifBlank { meta.bank }) },
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
                isBackSide = isBackSide
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(onClick = { isBackSide = !isBackSide }) {
                    Icon(imageVector = Icons.Rounded.Flip, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBackSide) "Show Front" else "Show Back")
                }
            }

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
                        text = "Card Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Full Number row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Card Number", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = displayNumber, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { onRevealField("card_number", 30) }) {
                                Icon(imageVector = if (numberRevealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = "Reveal")
                            }
                            IconButton(onClick = { secret?.let { onCopyField("Card Number", it.number) } }) {
                                Icon(imageVector = Icons.Rounded.ContentCopy, contentDescription = "Copy")
                            }
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
                            Text(text = "CVV", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = displayCvv, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { onRevealField("card_cvv", 30) }) {
                                Icon(imageVector = if (cvvRevealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = "Reveal")
                            }
                            IconButton(onClick = { secret?.let { onCopyField("CVV", it.cvv) } }) {
                                Icon(imageVector = Icons.Rounded.ContentCopy, contentDescription = "Copy")
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
        }
    }
}
