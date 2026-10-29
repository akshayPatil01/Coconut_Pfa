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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.local.entity.VaultItem
import com.patil.coconut_pfa.data.local.entity.VaultItemType
import com.patil.coconut_pfa.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialAddEditScreen(
    type: VaultItemType,
    existingItem: VaultItem?,
    existingMeta: Any?,
    existingSecret: Any?,
    onNavigateBack: () -> Unit,
    onSaveLogin: (String, String, String, String, String, List<String>, Boolean, List<Pair<String, String>>, List<Boolean>) -> Unit,
    onSaveBankLogin: (String, String, String, String, String, String, String?, List<String>, Boolean, List<Pair<String, String>>, List<Boolean>) -> Unit,
    onSaveNote: (String, String, List<String>, Boolean, List<Pair<String, String>>, List<Boolean>) -> Unit,
    onGeneratePassword: () -> String,
    modifier: Modifier = Modifier
) {
    val isEdit = existingItem != null

    var title by remember {
        mutableStateOf(
            when (existingMeta) {
                is LoginMeta -> existingMeta.title
                is BankLoginMeta -> existingMeta.title
                is NoteMeta -> existingMeta.title
                else -> ""
            }
        )
    }

    var username by remember {
        mutableStateOf((existingMeta as? LoginMeta)?.username ?: "")
    }
    var website by remember {
        mutableStateOf((existingMeta as? LoginMeta)?.website ?: "")
    }
    var password by remember {
        mutableStateOf(
            when (existingSecret) {
                is LoginSecret -> existingSecret.password
                is BankLoginSecret -> existingSecret.loginPassword
                else -> ""
            }
        )
    }
    var passwordVisible by remember { mutableStateOf(false) }

    var notes by remember {
        mutableStateOf(
            when (existingSecret) {
                is LoginSecret -> existingSecret.notes
                is BankLoginSecret -> existingSecret.notes
                else -> ""
            }
        )
    }

    var bank by remember {
        mutableStateOf((existingMeta as? BankLoginMeta)?.bank ?: "HDFC")
    }
    var customerId by remember {
        mutableStateOf((existingMeta as? BankLoginMeta)?.customerId ?: "")
    }
    var transactionPassword by remember {
        mutableStateOf((existingSecret as? BankLoginSecret)?.transactionPassword ?: "")
    }
    var linkedAccountId by remember {
        mutableStateOf(existingItem?.linkedAccountId)
    }

    var noteText by remember {
        mutableStateOf((existingSecret as? NoteSecret)?.note ?: "")
    }

    var favourite by remember { mutableStateOf(existingItem?.favourite ?: false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Credential" else "Add Credential") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (title.isBlank()) return@Button
                            when (type) {
                                VaultItemType.LOGIN -> {
                                    onSaveLogin(title, username, website, password, notes, emptyList(), favourite, emptyList(), emptyList())
                                }
                                VaultItemType.BANK_LOGIN -> {
                                    onSaveBankLogin(title, bank, customerId, password, transactionPassword, notes, linkedAccountId, emptyList(), favourite, emptyList(), emptyList())
                                }
                                VaultItemType.NOTE -> {
                                    onSaveNote(title, noteText, emptyList(), favourite, emptyList(), emptyList())
                                }
                                else -> {}
                            }
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
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            when (type) {
                VaultItemType.LOGIN -> {
                    item {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username / Email") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = website,
                            onValueChange = { website = it },
                            label = { Text("Website URL") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                    TextButton(onClick = { password = onGeneratePassword() }) {
                                        Text("Generate")
                                    }
                                }
                            }
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
                VaultItemType.BANK_LOGIN -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Never store your UPI PIN, ATM PIN or OTPs. No bank will ever ask for them.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = bank,
                            onValueChange = { bank = it },
                            label = { Text("Bank / Institution") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = customerId,
                            onValueChange = { customerId = it },
                            label = { Text("Customer ID / User ID") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Login Password *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = transactionPassword,
                            onValueChange = { transactionPassword = it },
                            label = { Text("Transaction Password (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false)
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
                VaultItemType.NOTE -> {
                    item {
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            label = { Text("Secure Note Content *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 5
                        )
                    }
                }
                else -> {}
            }
        }
    }
}
