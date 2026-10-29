package com.patil.coconut_pfa.ui.screens.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.model.CardMeta
import com.patil.coconut_pfa.data.model.CardSecret
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
    val item = uiState.selectedItemDetail ?: return
    val meta = uiState.selectedItemMeta as? CardMeta ?: return
    val secret = uiState.selectedItemSecret as? CardSecret ?: return

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(meta.title) },
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
                            tint = if (item.favourite) Color(0xFFFFC107) else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { onEdit(item.id) }) {
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { onMoveToTrash(item.id) }) {
                        Icon(imageVector = Icons.Rounded.DeleteOutline, contentDescription = "Move to Trash", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PaymentCardView(
                    title = meta.title,
                    cardHolderName = meta.cardHolderName,
                    last4 = meta.last4,
                    network = meta.network,
                    expiryMonth = meta.expiryMonth,
                    expiryYear = meta.expiryYear
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Card Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        val isCardNumberRevealed = uiState.revealedFields["card_number"] == true
                        SecretFieldRow(
                            label = "Card Number",
                            value = secret.cardNumber,
                            isRevealed = isCardNumberRevealed,
                            onReveal = { onRevealField("card_number", 30) },
                            onCopy = { onCopyField("Card Number", secret.cardNumber) }
                        )

                        val isCvvRevealed = uiState.revealedFields["cvv"] == true
                        SecretFieldRow(
                            label = "CVV",
                            value = secret.cvv,
                            isRevealed = isCvvRevealed,
                            onReveal = { onRevealField("cvv", 30) },
                            onCopy = { onCopyField("CVV", secret.cvv) }
                        )

                        DetailFieldRow(label = "Expiry Date", value = "${meta.expiryMonth}/${meta.expiryYear}", onCopy = { onCopyField("Expiry Date", "${meta.expiryMonth}/${meta.expiryYear}") })
                        DetailFieldRow(label = "Cardholder Name", value = meta.cardHolderName, onCopy = { onCopyField("Cardholder Name", meta.cardHolderName) })
                        DetailFieldRow(label = "Network", value = meta.network, onCopy = { onCopyField("Network", meta.network) })

                        if (secret.pin.isNotBlank()) {
                            val isPinRevealed = uiState.revealedFields["pin"] == true
                            SecretFieldRow(
                                label = "ATM PIN",
                                value = secret.pin,
                                isRevealed = isPinRevealed,
                                onReveal = { onRevealField("pin", 30) },
                                onCopy = { onCopyField("ATM PIN", secret.pin) }
                            )
                        }

                        if (secret.billingAddress.isNotBlank()) {
                            DetailFieldRow(label = "Billing Address", value = secret.billingAddress, onCopy = { onCopyField("Billing Address", secret.billingAddress) })
                        }
                        if (secret.phone.isNotBlank()) {
                            DetailFieldRow(label = "Phone", value = secret.phone, onCopy = { onCopyField("Phone", secret.phone) })
                        }
                        if (secret.notes.isNotBlank()) {
                            DetailFieldRow(label = "Notes", value = secret.notes, onCopy = { onCopyField("Notes", secret.notes) })
                        }
                    }
                }
            }
        }
    }
}
