package com.patil.coconut_pfa.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.EmptyState
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.viewmodel.TrashUiState
import com.patil.coconut_pfa.ui.viewmodel.TrashViewModel
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.launch

@Composable
fun TrashScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrashViewModel? = null
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf(TrashUiState(isLoading = false))
    }

    var showEmptyTrashDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate(Screen.Settings) }) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Trash & Data Recovery",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Items purged after 30 days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (uiState.trashTxns.isNotEmpty()) {
                    TextButton(onClick = { showEmptyTrashDialog = true }) {
                        Icon(imageVector = Icons.Rounded.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Empty Trash", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (uiState.trashTxns.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        title = "Trash is Empty",
                        description = "Deleted accounts, transactions, and bills will stay here for 30 days before being permanently erased.",
                        icon = Icons.Rounded.DeleteSweep,
                        actionLabel = "Back to Settings",
                        onActionClick = { onNavigate(Screen.Settings) }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.trashTxns, key = { it.id }) { tx ->
                        val accountName = tx.accountId?.let { uiState.accountMap[it]?.name }
                        val categoryName = tx.categoryId?.let { uiState.categoryMap[it]?.name } ?: tx.categoryId
                        val title = tx.payee ?: categoryName ?: "Transaction"

                        val deletedAt = tx.deletedAt ?: System.currentTimeMillis()
                        val daysPassed = ((System.currentTimeMillis() - deletedAt) / (86400 * 1000L)).toInt()
                        val daysLeft = maxOf(0, 30 - daysPassed)
                        val deletedDateStr = KolkataDateTimeUtils.epochMillisToDateString(deletedAt)

                        TrashTxnCard(
                            tx = tx,
                            title = title,
                            deletedDate = deletedDateStr,
                            itemType = "Transaction • ${tx.type.name}" + (accountName?.let { " • $it" } ?: ""),
                            daysLeft = daysLeft,
                            onRestore = {
                                viewModel?.restore(tx.id)
                                scope.launch { snackbarHostState.showSnackbar("Item restored successfully!") }
                            },
                            onDeleteForever = {
                                viewModel?.deleteForever(tx)
                                scope.launch { snackbarHostState.showSnackbar("Item permanently deleted") }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = { Text("Empty Trash?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete all ${uiState.trashTxns.size} items in the trash. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel?.emptyTrash()
                        showEmptyTrashDialog = false
                        scope.launch { snackbarHostState.showSnackbar("Trash emptied") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Forever")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TrashTxnCard(
    tx: TxnEntity,
    title: String,
    deletedDate: String,
    itemType: String,
    daysLeft: Int,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Deleted $deletedDate • $itemType",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "$daysLeft days left",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            AmountText(amountInPaise = tx.amountPaise, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRestore,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restore")
                }

                Button(
                    onClick = onDeleteForever,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Rounded.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Forever")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TrashScreenPreview() {
    Coconut_PfaTheme {
        TrashScreen(onNavigate = {})
    }
}
