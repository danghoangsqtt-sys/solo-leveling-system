package com.systemleveling.feature.quests.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.systemleveling.core.model.ItemRarity
import com.systemleveling.core.model.QuestRank
import com.systemleveling.core.model.RewardResult

@Composable
fun QuestCompleteDialog(
    result: RewardResult,
    onDismiss: () -> Unit
) {
    val rankColor = when (result.questRank) {
        QuestRank.E -> Color.White
        QuestRank.D -> Color(0xFF40E17E)
        QuestRank.C -> Color(0xFF4A9EFF)
        QuestRank.B -> Color(0xFFE040FB)
        QuestRank.A -> Color(0xFFFFAB40)
        QuestRank.S -> Color(0xFFFF5252)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF12122A))
                .border(2.dp, rankColor.copy(0.7f), RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Header ──────────────────────────────────────────────────
                Text(
                    text = "⚔️ QUEST COMPLETE! ⚔️",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = rankColor,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "[ RANK ${result.questRank} ]",
                    color = rankColor.copy(0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = rankColor.copy(0.2f))
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "\"${result.questTitle}\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(20.dp))

                // ── Level-up banner ──────────────────────────────────────────
                if (result.leveledUp && result.newLevel != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFFFD700).copy(0.3f), Color(0xFFFFAB40).copy(0.2f))))
                            .border(1.dp, Color(0xFFFFD700).copy(0.7f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎉 LEVEL UP!  →  Lv.${result.newLevel}",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // ── EXP + Gold ───────────────────────────────────────────────
                Text("─── REWARDS ACQUIRED ───", color = Color.Gray, fontSize = 10.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    RewardPill("⚡", "+${result.expGained} EXP", Color(0xFF4A9EFF))
                    RewardPill("💰", "+${result.goldGained} Gold", Color(0xFFFFD700))
                }

                // ── Stat changes ─────────────────────────────────────────────
                if (result.statChanges.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text("─── STAT GROWTH ───", color = Color.Gray, fontSize = 10.sp, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        result.statChanges.entries
                            .filter { it.value > 0 }
                            .forEach { (stat, gain) ->
                                StatGainChip(stat, gain)
                                Spacer(Modifier.width(8.dp))
                            }
                    }
                }

                // ── Skill points ─────────────────────────────────────────────
                if (result.skillPointChanges.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text("─── SKILL PROGRESS ───", color = Color.Gray, fontSize = 10.sp, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    result.skillPointChanges.entries.forEach { (skillName, sp) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🌟 $skillName", color = Color(0xFFE3E0F8), fontSize = 12.sp)
                            Text("+${sp} SP", color = Color(0xFF4A9EFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ── Skill level-ups ───────────────────────────────────────────
                if (result.skillLeveledUp.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    result.skillLeveledUp.forEach { info ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE040FB).copy(0.15f))
                                .border(1.dp, Color(0xFFE040FB).copy(0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💫", fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(info.skillName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        "${info.oldLevel.title} → ${info.newLevel.title}",
                                        color = Color(0xFFE040FB),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }

                // ── Item drop ─────────────────────────────────────────────────
                result.droppedItem?.let { item ->
                    Spacer(Modifier.height(14.dp))
                    Text("─── ITEM DROPPED ───", color = Color.Gray, fontSize = 10.sp, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    val rarityColor = when (item.rarity) {
                        ItemRarity.COMMON    -> Color.White
                        ItemRarity.UNCOMMON  -> Color(0xFF40E17E)
                        ItemRarity.RARE      -> Color(0xFF4A9EFF)
                        ItemRarity.EPIC      -> Color(0xFFE040FB)
                        ItemRarity.LEGENDARY -> Color(0xFFFFD700)
                        ItemRarity.MYTHIC    -> Color(0xFFFF5252)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(rarityColor.copy(0.12f))
                            .border(1.dp, rarityColor.copy(0.6f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.iconId, fontSize = 28.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(item.name, color = rarityColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(item.rarity.name, color = rarityColor.copy(0.7f), fontSize = 10.sp)
                                if (item.loreDescription.isNotBlank()) {
                                    Text(item.loreDescription, color = Color.Gray, fontSize = 10.sp, maxLines = 2)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Claim button ─────────────────────────────────────────────
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = rankColor.copy(0.8f)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "[ ✅ CLAIM REWARDS ]",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardPill(icon: String, text: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(0.12f))
            .border(1.dp, color.copy(0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(Modifier.width(6.dp))
        Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun StatGainChip(stat: String, gain: Int) {
    val color = when (stat.uppercase()) {
        "STR" -> Color(0xFF4A9EFF)
        "INT" -> Color(0xFFFFD700)
        "AGI" -> Color(0xFF2ED573)
        "VIT" -> Color(0xFFFF6B6B)
        "WIS" -> Color(0xFFB48EFF)
        "CHA" -> Color(0xFFFF9F43)
        else  -> Color.White
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(0.15f))
            .border(1.dp, color.copy(0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text("$stat +$gain", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
