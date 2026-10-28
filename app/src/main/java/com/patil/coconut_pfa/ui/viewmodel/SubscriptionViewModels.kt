package com.patil.coconut_pfa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.patil.coconut_pfa.data.local.entity.BillEntity
import com.patil.coconut_pfa.data.local.entity.DbBillFrequency
import com.patil.coconut_pfa.data.local.entity.DbBillKind
import com.patil.coconut_pfa.data.repository.BillRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SubscriptionsUiState(
    val subscriptions: List<BillEntity> = emptyList(),
    val monthlyTotalPaise: Long = 0L,
    val activeCount: Int = 0,
    val isLoading: Boolean = true
)

class SubscriptionsViewModel(
    private val billRepository: BillRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            billRepository.generateAllOccurrencesAndStatements()
        }
    }

    val uiState: StateFlow<SubscriptionsUiState> = billRepository.getAllBills()
        .map { allBills ->
            val subs = allBills.filter { it.kind == DbBillKind.SUBSCRIPTION }
            val activeSubs = subs.filter { it.active }
            val monthlyTotal = activeSubs.sumOf { sub ->
                val mult = when (sub.frequency) {
                    DbBillFrequency.MONTHLY -> 1.0
                    DbBillFrequency.QUARTERLY -> 1.0 / 3.0
                    DbBillFrequency.YEARLY -> 1.0 / 12.0
                    DbBillFrequency.EVERY_N_DAYS -> 1.0 / (sub.interval.toDouble().coerceAtLeast(1.0) / 30.0)
                }
                (sub.amountPaise * mult).toLong()
            }
            SubscriptionsUiState(
                subscriptions = subs,
                monthlyTotalPaise = monthlyTotal,
                activeCount = activeSubs.size,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SubscriptionsUiState(isLoading = true)
        )

    fun toggleActive(billId: String, active: Boolean) {
        viewModelScope.launch {
            val bill = billRepository.getBillById(billId) ?: return@launch
            billRepository.updateBill(bill.copy(active = active))
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            billRepository.deleteBill(bill)
        }
    }

    class Factory(
        private val billRepository: BillRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SubscriptionsViewModel(billRepository) as T
        }
    }
}
