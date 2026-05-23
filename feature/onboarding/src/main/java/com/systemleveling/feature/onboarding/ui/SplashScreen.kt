package com.systemleveling.feature.onboarding.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ── Palette ──────────────────────────────────────────────────────────────────
private val BgDeep   = Color(0xFF05050F)
private val BgNavy   = Color(0xFF0A0A1E)
private val Cyan     = Color(0xFF38BDF8)
private val CyanDim  = Color(0xFF1E8FCF)
private val CyanGlow = Color(0xFF7DD3F8)
private val Gold     = Color(0xFFFFD700)
private val GoldDim  = Color(0xFFB8860B)
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

private data class Particle(
    val x: Float, val y: Float, val r: Float,
    val speedY: Float, val alpha: Float, val color: Color
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

    val orbScale by infiniteTransition.animateFloat(
        1f, 1.10f,
        infiniteRepeatable(tween(1500, EaseInOutSine), RepeatMode.Reverse), "orb"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        0.35f, 0.85f,
        infiniteRepeatable(tween(1500, EaseInOutSine), RepeatMode.Reverse), "glow"
    )
    val ringRot by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(5000, LinearEasing)), "ring"
    )
    val innerRot by infiniteTransition.animateFloat(
        360f, 0f,
        infiniteRepeatable(tween(3200, LinearEasing)), "inner"
    )
    val particleProg by infiniteTransition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(4000, LinearEasing)), "particles"
    )
    val scanY by infiniteTransition.animateFloat(
        -0.05f, 1.05f,
        infiniteRepeatable(tween(2800, LinearEasing)), "scan"
    )
    val titleGlow by infiniteTransition.animateFloat(
        0.7f, 1f,
        infiniteRepeatable(tween(2000, EaseInOutSine), RepeatMode.Reverse), "title"
    )

    val stars = remember {
        val rng = java.util.Random(7L)
        List(StarCount) {
            Particle(
                x = rng.nextFloat(),
                y = rng.nextFloat(),
                r = rng.nextFloat() * 1.8f + 0.3f,
                speedY = rng.nextFloat() * 0.001f + 0.0002f,
                alpha = rng.nextFloat() * 0.5f + 0.1f,
                color = if (rng.nextInt(4) == 0) Gold else CyanGlow
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
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF0D0D2B), BgDeep),
                    radius = 2000f
                )
            )
            .alpha(animAlpha),
        contentAlignment = Alignment.Center
    ) {
        // Star field + floating particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEachIndexed { i, star ->
                val yPos = ((star.y + particleProg * star.speedY * 100f) % 1f) * size.height
                drawCircle(
                    color = star.color.copy(alpha = star.alpha),
                    radius = star.r * density,
                    center = Offset(star.x * size.width, yPos)
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

            // ── Title ─────────────────────────────────────────────────────────
            Text(
                text = "SOLO LEVELING SYSTEM",
                color = Cyan.copy(alpha = titleGlow),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.10f.em,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "CLOSED BETA  ·  IN-DEVELOPMENT BUILD",
                color = TextDim.copy(0.55f),
                fontSize = 9.sp,
                letterSpacing = 0.12f.em,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // ── Central energy figure (Canvas) ────────────────────────────────
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    // Outer ambient glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Cyan.copy(alpha = 0.18f * glowAlpha),
                                CyanDim.copy(alpha = 0.08f * glowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy), radius = 110f
                        ),
                        radius = 110f
                    )

                    // Outer rotating ring with tick marks
                    rotate(ringRot, Offset(cx, cy)) {
                        drawCircle(
                            color = Cyan.copy(0.20f), radius = 95f,
                            style = Stroke(width = 1f)
                        )
                        repeat(16) { i ->
                            val angle = i * (360f / 16f) * (PI / 180f).toFloat()
                            val inner = 91f
                            val outer = if (i % 4 == 0) 82f else 86f
                            drawLine(
                                color = Cyan.copy(if (i % 4 == 0) 0.7f else 0.35f),
                                start = Offset(cx + inner * cos(angle), cy + inner * sin(angle)),
                                end = Offset(cx + outer * cos(angle), cy + outer * sin(angle)),
                                strokeWidth = if (i % 4 == 0) 2f else 1f
                            )
                        }
                        // Bright dot at 0° (follows rotation)
                        drawCircle(
                            color = Gold.copy(0.9f), radius = 4f,
                            center = Offset(cx + 95f, cy)
                        )
                    }

                    // Inner counter-rotating ring
                    rotate(innerRot, Offset(cx, cy)) {
                        drawCircle(
                            color = CyanDim.copy(0.30f), radius = 65f,
                            style = Stroke(width = 1f)
                        )
                        repeat(8) { i ->
                            val angle = i * 45f * (PI / 180f).toFloat()
                            drawCircle(
                                color = Gold.copy(0.75f), radius = 3.5f,
                                center = Offset(cx + 65f * cos(angle), cy + 65f * sin(angle))
                            )
                        }
                    }

                    // Electric arcs from core outward
                    val arcCount = 6
                    repeat(arcCount) { i ->
                        val angle = (i * (360f / arcCount) + ringRot * 0.6f) * (PI / 180f).toFloat()
                        val len = 38f + (i % 3) * 12f
                        drawLine(
                            color = Cyan.copy(0.5f * glowAlpha),
                            start = Offset(cx + 22f * cos(angle), cy + 22f * sin(angle)),
                            end = Offset(cx + len * cos(angle), cy + len * sin(angle)),
                            strokeWidth = 1.2f
                        )
                    }

                    // Core glow orb (layers)
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(White.copy(0.95f), Cyan.copy(0.7f), CyanDim.copy(0.2f), Color.Transparent),
                            center = Offset(cx, cy), radius = 26f * orbScale
                        ),
                        radius = 26f * orbScale
                    )
                    // Core bright spot
                    drawCircle(color = White.copy(0.8f), radius = 8f * orbScale, center = Offset(cx, cy))
                }

                // Center text label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✦", color = Gold.copy(0.9f), fontSize = 12.sp)
                    Text(
                        "Level Up!",
                        color = Gold,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.04f.em
                    )
                    Text("✦", color = Gold.copy(0.5f), fontSize = 9.sp)
                }
            }

            // ── HP / MP bars ──────────────────────────────────────────────────
            HudBars(modifier = Modifier.padding(horizontal = 48.dp))

            Spacer(Modifier.height(20.dp))

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
