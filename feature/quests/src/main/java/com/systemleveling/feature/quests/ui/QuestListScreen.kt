package com.systemleveling.feature.quests.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.core.model.RewardResult
import com.systemleveling.core.model.WorkPlanItem
import com.systemleveling.core.database.entity.QuestEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import com.systemleveling.core.designsystem.components.GlassCard
import com.systemleveling.core.model.QuestRank
import com.systemleveling.core.model.QuestStatus
import java.text.SimpleDateFormat
import java.util.*

// ── Rank meta ─────────────────────────────────────────────────────────────────
private data class RankMeta(val color: Color, val bg: Color, val label: String, val glow: Boolean = false)

private fun rankMeta(rank: QuestRank) = when (rank) {
    QuestRank.E -> RankMeta(Color(0xFFAAAAAA), Color(0x22AAAAAA), "E")
    QuestRank.D -> RankMeta(Color(0xFF2ED573), Color(0x222ED573), "D")
    QuestRank.C -> RankMeta(Color(0xFF4A9EFF), Color(0x224A9EFF), "C")
    QuestRank.B -> RankMeta(Color(0xFFE040FB), Color(0x22E040FB), "B")
    QuestRank.A -> RankMeta(Color(0xFFFFAB40), Color(0x22FFAB40), "A", glow = true)
    QuestRank.S -> RankMeta(Color(0xFFFF5252), Color(0x33FF5252), "S", glow = true)
}

// ── Root ──────────────────────────────────────────────────────────────────────
@Composable
fun QuestListScreen(
    viewModel: QuestViewModel,
    onBack: () -> Unit
) {
    val quests by viewModel.quests.collectAsState()
    val workPlanItems by viewModel.workPlanItems.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val user by viewModel.user.collectAsState()
    var rewardToShow by remember { mutableStateOf<RewardResult?>(null) }
    var showWorkPlanSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.rewardResult.collectLatest { result ->
            rewardToShow = result
        }
    }

    val BG_DEEP  = Color(0xFF050508)
    val PRIMARY  = Color(0xFF4A9EFF)
    val GOLD     = Color(0xFFFFD700)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF0D0D22), BG_DEEP),
                    radius = 1600f
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xAA090912))
                    .border(BorderStroke(0.5.dp, Color(0x1FFFFFFF)))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onBack() }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("◀", color = PRIMARY, fontSize = 10.sp)
                        Spacer(Modifier.width(4.dp))
                        Text("HOME", color = PRIMARY, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08f.em)
                    }

                    // Center: title
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "📋 KẾ HOẠCH",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.1f.em
                        )
                    }

                    // Right: streak + plan
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (workPlanItems.isNotEmpty()) Color(0xFF3D1A00) else Color(0xFF0D1A2E)
                                )
                                .border(
                                    0.5.dp,
                                    if (workPlanItems.isNotEmpty()) Color(0xFFFF7043).copy(0.6f) else PRIMARY.copy(0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { showWorkPlanSheet = true }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (workPlanItems.isNotEmpty()) "📋 ${workPlanItems.size} việc" else "📋 Thêm việc",
                                color = if (workPlanItems.isNotEmpty()) Color(0xFFFF7043) else PRIMARY,
                                fontSize = 10.sp, fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2D1500))
                                .border(0.5.dp, Color(0xFFFF6B00).copy(0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔥", fontSize = 12.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${user?.streak ?: 0}",
                                color = Color(0xFFFF9800),
                                fontSize = 12.sp, fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            if (showWorkPlanSheet) {
                WorkPlanInputSheet(
                    items = workPlanItems,
                    onAdd = { viewModel.addWorkPlanItem(it) },
                    onRemove = { viewModel.removeWorkPlanItem(it) },
                    onDismiss = { showWorkPlanSheet = false },
                    onRegenerateQuests = { viewModel.regenerateQuests() }
                )
            }

            // ── Date header ───────────────────────────────────────────────────
            val dateFormat = SimpleDateFormat("EEEE — dd/MM/yyyy", Locale("vi", "VN"))
            val todayStr = dateFormat.format(Date()).uppercase()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(40.dp).height(0.5.dp).background(Color(0x33FFFFFF)))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "═══ $todayStr ═══",
                        color = Color(0xFF8899CC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.1f.em
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.width(40.dp).height(0.5.dp).background(Color(0x33FFFFFF)))
                }
            }

            // ── Quest stats row ───────────────────────────────────────────────
            val completed = quests.count { it.status == QuestStatus.COMPLETED }
            val total = quests.size
            val progress = if (total > 0) completed.toFloat() / total else 0f

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "NHIỆM VỤ HÔM NAY",
                        color = PRIMARY.copy(0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1f.em
                    )
                    Text(
                        "$completed / $total",
                        color = GOLD,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0x22FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                Brush.horizontalGradient(listOf(PRIMARY, Color(0xFF74BFFF)))
                            )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Timeline ──────────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(quests, key = { it.id }) { quest ->
                    QuestTimelineItem(quest = quest) {
                        if (quest.status != QuestStatus.COMPLETED) {
                            viewModel.completeQuest(quest)
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }

        // ── AI Generating overlay ─────────────────────────────────────────────
        if (isGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC090912)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
                        0.6f, 1f,
                        infiniteRepeatable(tween(700, easing = EaseInOutSine), RepeatMode.Reverse),
                        label = "p"
                    )
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(50))
                            .background(PRIMARY.copy(pulse * 0.15f))
                            .border(1.5.dp, PRIMARY.copy(pulse), RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) { Text("🤖", fontSize = 32.sp) }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "AI ĐANG PHÂN TÍCH...",
                        color = PRIMARY,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.12f.em
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Đang sinh nhiệm vụ cá nhân hóa từ kỹ năng & kế hoạch của bạn",
                        color = Color(0xFF8899CC),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // ── Reward dialog ─────────────────────────────────────────────────────
        rewardToShow?.let { result ->
            QuestCompleteDialog(
                result = result,
                onDismiss = { rewardToShow = null }
            )
        }
    }
}

// ── Quest timeline item ────────────────────────────────────────────────────────
@Composable
fun QuestTimelineItem(quest: QuestEntity, onComplete: () -> Unit) {
    val meta = rankMeta(quest.rank)
    val isCompleted = quest.status == QuestStatus.COMPLETED
    val isFailed = quest.status == QuestStatus.FAILED || quest.status == QuestStatus.EXPIRED

    // Glow pulse for A/S rank pending quests
    val glowAlpha by if (meta.glow && !isCompleted) {
        rememberInfiniteTransition(label = "glow_${quest.id}").animateFloat(
            0.3f, 0.85f,
            infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
            label = "g"
        )
    } else {
        remember { mutableFloatStateOf(0.5f) }
    }

    val cardBg = when {
        isCompleted -> Color(0xFF0A0A14)
        else -> Color(0xFF0F0F20)
    }
    val borderColor = when {
        isCompleted -> Color(0xFF2A2A40)
        else -> meta.color.copy(if (meta.glow) glowAlpha else 0.5f)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // ── Time + timeline spine ─────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(52.dp).padding(top = 2.dp)
        ) {
            Text(
                text = quest.timeStart ?: "--:--",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) Color(0xFF3A3A5A) else Color(0xFF7788AA),
                letterSpacing = 0.04f.em
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        if (isCompleted) SolidColor(Color(0xFF2A2A40))
                        else Brush.verticalGradient(
                            listOf(meta.color.copy(0.9f), meta.color.copy(0.1f))
                        )
                    )
            )
        }

        Spacer(Modifier.width(10.dp))

        // ── Quest card ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(cardBg)
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .clickable(enabled = !isCompleted && !isFailed) { onComplete() }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                // Title row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rank badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(meta.bg)
                            .border(0.5.dp, meta.color.copy(0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = meta.label,
                            color = meta.color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.1f.em
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = quest.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isCompleted) Color(0xFF3A3A5A) else Color.White,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.width(8.dp))

                    // Status indicator
                    when {
                        isCompleted -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✅", fontSize = 11.sp)
                            Spacer(Modifier.width(3.dp))
                            Text("Done", color = Color(0xFF2ED573), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        isFailed -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💀", fontSize = 11.sp)
                            Spacer(Modifier.width(3.dp))
                            Text("Failed", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        else -> Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(meta.color.copy(0.08f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(meta.color.copy(glowAlpha))
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Pending", color = meta.color.copy(0.8f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Description
                Text(
                    text = quest.description,
                    fontSize = 11.sp,
                    color = if (isCompleted) Color(0xFF2D2D45) else Color(0xFF9AA8C4),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )

                // Timer
                val timeStart = quest.timeStart
                if (!isCompleted && !isFailed && timeStart != null) {
                    Spacer(Modifier.height(8.dp))
                    QuestTimer(timeStart, quest.durationMinutes)
                }

                Spacer(Modifier.height(8.dp))

                // Reward footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniRewardBadge("⚡", "+${quest.expReward}", Color(0xFF4A9EFF), isCompleted)
                        MiniRewardBadge("💰", "+${quest.goldReward}", Color(0xFFFFD700), isCompleted)
                    }
                    // Category
                    Text(
                        text = quest.category.uppercase(),
                        fontSize = 9.sp,
                        color = if (isCompleted) Color(0xFF2D2D45) else meta.color.copy(0.5f),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.08f.em
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniRewardBadge(icon: String, text: String, color: Color, dimmed: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(if (dimmed) Color(0xFF111122) else color.copy(0.08f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 10.sp)
        Spacer(Modifier.width(3.dp))
        Text(
            text, color = if (dimmed) Color(0xFF2D2D45) else color,
            fontSize = 10.sp, fontWeight = FontWeight.Bold
        )
    }
}

// ── Quest timer ───────────────────────────────────────────────────────────────
@Composable
fun QuestTimer(timeStart: String, durationMinutes: Int) {
    var remainingText by remember { mutableStateOf("--:--:--") }
    var isUrgent by remember { mutableStateOf(false) }
    var isExpired by remember { mutableStateOf(false) }

    LaunchedEffect(timeStart, durationMinutes) {
        val parts = timeStart.split(":")
        if (parts.size >= 2) {
            val hour = parts[0].toIntOrNull() ?: 0
            val min = parts[1].toIntOrNull() ?: 0
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, min)
            calendar.set(Calendar.SECOND, 0)
            calendar.add(Calendar.MINUTE, durationMinutes)
            val endTimeMs = calendar.timeInMillis

            while (true) {
                val now = System.currentTimeMillis()
                val diff = endTimeMs - now
                if (diff <= 0) {
                    remainingText = "HẾT GIỜ"
                    isExpired = true
                    isUrgent = false
                    break
                } else {
                    val h = (diff / (1000 * 60 * 60)) % 24
                    val m = (diff / (1000 * 60)) % 60
                    val s = (diff / 1000) % 60
                    remainingText = String.format("%02d:%02d:%02d", h, m, s)
                    isUrgent = diff < 15 * 60 * 1000
                }
                delay(1000)
            }
        }
    }

    val timerColor = when {
        isExpired -> Color(0xFFFF5252)
        isUrgent  -> Color(0xFFFFAB40)
        else      -> Color(0xFF4A9EFF)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(timerColor.copy(0.08f))
            .border(0.5.dp, timerColor.copy(0.3f), RoundedCornerShape(5.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (isExpired) "💀" else "⏳", fontSize = 10.sp)
        Spacer(Modifier.width(4.dp))
        Text(
            text = remainingText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = timerColor,
            letterSpacing = 0.04f.em
        )
    }
}
