package com.systemleveling.feature.quests.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.core.model.RewardResult
import com.systemleveling.core.database.entity.QuestEntity
import kotlinx.coroutines.flow.collectLatest
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.feature.quests.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

// ── Root ──────────────────────────────────────────────────────────────────────
@Composable
fun QuestListScreen(
    viewModel: QuestViewModel,
    onBack: () -> Unit
) {
    val quests by viewModel.quests.collectAsState(initial = emptyList())
    val workPlanItems by viewModel.workPlanItems.collectAsState(initial = emptyList())
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationError by viewModel.generationError.collectAsState()
    val user by viewModel.user.collectAsState()
    var rewardToShow by remember { mutableStateOf<RewardResult?>(null) }
    var penaltyToShow by remember { mutableStateOf<QuestViewModel.PenaltyEvent?>(null) }
    var showWorkPlanSheet by remember { mutableStateOf(false) }
    var selectedQuest by remember { mutableStateOf<QuestEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.rewardResult.collectLatest { result ->
            rewardToShow = result
        }
    }
    LaunchedEffect(Unit) {
        viewModel.penaltyEvent.collect { event ->
            penaltyToShow = event
        }
    }

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
            val todayStr = remember {
                SimpleDateFormat("EEEE — dd/MM/yyyy", Locale("vi", "VN")).format(Date()).uppercase()
            }

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
            val completed = remember(quests) { quests.count { it.status == QuestStatus.COMPLETED } }
            val total = remember(quests) { quests.size }
            val progress = remember(completed, total) { if (total > 0) completed.toFloat() / total else 0f }

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

            // ── Generation error banner ──────────────────────────────────────
            generationError?.let { errMsg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2A0A0A))
                        .border(1.dp, Color(0xFFFF5252).copy(0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚠️", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            errMsg,
                            color = Color(0xFFFF8A80),
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.regenerateQuests() }
                                .background(Color(0xFF4A9EFF).copy(0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Thử lại", color = PRIMARY, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Timeline ──────────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(quests, key = { it.id }) { quest ->
                    QuestTimelineItem(
                        quest = quest,
                        onClick = { selectedQuest = quest },
                        onExpired = { viewModel.failExpiredQuest(quest) }
                    )
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

        // ── In-app penalty banner (slides from top when quest expires) ────────
        AnimatedVisibility(
            visible = penaltyToShow != null,
            enter = slideInVertically { -it } + fadeIn(tween(220)),
            exit = slideOutVertically { -it } + fadeOut(tween(180)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            penaltyToShow?.let { event ->
                PenaltyBanner(event = event, onDismiss = { penaltyToShow = null })
            }
        }

        // ── Reward dialog ─────────────────────────────────────────────────────
        rewardToShow?.let { result ->
            QuestCompleteDialog(
                result = result,
                onDismiss = { rewardToShow = null }
            )
        }

        // ── Quest Detail Sheet ────────────────────────────────────────────────
        selectedQuest?.let { quest ->
            QuestDetailSheet(
                quest = quest,
                onDismiss = { selectedQuest = null },
                onComplete = { 
                    viewModel.completeQuest(it)
                    selectedQuest = null
                }
            )
        }
    }
}
