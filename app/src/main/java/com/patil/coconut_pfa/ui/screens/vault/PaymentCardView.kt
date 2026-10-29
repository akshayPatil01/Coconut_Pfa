package com.patil.coconut_pfa.ui.screens.vault

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.patil.coconut_pfa.domain.CardNetwork
import com.patil.coconut_pfa.ui.components.PaymentCardView as ComponentsPaymentCardView

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
    modifier: Modifier = Modifier
) {
    ComponentsPaymentCardView(
        bankName = bankName,
        networkLogo = networkLogo,
        cardTypeLabel = cardTypeLabel,
        nickname = nickname,
        numberText = numberText,
        cardholderName = cardholderName,
        expiry = expiry,
        colour = colour,
        cvv = cvv,
        isBackSide = isBackSide,
        onClick = onClick,
        modifier = modifier
    )
}
