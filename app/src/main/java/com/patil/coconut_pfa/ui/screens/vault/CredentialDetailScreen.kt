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
import com.patil.coconut_pfa.data.local.entity.VaultItemType
import com.patil.coconut_pfa.data.model.*
import com.patil.coconut_pfa.ui.viewmodel.VaultCredentialsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialDetailScreen(
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
    val meta = uiState.selectedItemMeta
    val secret = uiState.selectedItemSecret

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(getMetaTitle(meta)) },
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
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = when (item.type) {
                                VaultItemType.LOGIN -> "Login / Website"
                                VaultItemType.BANK_LOGIN -> "Bank Login"
                                VaultItemType.NOTE -> "Secure Note"
                                VaultItemType.CARD -> "Card"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        when (item.type) {
                            VaultItemType.LOGIN -> {
                                val lMeta = meta as? LoginMeta
                                val lSecret = secret as? LoginSecret
                                if (lMeta != null) {
                                    DetailFieldRow(label = "Title", value = lMeta.title, onCopy = { onCopyField("Title", lMeta.title) })
                                    DetailFieldRow(label = "Username", value = lMeta.username, onCopy = { onCopyField("Username", lMeta.username) })
                                    DetailFieldRow(label = "Website", value = lMeta.website, onCopy = { onCopyField("Website", lMeta.website) })
                                }
                                if (lSecret != null) {
                                    val isRevealed = uiState.revealedFields["password"] == true
                                    SecretFieldRow(
                                        label = "Password",
                                        value = lSecret.password,
                                        isRevealed = isRevealed,
                                        onReveal = { onRevealField("password", 30) },
                                        onCopy = { onCopyField("Password", lSecret.password) }
                                    )
                                    if (lSecret.notes.isNotBlank()) {
                                        DetailFieldRow(label = "Notes", value = lSecret.notes, onCopy = { onCopyField("Notes", lSecret.notes) })
                                    }
                                    if (lSecret.passwordHistory.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Password History (Last ${lSecret.passwordHistory.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        lSecret.passwordHistory.forEachIndexed { idx, hist ->
                                            val histRevealed = uiState.revealedFields["history_$idx"] == true
                                            SecretFieldRow(
                                                label = "Previous Password ${idx + 1}",
                                                value = hist.password,
                                                isRevealed = histRevealed,
                                                onReveal = { onRevealField("history_$idx", 30) },
                                                onCopy = { onCopyField("Password History", hist.password) }
                                            )
                                        }
                                    }
                                }
                            }
                            VaultItemType.BANK_LOGIN -> {
                                val bMeta = meta as? BankLoginMeta
                                val bSecret = secret as? BankLoginSecret
                                if (bMeta != null) {
                                    DetailFieldRow(label = "Title", value = bMeta.title, onCopy = { onCopyField("Title", bMeta.title) })
                                    DetailFieldRow(label = "Bank", value = bMeta.bank, onCopy = { onCopyField("Bank", bMeta.bank) })
                                    DetailFieldRow(label = "Customer ID", value = bMeta.customerId, onCopy = { onCopyField("Customer ID", bMeta.customerId) })
                                }
                                if (bSecret != null) {
                                    val isRev1 = uiState.revealedFields["loginPassword"] == true
                                    SecretFieldRow(
                                        label = "Login Password",
                                        value = bSecret.loginPassword,
                                        isRevealed = isRev1,
                                        onReveal = { onRevealField("loginPassword", 30) },
                                        onCopy = { onCopyField("Login Password", bSecret.loginPassword) }
                                    )
                                    if (bSecret.transactionPassword.isNotBlank()) {
                                        val isRev2 = uiState.revealedFields["transactionPassword"] == true
                                        SecretFieldRow(
                                            label = "Transaction Password",
                                            value = bSecret.transactionPassword,
                                            isRevealed = isRev2,
                                            onReveal = { onRevealField("transactionPassword", 30) },
                                            onCopy = { onCopyField("Transaction Password", bSecret.transactionPassword) }
                                        )
                                    }
                                    if (bSecret.notes.isNotBlank()) {
                                        DetailFieldRow(label = "Notes", value = bSecret.notes, onCopy = { onCopyField("Notes", bSecret.notes) })
                                    }
                                }
                            }
                            VaultItemType.NOTE -> {
                                val nMeta = meta as? NoteMeta
                                val nSecret = secret as? NoteSecret
                                if (nMeta != null) {
                                    DetailFieldRow(label = "Title", value = nMeta.title, onCopy = { onCopyField("Title", nMeta.title) })
                                }
                                if (nSecret != null) {
                                    DetailFieldRow(label = "Note", value = nSecret.note, onCopy = { onCopyField("Note", nSecret.note) })
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

fun getMetaTitle(meta: Any?): String {
    return when (meta) {
        is LoginMeta -> meta.title
        is BankLoginMeta -> meta.title
        is NoteMeta -> meta.title
        is CardMeta -> meta.title
        else -> "Credential Detail"
    }
}

@Composable
fun DetailFieldRow(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onCopy) {
                Icon(imageVector = Icons.Rounded.ContentCopy, contentDescription = "Copy $label", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun SecretFieldRow(
    label: String,
    value: String,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onCopy: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isRevealed) value else "●●●●●●●●",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onReveal) {
                Icon(
                    imageVector = if (isRevealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = if (isRevealed) "Mask" else "Reveal",
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onCopy) {
                Icon(imageVector = Icons.Rounded.ContentCopy, contentDescription = "Copy $label", modifier = Modifier.size(20.dp))
            }
        }
    }
}
