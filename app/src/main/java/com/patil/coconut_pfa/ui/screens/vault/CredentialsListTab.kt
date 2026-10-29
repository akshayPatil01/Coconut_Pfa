package com.patil.coconut_pfa.ui.screens.vault

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patil.coconut_pfa.data.local.entity.VaultItemType
import com.patil.coconut_pfa.ui.viewmodel.CredentialDisplayItem
import com.patil.coconut_pfa.ui.viewmodel.CredentialFilter
import com.patil.coconut_pfa.ui.viewmodel.CredentialSort
import com.patil.coconut_pfa.ui.viewmodel.VaultCredentialsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsListTab(
    uiState: VaultCredentialsUiState,
    onSearchQueryChanged: (String) -> Unit,
    onFilterChanged: (CredentialFilter) -> Unit,
    onSortChanged: (CredentialSort) -> Unit,
    onItemClick: (String) -> Unit,
    onCopyUsername: (String) -> Unit,
    onCopyPassword: (String) -> Unit,
    onToggleFavourite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var contextMenuId by remember { mutableStateOf<String?>(null) }
    var contextMenuUsername by remember { mutableStateOf<String?>(null) }

    val filteredItems = remember(uiState.items, uiState.searchQuery, uiState.activeFilter, uiState.activeSort) {
        var list = uiState.items.filter { item ->
            val matchesSearch = uiState.searchQuery.isBlank() ||
                    item.title.contains(uiState.searchQuery, ignoreCase = true) ||
                    item.subtitle.contains(uiState.searchQuery, ignoreCase = true) ||
                    item.metaJson.contains(uiState.searchQuery, ignoreCase = true)

            val matchesFilter = when (uiState.activeFilter) {
                CredentialFilter.ALL -> true
                CredentialFilter.FAVOURITES -> item.favourite
                CredentialFilter.BANK -> item.type == VaultItemType.BANK_LOGIN
                CredentialFilter.LOGIN -> item.type == VaultItemType.LOGIN
                CredentialFilter.NOTES -> item.type == VaultItemType.NOTE
                CredentialFilter.CARDS -> item.type == VaultItemType.CARD
            }
            matchesSearch && matchesFilter
        }

        list = when (uiState.activeSort) {
            CredentialSort.NAME -> list.sortedWith(compareByDescending<CredentialDisplayItem> { it.favourite }.thenBy { it.title })
            CredentialSort.RECENTLY_USED -> list.sortedWith(compareByDescending<CredentialDisplayItem> { it.favourite }.thenByDescending { it.lastUsedAt ?: it.updatedAt })
        }
        list
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search logins, banks, notes...") },
            leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(imageVector = Icons.Rounded.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScrollableTabRow(
                selectedTabIndex = when (uiState.activeFilter) {
                    CredentialFilter.ALL -> 0
                    CredentialFilter.FAVOURITES -> 1
                    CredentialFilter.BANK -> 2
                    CredentialFilter.LOGIN -> 3
                    CredentialFilter.NOTES -> 4
                    CredentialFilter.CARDS -> 5
                },
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                FilterChip(
                    selected = uiState.activeFilter == CredentialFilter.ALL,
                    onClick = { onFilterChanged(CredentialFilter.ALL) },
                    label = { Text("All") }
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = uiState.activeFilter == CredentialFilter.FAVOURITES,
                    onClick = { onFilterChanged(CredentialFilter.FAVOURITES) },
                    label = { Text("★ Favourites") }
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = uiState.activeFilter == CredentialFilter.BANK,
                    onClick = { onFilterChanged(CredentialFilter.BANK) },
                    label = { Text("Bank") }
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = uiState.activeFilter == CredentialFilter.LOGIN,
                    onClick = { onFilterChanged(CredentialFilter.LOGIN) },
                    label = { Text("Login") }
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = uiState.activeFilter == CredentialFilter.NOTES,
                    onClick = { onFilterChanged(CredentialFilter.NOTES) },
                    label = { Text("Notes") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    val nextSort = if (uiState.activeSort == CredentialSort.NAME) CredentialSort.RECENTLY_USED else CredentialSort.NAME
                    onSortChanged(nextSort)
                }
            ) {
                Icon(imageVector = Icons.Rounded.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sort: ${if (uiState.activeSort == CredentialSort.NAME) "Name" else "Recently Used"}")
            }
        }

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No credentials found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems, key = { it.item.id }) { item ->
                    CredentialRowCard(
                        item = item,
                        onClick = { onItemClick(item.item.id) },
                        onLongClick = {
                            contextMenuId = item.item.id
                            contextMenuUsername = item.subtitle
                        },
                        onToggleFavourite = { onToggleFavourite(item.item.id) }
                    )
                }
            }
        }

        if (contextMenuId != null) {
            AlertDialog(
                onDismissRequest = { contextMenuId = null },
                title = { Text("Credential Options") },
                text = { Text("Choose an action for this item.") },
                confirmButton = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (!contextMenuUsername.isNullOrBlank()) {
                            Button(
                                onClick = {
                                    val un = contextMenuUsername ?: ""
                                    contextMenuId = null
                                    onCopyUsername(un)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Copy Username / ID")
                            }
                        }
                        Button(
                            onClick = {
                                val id = contextMenuId ?: ""
                                contextMenuId = null
                                onItemClick(id)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Detail & Password")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { contextMenuId = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CredentialRowCard(
    item: CredentialDisplayItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavourite: () -> Unit
) {
    val avatarColor = getAvatarColor(item.title)
    val firstLetter = item.title.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = avatarColor
                ) {}
                Text(
                    text = firstLetter,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    if (item.favourite) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "Favourite",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                if (item.subtitle.isNotBlank()) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Text(
                    text = when (item.type) {
                        VaultItemType.LOGIN -> "Login"
                        VaultItemType.BANK_LOGIN -> "Bank Login"
                        VaultItemType.NOTE -> "Secure Note"
                        VaultItemType.CARD -> "Card"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onToggleFavourite) {
                Icon(
                    imageVector = if (item.favourite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    contentDescription = "Favourite",
                    tint = if (item.favourite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun getAvatarColor(title: String): Color {
    val hash = title.hashCode()
    val colors = listOf(
        Color(0xFFE57373),
        Color(0xFF81C784),
        Color(0xFF64B5F6),
        Color(0xFFFFB74D),
        Color(0xFFBA68C8),
        Color(0xFF4DB6AC)
    )
    return colors[Math.abs(hash) % colors.size]
}
