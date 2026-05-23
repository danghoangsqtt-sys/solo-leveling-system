package com.systemleveling.feature.home.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.designsystem.theme.md_theme_dark_primary
import com.systemleveling.core.model.PlanItem
import com.systemleveling.core.model.PlanScope
import com.systemleveling.core.model.WorkPlanItem
import com.systemleveling.core.model.WorkPriority

@Composable
fun DailySummaryScreen(
    viewModel: DailySummaryViewModel,
    onBack: () -> Unit
) {
    val summary by viewModel.summary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isBefore22hAndEmpty by viewModel.isBefore22hAndEmpty.collectAsState()
    val statChanges by viewModel.statChanges.collectAsState()
    val skillProgress by viewModel.skillProgress.collectAsState()
    val tomorrowPlan by viewModel.tomorrowPlan.collectAsState()
    val tomorrowWorkPlan by viewModel.tomorrowWorkPlan.collectAsState()
    val weeklyPlanItems by viewModel.weeklyPlanItems.collectAsState()
    val monthlyPlanItems by viewModel.monthlyPlanItems.collectAsState()
    val plansSaved by viewModel.plansSaved.collectAsState()
    val todayIncome by viewModel.todayFinanceIncome.collectAsState()
    val todayExpense by viewModel.todayFinanceExpense.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(md_theme_dark_background)
    ) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = md_theme_dark_primary)
                    Spacer(Modifier.height(16.dp))
                    Text("Đang tạo báo cáo...", color = Color.White)
                }
            }
        } else if (isBefore22hAndEmpty) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "◀ HOME",
                    color = md_theme_dark_primary,
                    modifier = Modifier
                        .clickable { onBack() }
                        .align(Alignment.Start)
                        .padding(bottom = 32.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "📊 BÁO CÁO CHƯA SẴN SÀNG",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Báo cáo tổng hợp hằng ngày sẽ tự động được tạo sau 22h00.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.generateSummaryEarly() },
                    colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary)
                ) {
                    Text("TẠO BÁO CÁO SỚM", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
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
                            text = "📊 BÁO CÁO NGÀY",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(48.dp))
                    }
                }

                // Grade Card
                item {
                    summary?.let { s ->
                        GradeCard(
                            completionRate = s.completionRate,
                            streak = s.currentStreak,
                            expEarned = s.expEarned,
                            goldEarned = s.goldEarned
                        )
                    }
                }

                // Finance Today
                item {
                    val net = todayIncome - todayExpense
                    val netColor = if (net >= 0) Color(0xFF40E17E) else Color(0xFFFF5252)
                    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN"))
                    SectionCard(title = "💰 TÀI CHÍNH HÔM NAY", icon = "💰", borderColor = Color(0xFFFFD700).copy(0.3f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("+${formatter.format(todayIncome)}", color = Color(0xFF40E17E), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Thu nhập", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("-${formatter.format(todayExpense)}", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Chi tiêu", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(formatter.format(net), color = netColor, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Text("Thuần", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Quest Stats
                item {
                    summary?.let { s ->
                        SectionCard(
                            title = "⚔️ NHIỆM VỤ",
                            icon = "⚔️"
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatPill("Hoàn thành", "${s.completedQuests}", Color(0xFF40E17E))
                                StatPill("Thất bại", "${s.failedQuests}", Color(0xFFFF5252))
                                StatPill("Tổng", "${s.totalQuests}", Color(0xFF4A9EFF))
                            }
                            Spacer(Modifier.height(12.dp))
                            // Progress bar
                            LinearProgressIndicator(
                                progress = { s.completionRate.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = when {
                                    s.completionRate >= 0.7 -> Color(0xFF40E17E)
                                    s.completionRate >= 0.5 -> Color(0xFFFFD700)
                                    else -> Color(0xFFFF5252)
                                },
                                trackColor = Color(0xFF2A2A3E)
                            )
                            Text(
                                text = "${(s.completionRate * 100).toInt()}% hoàn thành",
                                color = Color.Gray,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Stat Changes
                item {
                    if (statChanges.isNotEmpty()) {
                        SectionCard(title = "📈 PHÁT TRIỂN STAT", icon = "📈") {
                            statChanges.forEach { (stat, gain) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stat,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "+$gain",
                                        color = Color(0xFF40E17E),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }

                // Skill Progress
                item {
                    if (skillProgress.isNotEmpty()) {
                        SectionCard(title = "🌟 TIẾN ĐỘ KỸ NĂNG", icon = "🌟") {
                            skillProgress.forEach { (skill, sp) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(skill, color = Color.White)
                                    Text("+${sp}SP", color = Color(0xFF4A9EFF), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Penalty Zone
                item {
                    summary?.let { s ->
                        if (s.debtPointsGained > 0 || s.currentDebtTotal > 0) {
                            SectionCard(
                                title = "⚠️ PENALTY ZONE",
                                icon = "⚠️",
                                borderColor = Color(0xFFFF5252)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatPill("Nợ hôm nay", "+${s.debtPointsGained}", Color(0xFFFF5252))
                                    StatPill("Tổng nợ", "${s.currentDebtTotal}", Color(0xFFFF9800))
                                }
                                if (s.currentDebtTotal >= 3) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = when {
                                            s.currentDebtTotal >= 10 -> "🚨 NGUY HIỂM! Có nguy cơ Level Down!"
                                            s.currentDebtTotal >= 5 -> "⚠️ Cảnh báo! Đã mất 10% Gold!"
                                            else -> "⚡ Cẩn thận! Sắp đến ngưỡng phạt Gold!"
                                        },
                                        color = Color(0xFFFF5252),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Items Dropped
                item {
                    summary?.let { s ->
                        if (s.itemsDropped > 0) {
                            SectionCard(title = "🎁 VẬT PHẨM THU ĐƯỢC", icon = "🎁") {
                                Text(
                                    text = "${s.itemsDropped} vật phẩm mới!",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Vào Kho Vật Phẩm để xem chi tiết",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // AI Journal
                item {
                    summary?.let { s ->
                        if (s.aiJournalContent.isNotBlank()) {
                            SectionCard(title = "📝 NHẬT KÝ AI", icon = "📝") {
                                Text(
                                    text = s.aiJournalContent,
                                    color = Color(0xFFCCCCCC),
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }

                // Tomorrow Plan
                item {
                    SectionCard(title = "📋 KẾ HOẠCH NGÀY MAI", icon = "📋") {
                        if (tomorrowPlan.isEmpty()) {
                            Text(
                                text = "AI sẽ gợi ý kế hoạch cho ngày mai...",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            tomorrowPlan.forEachIndexed { index, todo ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Priority indicator
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (todo.priority) {
                                                    "HIGH" -> Color(0xFFFF5252)
                                                    "MEDIUM" -> Color(0xFFFFD700)
                                                    else -> Color(0xFF40E17E)
                                                }
                                            )
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = todo.title,
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (todo.deadline.isNotBlank()) {
                                            Text(
                                                text = "⏰ ${todo.deadline}",
                                                color = Color.Gray,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                    Text(
                                        text = todo.priority,
                                        color = when (todo.priority) {
                                            "HIGH" -> Color(0xFFFF5252)
                                            "MEDIUM" -> Color(0xFFFFD700)
                                            else -> Color(0xFF40E17E)
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Planning sections ─────────────────────────────────────────
                item {
                    PlanningSection(
                        title = "📅 KẾ HOẠCH NGÀY MAI",
                        subtitle = "Quest AI sẽ dùng danh sách này để tạo nhiệm vụ ngày mai",
                        items = tomorrowWorkPlan.map { it.toPlanDisplayItem() },
                        onAdd = { title, priority, deadline ->
                            viewModel.addTomorrowItem(
                                WorkPlanItem(title = title, priority = priority, deadline = deadline)
                            )
                        },
                        onRemove = { id -> viewModel.removeTomorrowItem(id) },
                        accentColor = Color(0xFF4A9EFF)
                    )
                }

                item {
                    PlanningSection(
                        title = "📆 KẾ HOẠCH TUẦN",
                        subtitle = "Mục tiêu tuần này — AI sẽ điều chỉnh quest dài hạn",
                        items = weeklyPlanItems.map { it.toPlanDisplayItem() },
                        onAdd = { title, priority, deadline ->
                            viewModel.addWeeklyItem(
                                PlanItem(title = title, priority = priority, deadline = deadline, scope = PlanScope.WEEKLY.name)
                            )
                        },
                        onRemove = { id -> viewModel.removeWeeklyItem(id) },
                        accentColor = Color(0xFFE040FB)
                    )
                }

                item {
                    PlanningSection(
                        title = "🗓️ KẾ HOẠCH THÁNG",
                        subtitle = "Mục tiêu lớn trong tháng — AI sẽ dùng để định hướng quest tuần",
                        items = monthlyPlanItems.map { it.toPlanDisplayItem() },
                        onAdd = { title, priority, deadline ->
                            viewModel.addMonthlyItem(
                                PlanItem(title = title, priority = priority, deadline = deadline, scope = PlanScope.MONTHLY.name)
                            )
                        },
                        onRemove = { id -> viewModel.removeMonthlyItem(id) },
                        accentColor = Color(0xFFFFAB40)
                    )
                }

                // Save all plans button
                item {
                    val btnColor = if (plansSaved) Color(0xFF40E17E) else Color(0xFF4A9EFF)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(btnColor.copy(0.3f), btnColor.copy(0.15f))))
                            .border(1.dp, btnColor.copy(0.7f), RoundedCornerShape(14.dp))
                            .clickable(enabled = !plansSaved) { viewModel.saveAllPlans() }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (plansSaved) "✅ KẾ HOẠCH ĐÃ LƯU!" else "💾 LƯU TẤT CẢ KẾ HOẠCH",
                            color = btnColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Bottom spacer
                item { Spacer(Modifier.height(48.dp)) }
            }
        }
    }
}

@Composable
private fun GradeCard(
    completionRate: Double,
    streak: Int,
    expEarned: Int,
    goldEarned: Int
) {
    val grade = when {
        completionRate >= 0.95 -> "S"
        completionRate >= 0.85 -> "A"
        completionRate >= 0.70 -> "B"
        completionRate >= 0.50 -> "C"
        completionRate >= 0.30 -> "D"
        else -> "F"
    }
    val gradeColor = when (grade) {
        "S" -> Color(0xFFFFD700)
        "A" -> Color(0xFF40E17E)
        "B" -> Color(0xFF4A9EFF)
        "C" -> Color(0xFFFF9800)
        "D" -> Color(0xFFFF5252)
        else -> Color(0xFF666666)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        gradeColor.copy(alpha = 0.3f),
                        Color(0xFF1E1E2E)
                    )
                )
            )
            .border(1.dp, gradeColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ĐÁNH GIÁ NGÀY",
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = grade,
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = gradeColor
            )
            Text(
                text = when (grade) {
                    "S" -> "HOÀN HẢO!"
                    "A" -> "XUẤT SẮC!"
                    "B" -> "TỐT LẮM!"
                    "C" -> "CẦN CỐ GẮNG"
                    "D" -> "CHƯA ĐẠT"
                    else -> "THẤT BẠI"
                },
                color = gradeColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥", fontSize = 20.sp)
                    Text("$streak ngày", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Streak", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚡", fontSize = 20.sp)
                    Text("+$expEarned", color = Color(0xFF40E17E), fontWeight = FontWeight.Bold)
                    Text("EXP", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💰", fontSize = 20.sp)
                    Text("+$goldEarned", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                    Text("Gold", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: String,
    borderColor: Color = md_theme_dark_primary.copy(alpha = 0.3f),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E2E))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .animateContentSize()
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Text(
            text = label,
            color = Color.Gray,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

// ── Planning helpers ─────────────────────────────────────────────────────────

private data class PlanDisplayItem(val id: String, val title: String, val priority: String, val deadline: String)

private fun WorkPlanItem.toPlanDisplayItem() = PlanDisplayItem(id, title, priority, deadline)
private fun PlanItem.toPlanDisplayItem() = PlanDisplayItem(id, title, priority, deadline)

private fun WorkPriority.planColor(): Color = when (this) {
    WorkPriority.CRITICAL -> Color(0xFFFF4757)
    WorkPriority.HIGH     -> Color(0xFFFF7F50)
    WorkPriority.NORMAL   -> Color(0xFF4A9EFF)
    WorkPriority.LOW      -> Color(0xFF2ED573)
}

@Composable
private fun PlanningSection(
    title: String,
    subtitle: String,
    items: List<PlanDisplayItem>,
    onAdd: (title: String, priority: String, deadline: String) -> Unit,
    onRemove: (id: String) -> Unit,
    accentColor: Color
) {
    var showForm by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDeadline by remember { mutableStateOf("") }
    var newPriority by remember { mutableStateOf(WorkPriority.NORMAL) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E2E))
            .border(1.dp, accentColor.copy(0.3f), RoundedCornerShape(12.dp))
            .animateContentSize()
            .padding(16.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall)
                    Text(subtitle, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accentColor.copy(0.15f))
                        .border(1.dp, accentColor.copy(0.5f), CircleShape)
                        .clickable { showForm = !showForm }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (showForm) "✕" else "+", color = accentColor,
                        fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Inline add form
            AnimatedVisibility(visible = showForm, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Tên công việc *", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF14142A), unfocusedContainerColor = Color(0xFF14142A),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedIndicatorColor = accentColor, unfocusedIndicatorColor = Color(0x33FFFFFF)
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        WorkPriority.entries.forEach { p ->
                            val sel = newPriority == p
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) p.planColor().copy(0.2f) else Color.Transparent)
                                    .border(1.dp, if (sel) p.planColor().copy(0.8f) else Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                    .clickable { newPriority = p }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(p.label.take(10), color = if (sel) p.planColor() else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = newDeadline,
                        onValueChange = { newDeadline = it },
                        label = { Text("Deadline (HH:mm hoặc dd/MM)", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF14142A), unfocusedContainerColor = Color(0xFF14142A),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedIndicatorColor = accentColor, unfocusedIndicatorColor = Color(0x33FFFFFF)
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                .background(Color(0x0FFFFFFF))
                                .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                .clickable { showForm = false; newTitle = ""; newDeadline = "" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Huỷ", color = Color.Gray, fontSize = 12.sp) }
                        Box(
                            modifier = Modifier.weight(2f).clip(RoundedCornerShape(8.dp))
                                .background(if (newTitle.isNotBlank()) accentColor.copy(0.2f) else Color(0x0AFFFFFF))
                                .border(1.dp, if (newTitle.isNotBlank()) accentColor.copy(0.7f) else Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                .clickable(enabled = newTitle.isNotBlank()) {
                                    onAdd(newTitle.trim(), newPriority.name, newDeadline.trim())
                                    newTitle = ""; newDeadline = ""; showForm = false
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✅ Thêm", color = if (newTitle.isNotBlank()) accentColor else Color.Gray,
                                fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Item list
            if (items.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                items.forEach { item ->
                    val pColor = try { WorkPriority.valueOf(item.priority).planColor() } catch (_: Exception) { accentColor }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(pColor.copy(0.06f))
                            .border(0.5.dp, pColor.copy(0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(pColor))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            if (item.deadline.isNotBlank()) {
                                Text("⏰ ${item.deadline}", color = Color(0xFFFFD700), fontSize = 9.sp)
                            }
                        }
                        Box(
                            modifier = Modifier.clip(CircleShape).clickable { onRemove(item.id) }.padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("✕", color = Color.Gray.copy(0.6f), fontSize = 11.sp) }
                    }
                }
            } else if (!showForm) {
                Spacer(Modifier.height(8.dp))
                Text("Chưa có mục nào — nhấn + để thêm", color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
