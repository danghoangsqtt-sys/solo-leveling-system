package com.systemleveling.feature.skills.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.core.database.entity.SkillEntity
import com.systemleveling.core.model.SkillLevel

// ── Palette ──────────────────────────────────────────────────────────────────
private val BG_DEEP     = Color(0xFF0A0A1A)
private val BG          = Color(0xFF121222)
private val PRIMARY     = Color(0xFF4A9EFF)
private val PRIMARY_DIM = Color(0xFFA4C9FF)
private val GOLD        = Color(0xFFFFD700)
private val GREEN       = Color(0xFF2ED573)
private val PURPLE      = Color(0xFFB48EFF)
private val GLASS_BORDER = Color(0x1FFFFFFF)
private val TEXT_MUTED  = Color(0xFFC0C7D4)
private val CHILD_BG    = Color(0xFF0E0E1E)

// ── SkillLevel helpers ────────────────────────────────────────────────────────
private fun SkillLevel.displayColor(): Color = when (this) {
    SkillLevel.NOVICE, SkillLevel.APPRENTICE      -> Color(0xFF78909C)
    SkillLevel.INTERMEDIATE, SkillLevel.ADVANCED  -> PRIMARY
    SkillLevel.EXPERT, SkillLevel.MASTER          -> GOLD
    SkillLevel.GRAND_MASTER                        -> PURPLE
}

private fun SkillLevel.displayLabel(): String = when (this) {
    SkillLevel.NOVICE        -> "Nhập Môn"
    SkillLevel.APPRENTICE    -> "Sơ Cấp"
    SkillLevel.INTERMEDIATE  -> "Trung Sơ Cấp"
    SkillLevel.ADVANCED      -> "Trung Cấp"
    SkillLevel.EXPERT        -> "Tiền Cao Cấp"
    SkillLevel.MASTER        -> "Cao Cấp"
    SkillLevel.GRAND_MASTER  -> "⚡ Tiến Hóa"
}

// ── Root Screen ───────────────────────────────────────────────────────────────
@Composable
fun SkillTreeScreen(
    viewModel: SkillTreeViewModel,
    onBack: () -> Unit
) {
    val skillGroups by viewModel.skillGroups.collectAsState()
    val totalSkills = skillGroups.sumOf { it.children.size + 1 }
    val evolvedCount = skillGroups.count { it.masteryLevel == SkillLevel.GRAND_MASTER }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF1A1A2E), BG_DEEP), radius = 1400f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SkillTopBar(
                onBack = onBack,
                totalParents = skillGroups.size,
                totalSkills = totalSkills,
                evolvedCount = evolvedCount
            )

            Spacer(Modifier.height(4.dp))

            if (skillGroups.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(skillGroups, key = { it.parent.id }) { group ->
                        SkillGroupCard(group = group)
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────
@Composable
private fun SkillTopBar(
    onBack: () -> Unit,
    totalParents: Int,
    totalSkills: Int,
    evolvedCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x99121222))
            .border(BorderStroke(0.5.dp, GLASS_BORDER))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "◀",
                color = PRIMARY, fontSize = 18.sp,
                modifier = Modifier.clickable { onBack() }.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "KHO KỸ NĂNG",
                    color = PRIMARY_DIM, fontSize = 16.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 0.08f.em
                )
                Text(
                    "$totalParents nhóm · $totalSkills kỹ năng",
                    color = TEXT_MUTED, fontSize = 11.sp
                )
            }
            if (evolvedCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PURPLE.copy(alpha = 0.15f))
                        .border(1.dp, PURPLE.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("⚡ $evolvedCount", color = PURPLE, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Skill Group Card (expandable) ─────────────────────────────────────────────
@Composable
private fun SkillGroupCard(group: SkillGroup) {
    var expanded by remember { mutableStateOf(false) }
    val masteryColor = group.masteryLevel.displayColor()
    val isEvolved = group.masteryLevel == SkillLevel.GRAND_MASTER

    val infiniteTransition = rememberInfiniteTransition(label = "glow_${group.parent.id}")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "alpha"
    )

    val chevronAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(250),
        label = "chevron"
    )

    val borderBrush = if (isEvolved)
        Brush.linearGradient(listOf(GOLD, PURPLE, PRIMARY, GOLD))
    else null

    Column(modifier = Modifier.fillMaxWidth()) {
        // ── Parent card ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(if (expanded) 12.dp else 12.dp))
                .then(
                    if (borderBrush != null)
                        Modifier.border(1.5.dp, borderBrush, RoundedCornerShape(12.dp))
                    else
                        Modifier.border(1.dp, masteryColor.copy(0.35f), RoundedCornerShape(12.dp))
                )
                .background(
                    if (isEvolved)
                        Brush.linearGradient(listOf(PURPLE.copy(0.1f), GOLD.copy(0.06f)))
                    else
                        Brush.linearGradient(listOf(masteryColor.copy(0.08f), Color.Transparent))
                )
                .clickable { expanded = !expanded }
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(masteryColor.copy(0.12f))
                            .border(
                                width = if (isEvolved) 1.5.dp else 1.dp,
                                color = masteryColor.copy(if (isEvolved) glowAlpha else 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(group.parent.iconId ?: "🌟", fontSize = 24.sp)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                group.parent.name,
                                color = if (isEvolved) GOLD else Color.White,
                                fontSize = 15.sp, fontWeight = FontWeight.Bold
                            )
                            MasteryBadge(level = group.masteryLevel, glowAlpha = glowAlpha)
                        }
                        Text(
                            "${group.children.size} kỹ năng con",
                            color = TEXT_MUTED, fontSize = 11.sp
                        )
                    }

                    // Chevron
                    Text(
                        "▾",
                        color = masteryColor.copy(0.8f),
                        fontSize = 16.sp,
                        modifier = Modifier.rotate(chevronAngle)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Aggregate progress bar
                AggregateProgressBar(
                    group = group,
                    masteryColor = masteryColor,
                    isEvolved = isEvolved,
                    glowAlpha = glowAlpha
                )
            }
        }

        // ── Children (animated) ───────────────────────────────────────────
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .background(CHILD_BG.copy(0.85f))
                    .border(
                        BorderStroke(0.5.dp,
                            Brush.verticalGradient(listOf(Color.Transparent, masteryColor.copy(0.2f)))
                        ),
                        RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.children.forEachIndexed { index, child ->
                    ChildSkillRow(
                        skill = child,
                        isLast = index == group.children.lastIndex,
                        parentMasteryColor = masteryColor
                    )
                }
            }
        }
    }
}

// ── Aggregate progress bar ────────────────────────────────────────────────────
@Composable
private fun AggregateProgressBar(
    group: SkillGroup,
    masteryColor: Color,
    isEvolved: Boolean,
    glowAlpha: Float
) {
    val progress = group.aggregateProgress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = EaseInOutCubic),
        label = "prog_${group.parent.id}"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "THÀNH THẠO",
                color = masteryColor.copy(0.8f), fontSize = 9.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 0.1f.em
            )
            Text(
                "${group.aggregateCurrentSp} / ${group.aggregateMaxSp} SP  (${(animatedProgress * 100).toInt()}%)",
                color = if (isEvolved) GOLD else TEXT_MUTED,
                fontSize = 9.sp, fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(GLASS_BORDER)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        if (isEvolved)
                            Brush.horizontalGradient(listOf(GOLD.copy(glowAlpha), PURPLE.copy(glowAlpha)))
                        else
                            Brush.horizontalGradient(listOf(masteryColor.copy(0.7f), masteryColor.copy(glowAlpha)))
                    )
            )
            // Glow dot at tip
            if (animatedProgress > 0.02f) {
                Box(
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(animatedProgress)
                        .wrapContentWidth(Alignment.End)
                ) {
                    Box(
                        modifier = Modifier.size(6.dp).clip(CircleShape)
                            .background(if (isEvolved) GOLD.copy(glowAlpha) else masteryColor.copy(glowAlpha))
                    )
                }
            }
        }
    }
}

// ── Child skill row ───────────────────────────────────────────────────────────
@Composable
private fun ChildSkillRow(
    skill: SkillEntity,
    isLast: Boolean,
    parentMasteryColor: Color
) {
    val progress = (skill.currentSp.toFloat() / skill.level.maxSp).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(700, easing = EaseInOutCubic),
        label = "child_prog_${skill.id}"
    )
    val levelColor = skill.level.displayColor()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Tree connector line
        Column(
            modifier = Modifier.width(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(1.dp).height(14.dp)
                    .background(parentMasteryColor.copy(0.3f))
            )
            Box(
                modifier = Modifier
                    .size(6.dp).clip(CircleShape)
                    .background(parentMasteryColor.copy(0.5f))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp).height(28.dp)
                        .background(parentMasteryColor.copy(0.2f))
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // Child content
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(levelColor.copy(0.05f))
                .border(0.5.dp, levelColor.copy(0.2f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(skill.iconId ?: "⭐", fontSize = 18.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                skill.name,
                                color = Color.White, fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            MasteryBadge(level = skill.level, glowAlpha = 0.9f)
                        }
                        Text(
                            skill.description,
                            color = TEXT_MUTED, fontSize = 10.sp,
                            lineHeight = 14.sp, maxLines = 1
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // SP progress bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "SP", color = levelColor, fontSize = 8.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.1f.em,
                        modifier = Modifier.width(18.dp)
                    )
                    Box(
                        modifier = Modifier.weight(1f).height(3.dp)
                            .clip(RoundedCornerShape(2.dp)).background(GLASS_BORDER)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(levelColor.copy(0.6f), levelColor)
                                    )
                                )
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${skill.currentSp}/${skill.level.maxSp}",
                        color = TEXT_MUTED, fontSize = 8.sp, fontWeight = FontWeight.SemiBold
                    )
                }

                // SP source hint
                Spacer(Modifier.height(4.dp))
                Text(
                    "💡 Tăng SP qua hoàn thành nhiệm vụ hàng ngày / tuần / tháng",
                    color = TEXT_MUTED.copy(0.5f), fontSize = 9.sp, lineHeight = 12.sp
                )
            }
        }
    }
}

// ── Mastery badge ─────────────────────────────────────────────────────────────
@Composable
private fun MasteryBadge(level: SkillLevel, glowAlpha: Float) {
    val color = level.displayColor()
    val isEvolved = level == SkillLevel.GRAND_MASTER
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isEvolved)
                    Brush.linearGradient(listOf(GOLD.copy(0.2f), PURPLE.copy(0.2f)))
                else
                    Brush.linearGradient(listOf(color.copy(0.15f), color.copy(0.08f)))
            )
            .border(
                0.5.dp,
                if (isEvolved) GOLD.copy(glowAlpha) else color.copy(0.4f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            level.displayLabel(),
            color = if (isEvolved) GOLD else color,
            fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.06f.em
        )
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────
@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🌱", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text("Chưa có kỹ năng nào", color = TEXT_MUTED, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Hoàn thành Onboarding để hệ thống\nkhởi tạo lộ trình kỹ năng cho bạn.",
                color = TEXT_MUTED.copy(0.6f), fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
