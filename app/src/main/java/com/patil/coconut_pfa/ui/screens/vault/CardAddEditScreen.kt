package com.patil.coconut_pfa.ui.screens.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.local.entity.VaultItem
import com.patil.coconut_pfa.data.model.Account
import com.patil.coconut_pfa.data.model.CardMeta
import com.patil.coconut_pfa.data.model.CardSecret
import com.patil.coconut_pfa.domain.CardNetwork
import com.patil.coconut_pfa.domain.CardRules
import com.patil.coconut_pfa.domain.CardType
import com.patil.coconut_pfa.ui.components.NetworkBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardAddEditScreen(
    existingItem: VaultItem?,
    existingMeta: CardMeta?,
    existingSecret: CardSecret?,
    initialNumber: String = "",
    initialExpiry: String = "",
    initialCardholderName: String = "",
    accounts: List<Account> = emptyList(),
    onNavigateBack: () -> Unit,
    onSaveCard: (
        nickname: String,
        bank: String,
        cardType: CardType,
        network: CardNetwork,
        cardNumber: String,
        expiry: String,
        cardholderName: String,
        colour: String,
        notes: String,
        cvv: String,
        linkedAccountId: String?,
        favourite: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEdit = existingItem != null

    var nickname by remember { mutableStateOf(existingMeta?.nickname ?: "") }
    var bank by remember { mutableStateOf(existingMeta?.bank ?: "HDFC") }
    var cardType by remember { mutableStateOf(existingMeta?.cardType ?: CardType.CREDIT) }
    var network by remember { mutableStateOf(existingMeta?.network ?: CardNetwork.VISA) }
    var cardNumber by remember { mutableStateOf(existingSecret?.number ?: initialNumber) }
    var expiry by remember { mutableStateOf(existingMeta?.expiry ?: initialExpiry) }
    var cardholderName by remember { mutableStateOf(existingMeta?.cardholderName ?: initialCardholderName) }
    var colour by remember { mutableStateOf(existingMeta?.colour ?: "GRADIENT_1") }
    var notes by remember { mutableStateOf(existingMeta?.notes ?: "") }
    var cvv by remember { mutableStateOf(existingSecret?.cvv ?: "") }
    var linkedAccountId by remember { mutableStateOf(existingItem?.linkedAccountId) }
    var favourite by remember { mutableStateOf(existingItem?.favourite ?: false) }

    val cvvFocusRequester = remember { FocusRequester() }
    LaunchedEffect(initialNumber) {
        if (initialNumber.isNotBlank()) {
            cvvFocusRequester.requestFocus()
        }
    }

    val detectedNetwork = remember(cardNumber) {
        CardRules.detectNetwork(cardNumber)
    }
    val activeNetwork = if (network == CardNetwork.OTHER || detectedNetwork != CardNetwork.OTHER) detectedNetwork else network

    val isLuhnValid = remember(cardNumber) {
        CardRules.luhnCheck(cardNumber)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Payment Card" else "Add Payment Card") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (cardNumber.isBlank() || expiry.isBlank()) return@Button
                            onSaveCard(
                                nickname,
                                bank,
                                cardType,
                                activeNetwork,
                                cardNumber,
                                expiry,
                                cardholderName,
                                colour,
                                notes,
                                cvv,
                                linkedAccountId,
                                favourite
                            )
                        }
                    ) {
                        Text("Save")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { newValue ->
                    cardNumber = newValue.filter { it.isDigit() }
                },
                label = { Text("Card Number") },
                trailingIcon = {
                    NetworkBadge(network = activeNetwork)
                },
                supportingText = {
                    if (cardNumber.isNotBlank() && !isLuhnValid) {
                        Text("Luhn check warning: number may be invalid", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Detected network: ${activeNetwork.displayName}")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = expiry,
                    onValueChange = { newValue ->
                        val digits = newValue.filter { it.isDigit() }
                        expiry = when {
                            digits.length >= 3 -> "${digits.take(2)}/${digits.drop(2).take(2)}"
                            else -> digits
                        }
                    },
                    label = { Text("Expiry (MM/YY)") },
                    placeholder = { Text("MM/YY") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = cvv,
                    onValueChange = { cvv = it.filter { ch -> ch.isDigit() }.take(4) },
                    label = { Text("CVV") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(cvvFocusRequester),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = cardholderName,
                onValueChange = { cardholderName = it },
                label = { Text("Cardholder Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Card Nickname (e.g. Salary Card)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            var expandedBank by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedBank,
                onExpandedChange = { expandedBank = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = bank,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Bank / Institution") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBank) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedBank,
                    onDismissRequest = { expandedBank = false }
                ) {
                    listOf("HDFC", "ICICI", "SBI", "AXIS", "Other").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                bank = option
                                expandedBank = false
                            }
                        )
                    }
                }
            }

            var expandedType by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = cardType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Card Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }
                ) {
                    CardType.entries.forEach { typeOption ->
                        DropdownMenuItem(
                            text = { Text(typeOption.displayName) },
                            onClick = {
                                cardType = typeOption
                                expandedType = false
                            }
                        )
                    }
                }
            }

            var expandedColour by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedColour,
                onExpandedChange = { expandedColour = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = colour,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Card Gradient Colour") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedColour) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedColour,
                    onDismissRequest = { expandedColour = false }
                ) {
                    listOf("GRADIENT_1", "GRADIENT_2", "GRADIENT_3", "GRADIENT_4", "GRADIENT_5", "GRADIENT_6").forEach { col ->
                        DropdownMenuItem(
                            text = { Text(col) },
                            onClick = {
                                colour = col
                                expandedColour = false
                            }
                        )
                    }
                }
            }

            val filteredAccounts = accounts.filter { acc ->
                if (cardType == CardType.CREDIT) acc.type == com.patil.coconut_pfa.data.model.AccountType.CREDIT_CARD
                else acc.type == com.patil.coconut_pfa.data.model.AccountType.BANK || acc.type == com.patil.coconut_pfa.data.model.AccountType.WALLET
            }

            var expandedAccount by remember { mutableStateOf(false) }
            val selectedAccount = accounts.find { it.id == linkedAccountId }

            ExposedDropdownMenuBox(
                expanded = expandedAccount,
                onExpandedChange = { expandedAccount = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedAccount?.name ?: "None (Link Account)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Linked Coconut Account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccount) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedAccount,
                    onDismissRequest = { expandedAccount = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = {
                            linkedAccountId = null
                            expandedAccount = false
                        }
                    )
                    filteredAccounts.forEach { acc ->
                        DropdownMenuItem(
                            text = { Text("${acc.name} (${acc.type.displayName})") },
                            onClick = {
                                linkedAccountId = acc.id
                                expandedAccount = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
