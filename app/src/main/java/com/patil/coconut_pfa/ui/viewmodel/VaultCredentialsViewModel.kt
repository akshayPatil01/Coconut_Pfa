package com.patil.coconut_pfa.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patil.coconut_pfa.data.local.entity.VaultItem
import com.patil.coconut_pfa.data.local.entity.VaultItemType
import com.patil.coconut_pfa.data.local.security.VaultClipboardManager
import com.patil.coconut_pfa.data.local.security.VaultSession
import com.patil.coconut_pfa.data.model.*
import com.patil.coconut_pfa.data.repository.VaultRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import com.patil.coconut_pfa.vault.PasswordHealthAnalyzer
import com.patil.coconut_pfa.vault.PasswordHealthReport

data class CredentialDisplayItem(
    val item: VaultItem,
    val title: String,
    val subtitle: String, // username, customerId, or card last4/network
    val metaJson: String,
    val type: VaultItemType,
    val favourite: Boolean,
    val lastUsedAt: Long?,
    val updatedAt: Long,
    val deletedAt: Long?
)

data class CardDisplayItem(
    val item: VaultItem,
    val meta: CardMeta,
    val updatedAt: Long,
    val deletedAt: Long?
)

enum class CredentialFilter {
    ALL, FAVOURITES, BANK, LOGIN, NOTES, CARDS
}

enum class CredentialSort {
    NAME, RECENTLY_USED
}

data class ParsedLoginRecord(
    val title: String,
    val username: String,
    val url: String,
    val password: String,
    val notes: String
)

data class CredentialImportPreview(
    val totalCount: Int,
    val previewTitles: List<String>,
    val records: List<ParsedLoginRecord>
)

data class VaultCredentialsUiState(
    val items: List<CredentialDisplayItem> = emptyList(),
    val deletedItems: List<CredentialDisplayItem> = emptyList(),
    val cards: List<CardDisplayItem> = emptyList(),
    val deletedCards: List<CardDisplayItem> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: CredentialFilter = CredentialFilter.ALL,
    val activeSort: CredentialSort = CredentialSort.NAME,
    val selectedItemId: String? = null,
    val selectedItemDetail: VaultItem? = null,
    val selectedItemMeta: Any? = null,
    val selectedItemSecret: Any? = null,
    val selectedCardMeta: CardMeta? = null,
    val selectedCardSecret: CardSecret? = null,
    val revealedFields: Map<String, Boolean> = emptyMap(),
    val snackbarMessage: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showHealthDialog: Boolean = false,
    val healthReport: PasswordHealthReport? = null,
    val showPasswordGenerator: Boolean = false,
    val showImportPreviewDialog: Boolean = false,
    val importPreviewData: CredentialImportPreview? = null,
    val importFileUri: android.net.Uri? = null,
    val importSuccessCount: Int? = null,
    val importDeleteErrorMessage: String? = null
)

class VaultCredentialsViewModel(
    private val vaultRepository: VaultRepository
) : ViewModel() {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val loginMetaAdapter = moshi.adapter(LoginMeta::class.java)
    private val loginSecretAdapter = moshi.adapter(LoginSecret::class.java)
    private val bankLoginMetaAdapter = moshi.adapter(BankLoginMeta::class.java)
    private val bankLoginSecretAdapter = moshi.adapter(BankLoginSecret::class.java)
    private val noteMetaAdapter = moshi.adapter(NoteMeta::class.java)
    private val noteSecretAdapter = moshi.adapter(NoteSecret::class.java)
    private val cardMetaAdapter = moshi.adapter(CardMeta::class.java)
    private val cardSecretAdapter = moshi.adapter(CardSecret::class.java)

    private val _uiState = MutableStateFlow(VaultCredentialsUiState())
    val uiState: StateFlow<VaultCredentialsUiState> = _uiState.asStateFlow()

    private val _generatedPassword = MutableStateFlow<String?>(null)
    val generatedPassword: StateFlow<String?> = _generatedPassword.asStateFlow()

    private val revealJobs = mutableMapOf<String, Job>()

    init {
        observeItems()
    }

    private fun observeItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            vaultRepository.getAllItemsFlow().collectLatest { rawItems ->
                if (VaultSession.isLocked) {
                    _uiState.value = _uiState.value.copy(items = emptyList(), deletedItems = emptyList(), isLoading = false)
                    return@collectLatest
                }

                val displayItems = mutableListOf<CredentialDisplayItem>()
                val deletedDisplayItems = mutableListOf<CredentialDisplayItem>()

                for (item in rawItems) {
                    try {
                        val metaJson = vaultRepository.readMeta(item)
                        val titleAndSubtitle = parseTitleAndSubtitle(item.type, metaJson)
                        val displayItem = CredentialDisplayItem(
                            item = item,
                            title = titleAndSubtitle.first,
                            subtitle = titleAndSubtitle.second,
                            metaJson = metaJson,
                            type = item.type,
                            favourite = item.favourite,
                            lastUsedAt = item.lastUsedAt,
                            updatedAt = item.updatedAt,
                            deletedAt = item.deletedAt
                        )
                        if (item.deletedAt != null) {
                            deletedDisplayItems.add(displayItem)
                        } else {
                            displayItems.add(displayItem)
                        }
                    } catch (e: Exception) {
                        // Skip unreadable items
                    }
                }

                _uiState.value = _uiState.value.copy(
                    items = displayItems,
                    deletedItems = deletedDisplayItems,
                    isLoading = false
                )
            }
        }
    }

    private fun parseTitleAndSubtitle(type: VaultItemType, metaJson: String): Pair<String, String> {
        return try {
            when (type) {
                VaultItemType.LOGIN -> {
                    val meta = loginMetaAdapter.fromJson(metaJson)
                    Pair(meta?.title ?: "Untitled", meta?.username ?: "")
                }
                VaultItemType.BANK_LOGIN -> {
                    val meta = bankLoginMetaAdapter.fromJson(metaJson)
                    Pair(meta?.title ?: "Untitled Bank", meta?.customerId ?: "")
                }
                VaultItemType.NOTE -> {
                    val meta = noteMetaAdapter.fromJson(metaJson)
                    Pair(meta?.title ?: "Untitled Note", "Secure Note")
                }
                VaultItemType.CARD -> {
                    val meta = cardMetaAdapter.fromJson(metaJson)
                    Pair(meta?.title ?: "Untitled Card", "${meta?.network ?: ""} •••• ${meta?.last4 ?: ""}")
                }
            }
        } catch (e: Exception) {
            Pair("Item", "")
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setFilter(filter: CredentialFilter) {
        _uiState.value = _uiState.value.copy(activeFilter = filter)
    }

    fun setSort(sort: CredentialSort) {
        _uiState.value = _uiState.value.copy(activeSort = sort)
    }

    fun selectItem(itemId: String?) {
        if (itemId == null) {
            _uiState.value = _uiState.value.copy(
                selectedItemId = null,
                selectedItemDetail = null,
                selectedItemMeta = null,
                selectedItemSecret = null,
                revealedFields = emptyMap()
            )
            return
        }

        viewModelScope.launch {
            val item = vaultRepository.getItemById(itemId)
            if (item != null) {
                try {
                    val now = System.currentTimeMillis()
                    val metaJson = vaultRepository.readMeta(item)
                    val secretJson = vaultRepository.readSecret(item)

                    // Update lastUsedAt
                    val updatedItem = item.copy(lastUsedAt = now)
                    vaultRepository.saveItem(
                        type = updatedItem.type,
                        metaJson = metaJson,
                        secretJson = secretJson,
                        id = updatedItem.id,
                        linkedAccountId = updatedItem.linkedAccountId,
                        favourite = updatedItem.favourite
                    )

                    val (metaObj, secretObj) = when (item.type) {
                        VaultItemType.LOGIN -> Pair(loginMetaAdapter.fromJson(metaJson), loginSecretAdapter.fromJson(secretJson))
                        VaultItemType.BANK_LOGIN -> Pair(bankLoginMetaAdapter.fromJson(metaJson), bankLoginSecretAdapter.fromJson(secretJson))
                        VaultItemType.NOTE -> Pair(noteMetaAdapter.fromJson(metaJson), noteSecretAdapter.fromJson(secretJson))
                        VaultItemType.CARD -> Pair(cardMetaAdapter.fromJson(metaJson), cardSecretAdapter.fromJson(secretJson))
                    }

                    _uiState.value = _uiState.value.copy(
                        selectedItemId = itemId,
                        selectedItemDetail = updatedItem,
                        selectedItemMeta = if (item.type == VaultItemType.CARD) null else metaObj,
                        selectedItemSecret = if (item.type == VaultItemType.CARD) null else secretObj,
                        selectedCardMeta = if (item.type == VaultItemType.CARD) metaObj as? CardMeta else null,
                        selectedCardSecret = if (item.type == VaultItemType.CARD) secretObj as? CardSecret else null,
                        revealedFields = emptyMap()
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Failed to load item detail: ${e.message}")
                }
            }
        }
    }

    fun toggleFavourite(itemId: String) {
        viewModelScope.launch {
            val item = vaultRepository.getItemById(itemId) ?: return@launch
            val metaJson = vaultRepository.readMeta(item)
            val secretJson = vaultRepository.readSecret(item)
            vaultRepository.saveItem(
                type = item.type,
                metaJson = metaJson,
                secretJson = secretJson,
                id = item.id,
                linkedAccountId = item.linkedAccountId,
                favourite = !item.favourite
            )
            if (_uiState.value.selectedItemId == itemId) {
                selectItem(itemId)
            }
        }
    }

    fun moveToRecentlyDeleted(itemId: String) {
        viewModelScope.launch {
            vaultRepository.softDeleteItem(itemId)
            if (_uiState.value.selectedItemId == itemId) {
                selectItem(null)
            }
        }
    }

    fun restoreItem(itemId: String) {
        viewModelScope.launch {
            vaultRepository.restoreItem(itemId)
        }
    }

    fun deleteForever(itemId: String) {
        viewModelScope.launch {
            vaultRepository.deleteItem(itemId)
            if (_uiState.value.selectedItemId == itemId) {
                selectItem(null)
            }
        }
    }

    fun revealField(fieldKey: String, revealSeconds: Int = 30) {
        val currentRevealed = _uiState.value.revealedFields.toMutableMap()
        currentRevealed[fieldKey] = true
        _uiState.value = _uiState.value.copy(revealedFields = currentRevealed)

        revealJobs[fieldKey]?.cancel()
        revealJobs[fieldKey] = viewModelScope.launch {
            delay(revealSeconds * 1000L)
            val map = _uiState.value.revealedFields.toMutableMap()
            map[fieldKey] = false
            _uiState.value = _uiState.value.copy(revealedFields = map)
            revealJobs.remove(fieldKey)
        }
    }

    fun copyField(context: Context, label: String, value: String) {
        VaultClipboardManager.copyToClipboard(context, label, value, 30)
        _uiState.value = _uiState.value.copy(snackbarMessage = "Copied $label, clears in 30 s")
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun saveLogin(
        id: String?,
        title: String,
        username: String,
        website: String,
        password: String,
        notes: String,
        tags: List<String>,
        favourite: Boolean,
        customFields: List<Pair<String, String>>,
        hiddenFlags: List<Boolean>
    ) {
        viewModelScope.launch {
            val itemId = id ?: UUID.randomUUID().toString()
            var passwordHistory = emptyList<PasswordHistoryItem>()

            if (id != null) {
                val existingItem = vaultRepository.getItemById(id)
                if (existingItem != null) {
                    try {
                        val oldSecretJson = vaultRepository.readSecret(existingItem)
                        val oldSecret = loginSecretAdapter.fromJson(oldSecretJson)
                        if (oldSecret != null && oldSecret.password != password) {
                            val historyItem = PasswordHistoryItem(password = oldSecret.password, updatedAt = System.currentTimeMillis())
                            passwordHistory = listOf(historyItem) + oldSecret.passwordHistory
                            if (passwordHistory.size > 5) passwordHistory = passwordHistory.take(5)
                        } else if (oldSecret != null) {
                            passwordHistory = oldSecret.passwordHistory
                        }
                    } catch (e: Exception) {}
                }
            }

            val metaFields = customFields.indices.map { i ->
                CustomFieldMeta(label = customFields[i].first, hidden = hiddenFlags.getOrElse(i) { false })
            }
            val secretFields = customFields.indices.map { i ->
                CustomFieldSecret(value = customFields[i].second)
            }

            val meta = LoginMeta(title = title, username = username, website = website, tags = tags, customFields = metaFields)
            val secret = LoginSecret(password = password, notes = notes, passwordHistory = passwordHistory, customFields = secretFields)

            vaultRepository.saveItem(
                type = VaultItemType.LOGIN,
                metaJson = loginMetaAdapter.toJson(meta),
                secretJson = loginSecretAdapter.toJson(secret),
                id = itemId,
                favourite = favourite
            )
            selectItem(itemId)
        }
    }

    fun saveBankLogin(
        id: String?,
        title: String,
        bank: String,
        customerId: String,
        loginPassword: String,
        transactionPassword: String,
        notes: String,
        linkedAccountId: String?,
        tags: List<String>,
        favourite: Boolean,
        customFields: List<Pair<String, String>>,
        hiddenFlags: List<Boolean>
    ) {
        viewModelScope.launch {
            val itemId = id ?: UUID.randomUUID().toString()
            var passwordHistory = emptyList<PasswordHistoryItem>()

            if (id != null) {
                val existingItem = vaultRepository.getItemById(id)
                if (existingItem != null) {
                    try {
                        val oldSecretJson = vaultRepository.readSecret(existingItem)
                        val oldSecret = bankLoginSecretAdapter.fromJson(oldSecretJson)
                        if (oldSecret != null && oldSecret.loginPassword != loginPassword) {
                            val historyItem = PasswordHistoryItem(password = oldSecret.loginPassword, updatedAt = System.currentTimeMillis())
                            passwordHistory = listOf(historyItem) + oldSecret.passwordHistory
                            if (passwordHistory.size > 5) passwordHistory = passwordHistory.take(5)
                        } else if (oldSecret != null) {
                            passwordHistory = oldSecret.passwordHistory
                        }
                    } catch (e: Exception) {}
                }
            }

            val metaFields = customFields.indices.map { i ->
                CustomFieldMeta(label = customFields[i].first, hidden = hiddenFlags.getOrElse(i) { false })
            }
            val secretFields = customFields.indices.map { i ->
                CustomFieldSecret(value = customFields[i].second)
            }

            val meta = BankLoginMeta(title = title, bank = bank, customerId = customerId, tags = tags, customFields = metaFields)
            val secret = BankLoginSecret(loginPassword = loginPassword, transactionPassword = transactionPassword, notes = notes, passwordHistory = passwordHistory, customFields = secretFields)

            vaultRepository.saveItem(
                type = VaultItemType.BANK_LOGIN,
                metaJson = bankLoginMetaAdapter.toJson(meta),
                secretJson = bankLoginSecretAdapter.toJson(secret),
                id = itemId,
                linkedAccountId = linkedAccountId,
                favourite = favourite
            )
            selectItem(itemId)
        }
    }

    fun saveNote(
        id: String?,
        title: String,
        noteText: String,
        tags: List<String>,
        favourite: Boolean,
        customFields: List<Pair<String, String>>,
        hiddenFlags: List<Boolean>
    ) {
        viewModelScope.launch {
            val itemId = id ?: UUID.randomUUID().toString()

            val metaFields = customFields.indices.map { i ->
                CustomFieldMeta(label = customFields[i].first, hidden = hiddenFlags.getOrElse(i) { false })
            }
            val secretFields = customFields.indices.map { i ->
                CustomFieldSecret(value = customFields[i].second)
            }

            val meta = NoteMeta(title = title, tags = tags, customFields = metaFields)
            val secret = NoteSecret(note = noteText, customFields = secretFields)

            vaultRepository.saveItem(
                type = VaultItemType.NOTE,
                metaJson = noteMetaAdapter.toJson(meta),
                secretJson = noteSecretAdapter.toJson(secret),
                id = itemId,
                favourite = favourite
            )
            selectItem(itemId)
        }
    }

    fun saveCard(
        id: String?,
        nickname: String,
        bank: String,
        bankCustom: String? = null,
        cardType: com.patil.coconut_pfa.domain.CardType,
        network: com.patil.coconut_pfa.domain.CardNetwork,
        cardNumber: String,
        expiry: String,
        cardholderName: String,
        colour: String,
        notes: String,
        cvv: String,
        linkedAccountId: String?,
        favourite: Boolean
    ) {
        viewModelScope.launch {
            val itemId = id ?: UUID.randomUUID().toString()
            val cleanNumber = cardNumber.filter { it.isDigit() }
            val last4 = if (cleanNumber.length >= 4) cleanNumber.takeLast(4) else cleanNumber

            val meta = CardMeta(
                title = cardholderName.ifBlank { nickname.ifBlank { bank } },
                nickname = nickname,
                bank = bank,
                bankCustom = bankCustom,
                cardType = cardType,
                network = network,
                last4 = last4,
                expiry = expiry,
                cardholderName = cardholderName,
                colour = colour,
                notes = notes
            )
            val secret = CardSecret(
                number = cleanNumber,
                cvv = cvv
            )

            vaultRepository.saveItem(
                type = VaultItemType.CARD,
                metaJson = cardMetaAdapter.toJson(meta),
                secretJson = cardSecretAdapter.toJson(secret),
                id = itemId,
                linkedAccountId = linkedAccountId,
                favourite = favourite
            )
            selectItem(itemId)
        }
    }

    fun generatePlaceholderPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$!%*?&"
        val random = java.security.SecureRandom()
        return (1..16).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }

    fun showPasswordGenerator() {
        _uiState.value = _uiState.value.copy(showPasswordGenerator = true)
    }

    fun dismissPasswordGenerator() {
        _uiState.value = _uiState.value.copy(showPasswordGenerator = false)
    }

    fun setGeneratedPassword(pwd: String) {
        _generatedPassword.value = pwd
        dismissPasswordGenerator()
    }

    fun clearGeneratedPassword() {
        _generatedPassword.value = null
    }

    fun showHealthDialog(report: PasswordHealthReport) {
        _uiState.value = _uiState.value.copy(showHealthDialog = true, healthReport = report)
    }

    fun dismissHealthDialog() {
        _uiState.value = _uiState.value.copy(showHealthDialog = false, healthReport = null)
    }

    suspend fun computePasswordHealth(): PasswordHealthReport {
        val rawItems = vaultRepository.getAllRawItems()
        return PasswordHealthAnalyzer.analyze(
            items = rawItems,
            secretReader = { item -> kotlinx.coroutines.runBlocking { vaultRepository.readSecret(item) } },
            metaReader = { item -> kotlinx.coroutines.runBlocking { vaultRepository.readMeta(item) } }
        )
    }

    fun prepareCsvImport(context: Context, uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val records = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    parseLoginsCsv(inputStream)
                } ?: emptyList()

                if (records.isEmpty()) {
                    _uiState.value = _uiState.value.copy(snackbarMessage = "No login records found in CSV file.")
                    return@launch
                }

                val preview = CredentialImportPreview(
                    totalCount = records.size,
                    previewTitles = records.take(5).map { it.title },
                    records = records
                )

                _uiState.value = _uiState.value.copy(
                    showImportPreviewDialog = true,
                    importPreviewData = preview,
                    importFileUri = uri,
                    importDeleteErrorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(snackbarMessage = "Failed to read CSV: ${e.message}")
            }
        }
    }

    fun dismissImportPreview() {
        _uiState.value = _uiState.value.copy(
            showImportPreviewDialog = false,
            importPreviewData = null,
            importFileUri = null
        )
    }

    fun executeCsvImport() {
        val preview = _uiState.value.importPreviewData ?: return
        viewModelScope.launch {
            try {
                val rawItems = vaultRepository.getAllRawItems()
                val existingLogins = mutableSetOf<Pair<String, String>>()
                for (item in rawItems) {
                    if (item.type == VaultItemType.LOGIN) {
                        try {
                            val metaJson = vaultRepository.readMeta(item)
                            val meta = loginMetaAdapter.fromJson(metaJson)
                            if (meta != null) {
                                existingLogins.add(Pair(meta.title.trim().lowercase(), meta.username.trim().lowercase()))
                            }
                        } catch (e: Exception) {}
                    }
                }

                var importedCount = 0
                for (record in preview.records) {
                    val key = Pair(record.title.trim().lowercase(), record.username.trim().lowercase())
                    if (existingLogins.contains(key)) {
                        continue
                    }
                    existingLogins.add(key)

                    val meta = LoginMeta(
                        title = record.title,
                        username = record.username,
                        website = record.url,
                        tags = emptyList(),
                        customFields = emptyList()
                    )
                    val secret = LoginSecret(
                        password = record.password,
                        notes = record.notes,
                        passwordHistory = emptyList(),
                        customFields = emptyList()
                    )

                    vaultRepository.saveItem(
                        type = VaultItemType.LOGIN,
                        metaJson = loginMetaAdapter.toJson(meta),
                        secretJson = loginSecretAdapter.toJson(secret)
                    )
                    importedCount++
                }

                dismissImportPreview()
                _uiState.value = _uiState.value.copy(
                    importSuccessCount = importedCount,
                    snackbarMessage = "Imported $importedCount logins."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(snackbarMessage = "Import failed: ${e.message}")
            }
        }
    }

    fun deleteCsvFile(context: Context, uri: android.net.Uri) {
        try {
            val deleted = android.provider.DocumentsContract.deleteDocument(context.contentResolver, uri)
            if (deleted) {
                _uiState.value = _uiState.value.copy(
                    importSuccessCount = null,
                    importFileUri = null,
                    importDeleteErrorMessage = null,
                    snackbarMessage = "CSV file deleted successfully."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    importDeleteErrorMessage = "System provider denied file deletion. Please delete the CSV file manually using your File Manager app."
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                importDeleteErrorMessage = e.message ?: "Failed to delete file. Please delete the CSV file manually using your File Manager app."
            )
        }
    }

    fun dismissImportSuccess() {
        _uiState.value = _uiState.value.copy(
            importSuccessCount = null,
            importFileUri = null,
            importDeleteErrorMessage = null
        )
    }

    private fun parseLoginsCsv(inputStream: java.io.InputStream): List<ParsedLoginRecord> {
        val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream, Charsets.UTF_8))
        val lines = reader.readLines()
        if (lines.isEmpty()) return emptyList()

        val headerLine = lines.first()
        val headers = parseCsvLine(headerLine).map { it.lowercase().replace(" ", "_").trim('"') }

        val titleIdx = headers.indexOfFirst { it in listOf("name", "title") }
        val urlIdx = headers.indexOfFirst { it in listOf("url", "login_uri", "uri", "website") }
        val userIdx = headers.indexOfFirst { it in listOf("username", "login_username", "user", "email") }
        val passIdx = headers.indexOfFirst { it in listOf("password", "login_password", "pass") }
        val noteIdx = headers.indexOfFirst { it in listOf("note", "notes") }

        val records = mutableListOf<ParsedLoginRecord>()
        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue
            val cols = parseCsvLine(line)

            val title = if (titleIdx in cols.indices) cols[titleIdx].trim('"') else if (cols.isNotEmpty()) cols[0].trim('"') else "Untitled"
            val url = if (urlIdx in cols.indices) cols[urlIdx].trim('"') else if (cols.size > 1) cols[1].trim('"') else ""
            val username = if (userIdx in cols.indices) cols[userIdx].trim('"') else if (cols.size > 2) cols[2].trim('"') else ""
            val password = if (passIdx in cols.indices) cols[passIdx].trim('"') else if (cols.size > 3) cols[3].trim('"') else ""
            val notes = if (noteIdx in cols.indices) cols[noteIdx].trim('"') else if (cols.size > 4) cols[4].trim('"') else ""

            if (title.isNotBlank() || username.isNotBlank() || password.isNotBlank()) {
                records.add(ParsedLoginRecord(
                    title = if (title.isBlank()) "Untitled" else title,
                    username = username,
                    url = url,
                    password = password,
                    notes = notes
                ))
            }
        }
        return records
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var cur = StringBuilder()
        var inQuotes = false
        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(cur.toString().trim())
                    cur = StringBuilder()
                }
                else -> cur.append(char)
            }
        }
        result.add(cur.toString().trim())
        return result
    }
}
