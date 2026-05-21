package com.systemleveling.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════════
//  SYSTEM LEVELING — COSMIC VOID DESIGN TOKENS
//  Source of truth: themes/design_theme.json + themes/design_system.md
// ══════════════════════════════════════════════════════════════════════════════

// ── Material 3 Color Scheme (Dark only) ──────────────────────────────────────
val md_theme_dark_primary                = Color(0xFFA4C9FF)
val md_theme_dark_onPrimary              = Color(0xFF00315D)
val md_theme_dark_primaryContainer       = Color(0xFF4A9EFF)
val md_theme_dark_onPrimaryContainer     = Color(0xFF003463)

val md_theme_dark_secondary              = Color(0xFFFFF9EF)
val md_theme_dark_onSecondary            = Color(0xFF3A3000)
val md_theme_dark_secondaryContainer     = Color(0xFFFFDB3C)
val md_theme_dark_onSecondaryContainer   = Color(0xFF725F00)

val md_theme_dark_tertiary               = Color(0xFF40E17E)
val md_theme_dark_onTertiary             = Color(0xFF003919)
val md_theme_dark_tertiaryContainer      = Color(0xFF00B35B)
val md_theme_dark_onTertiaryContainer    = Color(0xFF003C1A)

val md_theme_dark_error                  = Color(0xFFFFB4AB)
val md_theme_dark_onError                = Color(0xFF690005)
val md_theme_dark_errorContainer         = Color(0xFF93000A)
val md_theme_dark_onErrorContainer       = Color(0xFFFFDAD6)

val md_theme_dark_background             = Color(0xFF121222)
val md_theme_dark_onBackground           = Color(0xFFE3E0F8)

val md_theme_dark_surface                = Color(0xFF121222)
val md_theme_dark_onSurface              = Color(0xFFE3E0F8)
val md_theme_dark_surfaceVariant         = Color(0xFF333345)
val md_theme_dark_onSurfaceVariant       = Color(0xFFC0C7D4)

val md_theme_dark_outline                = Color(0xFF8A919E)
val md_theme_dark_outlineVariant         = Color(0xFF414752)

val md_theme_dark_inverseSurface         = Color(0xFFE3E0F8)
val md_theme_dark_inverseOnSurface       = Color(0xFF2F2F40)
val md_theme_dark_inversePrimary         = Color(0xFF005FAD)
val md_theme_dark_surfaceTint            = Color(0xFFA4C9FF)

// Surface container hierarchy (from design_theme.json)
val md_theme_dark_surfaceContainerLowest  = Color(0xFF0C0C1D)
val md_theme_dark_surfaceContainerLow     = Color(0xFF1A1A2B)
val md_theme_dark_surfaceContainer        = Color(0xFF1E1E2F)
val md_theme_dark_surfaceContainerHigh    = Color(0xFF29283A)
val md_theme_dark_surfaceContainerHighest = Color(0xFF333345)
val md_theme_dark_surfaceBright           = Color(0xFF38374A)
val md_theme_dark_surfaceDim              = Color(0xFF121222)

// ── Semantic Design Tokens ────────────────────────────────────────────────────
// Named after their role in the UI, not their color value.

/** Standard HUD interactivity, focused states, system pulses */
val SystemBlue   = Color(0xFF4A9EFF)
/** Dim variant of SystemBlue — headings and labels */
val SystemBlueDim = Color(0xFFA4C9FF)
/** Epic/Legendary moments — level-up, rare achievements */
val SystemGold   = Color(0xFFFFD700)
/** System health, buffs active, online status */
val SystemGreen  = Color(0xFF2ED573)
/** Evolved/GRAND_MASTER state, rare purple energy */
val SystemPurple = Color(0xFFB48EFF)
/** Warning, debt, penalties */
val SystemRed    = Color(0xFFFF6B6B)
/** Finance, income positive */
val SystemOrange = Color(0xFFFF9F43)

/** Deep background — Cosmic Void base */
val BgDeep = Color(0xFF0A0A1A)
/** Standard background */
val BgBase = Color(0xFF121222)
/** Elevated surface — glass panels */
val BgSurface = Color(0xFF1E1E2F)

/** Muted text, descriptions, secondary information */
val TextMuted   = Color(0xFFC0C7D4)
/** Primary text */
val TextPrimary = Color(0xFFE3E0F8)

// ── Glassmorphism Tokens ──────────────────────────────────────────────────────
/** Standard glass panel fill — 60% opacity dark for glass panels */
val GlassSurface          = Color(0x991E1E2F)
/** Light glass fill — for nested elements */
val GlassSurfaceLight     = Color(0x1AFFFFFF)
/** Standard frost border — 12% white */
val GlassBorder           = Color(0x1FFFFFFF)
/** Highlighted border — active focus */
val GlassBorderHighlight  = Color(0x664A9EFF)
/** Gold border — legendary/rare content */
val GlassBorderGold       = Color(0x66FFD700)
/** Purple border — evolved/grand master content */
val GlassBorderPurple     = Color(0x66B48EFF)

// ── Stat Colors ───────────────────────────────────────────────────────────────
// Consistent mapping used across OnboardingScreen, HomeScreen, SkillScreen.
val StatStr = Color(0xFF4A9EFF)  // STR — System Blue
val StatInt = Color(0xFFFFD700)  // INT — Gold
val StatAgi = Color(0xFF2ED573)  // AGI — Green
val StatVit = Color(0xFFFF6B6B)  // VIT — Red/Health
val StatWis = Color(0xFFB48EFF)  // WIS — Purple
val StatCha = Color(0xFFFF9F43)  // CHA — Orange

// ── Tier Colors (Skill Progression) ──────────────────────────────────────────
val TierBeginner     = Color(0xFF78909C)   // NOVICE / APPRENTICE
val TierIntermediate = Color(0xFF4A9EFF)   // INTERMEDIATE / ADVANCED
val TierMaster       = Color(0xFFFFD700)   // EXPERT / MASTER
val TierEvolved      = Color(0xFFB48EFF)   // GRAND_MASTER

// ── Gradient Brushes ──────────────────────────────────────────────────────────
/** Background: cosmic void radial gradient */
val BrushCosmicVoid = Brush.radialGradient(
    colors = listOf(Color(0xFF1A1A2E), BgDeep),
    radius = 1400f
)

/** EXP bar: gold shimmer */
val BrushExpBar = Brush.horizontalGradient(
    colors = listOf(SystemGold, Color(0xFFFFE16D))
)

/** Primary action button gradient */
val BrushSystemButton = Brush.horizontalGradient(
    colors = listOf(Color(0xFF1B4F8A), SystemBlue, Color(0xFF1B4F8A))
)

/** Evolved / GRAND_MASTER rainbow */
val BrushEvolved = Brush.linearGradient(
    colors = listOf(SystemGold, SystemPurple, SystemBlue, SystemGreen, SystemGold)
)

/** Gold → purple for evolved detail elements */
val BrushEvolvedDetail = Brush.horizontalGradient(
    colors = listOf(SystemGold, SystemPurple)
)

/** Stat bar generic gradient (use with color parameter) */
fun statBarBrush(color: Color) = Brush.horizontalGradient(
    colors = listOf(color.copy(alpha = 0.6f), color)
)
