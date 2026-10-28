package com.patil.coconut_pfa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.BillEntity
import com.patil.coconut_pfa.data.local.entity.CategoryEntity
import com.patil.coconut_pfa.data.local.entity.DbBillFrequency
import com.patil.coconut_pfa.data.local.entity.DbBillKind
import com.patil.coconut_pfa.data.repository.AccountRepository
import com.patil.coconut_pfa.data.repository.BillRepository
import com.patil.coconut_pfa.data.repository.CategoryRepository
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.util.UUID

data class AddBillUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = true
)

class AddBillViewModel(
    private val billRepository: BillRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<AddBillUiState> = combine(
        accountRepository.getActiveAccounts(),
        categoryRepository.getAllCategories()
    ) { accounts, categories ->
        AddBillUiState(
            accounts = accounts,
            categories = categories,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddBillUiState(isLoading = true)
    )

    suspend fun saveBill(
        name: String,
        kind: DbBillKind,
        amountPaise: Long,
        isVariable: Boolean,
        frequency: DbBillFrequency,
        dueDay: Int,
        payFromAccountId: String?,
        categoryId: String?,
        remindDaysBefore: Int
    ): Boolean {
        if (name.isBlank()) return false

        val today = KolkataDateTimeUtils.currentLocalDate()
        val anchorLocalDate = try {
            val clampedDay = minOf(dueDay.coerceIn(1, 31), today.lengthOfMonth())
            today.withDayOfMonth(clampedDay)
        } catch (e: Exception) { today }

        val anchorDateStr = anchorLocalDate.format(KolkataDateTimeUtils.DATE_FORMATTER)

        val bill = BillEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            kind = kind,
            amountPaise = amountPaise,
            isVariable = isVariable,
            frequency = frequency,
            interval = 1,
            anchorDate = anchorDateStr,
            payFromAccountId = payFromAccountId,
            categoryId = categoryId,
            reminderDaysBefore = remindDaysBefore,
            active = true
        )

        billRepository.saveBill(bill)
        return true
    }

    class Factory(
        private val billRepository: BillRepository,
        private val accountRepository: AccountRepository,
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddBillViewModel(
                billRepository,
                accountRepository,
                categoryRepository
            ) as T
        }
    }
}
