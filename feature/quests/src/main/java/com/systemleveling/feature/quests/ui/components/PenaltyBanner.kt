package com.systemleveling.feature.quests.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.feature.quests.ui.QuestViewModel
import kotlinx.coroutines.delay

// ── Penalty banner (in-app overlay, slides from top) ─────────────────────────
@Composable
fun PenaltyBanner(
    event: QuestViewModel.PenaltyEvent,
    onDismiss: () -> Unit
) {
    LaunchedEffect(event) {
        delay(5_000)
        onDismiss()
    }

    val pulse = rememberInfiniteTransition(label = "pb")
    val borderAlpha by pulse.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "ba"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xF0180008))
            .border(
                BorderStroke(1.5.dp, Color(0xFFFF2244).copy(borderAlpha))
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text("💀", fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))

            // Content
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "⚠ NHIỆM VỤ THẤT BẠI",
                    color = Color(0xFFFF2244),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.1.em
                )
                Text(
                    event.questTitle,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "⚡ -${event.expLost} EXP",
                        color = Color(0xFFFF6B6B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (event.debtAdded > 0) {
                        Text(
                            "💔 +${event.debtAdded} Debt",
                            color = Color(0xFFFF9800),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Dismiss
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x33FF2244))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text("✕", color = Color(0xFFFF2244), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
