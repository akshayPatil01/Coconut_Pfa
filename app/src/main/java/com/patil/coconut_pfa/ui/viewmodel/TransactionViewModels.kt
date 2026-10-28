package com.patil.coconut_pfa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.CategoryEntity
import com.patil.coconut_pfa.data.local.entity.DbCategoryKind
import com.patil.coconut_pfa.data.local.entity.DbTxnSource
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.repository.AccountRepository
import com.patil.coconut_pfa.data.repository.CategoryRepository
import com.patil.coconut_pfa.data.repository.SettingsRepository
import com.patil.coconut_pfa.data.repository.TransactionRepository
import com.patil.coconut_pfa.domain.SalaryCycleCalculator
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

// Data structure for day-grouped transactions
data class DayGroupedTransactions(
    val dateLabel: String,
    val date: LocalDate,
    val transactions: List<TxnEntity>,
    val daySpentPaise: Long,
    val dayReceivedPaise: Long
)

// UI State for Transactions Screen
data class TransactionsUiState(
    val groupedTransactions: List<DayGroupedTransactions> = emptyList(),
    val accountMap: Map<String, AccountEntity> = emptyMap(),
    val categoryMap: Map<String, CategoryEntity> = emptyMap(),
    val searchQuery: String = "",
    val selectedType: DbTxnType? = null,
    val selectedCategory: String? = null,
    val selectedAccount: String? = null,
    val selectedCycle: String = "Current Cycle",
    val selectedSource: DbTxnSource? = null,
    val isLoading: Boolean = true
)

class TransactionsViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<DbTxnType?>(null)
    val selectedType = _selectedType.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedAccount = MutableStateFlow<String?>(null)
    val selectedAccount = _selectedAccount.asStateFlow()

    private val _selectedCycle = MutableStateFlow("Current Cycle")
    val selectedCycle = _selectedCycle.asStateFlow()

    private val _selectedSource = MutableStateFlow<DbTxnSource?>(null)
    val selectedSource = _selectedSource.asStateFlow()

    private val baseDataFlow = combine(
        transactionRepository.getAllActiveTransactions(),
        accountRepository.getAllAccounts(),
        categoryRepository.getAllCategories(),
        settingsRepository.settingsFlow
    ) { txns, accounts, categories, settings ->
        Quadruple(txns, accounts, categories, settings)
    }

    private val filterTupleFlow = combine(
        combine(_searchQuery, _selectedType) { q, t -> Pair(q, t) },
        combine(_selectedCategory, _selectedAccount) { c, a -> Pair(c, a) },
        combine(_selectedCycle, _selectedSource) { cy, s -> Pair(cy, s) }
    ) { (query, type), (category, account), (cycle, source) ->
        FilterTuple(query, type, category, account, cycle, source)
    }

    val uiState: StateFlow<TransactionsUiState> = combine(
        baseDataFlow,
        filterTupleFlow
    ) { (txns, accounts, categories, settings), filters ->
        val accountMap = accounts.associateBy { it.id }
        val categoryMap = categories.associateBy { it.id }

        val today = KolkataDateTimeUtils.currentLocalDate()
        val currentCyclePeriod = SalaryCycleCalculator.calculateCycle(settings.salaryCycleStartDay, today)

        val cycleRange = when (filters.selectedCycle) {
            "Current Cycle" -> currentCyclePeriod.startDate..currentCyclePeriod.endDate
            "Previous Cycle" -> {
                val prevPeriod = SalaryCycleCalculator.calculateCycle(settings.salaryCycleStartDay, currentCyclePeriod.startDate.minusDays(1))
                prevPeriod.startDate..prevPeriod.endDate
            }
            else -> null // All Time
        }

        val filteredTxns = txns.filter { tx ->
            val txDate = KolkataDateTimeUtils.epochMillisToLocalDate(tx.occurredAt)

            // Date cycle filter
            if (cycleRange != null && txDate !in cycleRange) {
                return@filter false
            }

            // Type filter
            if (filters.selectedType != null && tx.type != filters.selectedType) {
                return@filter false
            }

            // Category filter (match ID or category name)
            if (filters.selectedCategory != null) {
                val catEntity = categoryMap[tx.categoryId]
                val catMatches = tx.categoryId == filters.selectedCategory ||
                        catEntity?.name?.equals(filters.selectedCategory, ignoreCase = true) == true
                if (!catMatches) return@filter false
            }

            // Account filter (check accountId or toAccountId)
            if (filters.selectedAccount != null) {
                if (tx.accountId != filters.selectedAccount && tx.toAccountId != filters.selectedAccount) {
                    return@filter false
                }
            }

            // Source filter
            if (filters.selectedSource != null && tx.source != filters.selectedSource) {
                return@filter false
            }

            // Search query filter
            if (filters.searchQuery.isNotBlank()) {
                val q = filters.searchQuery.trim().lowercase()
                val payeeMatches = tx.payee?.lowercase()?.contains(q) == true
                val noteMatches = tx.note?.lowercase()?.contains(q) == true
                val catNameMatches = categoryMap[tx.categoryId]?.name?.lowercase()?.contains(q) == true
                val amountMatches = (tx.amountPaise / 100.0).toString().contains(q) || tx.amountPaise.toString().contains(q)

                if (!payeeMatches && !noteMatches && !catNameMatches && !amountMatches) {
                    return@filter false
                }
            }

            true
        }

        // Group by LocalDate
        val groupedMap = filteredTxns.groupBy { KolkataDateTimeUtils.epochMillisToLocalDate(it.occurredAt) }
        val sortedDates = groupedMap.keys.sortedDescending()

        val groupedList = sortedDates.map { date ->
            val dayTxns = groupedMap[date] ?: emptyList()
            val daySpent = dayTxns.filter { it.type == DbTxnType.EXPENSE || it.type == DbTxnType.TRANSFER }.sumOf { it.amountPaise }
            val dayRecv = dayTxns.filter { it.type == DbTxnType.INCOME || it.type == DbTxnType.REFUND }.sumOf { it.amountPaise }
            val dateLabel = formatDayLabel(date, today)

            DayGroupedTransactions(
                dateLabel = dateLabel,
                date = date,
                transactions = dayTxns,
                daySpentPaise = daySpent,
                dayReceivedPaise = dayRecv
            )
        }

        TransactionsUiState(
            groupedTransactions = groupedList,
            accountMap = accountMap,
            categoryMap = categoryMap,
            searchQuery = filters.searchQuery,
            selectedType = filters.selectedType,
            selectedCategory = filters.selectedCategory,
            selectedAccount = filters.selectedAccount,
            selectedCycle = filters.selectedCycle,
            selectedSource = filters.selectedSource,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsUiState(isLoading = true)
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedType(type: DbTxnType?) {
        _selectedType.value = type
    }

    fun updateSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun updateSelectedAccount(accountId: String?) {
        _selectedAccount.value = accountId
    }

    fun updateSelectedCycle(cycle: String) {
        _selectedCycle.value = cycle
    }

    fun updateSelectedSource(source: DbTxnSource?) {
        _selectedSource.value = source
    }

    fun softDeleteTransaction(id: String) {
        viewModelScope.launch {
            transactionRepository.softDeleteTransaction(id)
        }
    }

    fun restoreTransaction(id: String) {
        viewModelScope.launch {
            transactionRepository.restoreTransaction(id)
        }
    }

    private fun formatDayLabel(date: LocalDate, today: LocalDate): String {
        return when (date) {
            today -> "Today, ${date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}"
            today.minusDays(1) -> "Yesterday, ${date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}"
            else -> date.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"))
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
    private data class FilterTuple(
        val searchQuery: String,
        val selectedType: DbTxnType?,
        val selectedCategory: String?,
        val selectedAccount: String?,
        val selectedCycle: String,
        val selectedSource: DbTxnSource?
    )

    class Factory(
        private val transactionRepository: TransactionRepository,
        private val accountRepository: AccountRepository,
        private val categoryRepository: CategoryRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransactionsViewModel(
                transactionRepository,
                accountRepository,
                categoryRepository,
                settingsRepository
            ) as T
        }
    }
}

// UI State for Add / Edit Transaction Screen
data class AddTransactionUiState(
    val initialTxn: TxnEntity? = null,
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val distinctPayees: List<String> = emptyList(),
    val lastUsedAccountId: String? = null,
    val lastUsedCategoryId: String? = null,
    val isLoading: Boolean = true
)

class AddTransactionViewModel(
    private val txnId: String?,
    private val duplicateTxnId: String?,
    private val initialDateMillis: Long?,
    private val billOccurrenceId: String? = null,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository,
    private val billRepository: com.patil.coconut_pfa.data.repository.BillRepository? = null
) : ViewModel() {

    val uiState: StateFlow<AddTransactionUiState> = combine(
        accountRepository.getActiveAccounts(),
        categoryRepository.getAllCategories(),
        transactionRepository.getDistinctPayees(),
        settingsRepository.settingsFlow
    ) { accounts, categories, payees, settings ->
        val existingTxn = when {
            txnId != null -> transactionRepository.getTransactionById(txnId)
            duplicateTxnId != null -> transactionRepository.getTransactionById(duplicateTxnId)
            else -> null
        }

        AddTransactionUiState(
            initialTxn = existingTxn,
            accounts = accounts,
            categories = categories,
            distinctPayees = payees,
            lastUsedAccountId = settings.lastUsedAccountId,
            lastUsedCategoryId = settings.lastUsedCategoryId,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddTransactionUiState(isLoading = true)
    )

    suspend fun saveTransaction(
        id: String = txnId ?: UUID.randomUUID().toString(),
        type: DbTxnType,
        amountPaise: Long,
        accountId: String?,
        toAccountId: String?,
        categoryId: String?,
        payee: String?,
        note: String?,
        occurredAt: Long
    ): Boolean {
        if (amountPaise <= 0L) return false
        if (accountId == null) return false
        if (type == DbTxnType.TRANSFER && (toAccountId == null || accountId == toAccountId)) return false
        if (type != DbTxnType.TRANSFER && categoryId == null) return false

        if (billOccurrenceId != null && billRepository != null) {
            val success = billRepository.payBillOccurrence(
                occurrenceId = billOccurrenceId,
                payFromAccountId = accountId,
                amountPaise = amountPaise,
                occurredAtMillis = occurredAt
            )
            if (success) {
                settingsRepository.setLastUsedAccount(accountId)
                if (categoryId != null) {
                    settingsRepository.setLastUsedCategory(categoryId)
                }
                return true
            }
        }

        val txn = TxnEntity(
            id = id,
            type = type,
            amountPaise = amountPaise,
            accountId = accountId,
            toAccountId = if (type == DbTxnType.TRANSFER) toAccountId else null,
            categoryId = if (type != DbTxnType.TRANSFER) categoryId else null,
            payee = payee?.trim()?.ifEmpty { null },
            note = note?.trim()?.ifEmpty { null },
            occurredAt = occurredAt,
            source = DbTxnSource.MANUAL
        )

        transactionRepository.saveTransaction(txn)
        settingsRepository.setLastUsedAccount(accountId)
        if (categoryId != null) {
            settingsRepository.setLastUsedCategory(categoryId)
        }
        return true
    }

    class Factory(
        private val txnId: String?,
        private val duplicateTxnId: String?,
        private val initialDateMillis: Long?,
        private val billOccurrenceId: String? = null,
        private val accountRepository: AccountRepository,
        private val categoryRepository: CategoryRepository,
        private val transactionRepository: TransactionRepository,
        private val settingsRepository: SettingsRepository,
        private val billRepository: com.patil.coconut_pfa.data.repository.BillRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddTransactionViewModel(
                txnId,
                duplicateTxnId,
                initialDateMillis,
                billOccurrenceId,
                accountRepository,
                categoryRepository,
                transactionRepository,
                settingsRepository,
                billRepository
            ) as T
        }
    }
}

// UI State for Trash Screen
data class TrashUiState(
    val trashTxns: List<TxnEntity> = emptyList(),
    val accountMap: Map<String, AccountEntity> = emptyMap(),
    val categoryMap: Map<String, CategoryEntity> = emptyMap(),
    val isLoading: Boolean = true
)

class TrashViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<TrashUiState> = combine(
        transactionRepository.getTrashTransactions(),
        accountRepository.getAllAccounts(),
        categoryRepository.getAllCategories()
    ) { trashTxns, accounts, categories ->
        val activeTrash = trashTxns.filter { tx ->
            val deletedAt = tx.deletedAt ?: 0L
            val cutoff = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000L)
            deletedAt >= cutoff
        }

        TrashUiState(
            trashTxns = activeTrash,
            accountMap = accounts.associateBy { it.id },
            categoryMap = categories.associateBy { it.id },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrashUiState(isLoading = true)
    )

    fun restore(id: String) {
        viewModelScope.launch {
            transactionRepository.restoreTransaction(id)
        }
    }

    fun deleteForever(txn: TxnEntity) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(txn)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            val items = uiState.value.trashTxns
            for (tx in items) {
                transactionRepository.deleteTransaction(tx)
            }
        }
    }

    class Factory(
        private val transactionRepository: TransactionRepository,
        private val accountRepository: AccountRepository,
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrashViewModel(transactionRepository, accountRepository, categoryRepository) as T
        }
    }
}
