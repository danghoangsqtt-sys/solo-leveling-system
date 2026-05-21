package com.systemleveling.feature.home.advancement

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.core.database.entity.StatEntity
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val BgDeep   = Color(0xFF080818)
private val Cyan     = Color(0xFF38BDF8)
private val CyanDim  = Color(0xFF1E6F9F)
private val Gold     = Color(0xFFFFD700)
private val Purple   = Color(0xFFB48EFF)
private val Green    = Color(0xFF2ED573)
private val TextMuted = Color(0xFFC0C7D4)

@Composable
fun ClassAdvancementScreen(
    viewModel: ClassAdvancementViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "adv")
    val pulse by infiniteTransition.animateFloat(
        0.6f, 1f,
        infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse"
    )
    val btnGlow by infiniteTransition.animateFloat(
        0.7f, 1f,
        infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "btn"
    )

    val radarReveal by animateFloatAsState(
        targetValue = if (state.stats != null) 1f else 0f,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "radar"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF141428), BgDeep),
                    radius = 1600f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Cyan.copy(0.05f), Color.Transparent),
                    center = Offset(size.width / 2f, size.height * 0.35f),
                    radius = size.minDimension * 0.7f
                ),
                center = Offset(size.width / 2f, size.height * 0.35f),
                radius = size.minDimension * 0.7f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // SYSTEM ALERT badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Cyan.copy(0.1f))
                    .border(1.dp, Cyan.copy(pulse * 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Cyan.copy(pulse))
                    )
                    Text(
                        "SYSTEM ALERT",
                        color = Cyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.18f.em
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Title
            Text(
                "AWAKENING\nCOMPLETE",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.04f.em,
                textAlign = TextAlign.Center,
                lineHeight = 1.1.em
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Tiềm năng ẩn của Thợ Săn đã được đo lường.",
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Radar chart
            state.stats?.let { stats ->
                StatRadarChart(
                    stats = stats,
                    cap = state.user?.statCap ?: 100,
                    revealProgress = radarReveal,
                    modifier = Modifier.size(220.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            // Class card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0E0E20))
                    .border(1.dp, Cyan.copy(pulse * 0.5f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    // Class icon orb
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Cyan.copy(0.25f), Color(0xFF05051A))))
                            .border(1.5.dp, Cyan.copy(pulse * 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(classIcon(state.newClass), fontSize = 22.sp)
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        state.newClass.uppercase(),
                        color = Gold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.1f.em
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Gold.copy(0.12f))
                            .border(0.5.dp, Gold.copy(0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            ClassAdvancementViewModel.classTierLabel(state.newTier),
                            color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1f.em
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TraitRow("Primary Trait", ClassAdvancementViewModel.classPrimaryTrait(state.newClass), TextMuted)
                        TraitRow("Starting Skill", ClassAdvancementViewModel.classStartingSkill(state.newClass), Green)
                    }

                    Spacer(Modifier.height(12.dp))

                    // New stat cap info
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Purple.copy(0.08f))
                            .border(0.5.dp, Purple.copy(0.25f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⬆", fontSize = 12.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Giới hạn chỉ số tăng lên ${state.newStatCap}",
                                color = Purple, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ARISE button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(listOf(CyanDim.copy(btnGlow), Cyan, CyanDim.copy(btnGlow)))
                    )
                    .border(1.5.dp, Cyan.copy(btnGlow), RoundedCornerShape(12.dp))
                    .clickable {
                        viewModel.confirmAdvancement()
                        onComplete()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("✦", fontSize = 16.sp, color = Color.White.copy(btnGlow))
                    Text(
                        "THỨC TỈNH",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.14f.em
                    )
                    Text("✦", fontSize = 16.sp, color = Color.White.copy(btnGlow))
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Xác nhận để bắt đầu hành trình chuyển nghề.",
                color = TextMuted.copy(0.5f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TraitRow(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, color = TextMuted, fontSize = 10.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatRadarChart(
    stats: StatEntity,
    cap: Int,
    revealProgress: Float,
    modifier: Modifier = Modifier
) {
    val statValues = listOf(
        "STR" to stats.str,
        "AGI" to stats.agi,
        "INT" to stats.intStat,
        "VIT" to stats.vit,
        "WIS" to stats.wis,
        "CHA" to stats.cha
    )
    val colors = listOf(Cyan, Green, Gold, Color(0xFFFF6B6B), Purple, Color(0xFFFF9F43))
    val n = statValues.size
    val capF = cap.toFloat().coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxR = min(cx, cy) * 0.82f
        val angleStep = (2 * Math.PI / n).toFloat()
        val startAngle = (-Math.PI / 2).toFloat()

        // Grid rings
        for (ring in 1..4) {
            val r = maxR * ring / 4f
            val pts = (0 until n).map { i ->
                val a = startAngle + i * angleStep
                Offset(cx + r * cos(a), cy + r * sin(a))
            }
            for (j in pts.indices) {
                drawLine(
                    color = Cyan.copy(0.12f),
                    start = pts[j], end = pts[(j + 1) % n],
                    strokeWidth = 0.8f.dp.toPx()
                )
            }
        }

        // Axis lines
        for (i in 0 until n) {
            val a = startAngle + i * angleStep
            drawLine(
                color = Cyan.copy(0.15f),
                start = Offset(cx, cy),
                end = Offset(cx + maxR * cos(a), cy + maxR * sin(a)),
                strokeWidth = 0.8f.dp.toPx()
            )
        }

        // Stat polygon (animated reveal)
        val pts = statValues.mapIndexed { i, (_, v) ->
            val norm = (v / capF * revealProgress).coerceIn(0f, 1f)
            val a = startAngle + i * angleStep
            Offset(cx + maxR * norm * cos(a), cy + maxR * norm * sin(a))
        }

        // Fill
        val path = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
            close()
        }
        drawPath(
            path,
            brush = Brush.radialGradient(
                listOf(Cyan.copy(0.3f), Cyan.copy(0.08f)),
                center = Offset(cx, cy), radius = maxR
            )
        )
        drawPath(path, color = Cyan.copy(0.7f), style = Stroke(1.5f.dp.toPx()))

        // Stat dots + labels
        statValues.forEachIndexed { i, (label, v) ->
            val norm = (v / capF * revealProgress).coerceIn(0f, 1f)
            val a = startAngle + i * angleStep
            val dot = Offset(cx + maxR * norm * cos(a), cy + maxR * norm * sin(a))
            val lp = Offset(cx + (maxR + 18.dp.toPx()) * cos(a), cy + (maxR + 18.dp.toPx()) * sin(a))

            drawCircle(colors[i], 4.dp.toPx(), dot)
            drawCircle(colors[i].copy(0.25f), 8.dp.toPx(), dot, style = Stroke(1f.dp.toPx()))

            drawContext.canvas.nativeCanvas.drawText(
                "$label (${v})",
                lp.x, lp.y + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = colors[i].copy(0.9f).toArgb()
                    textSize = 9.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
            )
        }
    }
}

private fun classIcon(className: String): String = when {
    "Warrior" in className || "Sovereign" in className || "Monarch" in className -> "⚔️"
    "Mage" in className || "Alchemist" in className || "Sorcerer" in className  -> "🔮"
    "Ranger" in className || "Assassin" in className || "Hunter" in className    -> "🏹"
    else -> "⚡"
}
