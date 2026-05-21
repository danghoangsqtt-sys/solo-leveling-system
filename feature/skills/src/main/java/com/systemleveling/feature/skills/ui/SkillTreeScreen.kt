package com.systemleveling.feature.skills.ui

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.core.database.entity.SkillEntity
import com.systemleveling.core.model.SkillLevel

// ── Palette ──────────────────────────────────────────────────────────────────
private val BG_DEEP      = Color(0xFF0A0A1A)
private val BG           = Color(0xFF121222)
private val PRIMARY      = Color(0xFF4A9EFF)
private val PRIMARY_DIM  = Color(0xFFA4C9FF)
private val GOLD         = Color(0xFFFFD700)
private val GREEN        = Color(0xFF2ED573)
private val PURPLE       = Color(0xFFB48EFF)
private val GLASS_BORDER = Color(0x1FFFFFFF)
private val TEXT_MUTED   = Color(0xFFC0C7D4)

// ── Tier definitions ─────────────────────────────────────────────────────────
private enum class Tier(val label: String, val labelShort: String, val color: Color) {
    BEGINNER("SƠ CẤP",  "Sơ Cấp", Color(0xFF78909C)),
    INTERMEDIATE("TRUNG CẤP", "Trung Cấp", PRIMARY),
    MASTER("MASTER", "Master",  GOLD),
    EVOLVED("⚡ TIẾN HÓA", "Tiến Hóa", PURPLE)
}

private fun SkillLevel.tier(): Tier = when (this) {
    SkillLevel.NOVICE, SkillLevel.APPRENTICE               -> Tier.BEGINNER
    SkillLevel.INTERMEDIATE, SkillLevel.ADVANCED            -> Tier.INTERMEDIATE
    SkillLevel.EXPERT, SkillLevel.MASTER                   -> Tier.MASTER
    SkillLevel.GRAND_MASTER                                 -> Tier.EVOLVED
}

private fun SkillLevel.tierLabel(): String = when (this) {
    SkillLevel.NOVICE         -> "Nhập Môn"
    SkillLevel.APPRENTICE     -> "Sơ Cấp"
    SkillLevel.INTERMEDIATE   -> "Trung Sơ Cấp"
    SkillLevel.ADVANCED       -> "Trung Cấp"
    SkillLevel.EXPERT         -> "Tiền Cao Cấp"
    SkillLevel.MASTER         -> "Cao Cấp"
    SkillLevel.GRAND_MASTER   -> "⚡ Tiến Hóa"
}

// ── Root ─────────────────────────────────────────────────────────────────────
@Composable
fun SkillTreeScreen(
    viewModel: SkillTreeViewModel,
    onBack: () -> Unit
) {
    val skills by viewModel.skills.collectAsState()
    var selectedFilter by remember { mutableStateOf<Tier?>(null) }
    var selectedSkill by remember { mutableStateOf<SkillEntity?>(null) }

    val filtered = if (selectedFilter == null) skills
    else skills.filter { it.level.tier() == selectedFilter }

    // Group by tier in display order
    val tierOrder = listOf(Tier.BEGINNER, Tier.INTERMEDIATE, Tier.MASTER, Tier.EVOLVED)
    val grouped = tierOrder
        .mapNotNull { tier ->
            val group = filtered.filter { it.level.tier() == tier }
            if (group.isNotEmpty()) tier to group else null
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF1A1A2E), BG_DEEP), radius = 1400f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            SkillTopBar(onBack = onBack, totalSkills = skills.size, evolvedCount = skills.count { it.level == SkillLevel.GRAND_MASTER })

            // Filter tabs
            FilterTabRow(selected = selectedFilter, onSelect = { selectedFilter = if (selectedFilter == it) null else it })

            Spacer(Modifier.height(8.dp))

            // Skill list
            if (skills.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (tier, tierSkills) ->
                        item {
                            TierHeader(tier = tier, count = tierSkills.size)
                            Spacer(Modifier.height(6.dp))
                        }
                        items(tierSkills, key = { it.id }) { skill ->
                            SkillListRow(skill = skill, onClick = { selectedSkill = skill })
                            Spacer(Modifier.height(4.dp))
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }
        }

        // Detail bottom sheet
        selectedSkill?.let { skill ->
            SkillDetailBottomSheet(skill = skill, onDismiss = { selectedSkill = null })
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────
@Composable
private fun SkillTopBar(onBack: () -> Unit, totalSkills: Int, evolvedCount: Int) {
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
                    "$totalSkills kỹ năng · $evolvedCount đã tiến hóa",
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

// ── Filter tabs ───────────────────────────────────────────────────────────────
@Composable
private fun FilterTabRow(selected: Tier?, onSelect: (Tier) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Tier.entries.forEach { tier ->
            val isActive = selected == tier
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isActive) tier.color.copy(alpha = 0.2f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (isActive) tier.color.copy(alpha = 0.7f) else GLASS_BORDER,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(tier) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    tier.labelShort,
                    color = if (isActive) tier.color else TEXT_MUTED,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    letterSpacing = 0.05f.em
                )
            }
        }
    }
}

// ── Tier header ───────────────────────────────────────────────────────────────
@Composable
private fun TierHeader(tier: Tier, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f).height(0.5.dp).background(tier.color.copy(alpha = 0.3f)))
        Text(
            "  ${tier.label}  ($count)  ",
            color = tier.color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.1f.em
        )
        Box(Modifier.weight(1f).height(0.5.dp).background(tier.color.copy(alpha = 0.3f)))
    }
}

// ── Skill list row ────────────────────────────────────────────────────────────
@Composable
private fun SkillListRow(skill: SkillEntity, onClick: () -> Unit) {
    val tier = skill.level.tier()
    val isEvolved = tier == Tier.EVOLVED
    val progress = (skill.currentSp.toFloat() / skill.level.maxSp).coerceIn(0f, 1f)

    // Glow animation on SP bar
    val infiniteTransition = rememberInfiniteTransition(label = "glow_${skill.id}")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "alpha"
    )

    val borderBrush = if (isEvolved) {
        Brush.linearGradient(listOf(GOLD, PURPLE, PRIMARY, GREEN, GOLD))
    } else null

    val borderColor = if (isEvolved) Color.Transparent else tier.color.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (borderBrush != null)
                    Modifier.border(1.5.dp, borderBrush, RoundedCornerShape(12.dp))
                else
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(12.dp))
            )
            .background(
                if (isEvolved) Brush.linearGradient(listOf(PURPLE.copy(0.08f), GOLD.copy(0.05f)))
                else Brush.linearGradient(listOf(tier.color.copy(0.06f), Color.Transparent))
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tier.color.copy(alpha = 0.12f))
                    .border(
                        width = if (isEvolved) 1.5.dp else 1.dp,
                        color = tier.color.copy(alpha = if (isEvolved) glowAlpha else 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(skill.iconId ?: "⭐", fontSize = 22.sp)
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        skill.name,
                        color = if (isEvolved) GOLD else Color.White,
                        fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                    // Tier badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(tier.color.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            skill.level.tierLabel(),
                            color = tier.color, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 0.06f.em
                        )
                    }
                }
                Text(
                    skill.description,
                    color = TEXT_MUTED, fontSize = 11.sp, lineHeight = 15.sp,
                    maxLines = 1
                )

                Spacer(Modifier.height(8.dp))

                // SP progress row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "SP", color = tier.color, fontSize = 9.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.1f.em,
                        modifier = Modifier.width(20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(GLASS_BORDER)
                    ) {
                        // Filled portion
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(
                                    if (isEvolved)
                                        Brush.horizontalGradient(listOf(GOLD.copy(glowAlpha), PURPLE.copy(glowAlpha)))
                                    else
                                        Brush.horizontalGradient(listOf(tier.color.copy(0.7f), tier.color.copy(glowAlpha)))
                                )
                        )
                        // Glow dot at tip
                        if (progress > 0.02f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .wrapContentWidth(Alignment.End)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isEvolved) GOLD.copy(glowAlpha) else tier.color.copy(glowAlpha))
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${skill.currentSp}/${skill.level.maxSp}",
                        color = if (isEvolved) GOLD else TEXT_MUTED,
                        fontSize = 9.sp, fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Evolved overlay badge
        if (isEvolved) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(bottomStart = 8.dp))
                    .background(Brush.linearGradient(listOf(GOLD.copy(glowAlpha * 0.8f), PURPLE.copy(glowAlpha * 0.8f))))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("⚡ TIẾN HÓA", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.05f.em)
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────
@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🌱", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text("Chưa có kỹ năng nào", color = TEXT_MUTED, fontSize = 14.sp)
            Text("Bắt đầu luyện tập để khai mở!", color = TEXT_MUTED.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}

// ── Skill detail bottom sheet ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SkillDetailBottomSheet(skill: SkillEntity, onDismiss: () -> Unit) {
    val tier = skill.level.tier()
    val isEvolved = tier == Tier.EVOLVED
    val progress = (skill.currentSp.toFloat() / skill.level.maxSp).coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition(label = "sheet_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A2B),
        dragHandle = {
            Box(
                modifier = Modifier.padding(vertical = 10.dp)
                    .width(40.dp).height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isEvolved) Brush.horizontalGradient(listOf(GOLD, PURPLE)) else Brush.horizontalGradient(listOf(tier.color, tier.color.copy(0.5f))))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(tier.color.copy(alpha = 0.12f))
                        .border(1.5.dp, tier.color.copy(alpha = glowAlpha), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) { Text(skill.iconId ?: "⭐", fontSize = 28.sp) }

                Spacer(Modifier.width(16.dp))
                Column {
                    if (isEvolved) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.linearGradient(listOf(GOLD.copy(0.2f), PURPLE.copy(0.2f))))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("⚡ TIẾN HÓA", color = GOLD, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        skill.name,
                        color = if (isEvolved) GOLD else Color.White,
                        fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                    Text(skill.level.tierLabel(), color = tier.color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Description
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GLASS_BORDER.copy(alpha = 0.05f))
                    .padding(12.dp)
            ) {
                Text(skill.description, color = TEXT_MUTED, fontSize = 13.sp, lineHeight = 18.sp)
            }

            Spacer(Modifier.height(20.dp))

            // SP Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ĐIỂM KỸ NĂNG (SP)", color = tier.color, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08f.em)
                Text(
                    "${skill.currentSp} / ${skill.level.maxSp}  (${(progress * 100).toInt()}%)",
                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(GLASS_BORDER)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            if (isEvolved)
                                Brush.horizontalGradient(listOf(GOLD, PURPLE, PRIMARY))
                            else
                                Brush.horizontalGradient(listOf(tier.color.copy(0.7f), tier.color))
                        )
                )
            }

            Spacer(Modifier.height(8.dp))

            // Next level info
            val nextLevel = SkillLevel.entries.getOrNull(skill.level.ordinal + 1)
            if (nextLevel != null) {
                val remaining = skill.level.maxSp - skill.currentSp
                Text(
                    if (skill.level == SkillLevel.MASTER)
                        "⚡ Cần $remaining SP nữa để Tiến Hóa → ${nextLevel.title}"
                    else
                        "Cần $remaining SP nữa để lên ${nextLevel.title}",
                    color = if (skill.level == SkillLevel.MASTER) GOLD else TEXT_MUTED,
                    fontSize = 11.sp
                )
            } else {
                Text("✨ Đây là cấp độ tối thượng!", color = PURPLE, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))

            // Action button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isEvolved)
                            Brush.horizontalGradient(listOf(GOLD.copy(0.3f), PURPLE.copy(0.3f)))
                        else
                            Brush.horizontalGradient(listOf(tier.color.copy(0.2f), tier.color.copy(0.1f)))
                    )
                    .border(
                        1.dp,
                        if (isEvolved) GOLD.copy(glowAlpha) else tier.color.copy(0.6f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "[ LUYỆN TẬP KỸ NĂNG NÀY ]",
                    color = if (isEvolved) GOLD else tier.color,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08f.em
                )
            }
        }
    }
}
