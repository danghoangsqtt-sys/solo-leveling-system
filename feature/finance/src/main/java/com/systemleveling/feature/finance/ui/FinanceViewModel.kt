package com.systemleveling.feature.finance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.FinanceDao
import com.systemleveling.core.database.entity.TransactionEntity
import com.systemleveling.core.model.FinanceCategory
import com.systemleveling.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val financeDao: FinanceDao
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()

    private val _totalIncome = MutableStateFlow(0L)
    val totalIncome: StateFlow<Long> = _totalIncome.asStateFlow()

    private val _totalExpense = MutableStateFlow(0L)
    val totalExpense: StateFlow<Long> = _totalExpense.asStateFlow()

    val balance: StateFlow<Long> = combine(_totalIncome, _totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0L)

    init {
        viewModelScope.launch {
            financeDao.getAllTransactions().collect { dbTx ->
                if (dbTx.isEmpty()) {
                    seedMockData()
                } else {
                    _transactions.value = dbTx
                }
            }
        }
        viewModelScope.launch {
            financeDao.getTotalIncome().collect { income ->
                _totalIncome.value = income ?: 0L
            }
        }
        viewModelScope.launch {
            financeDao.getTotalExpense().collect { expense ->
                _totalExpense.value = expense ?: 0L
            }
        }
    }

    private suspend fun seedMockData() {
        val mockData = listOf(
            TransactionEntity("TX1", 20000000, TransactionType.INCOME, FinanceCategory.SALARY, "Lương tháng 5"),
            TransactionEntity("TX2", 500000, TransactionType.EXPENSE, FinanceCategory.FOOD, "Đi siêu thị"),
            TransactionEntity("TX3", 1200000, TransactionType.EXPENSE, FinanceCategory.BILL, "Đóng tiền điện, mạng"),
            TransactionEntity("TX4", 3000000, TransactionType.EXPENSE, FinanceCategory.INVESTMENT, "Đầu tư chứng khoán"),
            TransactionEntity("TX5", 1500000, TransactionType.INCOME, FinanceCategory.OTHER, "Thu nhập phụ trợ")
        )
        financeDao.insertTransactions(mockData)
    }

    fun addTransaction(amount: Long, type: TransactionType, category: FinanceCategory, note: String) {
        viewModelScope.launch {
            financeDao.insertTransaction(
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    amount = amount,
                    type = type,
                    category = category,
                    note = note
                )
            )
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch { financeDao.deleteTransaction(id) }
    }
}
