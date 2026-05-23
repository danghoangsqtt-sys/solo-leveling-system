package com.systemleveling.feature.finance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemleveling.core.database.dao.FinanceDao
import com.systemleveling.core.database.dao.BudgetDao
import com.systemleveling.core.database.dao.DebtDao
import com.systemleveling.core.database.entity.TransactionEntity
import com.systemleveling.core.database.entity.BudgetEntity
import com.systemleveling.core.database.entity.DebtEntity
import com.systemleveling.core.database.entity.DebtType
import com.systemleveling.core.model.FinanceCategory
import com.systemleveling.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val financeDao: FinanceDao,
    private val budgetDao: BudgetDao,
    private val debtDao: DebtDao
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()

    val hasAnyTransaction: StateFlow<Boolean> = _transactions.map { it.isNotEmpty() }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), false)

    // ── Month-to-month state ────────────────────────────────────────────────
    val selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1) // 1-based (1 to 12)

    // ── Monthly Transactions & Totals ───────────────────────────────────────
    val monthlyTransactions: StateFlow<List<TransactionEntity>> = combine(
        _transactions, selectedYear, selectedMonth
    ) { txs, year, month ->
        val (start, end) = getStartAndEndOfMonth(year, month)
        txs.filter { it.timestamp in start until end }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyIncome: StateFlow<Long> = monthlyTransactions.map { list ->
        list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0L)

    val monthlyExpense: StateFlow<Long> = monthlyTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0L)

    val monthlyBalance: StateFlow<Long> = combine(monthlyIncome, monthlyExpense) { inc, exp ->
        inc - exp
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0L)

    val categorySpent: StateFlow<Map<FinanceCategory, Long>> = monthlyTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyMap())

    // ── Budgets (Spending Pots) ─────────────────────────────────────────────
    private val _budgets = MutableStateFlow<List<BudgetEntity>>(emptyList())
    val budgets: StateFlow<List<BudgetEntity>> = _budgets.asStateFlow()

    // ── Debts ───────────────────────────────────────────────────────────────
    private val _debts = MutableStateFlow<List<DebtEntity>>(emptyList())
    val debts: StateFlow<List<DebtEntity>> = _debts.asStateFlow()

    val borrowedDebts: StateFlow<List<DebtEntity>> = _debts.map { list ->
        list.filter { it.type == DebtType.BORROWED }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val lentDebts: StateFlow<List<DebtEntity>> = _debts.map { list ->
        list.filter { it.type == DebtType.LENT }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            financeDao.getAllTransactions().collect { dbTx ->
                _transactions.value = dbTx
            }
        }
        viewModelScope.launch {
            budgetDao.getAllBudgets().collect {
                _budgets.value = it
            }
        }
        viewModelScope.launch {
            debtDao.getAllDebts().collect {
                _debts.value = it
            }
        }
    }

    private fun getStartAndEndOfMonth(year: Int, month: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    fun changeMonth(offset: Int) {
        val curYear = selectedYear.value
        val curMonth = selectedMonth.value
        val calendar = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, curYear)
            set(Calendar.MONTH, curMonth - 1)
            add(Calendar.MONTH, offset)
        }
        selectedYear.value = calendar.get(Calendar.YEAR)
        selectedMonth.value = calendar.get(Calendar.MONTH) + 1
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

    fun clearAllTransactions() {
        viewModelScope.launch { financeDao.clearAllTransactions() }
    }

    // ── Budget CRUD ─────────────────────────────────────────────────────────
    fun setBudget(category: FinanceCategory, limit: Long) {
        viewModelScope.launch {
            budgetDao.insertBudget(BudgetEntity(category, limit))
        }
    }

    fun deleteBudget(category: FinanceCategory) {
        viewModelScope.launch {
            budgetDao.deleteBudget(category)
        }
    }

    // ── Debt CRUD ───────────────────────────────────────────────────────────
    fun addDebt(personName: String, amount: Long, type: DebtType, note: String) {
        viewModelScope.launch {
            debtDao.insertDebt(
                DebtEntity(
                    personName = personName,
                    amount = amount,
                    type = type,
                    note = note
                )
            )
        }
    }

    fun toggleDebtPaid(id: String, isPaid: Boolean) {
        viewModelScope.launch {
            val debt = _debts.value.find { it.id == id } ?: return@launch
            debtDao.insertDebt(debt.copy(isPaid = isPaid))
        }
    }

    fun deleteDebt(id: String) {
        viewModelScope.launch {
            debtDao.deleteDebt(id)
        }
    }
}
