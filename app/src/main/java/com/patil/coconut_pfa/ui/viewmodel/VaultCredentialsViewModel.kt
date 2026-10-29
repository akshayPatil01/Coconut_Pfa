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

enum class CredentialFilter {
    ALL, FAVOURITES, BANK, LOGIN, NOTES, CARDS
}

enum class CredentialSort {
    NAME, RECENTLY_USED
}

data class VaultCredentialsUiState(
    val items: List<CredentialDisplayItem> = emptyList(),
    val deletedItems: List<CredentialDisplayItem> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: CredentialFilter = CredentialFilter.ALL,
    val activeSort: CredentialSort = CredentialSort.NAME,
    val selectedItemId: String? = null,
    val selectedItemDetail: VaultItem? = null,
    val selectedItemMeta: Any? = null,
    val selectedItemSecret: Any? = null,
    val revealedFields: Map<String, Boolean> = emptyMap(),
    val snackbarMessage: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
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
                        selectedItemMeta = metaObj,
                        selectedItemSecret = secretObj,
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
        title: String,
        cardHolderName: String,
        cardNumber: String,
        expiryMonth: String,
        expiryYear: String,
        cvv: String,
        pin: String,
        billingAddress: String,
        phone: String,
        notes: String,
        favourite: Boolean
    ) {
        viewModelScope.launch {
            val itemId = id ?: UUID.randomUUID().toString()
            val cleanNumber = cardNumber.filter { it.isDigit() }
            val last4 = if (cleanNumber.length >= 4) cleanNumber.takeLast(4) else cleanNumber
            val network = CardRules.detectNetwork(cleanNumber).name

            val meta = CardMeta(
                title = title,
                cardHolderName = cardHolderName,
                last4 = last4,
                network = network,
                expiryMonth = expiryMonth,
                expiryYear = expiryYear
            )
            val secret = CardSecret(
                cardNumber = cleanNumber,
                cvv = cvv,
                pin = pin,
                billingAddress = billingAddress,
                phone = phone,
                notes = notes
            )

            vaultRepository.saveItem(
                type = VaultItemType.CARD,
                metaJson = cardMetaAdapter.toJson(meta),
                secretJson = cardSecretAdapter.toJson(secret),
                id = itemId,
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
}
