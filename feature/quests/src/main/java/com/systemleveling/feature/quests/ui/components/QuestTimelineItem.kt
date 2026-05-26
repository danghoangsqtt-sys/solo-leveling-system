package com.systemleveling.feature.quests.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.model.QuestStatus

// ── Quest timeline item ────────────────────────────────────────────────────────
@Composable
fun QuestTimelineItem(
    quest: QuestEntity,
    onClick: () -> Unit,
    onExpired: () -> Unit = {}
) {
    val meta = rankMeta(quest.rank)
    val isCompleted = quest.status == QuestStatus.COMPLETED
    val isFailed = quest.status == QuestStatus.FAILED || quest.status == QuestStatus.EXPIRED
    // Tracks real-time timer expiry before DB status updates
    var localExpired by remember(quest.id) { mutableStateOf(false) }

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
                .clickable { onClick() }
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
                    QuestTimer(
                        timeStart = timeStart,
                        durationMinutes = quest.durationMinutes,
                        onExpired = {
                            localExpired = true
                            onExpired()
                        }
                    )
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
internal fun MiniRewardBadge(icon: String, text: String, color: Color, dimmed: Boolean) {
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
