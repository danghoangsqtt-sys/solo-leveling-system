package com.systemleveling.feature.finance.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemleveling.core.database.entity.BudgetEntity
import com.systemleveling.core.database.entity.DebtEntity
import com.systemleveling.core.database.entity.DebtType
import com.systemleveling.core.database.entity.TransactionEntity
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.designsystem.theme.md_theme_dark_primary
import com.systemleveling.core.model.FinanceCategory
import com.systemleveling.core.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

enum class FinanceSubTab {
    TRANSACTIONS, BUDGETS, DEBTS
}

@Composable
fun FinanceScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val transactions by viewModel.monthlyTransactions.collectAsState()
    val income by viewModel.monthlyIncome.collectAsState()
    val expense by viewModel.monthlyExpense.collectAsState()
    val balance by viewModel.monthlyBalance.collectAsState()
    val categorySpent by viewModel.categorySpent.collectAsState()

    val hasAnyTransaction by viewModel.hasAnyTransaction.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val borrowedDebts by viewModel.borrowedDebts.collectAsState()
    val lentDebts by viewModel.lentDebts.collectAsState()

    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    var activeSubTab by remember { mutableStateOf(FinanceSubTab.TRANSACTIONS) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // Dialogs
    if (showAddTxDialog) {
        AddTransactionDialog(
            onDismiss = { showAddTxDialog = false },
            onConfirm = { amount, type, category, note ->
                viewModel.addTransaction(amount, type, category, note)
                showAddTxDialog = false
            }
        )
    }

    if (showAddBudgetDialog) {
        AddBudgetDialog(
            onDismiss = { showAddBudgetDialog = false },
            onConfirm = { category, limit ->
                viewModel.setBudget(category, limit)
                showAddBudgetDialog = false
            }
        )
    }

    if (showAddDebtDialog) {
        AddDebtDialog(
            onDismiss = { showAddDebtDialog = false },
            onConfirm = { personName, amount, type, note ->
                viewModel.addDebt(personName, amount, type, note)
                showAddDebtDialog = false
            }
        )
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            containerColor = Color(0xFF1E1E2F),
            title = { Text("Xóa tất cả giao dịch?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Hành động này sẽ xóa toàn bộ lịch sử giao dịch và không thể hoàn tác.", color = Color.Gray) },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearAllTransactions(); showClearAllConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) { Text("XÓA TẤT CẢ", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) { Text("HỦY", color = Color.Gray) }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(md_theme_dark_background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "◀ HOME",
                    color = md_theme_dark_primary,
                    modifier = Modifier.clickable { onBack() }
                )
                Text(
                    text = "💰 TÀI CHÍNH",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub Tab Navigation Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A2E))
                    .padding(4.dp)
            ) {
                FinanceSubTab.entries.forEach { tab ->
                    val selected = activeSubTab == tab
                    val label = when (tab) {
                        FinanceSubTab.TRANSACTIONS -> "Giao Dịch"
                        FinanceSubTab.BUDGETS -> "Hũ Ngân Sách"
                        FinanceSubTab.DEBTS -> "Ghi Nợ"
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selected) md_theme_dark_primary.copy(alpha = 0.3f) else Color.Transparent)
                            .border(1.dp, if (selected) md_theme_dark_primary else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { activeSubTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selected) md_theme_dark_primary else Color.Gray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (activeSubTab) {
                FinanceSubTab.TRANSACTIONS -> {
                    // Month Switcher Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.changeMonth(-1) }) {
                            Text("◀", color = md_theme_dark_primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text(
                            text = "THÁNG $selectedMonth / $selectedYear",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            letterSpacing = 1.sp
                        )
                        IconButton(onClick = { viewModel.changeMonth(1) }) {
                            Text("▶", color = md_theme_dark_primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dashboard Summary Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E1E2E))
                            .border(1.dp, Color(0xFF2F2F40), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "TỔNG SỐ DƯ THÁNG", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = formatter.format(balance),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(text = "THU NHẬP", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "+${formatter.format(income)}",
                                        color = Color(0xFF40E17E),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "CHI TIÊU", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "-${formatter.format(expense)}",
                                        color = Color(0xFFFF5252),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Expenses Chart & Legends
                    if (expense > 0 && categorySpent.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121225)),
                            border = BorderStroke(0.5.dp, Color(0x33FFFFFF))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "BIỂU ĐỒ CHI TIÊU",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Custom Pie Chart using Canvas
                                    Box(
                                        modifier = Modifier.size(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val categoryColors = getCategoryColors()
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            var startAngle = -90f
                                            categorySpent.forEach { (cat, spent) ->
                                                val sweepAngle = (spent.toFloat() / expense.toFloat()) * 360f
                                                drawArc(
                                                    color = categoryColors[cat] ?: Color.Gray,
                                                    startAngle = startAngle,
                                                    sweepAngle = sweepAngle,
                                                    useCenter = true,
                                                    size = Size(size.width, size.height)
                                                )
                                                startAngle += sweepAngle
                                            }
                                        }
                                        // Center hole to make it a donut chart
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF121225))
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Legends
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val colors = getCategoryColors()
                                        categorySpent.entries.sortedByDescending { it.value }.take(4).forEach { (cat, spent) ->
                                            val pct = (spent.toDouble() / expense.toDouble() * 100).toInt()
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(colors[cat] ?: Color.Gray)
                                                )
                                                Text(
                                                    text = "${cat.name} ($pct%)",
                                                    color = Color.LightGray,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "LỊCH SỬ GIAO DỊCH", color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        if (hasAnyTransaction) {
                            Text(
                                text = "Xóa tất cả",
                                color = Color(0xFFFF5252).copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                modifier = Modifier.clickable { showClearAllConfirm = true }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Chưa có giao dịch nào trong tháng này.", color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        // Transaction List
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(transactions, key = { it.id }) { tx ->
                                TransactionItem(
                                    tx = tx,
                                    formatter = formatter,
                                    onDelete = { viewModel.deleteTransaction(tx.id) }
                                )
                            }
                        }
                    }
                }

                FinanceSubTab.BUDGETS -> {
                    // Budgets Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "CÁC HŨ NGÂN SÁCH TÀI CHÍNH", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showAddBudgetDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary.copy(0.2f)),
                            border = BorderStroke(1.dp, md_theme_dark_primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Cài Đặt", color = md_theme_dark_primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (budgets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Chưa cấu hình hũ chi tiêu nào. Bấm 'Cài Đặt' để thêm.", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(budgets) { budget ->
                                val spent = categorySpent[budget.category] ?: 0L
                                BudgetCard(
                                    budget = budget,
                                    spent = spent,
                                    formatter = formatter,
                                    onDelete = { viewModel.deleteBudget(budget.category) }
                                )
                            }
                        }
                    }
                }

                FinanceSubTab.DEBTS -> {
                    // Debts Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "TỔNG HỢP CÁC KHOẢN NỢ", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showAddDebtDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary.copy(0.2f)),
                            border = BorderStroke(1.dp, md_theme_dark_primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Thêm Ghi Nợ", color = md_theme_dark_primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Debts summary statistics
                    val totalToPay = borrowedDebts.filter { !it.isPaid }.sumOf { it.amount }
                    val totalToCollect = lentDebts.filter { !it.isPaid }.sumOf { it.amount }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1420))
                                .border(0.5.dp, Color(0xFFFF5252).copy(0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Bản thân nợ", color = Color.Gray, fontSize = 10.sp)
                                Text(formatter.format(totalToPay), color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF141E20))
                                .border(0.5.dp, Color(0xFF40E17E).copy(0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Người khác nợ", color = Color.Gray, fontSize = 10.sp)
                                Text(formatter.format(totalToCollect), color = Color(0xFF40E17E), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text("BẢN THÂN NỢ (ĐANG VAY)", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (borrowedDebts.isEmpty()) {
                                Text("Không có khoản nợ nào.", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                            }
                        }

                        items(borrowedDebts, key = { it.id }) { debt ->
                            DebtItemCard(
                                debt = debt,
                                formatter = formatter,
                                onTogglePaid = { isPaid -> viewModel.toggleDebtPaid(debt.id, isPaid) },
                                onDelete = { viewModel.deleteDebt(debt.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("NGƯỜI KHÁC NỢ (CHO VAY)", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (lentDebts.isEmpty()) {
                                Text("Không có khoản cho vay nào.", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                            }
                        }

                        items(lentDebts, key = { it.id }) { debt ->
                            DebtItemCard(
                                debt = debt,
                                formatter = formatter,
                                onTogglePaid = { isPaid -> viewModel.toggleDebtPaid(debt.id, isPaid) },
                                onDelete = { viewModel.deleteDebt(debt.id) }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button to add quick Transaction
        if (activeSubTab == FinanceSubTab.TRANSACTIONS) {
            FloatingActionButton(
                onClick = { showAddTxDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = md_theme_dark_primary,
                shape = CircleShape
            ) {
                Text("+", fontSize = 24.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun getCategoryColors() = mapOf(
    FinanceCategory.FOOD to Color(0xFFFF7F50),
    FinanceCategory.TRANSPORT to Color(0xFF1E90FF),
    FinanceCategory.ENTERTAINMENT to Color(0xFFFF1493),
    FinanceCategory.STUDY to Color(0xFF32CD32),
    FinanceCategory.BILL to Color(0xFFBA55D3),
    FinanceCategory.SALARY to Color(0xFF20B2AA),
    FinanceCategory.INVESTMENT to Color(0xFFFFD700),
    FinanceCategory.OTHER to Color(0xFF808080)
)

private fun getCategoryIcons() = mapOf(
    FinanceCategory.FOOD to "🍔", FinanceCategory.TRANSPORT to "🚕",
    FinanceCategory.ENTERTAINMENT to "🎮", FinanceCategory.STUDY to "📚",
    FinanceCategory.BILL to "🧾", FinanceCategory.SALARY to "💵",
    FinanceCategory.INVESTMENT to "📈", FinanceCategory.OTHER to "📦"
)

// ── Transaction Item Component ───────────────────────────────────────────────

@Composable
fun TransactionItem(tx: TransactionEntity, formatter: NumberFormat, onDelete: (() -> Unit)? = null) {
    val isIncome = tx.type == TransactionType.INCOME
    val amountColor = if (isIncome) Color(0xFF40E17E) else Color(0xFFFF5252)
    val sign = if (isIncome) "+" else "-"
    var confirmDelete by remember { mutableStateOf(false) }

    val categoryIcon = when (tx.category) {
        FinanceCategory.FOOD -> "🍔"
        FinanceCategory.TRANSPORT -> "🚕"
        FinanceCategory.ENTERTAINMENT -> "🎮"
        FinanceCategory.STUDY -> "📚"
        FinanceCategory.BILL -> "🧾"
        FinanceCategory.SALARY -> "💵"
        FinanceCategory.INVESTMENT -> "📈"
        FinanceCategory.OTHER -> "📦"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF121222))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E1E2E)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = categoryIcon, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = tx.note, color = Color.White, style = MaterialTheme.typography.titleSmall)
            Text(text = tx.category.name, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        }

        Text(
            text = "$sign${formatter.format(tx.amount)}",
            color = amountColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (onDelete != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (confirmDelete) Color(0xFFFF5252).copy(0.2f) else Color.Transparent)
                    .clickable {
                        if (confirmDelete) { onDelete(); confirmDelete = false }
                        else confirmDelete = true
                    }
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (confirmDelete) "✓" else "🗑",
                    fontSize = 16.sp,
                    color = if (confirmDelete) Color(0xFFFF5252) else Color.Gray
                )
            }
        }
    }
}

// ── Add Transaction Dialog Component ─────────────────────────────────────────

@Composable
private fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Long, type: TransactionType, category: FinanceCategory, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf(FinanceCategory.OTHER) }

    val categories = FinanceCategory.entries
    val categoryIcons = mapOf(
        FinanceCategory.FOOD to "🍔", FinanceCategory.TRANSPORT to "🚕",
        FinanceCategory.ENTERTAINMENT to "🎮", FinanceCategory.STUDY to "📚",
        FinanceCategory.BILL to "🧾", FinanceCategory.SALARY to "💵",
        FinanceCategory.INVESTMENT to "📈", FinanceCategory.OTHER to "📦"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2F),
        title = { Text("Thêm Giao Dịch", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type toggle
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf(TransactionType.INCOME to "Thu Nhập", TransactionType.EXPENSE to "Chi Tiêu")
                        .forEach { (type, label) ->
                            val selected = selectedType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) md_theme_dark_primary.copy(0.3f) else Color.Transparent)
                                    .border(1.dp, if (selected) md_theme_dark_primary else Color.Gray, RoundedCornerShape(8.dp))
                                    .clickable { selectedType = type }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (selected) md_theme_dark_primary else Color.Gray, fontWeight = FontWeight.SemiBold)
                            }
                            if (type == TransactionType.INCOME) Spacer(Modifier.width(8.dp))
                        }
                }

                // Amount input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("Số tiền (VNĐ)", color = Color.Gray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = md_theme_dark_primary,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Note input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = md_theme_dark_primary,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Category chips
                Text("Danh mục", color = Color.Gray, fontSize = 12.sp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { cat ->
                                val selected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) md_theme_dark_primary.copy(0.25f) else Color(0xFF121222))
                                        .border(0.5.dp, if (selected) md_theme_dark_primary else Color.Gray, RoundedCornerShape(6.dp))
                                        .clickable { selectedCategory = cat }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(categoryIcons[cat] ?: "📦", fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: return@Button
                    onConfirm(amount, selectedType, selectedCategory, note.ifBlank { selectedCategory.name })
                },
                colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary)
            ) { Text("LƯU", color = Color.Black, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("HỦY", color = Color.Gray) }
        }
    )
}

// ── Budget UI Card Component ────────────────────────────────────────────────

@Composable
private fun BudgetCard(
    budget: BudgetEntity,
    spent: Long,
    formatter: NumberFormat,
    onDelete: () -> Unit
) {
    val categoryIcons = getCategoryIcons()
    val pct = if (budget.limitAmount > 0) (spent.toFloat() / budget.limitAmount.toFloat()) else 0f
    val isOver = spent > budget.limitAmount
    val progressColor = if (isOver) Color(0xFFFF5252) else md_theme_dark_primary
    var confirmDelete by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF121222))
            .border(0.5.dp, progressColor.copy(0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E1E2E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = categoryIcons[budget.category] ?: "📦", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = budget.category.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "Đã tiêu: ${formatter.format(spent)} / Hạn mức: ${formatter.format(budget.limitAmount)}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (confirmDelete) Color(0xFFFF5252).copy(0.2f) else Color.Transparent)
                        .clickable {
                            if (confirmDelete) { onDelete(); confirmDelete = false }
                            else confirmDelete = true
                        }
                        .padding(6.dp)
                ) {
                    Text(
                        text = if (confirmDelete) "✓ Xoá" else "🗑",
                        fontSize = 12.sp,
                        color = if (confirmDelete) Color(0xFFFF5252) else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { pct.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = Color(0xFF2A2A3E)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(pct * 100).toInt()}% đã tiêu",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
                if (isOver) {
                    Text(
                        text = "⚠️ Vượt hạn mức chi tiêu!",
                        color = Color(0xFFFF5252),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Debt Item Card Component ─────────────────────────────────────────────────

@Composable
private fun DebtItemCard(
    debt: DebtEntity,
    formatter: NumberFormat,
    onTogglePaid: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(debt.timestamp)
    val isBorrowed = debt.type == DebtType.BORROWED
    val primaryColor = if (isBorrowed) Color(0xFFFF5252) else Color(0xFF40E17E)
    var confirmDelete by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF121222))
            .border(0.5.dp, if (debt.isPaid) Color.Gray.copy(0.3f) else primaryColor.copy(0.4f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox/Paid status toggle
        Checkbox(
            checked = debt.isPaid,
            onCheckedChange = onTogglePaid,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Gray,
                uncheckedColor = primaryColor
            )
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = debt.personName,
                color = if (debt.isPaid) Color.Gray else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textDecoration = if (debt.isPaid) TextDecoration.LineThrough else TextDecoration.None
            )
            if (debt.note.isNotBlank()) {
                Text(
                    text = debt.note,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    textDecoration = if (debt.isPaid) TextDecoration.LineThrough else TextDecoration.None
                )
            }
            Text(
                text = "Ngày tạo: $dateStr",
                color = Color.DarkGray,
                fontSize = 9.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = formatter.format(debt.amount),
            color = if (debt.isPaid) Color.Gray else primaryColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            textDecoration = if (debt.isPaid) TextDecoration.LineThrough else TextDecoration.None
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (confirmDelete) Color(0xFFFF5252).copy(0.2f) else Color.Transparent)
                .clickable {
                    if (confirmDelete) { onDelete(); confirmDelete = false }
                    else confirmDelete = true
                }
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (confirmDelete) "✓" else "🗑",
                fontSize = 14.sp,
                color = if (confirmDelete) Color(0xFFFF5252) else Color.Gray
            )
        }
    }
}

// ── Add Budget Dialog ────────────────────────────────────────────────────────

@Composable
private fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: FinanceCategory, limit: Long) -> Unit
) {
    var limitText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(FinanceCategory.FOOD) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2F),
        title = { Text("Đặt Hạn Mức Hũ Chi Tiêu", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Limit amount input
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it.filter { c -> c.isDigit() } },
                    label = { Text("Hạn mức tối đa (VNĐ)", color = Color.Gray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = md_theme_dark_primary,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Category chips
                Text("Chọn danh mục ngân sách", color = Color.Gray, fontSize = 12.sp)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(FinanceCategory.entries) { cat ->
                        val selected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) md_theme_dark_primary.copy(0.25f) else Color(0xFF121222))
                                .border(0.5.dp, if (selected) md_theme_dark_primary else Color.Gray, RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cat.name, fontSize = 10.sp, color = if (selected) md_theme_dark_primary else Color.LightGray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toLongOrNull() ?: return@Button
                    onConfirm(selectedCategory, limit)
                },
                colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary)
            ) { Text("LƯU", color = Color.Black, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("HỦY", color = Color.Gray) }
        }
    )
}

// ── Add Debt Dialog ──────────────────────────────────────────────────────────

@Composable
private fun AddDebtDialog(
    onDismiss: () -> Unit,
    onConfirm: (personName: String, amount: Long, type: DebtType, note: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DebtType.BORROWED) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2F),
        title = { Text("Thêm Ghi Nợ", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type selector (Borrowed / Lent)
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf(DebtType.BORROWED to "Tôi đi nợ", DebtType.LENT to "Cho người khác nợ")
                        .forEach { (type, label) ->
                            val selected = selectedType == type
                            val color = if (type == DebtType.BORROWED) Color(0xFFFF5252) else Color(0xFF40E17E)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) color.copy(0.2f) else Color.Transparent)
                                    .border(1.dp, if (selected) color else Color.Gray, RoundedCornerShape(8.dp))
                                    .clickable { selectedType = type }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (selected) color else Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                            if (type == DebtType.BORROWED) Spacer(Modifier.width(8.dp))
                        }
                }

                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên người nợ / cho nợ", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = md_theme_dark_primary,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Amount input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("Số tiền (VNĐ)", color = Color.Gray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = md_theme_dark_primary,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Note input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú/Mục đích", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = md_theme_dark_primary,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: return@Button
                    if (name.isBlank()) return@Button
                    onConfirm(name.trim(), amount, selectedType, note.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary)
            ) { Text("THÊM", color = Color.Black, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("HỦY", color = Color.Gray) }
        }
    )
}
