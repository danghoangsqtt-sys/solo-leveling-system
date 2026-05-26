package com.systemleveling.feature.quests.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.core.database.entity.QuestEntity
import com.systemleveling.core.model.QuestStatus
import org.json.JSONArray

// ── Quest Detail Sheet ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestDetailSheet(
    quest: QuestEntity,
    onDismiss: () -> Unit,
    onComplete: (QuestEntity) -> Unit
) {
    val meta = rankMeta(quest.rank)
    val isCompleted = quest.status == QuestStatus.COMPLETED
    val isFailed = quest.status == QuestStatus.FAILED || quest.status == QuestStatus.EXPIRED

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141420),
        scrimColor = Color(0xCC000000)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(bottom = 32.dp)
        ) {
            // Rank and Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(meta.bg)
                        .border(1.dp, meta.color.copy(0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "RANK ${meta.label}",
                        color = meta.color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.1f.em
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = quest.category.uppercase(),
                    color = Color(0xFF8899CC),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.1f.em
                )
            }
            
            Spacer(Modifier.height(16.dp))

            Text(
                text = quest.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(12.dp))

            // Description
            Text(
                text = quest.description,
                color = Color(0xFFB0C4DE),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(20.dp))

            // Subtasks
            val subtasksList = remember(quest.subtasks) {
                buildList {
                    if (quest.subtasks.isNotBlank() && quest.subtasks != "[]") {
                        try {
                            val array = JSONArray(quest.subtasks)
                            for (i in 0 until array.length()) add(array.getString(i))
                        } catch (e: Exception) {}
                    }
                }
            }
            if (subtasksList.isNotEmpty()) {
                Text("CÁC BƯỚC THỰC HIỆN", color = PRIMARY, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                subtasksList.forEach { task ->
                    Row(
                        modifier = Modifier.padding(bottom = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("🔸", fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(task, color = Color.White, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Rewards
            Text("PHẦN THƯỞNG", color = GOLD, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF1A1A2E)).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("+${quest.expReward} EXP", color = Color(0xFF4A9EFF), fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF2E221A)).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💰", fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("+${quest.goldReward} G", color = GOLD, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Complete Button
            Button(
                onClick = { onComplete(quest) },
                enabled = !isCompleted && !isFailed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PRIMARY,
                    disabledContainerColor = Color(0xFF2A2A40)
                ),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = when {
                        isCompleted -> "ĐÃ HOÀN THÀNH"
                        isFailed -> "THẤT BẠI"
                        else -> "XÁC NHẬN HOÀN THÀNH"
                    },
                    color = if (!isCompleted && !isFailed) Color.White else Color(0xFF8899CC),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.1f.em
                )
            }
        }
    }
}
