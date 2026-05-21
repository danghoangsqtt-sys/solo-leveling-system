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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val BgNavy   = Color(0xFF0A0A1E)
private val Cyan     = Color(0xFF38BDF8)
private val CyanDim  = Color(0xFF1E8FCF)
private val Gold     = Color(0xFFFFD700)
private val Green    = Color(0xFF00FF87)
private val TextDim  = Color(0xFF8BA3C7)

private val BootMessages = listOf(
    "[SYSTEM INITIALIZING...]",
    "[SCANNING HOST COMPATIBILITY...]",
    "[AWAKENING PROTOCOL ENGAGED...]",
    "[SOLO LEVELING SYSTEM v2.0 — READY]"
)

@Composable
fun SplashScreen(onComplete: () -> Unit) {
    var msgVisible by remember { mutableStateOf(0) }
    var loadingProgress by remember { mutableStateOf(0f) }
    var showReady by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val orbPulse by infiniteTransition.animateFloat(
        1f, 1.08f,
        infiniteRepeatable(tween(1600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "orb"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        0.35f, 0.85f,
        infiniteRepeatable(tween(1600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )
    val ringRot by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "ring"
    )
    val scanY by infiniteTransition.animateFloat(
        -0.2f, 1.2f,
        infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Restart),
        label = "scan"
    )

    LaunchedEffect(Unit) {
        BootMessages.forEachIndexed { i, _ ->
            delay(if (i == 0) 300L else 550L)
            msgVisible = i + 1
            loadingProgress = (i + 1).toFloat() / BootMessages.size
        }
        delay(400)
        showReady = true
        delay(700)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgNavy),
        contentAlignment = Alignment.Center
    ) {
        // Scan line
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scanPos = scanY * size.height
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, Cyan.copy(0.15f), Color.Transparent),
                    startY = scanPos - 40f,
                    endY = scanPos + 40f
                ),
                size = size
            )
        }

        // HUD corners
        HudCorners()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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

            // Boot messages
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(0.03f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BootMessages.forEachIndexed { i, msg ->
                    val visible = i < msgVisible
                    val isLast = i == BootMessages.lastIndex
                    if (visible) {
                        Text(
                            text = msg,
                            color = if (isLast && showReady) Green else Cyan.copy(0.75f),
                            fontSize = 11.sp,
                            fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Normal,
                            letterSpacing = 0.06f.em
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Progress bar
            Column(
                modifier = Modifier.width(280.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "LOADING",
                        color = TextDim.copy(0.6f),
                        fontSize = 9.sp,
                        letterSpacing = 0.15f.em
                    )
                    Text(
                        "${(loadingProgress * 100).toInt()}%",
                        color = Cyan.copy(0.8f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(0.08f))
                ) {
                    val animProgress by animateFloatAsState(
                        targetValue = loadingProgress,
                        animationSpec = tween(500),
                        label = "prog"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animProgress)
                            .background(
                                Brush.horizontalGradient(listOf(CyanDim, Cyan, Green))
                            )
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            if (showReady) {
                Text(
                    "[ SYSTEM READY ]",
                    color = Green.copy(glowAlpha),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.1f.em
                )
            }
        }

        // Bottom version
        Text(
            "v2.0  ·  DH SYSTEM",
            color = TextDim.copy(0.3f),
            fontSize = 9.sp,
            letterSpacing = 0.12f.em,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun HudCorners() {
    val borderColor = Cyan.copy(0.3f)
    val size = 28.dp
    val stroke = 2.dp

    Box(modifier = Modifier.fillMaxSize()) {
        // TL
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
                .size(size)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(borderColor, Offset(0f, 0f), Offset(this.size.width, 0f), stroke.toPx())
                drawLine(borderColor, Offset(0f, 0f), Offset(0f, this.size.height), stroke.toPx())
            }
        }
        // TR
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(size)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(borderColor, Offset(this.size.width, 0f), Offset(0f, 0f), stroke.toPx())
                drawLine(borderColor, Offset(this.size.width, 0f), Offset(this.size.width, this.size.height), stroke.toPx())
            }
        }
        // BL
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .size(size)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(borderColor, Offset(0f, this.size.height), Offset(this.size.width, this.size.height), stroke.toPx())
                drawLine(borderColor, Offset(0f, this.size.height), Offset(0f, 0f), stroke.toPx())
            }
        }
        // BR
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(size)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(borderColor, Offset(this.size.width, this.size.height), Offset(0f, this.size.height), stroke.toPx())
                drawLine(borderColor, Offset(this.size.width, this.size.height), Offset(this.size.width, 0f), stroke.toPx())
            }
        }
    }
}
