package com.patil.coconut_pfa.ui.screens.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.local.entity.VaultItem
import com.patil.coconut_pfa.data.model.CardMeta
import com.patil.coconut_pfa.data.model.CardSecret

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardAddEditScreen(
    existingItem: VaultItem?,
    existingMeta: Any?,
    existingSecret: Any?,
    onNavigateBack: () -> Unit,
    onSaveCard: (String, String, String, String, String, String, String, String, String, String, Boolean) -> Unit,
    onScanCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEdit = existingItem != null

    var title by remember { mutableStateOf((existingMeta as? CardMeta)?.title ?: "") }
    var cardHolderName by remember { mutableStateOf((existingMeta as? CardMeta)?.cardHolderName ?: "") }
    var cardNumber by remember { mutableStateOf((existingSecret as? CardSecret)?.cardNumber ?: "") }
    var expiryMonth by remember { mutableStateOf((existingMeta as? CardMeta)?.expiryMonth ?: "") }
    var expiryYear by remember { mutableStateOf((existingMeta as? CardMeta)?.expiryYear ?: "") }
    var cvv by remember { mutableStateOf((existingSecret as? CardSecret)?.cvv ?: "") }
    var pin by remember { mutableStateOf((existingSecret as? CardSecret)?.pin ?: "") }
    var billingAddress by remember { mutableStateOf((existingSecret as? CardSecret)?.billingAddress ?: "") }
    var phone by remember { mutableStateOf((existingSecret as? CardSecret)?.phone ?: "") }
    var notes by remember { mutableStateOf((existingSecret as? CardSecret)?.notes ?: "") }
    var favourite by remember { mutableStateOf(existingItem?.favourite ?: false) }

    var cvvVisible by remember { mutableStateOf(false) }
    var pinVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Payment Card" else "Add Payment Card") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onScanCardClick) {
                        Icon(imageVector = Icons.Rounded.CameraAlt, contentDescription = "Scan Card")
                    }
                    Button(
                        onClick = {
                            if (title.isBlank() || cardNumber.isBlank()) return@Button
                            onSaveCard(title, cardHolderName, cardNumber, expiryMonth, expiryYear, cvv, pin, billingAddress, phone, notes, favourite)
                        }
                    ) {
                        Text("Save")
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
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Card Title (e.g. HDFC Regalia) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = cardHolderName,
                    onValueChange = { cardHolderName = it },
                    label = { Text("Cardholder Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Card Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, autoCorrectEnabled = false)
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = expiryMonth,
                        onValueChange = { expiryMonth = it },
                        label = { Text("MM") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = expiryYear,
                        onValueChange = { expiryYear = it },
                        label = { Text("YY") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { cvv = it },
                        label = { Text("CVV") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (cvvVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                        trailingIcon = {
                            IconButton(onClick = { cvvVisible = !cvvVisible }) {
                                Icon(imageVector = if (cvvVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = null)
                            }
                        }
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("ATM PIN (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                    trailingIcon = {
                        IconButton(onClick = { pinVisible = !pinVisible }) {
                            Icon(imageVector = if (pinVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = null)
                        }
                    }
                )
            }
            item {
                OutlinedTextField(
                    value = billingAddress,
                    onValueChange = { billingAddress = it },
                    label = { Text("Billing Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )
            }
            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )
            }
        }
    }
}
