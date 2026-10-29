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
import com.patil.coconut_pfa.ui.viewmodel.*
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultMainScreen(
    initialTab: VaultTab = VaultTab.CREDENTIALS,
    vaultViewModel: VaultViewModel,
    credentialsViewModel: VaultCredentialsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAccount: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val credentialsUiState by credentialsViewModel.uiState.collectAsState()
    val generatedPassword by credentialsViewModel.generatedPassword.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(if (initialTab == VaultTab.CARDS) 0 else 1) }
    val tabs = listOf("Payment Cards", "Credentials & Logins")

    var currentScreenMode by remember { mutableStateOf<VaultScreenMode>(if (initialTab == VaultTab.CARDS) VaultScreenMode.CardList else VaultScreenMode.List) }

    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            credentialsViewModel.prepareCsvImport(context, uri)
        }
    }

    LaunchedEffect(credentialsUiState.snackbarMessage) {
        credentialsUiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            credentialsViewModel.clearSnackbar()
        }
    }

    if (credentialsUiState.showPasswordGenerator) {
        PasswordGeneratorBottomSheet(
            onDismiss = credentialsViewModel::dismissPasswordGenerator,
            onUsePassword = { generated ->
                credentialsViewModel.setGeneratedPassword(generated)
            }
        )
    }

    if (credentialsUiState.showHealthDialog && credentialsUiState.healthReport != null) {
        PasswordHealthDialog(
            report = credentialsUiState.healthReport!!,
            onDismiss = credentialsViewModel::dismissHealthDialog,
            onItemClick = { itemId ->
                currentScreenMode = VaultScreenMode.Detail(itemId)
            }
        )
    }

    if (credentialsUiState.showImportPreviewDialog && credentialsUiState.importPreviewData != null) {
        val preview = credentialsUiState.importPreviewData!!
        AlertDialog(
            onDismissRequest = credentialsViewModel::dismissImportPreview,
            title = { Text("Import Logins from CSV") },
            text = {
                Column {
                    Text("Found ${preview.totalCount} logins in the selected CSV file.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("First items (passwords hidden):", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    preview.previewTitles.forEach { title ->
                        Text("• $title")
                    }
                    if (preview.totalCount > 5) {
                        Text("... and ${preview.totalCount - 5} more")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Duplicates (same title + username) will be skipped automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        credentialsViewModel.executeCsvImport()
                    }
                ) {
                    Text("Import ${preview.totalCount} Logins")
                }
            },
            dismissButton = {
                TextButton(onClick = credentialsViewModel::dismissImportPreview) {
                    Text("Cancel")
                }
            }
        )
    }

    if (credentialsUiState.importSuccessCount != null) {
        val count = credentialsUiState.importSuccessCount!!
        val fileUri = credentialsUiState.importFileUri
        AlertDialog(
            onDismissRequest = credentialsViewModel::dismissImportSuccess,
            title = { Text("Import Complete") },
            text = {
                Column {
                    Text("Imported $count logins. This CSV holds your passwords in plain text - delete it now.")
                    if (credentialsUiState.importDeleteErrorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            credentialsUiState.importDeleteErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                if (fileUri != null) {
                    Button(
                        onClick = {
                            credentialsViewModel.deleteCsvFile(context, fileUri)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete file")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = credentialsViewModel::dismissImportSuccess) {
                    Text("Done")
                }
            }
        )
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
                    if (selectedTabIndex == 1 && currentScreenMode is VaultScreenMode.List) {
                        IconButton(onClick = {
                            csvPickerLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain", "*/*"))
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.UploadFile,
                                contentDescription = "Import CSV Logins"
                            )
                        }
                        IconButton(onClick = {
                            scope.launch {
                                val report = credentialsViewModel.computePasswordHealth()
                                credentialsViewModel.showHealthDialog(report)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = "Password Health Check"
                            )
                        }
                        IconButton(onClick = credentialsViewModel::showPasswordGenerator) {
                            Icon(
                                imageVector = Icons.Rounded.VpnKey,
                                contentDescription = "Password Generator"
                            )
                        }
                        IconButton(onClick = { currentScreenMode = VaultScreenMode.RecentlyDeleted }) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Recently Deleted"
                            )
                        }
                    }
                    IconButton(onClick = { vaultViewModel.lockVault(context) }) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Lock Vault",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0 && (currentScreenMode is VaultScreenMode.CardList || currentScreenMode is VaultScreenMode.List)) {
                FloatingActionButton(
                    onClick = { currentScreenMode = VaultScreenMode.ChooseCardAddMethod },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Card")
                }
            } else if (selectedTabIndex == 1 && currentScreenMode is VaultScreenMode.List) {
                FloatingActionButton(
                    onClick = { currentScreenMode = VaultScreenMode.ChooseTypeForAdd },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Credential")
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
                            currentScreenMode = if (index == 0) VaultScreenMode.CardList else VaultScreenMode.List
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
                        is VaultScreenMode.CardList, is VaultScreenMode.List -> {
                            CardsListTab(
                                cards = credentialsUiState.cards,
                                onCardClick = { itemId ->
                                    credentialsViewModel.selectItem(itemId)
                                    currentScreenMode = VaultScreenMode.CardDetail(itemId)
                                },
                                onAddCardClick = {
                                    currentScreenMode = VaultScreenMode.ChooseCardAddMethod
                                }
                            )
                        }
                        is VaultScreenMode.CardDetail -> {
                            CardDetailScreen(
                                uiState = credentialsUiState,
                                onNavigateBack = {
                                    credentialsViewModel.selectItem(null)
                                    currentScreenMode = VaultScreenMode.CardList
                                },
                                onEdit = { itemId ->
                                    currentScreenMode = VaultScreenMode.CardEdit(itemId)
                                },
                                onToggleFavourite = credentialsViewModel::toggleFavourite,
                                onMoveToTrash = { itemId ->
                                    credentialsViewModel.moveToRecentlyDeleted(itemId)
                                    currentScreenMode = VaultScreenMode.CardList
                                },
                                onRevealField = credentialsViewModel::revealField,
                                onCopyField = { label, value, customMsg -> credentialsViewModel.copyField(context, label, value, customMsg) },
                                onOpenAccount = onNavigateToAccount
                            )
                        }
                        is VaultScreenMode.CardAdd -> {
                            CardAddEditScreen(
                                existingItem = null,
                                existingMeta = null,
                                existingSecret = null,
                                accounts = credentialsUiState.accounts,
                                onNavigateBack = { currentScreenMode = VaultScreenMode.CardList },
                                onSaveCard = { nickname, bank, cardType, network, cardNumber, expiry, cardholderName, colour, notes, cvv, linkedAccountId, favourite ->
                                    credentialsViewModel.saveCard(null, nickname, bank, null, cardType, network, cardNumber, expiry, cardholderName, colour, notes, cvv, linkedAccountId, favourite)
                                    currentScreenMode = VaultScreenMode.CardList
                                }
                            )
                        }
                        is VaultScreenMode.CardEdit -> {
                            CardAddEditScreen(
                                existingItem = credentialsUiState.selectedItemDetail,
                                existingMeta = credentialsUiState.selectedCardMeta,
                                existingSecret = credentialsUiState.selectedCardSecret,
                                accounts = credentialsUiState.accounts,
                                onNavigateBack = { currentScreenMode = VaultScreenMode.CardDetail(mode.itemId) },
                                onSaveCard = { nickname, bank, cardType, network, cardNumber, expiry, cardholderName, colour, notes, cvv, linkedAccountId, favourite ->
                                    credentialsViewModel.saveCard(mode.itemId, nickname, bank, null, cardType, network, cardNumber, expiry, cardholderName, colour, notes, cvv, linkedAccountId, favourite)
                                    currentScreenMode = VaultScreenMode.CardDetail(mode.itemId)
                                }
                            )
                        }
                        is VaultScreenMode.ChooseCardAddMethod -> {
                            CardsListTab(
                                cards = credentialsUiState.cards,
                                onCardClick = { itemId ->
                                    credentialsViewModel.selectItem(itemId)
                                    currentScreenMode = VaultScreenMode.CardDetail(itemId)
                                },
                                onAddCardClick = { currentScreenMode = VaultScreenMode.ChooseCardAddMethod }
                            )
                            AlertDialog(
                                onDismissRequest = { currentScreenMode = VaultScreenMode.CardList },
                                title = { Text("Add Payment Card") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                currentScreenMode = VaultScreenMode.CardList
                                                credentialsViewModel.copyField(context, "Scanner", "Card scanner coming in Phase 16 Step 3")
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Scan Card (Placeholder)")
                                        }
                                        Button(
                                            onClick = {
                                                currentScreenMode = VaultScreenMode.CardAdd
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Enter Manually")
                                        }
                                    }
                                },
                                confirmButton = {},
                                dismissButton = {
                                    TextButton(onClick = { currentScreenMode = VaultScreenMode.CardList }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                        else -> {
                            CardsListTab(
                                cards = credentialsUiState.cards,
                                onCardClick = { itemId ->
                                    credentialsViewModel.selectItem(itemId)
                                    currentScreenMode = VaultScreenMode.CardDetail(itemId)
                                },
                                onAddCardClick = { currentScreenMode = VaultScreenMode.ChooseCardAddMethod }
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
                                onCopyUsername = { username ->
                                    credentialsViewModel.copyField(context, "Username", username)
                                },
                                onCopyPassword = { password ->
                                    credentialsViewModel.copyField(context, "Password", password)
                                },
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
                                onGeneratePassword = credentialsViewModel::generatePlaceholderPassword,
                                onOpenGenerator = credentialsViewModel::showPasswordGenerator,
                                generatedPassword = generatedPassword,
                                onClearGeneratedPassword = credentialsViewModel::clearGeneratedPassword
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
                                onGeneratePassword = credentialsViewModel::generatePlaceholderPassword,
                                onOpenGenerator = credentialsViewModel::showPasswordGenerator,
                                generatedPassword = generatedPassword,
                                onClearGeneratedPassword = credentialsViewModel::clearGeneratedPassword
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
                                onCopyUsername = { username ->
                                    credentialsViewModel.copyField(context, "Username", username)
                                },
                                onCopyPassword = { password ->
                                    credentialsViewModel.copyField(context, "Password", password)
                                },
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
    data object ChooseTypeForAdd : VaultScreenMode
    data object RecentlyDeleted : VaultScreenMode

    data object CardList : VaultScreenMode
    data class CardDetail(val itemId: String) : VaultScreenMode
    data object CardAdd : VaultScreenMode
    data class CardEdit(val itemId: String) : VaultScreenMode
    data object ChooseCardAddMethod : VaultScreenMode
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
