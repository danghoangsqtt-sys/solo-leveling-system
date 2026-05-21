package com.systemleveling.feature.quests.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.designsystem.components.GlassCard
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.designsystem.theme.md_theme_dark_outline
import com.systemleveling.core.designsystem.theme.md_theme_dark_primary
import com.systemleveling.core.model.QuestRank
import com.systemleveling.core.model.QuestStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun QuestListScreen(
    viewModel: QuestViewModel,
    onBack: () -> Unit
) {
    val quests by viewModel.quests.collectAsState()
    var selectedQuest by remember { mutableStateOf<QuestEntity?>(null) }

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
                    text = "🔥 Streak: 12",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val dateFormat = SimpleDateFormat("EEEE — dd/MM/yyyy", Locale("vi", "VN"))
            val todayStr = dateFormat.format(Date()).uppercase()
            Text(
                text = "═══ $todayStr ═══",
                style = MaterialTheme.typography.titleLarge,
                color = md_theme_dark_outline,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Timeline
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(quests) { quest ->
                    QuestTimelineItem(quest = quest) {
                        if (quest.status != QuestStatus.COMPLETED) {
                            viewModel.completeQuest(quest)
                            selectedQuest = quest
                        }
                    }
                }
            }
        }

        // Quest Complete Dialog
        selectedQuest?.let { quest ->
            QuestCompleteDialog(
                quest = quest,
                onDismiss = { selectedQuest = null }
            )
        }
    }
}

@Composable
fun QuestTimelineItem(quest: QuestEntity, onComplete: () -> Unit) {
    val rankColor = when (quest.rank) {
        QuestRank.E -> Color.White
        QuestRank.D -> Color(0xFF40E17E) // Green
        QuestRank.C -> Color(0xFF4A9EFF) // Blue
        QuestRank.B -> Color(0xFFE040FB) // Purple
        QuestRank.A -> Color(0xFFFFAB40) // Orange
        QuestRank.S -> Color(0xFFFF5252) // Red Glow
    }

    val isCompleted = quest.status == QuestStatus.COMPLETED

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isCompleted) { onComplete() },
        verticalAlignment = Alignment.Top
    ) {
        // Time Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            Text(
                text = quest.timeStart ?: "--:--",
                style = MaterialTheme.typography.labelSmall,
                color = if (isCompleted) md_theme_dark_outline else Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp)
                    .background(if (isCompleted) md_theme_dark_outline else rankColor)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Quest Card
        GlassCard(
            modifier = Modifier.weight(1f),
            backgroundColor = if (isCompleted) Color(0x33121222) else Color(0x99121222),
            borderColor = if (isCompleted) md_theme_dark_outline else rankColor
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "[${quest.rank}] ${quest.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCompleted) md_theme_dark_outline else rankColor,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                    )
                    Text(
                        text = if (isCompleted) "✅ Done" else "○ Pending",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) Color(0xFF40E17E) else md_theme_dark_outline
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "→ ${quest.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCompleted) md_theme_dark_outline else Color(0xFFE3E0F8)
                )
                
                val timeStart = quest.timeStart
                if (!isCompleted && timeStart != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    QuestTimer(timeStart, quest.durationMinutes)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "→ +${quest.expReward} EXP  +${quest.goldReward} Gold",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCompleted) md_theme_dark_outline else Color(0xFFFFDB3C)
                )
            }
        }
    }
}

@Composable
fun QuestTimer(timeStart: String, durationMinutes: Int) {
    var remainingText by remember { mutableStateOf("--:--:--") }
    var isUrgent by remember { mutableStateOf(false) }

    LaunchedEffect(timeStart, durationMinutes) {
        val parts = timeStart.split(":")
        if (parts.size >= 2) {
            val hour = parts[0].toIntOrNull() ?: 0
            val min = parts[1].toIntOrNull() ?: 0
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, min)
            calendar.set(Calendar.SECOND, 0)
            
            // Add duration
            calendar.add(Calendar.MINUTE, durationMinutes)
            val endTimeMs = calendar.timeInMillis
            
            while (true) {
                val now = System.currentTimeMillis()
                val diff = endTimeMs - now
                
                if (diff <= 0) {
                    remainingText = "EXPIRED"
                    isUrgent = true
                    break
                } else {
                    val h = (diff / (1000 * 60 * 60)) % 24
                    val m = (diff / (1000 * 60)) % 60
                    val s = (diff / 1000) % 60
                    remainingText = String.format("%02d:%02d:%02d", h, m, s)
                    isUrgent = diff < 15 * 60 * 1000 // Less than 15 mins
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isUrgent) Color(0x33FF5252) else Color(0x1AFFFFFF))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⏳",
            fontSize = 12.sp
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = remainingText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (remainingText == "EXPIRED") Color(0xFFFF5252)
                    else if (isUrgent) Color(0xFFFFAB40)
                    else Color(0xFF4A9EFF)
        )
    }
}
