package com.systemleveling.feature.onboarding.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import kotlinx.coroutines.delay
import java.util.Random

// ── Palette ──────────────────────────────────────────────────────────────────
private val BgDeep   = Color(0xFF05050F)
private val BgNavy   = Color(0xFF0A0A1E)
private val Cyan     = Color(0xFF38BDF8)
private val CyanDim  = Color(0xFF1E8FCF)
private val CyanGlow = Color(0xFF7DD3F8)
private val Gold     = Color(0xFFFFD700)
private val Green    = Color(0xFF00FF87)
private val White    = Color(0xFFFFFFFF)
private val TextDim  = Color(0xFF8BA3C7)

private val BootMessages = listOf(
    "[KHỞI ĐỘNG HỆ THỐNG...]",
    "[QUÉT CHỮ KÝ NEURAL...]",
    "[KẾT NỐI CƠ SỞ DỮ LIỆU HUNTER...]",
    "[ỔN ĐỊNH NHÂN MANA CORE...]",
    "[TẢI GIAO THỨC SHADOW MONARCH...]",
    "[HIỆU CHỈNH MA TRẬN NHIỆM VỤ: OK]",
    "[MỞ CỔNG DUNGEON HÀNG NGÀY...]",
    "[SOLO LEVELING SYSTEM v2.0 — KÍCH HOẠT ✓]"
)

private val StarCount = 60

private data class Star(
    val x: Float, val y: Float, val r: Float, val a: Float
)

@Composable
fun SplashScreen(onComplete: () -> Unit) {
    var msgVisible by remember { mutableStateOf(0) }
    var loadingProgress by remember { mutableStateOf(0f) }
    var showReady by remember { mutableStateOf(false) }
    var screenAlpha by remember { mutableStateOf(0f) }

    val animAlpha by animateFloatAsState(
        targetValue = screenAlpha,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "fade"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val orbPulse by infiniteTransition.animateFloat(
        0.9f, 1.05f,
        infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse), "orb"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        0.35f, 0.85f,
        infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse), "glow"
    )
    val ringRot by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(5000, easing = LinearEasing)), "ring"
    )
    val starTwinkle by infiniteTransition.animateFloat(
        0.5f, 1f,
        infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), "stars"
    )
    val scanY by infiniteTransition.animateFloat(
        -0.05f, 1.05f,
        infiniteRepeatable(tween(2800, easing = LinearEasing)), "scan"
    )

    val stars = remember {
        val rng = Random(7L)
        List(StarCount) {
            Star(
                x = rng.nextFloat(),
                y = rng.nextFloat(),
                r = rng.nextFloat() * 1.5f + 0.5f,
                a = rng.nextFloat() * 0.5f + 0.3f
            )
        }
    }

    LaunchedEffect(Unit) {
        screenAlpha = 1f
        BootMessages.forEachIndexed { i, _ ->
            delay(if (i == 0) 250L else 290L)
            msgVisible = i + 1
            loadingProgress = (i + 1).toFloat() / BootMessages.size
        }
        delay(350)
        showReady = true
        delay(500)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgNavy),
        contentAlignment = Alignment.Center
    ) {
        // Star field
        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEach { star ->
                drawCircle(
                    color = Cyan.copy(alpha = star.a * starTwinkle),
                    radius = star.r.dp.toPx(),
                    center = Offset(star.x * size.width, star.y * size.height)
                )
            }
        }
        
        // Scan line
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pos = scanY * size.height
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, Cyan.copy(0.07f), Color.Transparent),
                    startY = pos - 50f, endY = pos + 50f
                ),
                size = size
            )
        }

        // Main content column
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // System orb
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.minDimension / 2f

                    // Outer ambient glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Cyan.copy(glowAlpha * 0.25f), Color.Transparent),
                            center = Offset(cx, cy), radius = r
                        ),
                        center = Offset(cx, cy), radius = r
                    )

                    // Rotating arc 1
                    drawArc(
                        color = Cyan.copy(glowAlpha * 0.8f),
                        startAngle = ringRot,
                        sweepAngle = 220f, useCenter = false,
                        style = Stroke(2.5f.dp.toPx())
                    )
                    // Rotating arc 2 (offset)
                    drawArc(
                        color = Gold.copy(glowAlpha * 0.5f),
                        startAngle = ringRot + 200f,
                        sweepAngle = 100f, useCenter = false,
                        style = Stroke(1.5f.dp.toPx())
                    )
                    // Inner ring (static)
                    drawCircle(
                        color = Cyan.copy(0.12f),
                        center = Offset(cx, cy),
                        radius = r * 0.58f,
                        style = Stroke(1f.dp.toPx())
                    )
                }

                // Core orb box
                Box(
                    modifier = Modifier
                        .size((72 * orbPulse).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFF0E2A44), Color(0xFF050510)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = (34 * orbPulse).sp, modifier = Modifier.alpha(glowAlpha))
                }
            }

            Spacer(Modifier.height(36.dp))

            // Title
            Text(
                "SYSTEM LEVELING",
                color = Cyan.copy(glowAlpha * 0.9f + 0.1f),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.18f.em
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "AWAKENING PROTOCOL",
                color = TextDim.copy(0.7f),
                fontSize = 10.sp,
                letterSpacing = 0.22f.em
            )

            Spacer(Modifier.height(40.dp))

            // ── Boot messages ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(White.copy(0.03f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                BootMessages.forEachIndexed { i, msg ->
                    if (i < msgVisible) {
                        val isLast = i == BootMessages.lastIndex
                        Text(
                            text = msg,
                            color = if (isLast && showReady) Green else Cyan.copy(0.7f),
                            fontSize = 10.sp,
                            fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Normal,
                            letterSpacing = 0.05f.em
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Loading progress bar ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("LOADING", color = TextDim.copy(0.5f), fontSize = 8.sp, letterSpacing = 0.14f.em)
                    val animProg by animateFloatAsState(loadingProgress, tween(400), label = "pct")
                    Text(
                        "${(animProg * 100).toInt()}%",
                        color = Cyan.copy(0.85f), fontSize = 8.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                val animProg by animateFloatAsState(loadingProgress, tween(400), label = "prog")
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(White.copy(0.07f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight().fillMaxWidth(animProg)
                            .background(Brush.horizontalGradient(listOf(CyanDim, Cyan, Green)))
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            if (showReady) {
                Text(
                    "[ HỆ THỐNG SẴN SÀNG ]",
                    color = Green.copy(glowAlpha),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.10f.em
                )
            }
        }

        // ── Skill icon row at absolute bottom ────────────────────────────────
        SkillIconRow(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )

        // ── HUD corner brackets ───────────────────────────────────────────────
        HudCorners()

        // ── Bottom version tag ────────────────────────────────────────────────
        Text(
            "v2.0  ·  SOLO LEVELING SYSTEM",
            color = TextDim.copy(0.25f),
            fontSize = 8.sp,
            letterSpacing = 0.12f.em,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
        )
    }
}

// ── HP/MP bars HUD widget ─────────────────────────────────────────────────────
@Composable
private fun HudBars(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        StatBar(label = "HP", value = 0.78f, color = Color(0xFFFF6B6B), trackColor = Color(0x33FF6B6B))
        StatBar(label = "MP", value = 0.62f, color = Cyan, trackColor = Color(0x3338BDF8))
    }
}

@Composable
private fun StatBar(label: String, value: Float, color: Color, trackColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = color.copy(0.85f), fontSize = 9.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 0.05f.em, modifier = Modifier.width(20.dp))
        Box(
            modifier = Modifier
                .weight(1f).height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight().fillMaxWidth(value)
                    .background(Brush.horizontalGradient(listOf(color.copy(0.6f), color)))
            )
        }
    }
}

// ── Skill icon row ────────────────────────────────────────────────────────────
private val SkillIcons = listOf("⚡", "🌀", "🏃", "🛡️", "◈")

@Composable
private fun SkillIconRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SkillIcons.forEachIndexed { i, icon ->
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (i < 4) Color(0x33387FF8) else Color(0x22FFFFFF)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 18.sp)
            }
        }
    }
}

// ── HUD corner brackets ───────────────────────────────────────────────────────
@Composable
private fun HudCorners() {
    val borderColor = Cyan.copy(alpha = 0.30f)
    val size = 26.dp
    val stroke = 2.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Corner(Alignment.TopStart, size, stroke, borderColor)
        Corner(Alignment.TopEnd, size, stroke, borderColor)
        Corner(Alignment.BottomStart, size, stroke, borderColor)
        Corner(Alignment.BottomEnd, size, stroke, borderColor)
    }
}

@Composable
private fun Corner(
    alignment: Alignment,
    size: androidx.compose.ui.unit.Dp,
    stroke: androidx.compose.ui.unit.Dp,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(alignment)
            .padding(16.dp)
            .size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val s = stroke.toPx()
            when (alignment) {
                Alignment.TopStart -> {
                    drawLine(color, Offset(0f, 0f), Offset(w, 0f), s)
                    drawLine(color, Offset(0f, 0f), Offset(0f, h), s)
                }
                Alignment.TopEnd -> {
                    drawLine(color, Offset(w, 0f), Offset(0f, 0f), s)
                    drawLine(color, Offset(w, 0f), Offset(w, h), s)
                }
                Alignment.BottomStart -> {
                    drawLine(color, Offset(0f, h), Offset(w, h), s)
                    drawLine(color, Offset(0f, h), Offset(0f, 0f), s)
                }
                else -> {
                    drawLine(color, Offset(w, h), Offset(0f, h), s)
                    drawLine(color, Offset(w, h), Offset(w, 0f), s)
                }
            }
        }
    }
}
