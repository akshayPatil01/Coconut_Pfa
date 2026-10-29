package com.patil.coconut_pfa.ui.screens.vault

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.local.entity.VaultItemType
import com.patil.coconut_pfa.navigation.VaultTab
import com.patil.coconut_pfa.ui.viewmodel.VaultCredentialsViewModel
import com.patil.coconut_pfa.ui.viewmodel.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultMainScreen(
    initialTab: VaultTab = VaultTab.CREDENTIALS,
    vaultViewModel: VaultViewModel,
    credentialsViewModel: VaultCredentialsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val credentialsUiState by credentialsViewModel.uiState.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(if (initialTab == VaultTab.CARDS) 0 else 1) }
    val tabs = listOf("Payment Cards", "Credentials & Logins")

    var currentScreenMode by remember { mutableStateOf<VaultScreenMode>(VaultScreenMode.List) }

    LaunchedEffect(credentialsUiState.snackbarMessage) {
        credentialsUiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            credentialsViewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { vaultViewModel.updateLastTouchTime() },
                    onTap = { vaultViewModel.updateLastTouchTime() }
                )
            },
        topBar = {
            TopAppBar(
                title = { Text(if (selectedTabIndex == 0) "Vault Cards" else "Coconut Vault") },
                actions = {
                    if (currentScreenMode is VaultScreenMode.List) {
                        IconButton(onClick = { currentScreenMode = VaultScreenMode.RecentlyDeleted }) {
                            Icon(imageVector = Icons.Rounded.DeleteOutline, contentDescription = "Recently Deleted")
                        }
                    }
                    IconButton(onClick = { vaultViewModel.lockVault(context) }) {
                        Icon(imageVector = Icons.Rounded.Lock, contentDescription = "Lock Vault", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentScreenMode is VaultScreenMode.List) {
                FloatingActionButton(
                    onClick = {
                        if (selectedTabIndex == 0) {
                            currentScreenMode = VaultScreenMode.AddCard
                        } else {
                            currentScreenMode = VaultScreenMode.ChooseTypeForAdd
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            currentScreenMode = VaultScreenMode.List
                            vaultViewModel.updateLastTouchTime()
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (selectedTabIndex == 0) {
                    when (val mode = currentScreenMode) {
                        is VaultScreenMode.List -> {
                            CardsListTab(
                                uiState = credentialsUiState,
                                onSearchQueryChanged = credentialsViewModel::setSearchQuery,
                                onItemClick = { itemId ->
                                    credentialsViewModel.selectItem(itemId)
                                    currentScreenMode = VaultScreenMode.CardDetail(itemId)
                                },
                                onCopyCardNumber = { num -> credentialsViewModel.copyField(context, "Card Number", num) },
                                onCopyCvv = { cvv -> credentialsViewModel.copyField(context, "CVV", cvv) },
                                onToggleFavourite = credentialsViewModel::toggleFavourite,
                                onAddCardClick = { currentScreenMode = VaultScreenMode.AddCard }
                            )
                        }
                        is VaultScreenMode.CardDetail -> {
                            CardDetailScreen(
                                uiState = credentialsUiState,
                                onNavigateBack = {
                                    credentialsViewModel.selectItem(null)
                                    currentScreenMode = VaultScreenMode.List
                                },
                                onEdit = { itemId ->
                                    currentScreenMode = VaultScreenMode.EditCard(itemId)
                                },
                                onToggleFavourite = credentialsViewModel::toggleFavourite,
                                onMoveToTrash = { itemId ->
                                    credentialsViewModel.moveToRecentlyDeleted(itemId)
                                    currentScreenMode = VaultScreenMode.List
                                },
                                onRevealField = credentialsViewModel::revealField,
                                onCopyField = { label, value -> credentialsViewModel.copyField(context, label, value) }
                            )
                        }
                        is VaultScreenMode.AddCard -> {
                            CardAddEditScreen(
                                existingItem = null,
                                existingMeta = null,
                                existingSecret = null,
                                onNavigateBack = { currentScreenMode = VaultScreenMode.List },
                                onSaveCard = { title, holder, num, mm, yy, cvv, pin, addr, phone, notes, fav ->
                                    credentialsViewModel.saveCard(null, title, holder, num, mm, yy, cvv, pin, addr, phone, notes, fav)
                                    currentScreenMode = VaultScreenMode.List
                                },
                                onScanCardClick = { currentScreenMode = VaultScreenMode.ScanCard }
                            )
                        }
                        is VaultScreenMode.EditCard -> {
                            CardAddEditScreen(
                                existingItem = credentialsUiState.selectedItemDetail,
                                existingMeta = credentialsUiState.selectedItemMeta,
                                existingSecret = credentialsUiState.selectedItemSecret,
                                onNavigateBack = { currentScreenMode = VaultScreenMode.CardDetail(mode.itemId) },
                                onSaveCard = { title, holder, num, mm, yy, cvv, pin, addr, phone, notes, fav ->
                                    credentialsViewModel.saveCard(mode.itemId, title, holder, num, mm, yy, cvv, pin, addr, phone, notes, fav)
                                    currentScreenMode = VaultScreenMode.CardDetail(mode.itemId)
                                },
                                onScanCardClick = { currentScreenMode = VaultScreenMode.ScanCard }
                            )
                        }
                        is VaultScreenMode.ScanCard -> {
                            CardScanScreen(
                                onNavigateBack = { currentScreenMode = VaultScreenMode.AddCard },
                                onCardScanned = { num, mm, yy ->
                                    currentScreenMode = VaultScreenMode.AddCard
                                }
                            )
                        }
                        is VaultScreenMode.RecentlyDeleted -> {
                            RecentlyDeletedCredentialsScreen(
                                deletedItems = credentialsUiState.deletedItems,
                                onNavigateBack = { currentScreenMode = VaultScreenMode.List },
                                onRestore = credentialsViewModel::restoreItem,
                                onDeleteForever = credentialsViewModel::deleteForever
                            )
                        }
                        else -> {
                            CardsListTab(
                                uiState = credentialsUiState,
                                onSearchQueryChanged = credentialsViewModel::setSearchQuery,
                                onItemClick = { itemId ->
                                    credentialsViewModel.selectItem(itemId)
                                    currentScreenMode = VaultScreenMode.CardDetail(itemId)
                                },
                                onCopyCardNumber = { num -> credentialsViewModel.copyField(context, "Card Number", num) },
                                onCopyCvv = { cvv -> credentialsViewModel.copyField(context, "CVV", cvv) },
                                onToggleFavourite = credentialsViewModel::toggleFavourite,
                                onAddCardClick = { currentScreenMode = VaultScreenMode.AddCard }
                            )
                        }
                    }
                } else {
                    when (val mode = currentScreenMode) {
                        is VaultScreenMode.List -> {
                            CredentialsListTab(
                                uiState = credentialsUiState,
                                onSearchQueryChanged = credentialsViewModel::setSearchQuery,
                                onFilterChanged = credentialsViewModel::setFilter,
                                onSortChanged = credentialsViewModel::setSort,
                                onItemClick = { itemId ->
                                    credentialsViewModel.selectItem(itemId)
                                    currentScreenMode = VaultScreenMode.Detail(itemId)
                                },
                                onCopyUsername = { username -> credentialsViewModel.copyField(context, "Username", username) },
                                onCopyPassword = { password -> credentialsViewModel.copyField(context, "Password", password) },
                                onToggleFavourite = credentialsViewModel::toggleFavourite
                            )
                        }
                        is VaultScreenMode.Detail -> {
                            CredentialDetailScreen(
                                uiState = credentialsUiState,
                                onNavigateBack = {
                                    credentialsViewModel.selectItem(null)
                                    currentScreenMode = VaultScreenMode.List
                                },
                                onEdit = { itemId ->
                                    currentScreenMode = VaultScreenMode.Edit(itemId, credentialsUiState.selectedItemDetail?.type ?: VaultItemType.LOGIN)
                                },
                                onToggleFavourite = credentialsViewModel::toggleFavourite,
                                onMoveToTrash = { itemId ->
                                    credentialsViewModel.moveToRecentlyDeleted(itemId)
                                    currentScreenMode = VaultScreenMode.List
                                },
                                onRevealField = credentialsViewModel::revealField,
                                onCopyField = { label, value -> credentialsViewModel.copyField(context, label, value) }
                            )
                        }
                        is VaultScreenMode.Add -> {
                            CredentialAddEditScreen(
                                type = mode.type,
                                existingItem = null,
                                existingMeta = null,
                                existingSecret = null,
                                onNavigateBack = { currentScreenMode = VaultScreenMode.List },
                                onSaveLogin = { title, username, website, password, notes, tags, fav, fields, hiddens ->
                                    credentialsViewModel.saveLogin(null, title, username, website, password, notes, tags, fav, fields, hiddens)
                                    currentScreenMode = VaultScreenMode.List
                                },
                                onSaveBankLogin = { title, bank, custId, logPass, txPass, notes, linkedAcc, tags, fav, fields, hiddens ->
                                    credentialsViewModel.saveBankLogin(null, title, bank, custId, logPass, txPass, notes, linkedAcc, tags, fav, fields, hiddens)
                                    currentScreenMode = VaultScreenMode.List
                                },
                                onSaveNote = { title, noteText, tags, fav, fields, hiddens ->
                                    credentialsViewModel.saveNote(null, title, noteText, tags, fav, fields, hiddens)
                                    currentScreenMode = VaultScreenMode.List
                                },
                                onGeneratePassword = credentialsViewModel::generatePlaceholderPassword
                            )
                        }
                        is VaultScreenMode.Edit -> {
                            CredentialAddEditScreen(
                                type = mode.type,
                                existingItem = credentialsUiState.selectedItemDetail,
                                existingMeta = credentialsUiState.selectedItemMeta,
                                existingSecret = credentialsUiState.selectedItemSecret,
                                onNavigateBack = { currentScreenMode = VaultScreenMode.Detail(mode.itemId) },
                                onSaveLogin = { title, username, website, password, notes, tags, fav, fields, hiddens ->
                                    credentialsViewModel.saveLogin(mode.itemId, title, username, website, password, notes, tags, fav, fields, hiddens)
                                    currentScreenMode = VaultScreenMode.Detail(mode.itemId)
                                },
                                onSaveBankLogin = { title, bank, custId, logPass, txPass, notes, linkedAcc, tags, fav, fields, hiddens ->
                                    credentialsViewModel.saveBankLogin(mode.itemId, title, bank, custId, logPass, txPass, notes, linkedAcc, tags, fav, fields, hiddens)
                                    currentScreenMode = VaultScreenMode.Detail(mode.itemId)
                                },
                                onSaveNote = { title, noteText, tags, fav, fields, hiddens ->
                                    credentialsViewModel.saveNote(mode.itemId, title, noteText, tags, fav, fields, hiddens)
                                    currentScreenMode = VaultScreenMode.Detail(mode.itemId)
                                },
                                onGeneratePassword = credentialsViewModel::generatePlaceholderPassword
                            )
                        }
                        is VaultScreenMode.ChooseTypeForAdd -> {
                            ChooseTypeDialog(
                                onDismiss = { currentScreenMode = VaultScreenMode.List },
                                onTypeSelected = { type ->
                                    currentScreenMode = VaultScreenMode.Add(type)
                                }
                            )
                        }
                        is VaultScreenMode.RecentlyDeleted -> {
                            RecentlyDeletedCredentialsScreen(
                                deletedItems = credentialsUiState.deletedItems,
                                onNavigateBack = { currentScreenMode = VaultScreenMode.List },
                                onRestore = credentialsViewModel::restoreItem,
                                onDeleteForever = credentialsViewModel::deleteForever
                            )
                        }
                        else -> {
                            CredentialsListTab(
                                uiState = credentialsUiState,
                                onSearchQueryChanged = credentialsViewModel::setSearchQuery,
                                onFilterChanged = credentialsViewModel::setFilter,
                                onSortChanged = credentialsViewModel::setSort,
                                onItemClick = { itemId ->
                                    credentialsViewModel.selectItem(itemId)
                                    currentScreenMode = VaultScreenMode.Detail(itemId)
                                },
                                onCopyUsername = { username -> credentialsViewModel.copyField(context, "Username", username) },
                                onCopyPassword = { password -> credentialsViewModel.copyField(context, "Password", password) },
                                onToggleFavourite = credentialsViewModel::toggleFavourite
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed interface VaultScreenMode {
    data object List : VaultScreenMode
    data class Detail(val itemId: String) : VaultScreenMode
    data class Add(val type: VaultItemType) : VaultScreenMode
    data class Edit(val itemId: String, val type: VaultItemType) : VaultScreenMode
    data class CardDetail(val itemId: String) : VaultScreenMode
    data object AddCard : VaultScreenMode
    data class EditCard(val itemId: String) : VaultScreenMode
    data object ScanCard : VaultScreenMode
    data object ChooseTypeForAdd : VaultScreenMode
    data object RecentlyDeleted : VaultScreenMode
}

@Composable
fun ChooseTypeDialog(
    onDismiss: () -> Unit,
    onTypeSelected: (VaultItemType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Credential", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onTypeSelected(VaultItemType.LOGIN) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Rounded.Key, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Login / Website")
                }
                Button(
                    onClick = { onTypeSelected(VaultItemType.BANK_LOGIN) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Rounded.AccountBalance, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bank Login")
                }
                Button(
                    onClick = { onTypeSelected(VaultItemType.NOTE) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Rounded.Note, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Secure Note")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
