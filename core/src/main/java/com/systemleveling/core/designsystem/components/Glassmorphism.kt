package com.systemleveling.core.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.core.designsystem.theme.*

// ══════════════════════════════════════════════════════════════════════════════
//  SYSTEM LEVELING — SHARED GLASS COMPONENTS
//  All UI screens should import from here for visual consistency.
// ══════════════════════════════════════════════════════════════════════════════

// ── Modifier extensions ───────────────────────────────────────────────────────

/**
 * Standard glassmorphism surface — 65% opaque dark fill + frost border.
 * Applies to information panels, quest cards, stat containers.
 */
fun Modifier.glassmorphism(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = GlassSurface,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .border(borderWidth, borderColor, shape)
    .background(backgroundColor)

/**
 * Rare glassmorphism — gold border for legendary/high-tier content.
 * Use for character status panel, evolved skill cards.
 */
fun Modifier.glassmorphismRare(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = GlassSurface
): Modifier = this
    .clip(shape)
    .border(1.dp, GlassBorderGold.copy(alpha = 0.45f), shape)
    .background(backgroundColor)

// ── GlassCard variants ────────────────────────────────────────────────────────

/**
 * Standard glass card — standard surface container with frost border.
 * Default component for information panels across all screens.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = GlassSurface,
    borderColor: Color = GlassBorder,
    elevation: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = shape, ambientColor = Color.Black.copy(0.4f), spotColor = Color.Black.copy(0.3f))
            .glassmorphism(shape = shape, backgroundColor = backgroundColor, borderColor = borderColor),
        content = content
    )
}

/**
 * Rare glass card — gold border for legendary/character content.
 * Use for: character status panel, quest complete rewards, epic skill details.
 */
@Composable
fun GlassCardRare(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = shape, ambientColor = SystemGold.copy(0.2f), spotColor = Color.Black.copy(0.4f))
            .glassmorphismRare(shape = shape),
        content = content
    )
}

/**
 * Evolved glass card — rainbow border for GRAND_MASTER / evolved state.
 * Use for: evolved skill cards, max-rank achievements.
 */
@Composable
fun GlassCardEvolved(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "evolved_border")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(
        modifier = modifier
            .clip(shape)
            .border(1.5.dp, Brush.linearGradient(listOf(SystemGold.copy(glowAlpha), SystemPurple.copy(glowAlpha), SystemBlue.copy(glowAlpha), SystemGreen.copy(glowAlpha), SystemGold.copy(glowAlpha))), shape)
            .background(Brush.linearGradient(listOf(SystemPurple.copy(0.08f), SystemGold.copy(0.05f)))),
        content = content
    )
}

// ── System Button ─────────────────────────────────────────────────────────────

/**
 * Primary system-style action button.
 * Standard glass-tinted button with border glow. Used consistently across all screens.
 *
 * @param label Button text (caps recommended)
 * @param onClick Click handler
 * @param color Accent color (defaults to SystemBlue)
 * @param pulsing Enable infinite scale pulse animation (for CTAs)
 */
@Composable
fun SystemButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = SystemBlue,
    pulsing: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "btn_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (pulsing) 1.03f else 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "scale"
    )

    Box(
        modifier = modifier
            .then(if (pulsing) Modifier.then(Modifier) else Modifier) // scale handled below
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.08f.em
        )
    }
}

/**
 * CTA system button with full-width gradient + pulsing animation.
 * Used for primary activation calls like "KÍCH HOẠT HỆ THỐNG".
 */
@Composable
fun SystemButtonCta(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = SystemBlue
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cta_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.horizontalGradient(listOf(color.copy(0.3f), color.copy(0.2f), color.copy(0.3f))))
            .border(1.dp, color.copy(glowAlpha), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.1f.em
        )
    }
}

// ── Typography components ─────────────────────────────────────────────────────

/**
 * System label — Rajdhani bold caps header for sections.
 * Use for: screen titles, section dividers, attribute labels.
 */
@Composable
fun SystemLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SystemBlueDim,
    fontSize: Float = 18f
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.06f.em,
        modifier = modifier
    )
}

/**
 * Caps label badge — small all-caps label with colored background.
 * Use for: tier badges, class badges, status chips.
 */
@Composable
fun SystemBadge(
    text: String,
    color: Color = SystemBlue,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.1f.em
        )
    }
}

// ── Progress bars ─────────────────────────────────────────────────────────────

/**
 * Stat progress bar — for character attributes (STR/INT/AGI/VIT/WIS/CHA).
 * Renders a label, gradient fill bar, and numeric value.
 */
@Composable
fun StatProgressBar(
    name: String,
    value: Int,
    maxValue: Int = 100,
    color: Color,
    modifier: Modifier = Modifier,
    showValue: Boolean = true
) {
    val progress = (value.toFloat() / maxValue).coerceIn(0f, 1f)
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.08f.em,
            modifier = Modifier.width(36.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(2.5.dp))
                .background(GlassBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(Brush.horizontalGradient(listOf(color.copy(0.6f), color)))
            )
        }
        if (showValue) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = value.toString(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(28.dp)
            )
        }
    }
}

/**
 * SP progress bar with glow — for skill progression tracking.
 * Includes animated glow dot at the tip and current/max display.
 */
@Composable
fun SpProgressBar(
    currentSp: Int,
    maxSp: Int,
    color: Color,
    modifier: Modifier = Modifier,
    isEvolved: Boolean = false
) {
    val progress = (currentSp.toFloat() / maxSp).coerceIn(0f, 1f)
    val infiniteTransition = rememberInfiniteTransition(label = "sp_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "alpha"
    )

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "SP", color = color, fontSize = 9.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 0.1f.em,
            modifier = Modifier.width(20.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(GlassBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        if (isEvolved) BrushEvolvedDetail
                        else Brush.horizontalGradient(listOf(color.copy(0.7f), color.copy(glowAlpha)))
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
                            .background(if (isEvolved) SystemGold.copy(glowAlpha) else color.copy(glowAlpha))
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "$currentSp/$maxSp",
            color = if (isEvolved) SystemGold else TextMuted,
            fontSize = 9.sp, fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * EXP progress bar — gold gradient, used on HomeScreen character panel.
 */
@Composable
fun ExpProgressBar(
    currentExp: Int,
    maxExp: Int,
    modifier: Modifier = Modifier
) {
    val progress = (currentExp.toFloat() / maxExp).coerceIn(0f, 1f)
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "EXP", color = SystemGold, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 0.1f.em,
            modifier = Modifier.width(36.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(GlassBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(BrushExpBar)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text("$currentExp/$maxExp", color = SystemGold, fontSize = 10.sp)
    }
}

// ── Scanline overlay ──────────────────────────────────────────────────────────

/**
 * Atmospheric scanline overlay — subtle CRT effect across the full screen.
 * Place as first child of root Box in screens that use the Cosmic Void aesthetic.
 */
@Composable
fun ScanlineOverlay(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
        val lineSpacing = 4.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color.Black.copy(alpha = 0.07f),
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(size.width, y),
                strokeWidth = 1f
            )
            y += lineSpacing
        }
    }
}

// ── Currency badge ────────────────────────────────────────────────────────────

/**
 * Inline currency/stat badge with icon.
 * Use for: gold display, streak counter, debt indicator.
 */
@Composable
fun CurrencyBadge(
    icon: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f))
            .border(0.5.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 13.sp)
        Spacer(Modifier.width(5.dp))
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
