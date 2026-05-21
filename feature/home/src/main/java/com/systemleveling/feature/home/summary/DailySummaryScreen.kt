package com.systemleveling.feature.home.summary

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.designsystem.theme.md_theme_dark_primary

@Composable
fun DailySummaryScreen(
    viewModel: DailySummaryViewModel,
    onBack: () -> Unit
) {
    val summary by viewModel.summary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statChanges by viewModel.statChanges.collectAsState()
    val skillProgress by viewModel.skillProgress.collectAsState()
    val tomorrowPlan by viewModel.tomorrowPlan.collectAsState()

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

                // Bottom spacer
                item { Spacer(Modifier.height(32.dp)) }
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
