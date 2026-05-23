package com.systemleveling.feature.quests.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.systemleveling.core.model.ItemRarity
import com.systemleveling.core.model.QuestRank
import com.systemleveling.core.model.RewardResult

// ── Rank metadata ─────────────────────────────────────────────────────────────
private data class RankStyle(
    val primary: Color,
    val secondary: Color,
    val bg: Color,
    val title: String,
    val glow: Boolean = false
)

private fun dialogRankStyle(rank: QuestRank) = when (rank) {
    QuestRank.E -> RankStyle(Color(0xFFAAAAAA), Color(0xFF888888), Color(0x0FAAAAAA), "MISSION CLEARED", false)
    QuestRank.D -> RankStyle(Color(0xFF2ED573), Color(0xFF1AB558), Color(0x0F2ED573), "MISSION CLEARED", false)
    QuestRank.C -> RankStyle(Color(0xFF4A9EFF), Color(0xFF2A7EDF), Color(0x0F4A9EFF), "QUEST COMPLETE!", false)
    QuestRank.B -> RankStyle(Color(0xFFE040FB), Color(0xFFC020DB), Color(0x14E040FB), "QUEST COMPLETE!", true)
    QuestRank.A -> RankStyle(Color(0xFFFFAB40), Color(0xFFFF8C00), Color(0x18FFAB40), "OUTSTANDING!", true)
    QuestRank.S -> RankStyle(Color(0xFFFF5252), Color(0xFFDD2222), Color(0x22FF5252), "LEGENDARY CLEAR!", true)
}

// ── Dialog ────────────────────────────────────────────────────────────────────
@Composable
fun QuestCompleteDialog(
    result: RewardResult,
    onDismiss: () -> Unit
) {
    val style = dialogRankStyle(result.questRank)

    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val entryScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "entry_scale"
    )
    val entryAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(200),
        label = "entry_alpha"
    )

    // Glow pulse for high ranks
    val glowPulse by if (style.glow) {
        rememberInfiniteTransition(label = "glow").animateFloat(
            0.4f, 1f,
            infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
            label = "gp"
        )
    } else {
        remember { mutableFloatStateOf(0.6f) }
    }

    // Rotation for rank badge decoration
    val rotate by rememberInfiniteTransition(label = "rot").animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(12000, easing = LinearEasing)),
        label = "r"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .scale(entryScale)
                .alpha(entryAlpha)
        ) {
            // Outer glow ring (high ranks only)
            if (style.glow) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(style.primary.copy(glowPulse * 0.08f))
                        .border(1.5.dp, style.primary.copy(glowPulse * 0.6f), RoundedCornerShape(24.dp))
                        .padding(3.dp)
                )
            }

            // Main card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF080814))
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(style.primary.copy(0.9f), style.secondary.copy(0.4f), style.primary.copy(0.6f))),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {

                    // ── Hero header ──────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        style.primary.copy(0.18f),
                                        style.bg,
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(top = 28.dp, bottom = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            // Rank badge with decoration
                            Box(contentAlignment = Alignment.Center) {
                                // Rotating decoration ring (S/A rank)
                                if (style.glow) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .rotate(rotate)
                                            .border(
                                                1.dp,
                                                Brush.sweepGradient(
                                                    listOf(
                                                        Color.Transparent,
                                                        style.primary.copy(0.3f),
                                                        style.primary.copy(0.8f),
                                                        Color.Transparent
                                                    )
                                                ),
                                                CircleShape
                                            )
                                    )
                                }
                                // Inner rank circle
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(
                                                    style.primary.copy(0.25f),
                                                    style.primary.copy(0.05f)
                                                )
                                            )
                                        )
                                        .border(2.dp, style.primary.copy(glowPulse), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = result.questRank.name,
                                        color = style.primary,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.05f.em
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            Text(
                                text = style.title,
                                color = style.primary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.12f.em
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "\"${result.questTitle}\"",
                                color = Color(0xFFDDE4F0),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }

                    // ── Level-up banner ──────────────────────────────────────
                    if (result.leveledUp && result.newLevel != null) {
                        val lvGlow by rememberInfiniteTransition(label = "lv").animateFloat(
                            0.7f, 1f,
                            infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse),
                            label = "lg"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF2A1800), Color(0xFF1A1200), Color(0xFF2A1800))
                                    )
                                )
                                .border(1.5.dp, Color(0xFFFFD700).copy(lvGlow), RoundedCornerShape(12.dp))
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("⭐", fontSize = 18.sp)
                                Spacer(Modifier.width(10.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "LEVEL UP!",
                                        color = Color(0xFFFFD700),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.15f.em
                                    )
                                    Text(
                                        "→  Level ${result.newLevel}",
                                        color = Color(0xFFFFE066),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text("⭐", fontSize = 18.sp)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // ── Rewards section ──────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0C0C1E))
                            .border(1.dp, Color(0xFF1E1E3A), RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            SectionLabel("REWARDS ACQUIRED")
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                RewardTile(
                                    icon = "⚡",
                                    label = "EXP",
                                    value = "+${result.expGained}",
                                    color = Color(0xFF4A9EFF)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(48.dp)
                                        .background(Color(0xFF1E1E3A))
                                )
                                RewardTile(
                                    icon = "💰",
                                    label = "GOLD",
                                    value = "+${result.goldGained}",
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // ── Stat growth ───────────────────────────────────────────
                    if (result.statChanges.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0C0C1E))
                                .border(1.dp, Color(0xFF1E1E3A), RoundedCornerShape(14.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                SectionLabel("STAT GROWTH")
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    result.statChanges.entries
                                        .filter { it.value > 0 }
                                        .forEach { (stat, gain) ->
                                            StatGrowthBadge(stat, gain)
                                            Spacer(Modifier.width(8.dp))
                                        }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    // ── Skill progress ────────────────────────────────────────
                    if (result.skillPointChanges.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0C0C1E))
                                .border(1.dp, Color(0xFF1E1E3A), RoundedCornerShape(14.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                SectionLabel("SKILL PROGRESS")
                                Spacer(Modifier.height(10.dp))
                                result.skillPointChanges.entries.forEachIndexed { idx, (skillName, sp) ->
                                    if (idx > 0) Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("✦", color = Color(0xFFB48EFF), fontSize = 10.sp)
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                skillName,
                                                color = Color(0xFFDDE4F0),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(Color(0xFF4A9EFF).copy(0.12f))
                                                .border(0.5.dp, Color(0xFF4A9EFF).copy(0.4f), RoundedCornerShape(5.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                "+$sp SP",
                                                color = Color(0xFF4A9EFF),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    // ── Skill level-ups ───────────────────────────────────────
                    if (result.skillLeveledUp.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            result.skillLeveledUp.forEach { info ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1A0A2E))
                                        .border(1.dp, Color(0xFFE040FB).copy(0.5f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFE040FB).copy(0.15f))
                                                .border(1.dp, Color(0xFFE040FB).copy(0.5f), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) { Text("💫", fontSize = 14.sp) }
                                        Spacer(Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "SKILL LEVEL UP!",
                                                color = Color(0xFFE040FB),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 0.1f.em
                                            )
                                            Text(
                                                info.skillName,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                info.oldLevel.title,
                                                color = Color(0xFF666688),
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                "→ ${info.newLevel.title}",
                                                color = Color(0xFFE040FB),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    // ── Item drop ─────────────────────────────────────────────
                    val droppedItem = result.droppedItem
                    if (droppedItem != null) {
                        val itemColor = rarityColor(droppedItem.rarity)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(itemColor.copy(0.12f), Color(0xFF080814), itemColor.copy(0.06f))
                                    )
                                )
                                .border(1.5.dp, itemColor.copy(0.7f), RoundedCornerShape(14.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎁", fontSize = 12.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "ITEM DROP!",
                                        color = itemColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.12f.em
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(itemColor.copy(0.12f))
                                            .border(1.dp, itemColor.copy(0.5f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(droppedItem.iconId, fontSize = 26.sp)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(itemColor.copy(0.12f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                droppedItem.rarity.name.uppercase(),
                                                color = itemColor,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 0.1f.em
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            droppedItem.name,
                                            color = itemColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (droppedItem.loreDescription.isNotBlank()) {
                                            Spacer(Modifier.height(3.dp))
                                            Text(
                                                droppedItem.loreDescription,
                                                color = Color(0xFF8899AA),
                                                fontSize = 10.sp,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0A0A18))
                                .border(1.dp, Color(0xFF1A1A30), RoundedCornerShape(14.dp))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🎲", fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Lần này không có vật phẩm rơi",
                                    color = Color(0xFF3A3A5A),
                                    fontSize = 11.sp
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("→ Tiếp tục!", color = Color(0xFF4A6A99), fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Claim button ─────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(style.primary.copy(0.9f), style.secondary.copy(0.7f))
                                )
                            )
                            .clickable { onDismiss() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("✅", fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "CLAIM REWARDS",
                                color = Color(0xFF080814),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.12f.em
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(16.dp).height(0.5.dp).background(Color(0xFF2A2A50)))
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = Color(0xFF6677AA),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.14f.em
        )
        Spacer(Modifier.width(6.dp))
        Box(Modifier.weight(1f).height(0.5.dp).background(Color(0xFF2A2A50)))
    }
}

@Composable
private fun RewardTile(icon: String, label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(0.10f))
                .border(1.dp, color.copy(0.35f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 20.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            label,
            color = color.copy(0.5f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.08f.em
        )
    }
}

@Composable
private fun StatGrowthBadge(stat: String, gain: Int) {
    val color = when (stat.uppercase()) {
        "STR" -> Color(0xFF4A9EFF)
        "INT" -> Color(0xFFFFD700)
        "AGI" -> Color(0xFF2ED573)
        "VIT" -> Color(0xFFFF6B6B)
        "WIS" -> Color(0xFFB48EFF)
        "CHA" -> Color(0xFFFF9F43)
        else  -> Color.White
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(0.10f))
            .border(1.dp, color.copy(0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(stat, color = color.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08f.em)
        Spacer(Modifier.height(2.dp))
        Text("+$gain", color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
    }
}

private fun rarityColor(rarity: ItemRarity) = when (rarity) {
    ItemRarity.COMMON    -> Color(0xFFAAAAAA)
    ItemRarity.UNCOMMON  -> Color(0xFF2ED573)
    ItemRarity.RARE      -> Color(0xFF4A9EFF)
    ItemRarity.EPIC      -> Color(0xFFE040FB)
    ItemRarity.LEGENDARY -> Color(0xFFFFD700)
    ItemRarity.MYTHIC    -> Color(0xFFFF5252)
}
