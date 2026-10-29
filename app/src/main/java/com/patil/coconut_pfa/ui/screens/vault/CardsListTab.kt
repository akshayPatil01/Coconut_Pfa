package com.patil.coconut_pfa.ui.screens.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.ui.components.EmptyState
import com.patil.coconut_pfa.ui.components.PaymentCardView
import com.patil.coconut_pfa.ui.viewmodel.CardDisplayItem

@Composable
fun CardsListTab(
    cards: List<CardDisplayItem>,
    onCardClick: (String) -> Unit,
    onAddCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedCards = cards.sortedWith(compareByDescending<CardDisplayItem> { it.item.favourite }.thenBy { it.item.createdAt })

    if (sortedCards.isEmpty()) {
        EmptyState(
            title = "Add your first card",
            description = "Add your credit, debit, prepaid or forex cards to your encrypted vault.",
            icon = Icons.Rounded.CreditCard,
            actionLabel = "Add Card",
            onActionClick = onAddCardClick,
            modifier = modifier.fillMaxSize()
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy((-140).dp)
            ) {
                sortedCards.forEachIndexed { index, cardItem ->
                    PaymentCardView(
                        bankName = cardItem.meta.bank,
                        networkLogo = cardItem.meta.network,
                        cardTypeLabel = cardItem.meta.cardType.displayName,
                        nickname = cardItem.meta.nickname,
                        numberText = "•••• •••• •••• ${cardItem.meta.last4}",
                        cardholderName = cardItem.meta.cardholderName,
                        expiry = cardItem.meta.expiry,
                        colour = cardItem.meta.colour,
                        onClick = { onCardClick(cardItem.item.id) }
                    )
                }
            }
        }
    }
}
