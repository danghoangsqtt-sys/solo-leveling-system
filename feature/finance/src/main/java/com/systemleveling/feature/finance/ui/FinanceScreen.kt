package com.systemleveling.feature.finance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemleveling.core.database.entity.TransactionEntity
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.designsystem.theme.md_theme_dark_primary
import com.systemleveling.core.model.FinanceCategory
import com.systemleveling.core.model.TransactionType
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FinanceScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val income by viewModel.totalIncome.collectAsState()
    val expense by viewModel.totalExpense.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, type, category, note ->
                viewModel.addTransaction(amount, type, category, note)
                showAddDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(md_theme_dark_background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).padding(top = 32.dp)
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
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dashboard Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E2E))
                    .border(1.dp, Color(0xFF2F2F40), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "TỔNG SỐ DƯ", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = formatter.format(balance),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(text = "THU NHẬP (INCOME)", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "+${formatter.format(income)}",
                                color = Color(0xFF40E17E), // Green
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "CHI TIÊU (EXPENSE)", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "-${formatter.format(expense)}",
                                color = Color(0xFFFF5252), // Red
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "LỊCH SỬ GIAO DỊCH", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Transaction List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(transactions) { tx ->
                    TransactionItem(tx = tx, formatter = formatter)
                }
            }
        }
        
        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = md_theme_dark_primary
        ) {
            Text("+", fontSize = 24.sp, color = Color.White)
        }
    }
}

@Composable
fun TransactionItem(tx: TransactionEntity, formatter: NumberFormat) {
    val isIncome = tx.type == TransactionType.INCOME
    val amountColor = if (isIncome) Color(0xFF40E17E) else Color(0xFFFF5252)
    val sign = if (isIncome) "+" else "-"
    
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
    }
}

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
            ) { Text("LƯU") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("HỦY", color = Color.Gray) }
        }
    )
}
