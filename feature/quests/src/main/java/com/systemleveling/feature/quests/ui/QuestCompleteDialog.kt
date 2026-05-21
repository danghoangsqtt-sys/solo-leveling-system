package com.systemleveling.feature.quests.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.systemleveling.core.database.entity.QuestEntity

@Composable
fun QuestCompleteDialog(
    quest: QuestEntity,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1A2E))
                .border(2.dp, Color(0xFF4A9EFF), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚔️ QUEST COMPLETE! ⚔️",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF4A9EFF)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "═══════════════════",
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "\"${quest.title}\"",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Rank: ${quest.rank}  ⏱️ Hoàn thành",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF40E17E)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "─── REWARDS ACQUIRED ───",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text("✨ +${quest.expReward} EXP", color = Color(0xFF4A9EFF), style = MaterialTheme.typography.bodyMedium)
                    Text("💰 +${quest.goldReward} Gold", color = Color(0xFFFFDB3C), style = MaterialTheme.typography.bodyMedium)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A9EFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("[ ✨ CLAIM REWARDS ]")
                }
            }
        }
    }
}
