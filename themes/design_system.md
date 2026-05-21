# System Leveling — Design System

**Source of truth:** `themes/design_theme.json`  
**Kotlin tokens:** `core/src/main/java/com/systemleveling/core/designsystem/theme/`  
**Shared components:** `core/src/main/java/com/systemleveling/core/designsystem/components/Glassmorphism.kt`

---

## Brand & Style

The design system evokes the high-stakes immersion of a "System Leveling" protagonist, blending the vibrant charm of **MapleStory M** with the polished volumetric depth of **Genshin Impact**. The UI feels like a futuristic "Hunter's Terminal" or AR HUD floating in a cosmic void.

Primary style: **Holographic Glassmorphism** — extreme backdrop blurs (40–50px), semi-transparent layers for "stacked volumetric" effect, high-contrast neon-tinted outlines, and holographic shimmers on rare elements.

---

## Colors

### Semantic Tokens (`Color.kt`)

| Token | Hex | Usage |
|-------|-----|-------|
| `SystemBlue` | `#4A9EFF` | Standard HUD interactivity, focused states, system pulses |
| `SystemBlueDim` | `#A4C9FF` | Headings, labels, dimmed primary |
| `SystemGold` | `#FFD700` | Epic/Legendary moments, level-up, rare achievements |
| `SystemGreen` | `#2ED573` | System health, buffs active, online status |
| `SystemPurple` | `#B48EFF` | Evolved/GRAND_MASTER state, rare purple energy |
| `SystemRed` | `#FF6B6B` | Warnings, debt, penalties |
| `SystemOrange` | `#FF9F43` | Finance, income positive |

### Background Tokens

| Token | Hex | Usage |
|-------|-----|-------|
| `BgDeep` | `#0A0A1A` | Cosmic void base — deepest layer |
| `BgBase` | `#121222` | Standard screen background |
| `BgSurface` | `#1E1E2F` | Elevated glass panels |

### Glassmorphism Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `GlassSurface` | `#1E1E2F @ 60%` | Standard glass panel fill |
| `GlassSurfaceLight` | `#FFFFFF @ 10%` | Nested/secondary elements |
| `GlassBorder` | `#FFFFFF @ 12%` | Standard frost border |
| `GlassBorderHighlight` | `#4A9EFF @ 40%` | Active/focused border |
| `GlassBorderGold` | `#FFD700 @ 40%` | Legendary/rare content |
| `GlassBorderPurple` | `#B48EFF @ 40%` | Evolved/GRAND_MASTER content |

### Stat Colors

| Stat | Token | Hex |
|------|-------|-----|
| STR | `StatStr` | `#4A9EFF` |
| INT | `StatInt` | `#FFD700` |
| AGI | `StatAgi` | `#2ED573` |
| VIT | `StatVit` | `#FF6B6B` |
| WIS | `StatWis` | `#B48EFF` |
| CHA | `StatCha` | `#FF9F43` |

### Tier Colors (Skill Progression)

| Tier | Token | Hex | Covers |
|------|-------|-----|--------|
| Beginner | `TierBeginner` | `#78909C` | NOVICE, APPRENTICE |
| Intermediate | `TierIntermediate` | `#4A9EFF` | INTERMEDIATE, ADVANCED |
| Master | `TierMaster` | `#FFD700` | EXPERT, MASTER |
| Evolved | `TierEvolved` | `#B48EFF` | GRAND_MASTER |

### Gradient Brushes

| Brush | Usage |
|-------|-------|
| `BrushCosmicVoid` | Radial background gradient for all screens |
| `BrushExpBar` | Gold shimmer for EXP progress bar |
| `BrushEvolved` | Rainbow gradient: GOLD→PURPLE→BLUE→GREEN→GOLD |
| `BrushEvolvedDetail` | Gold→Purple for evolved element accents |
| `statBarBrush(color)` | Generic stat bar gradient from `color@60%` → `color` |

---

## Typography

Dual-font strategy: **Rajdhani** (HUD/game feel) + **Inter** (readability).

| Style | Font | Weight | Size | Line-height | Tracking | M3 Slot |
|-------|------|--------|------|-------------|---------|---------|
| Display | Rajdhani | Bold | 48sp | 56sp | 2.4sp | `displayLarge` |
| Headline | Rajdhani | Bold | 24sp | 32sp | 0.48sp | `headlineMedium` |
| Title (mobile) | Rajdhani | Bold | 20sp | 28sp | 0.4sp | `titleLarge` |
| Body | Inter | Regular | 16sp | 24sp | 0sp | `bodyLarge` |
| Body Bold | Inter | SemiBold | 16sp | 24sp | 0sp | `bodyMedium` |
| Label Caps | Rajdhani | SemiBold | 12sp | 16sp | 1.2sp | `labelSmall` |

**Compose usage:**

```kotlin
// Rajdhani for headers/labels
MaterialTheme.typography.titleLarge  // 20sp Rajdhani Bold
MaterialTheme.typography.labelSmall  // 12sp Rajdhani SemiBold, caps

// Inter for body text
MaterialTheme.typography.bodyLarge   // 16sp Inter Regular
MaterialTheme.typography.bodyMedium  // 16sp Inter SemiBold
```

**Fallback:** Both font families fall back to `FontFamily.SansSerif` when Google Play Services cannot serve fonts.

---

## Elevation & Depth Layers

| Level | Surface | Blur | Fill | Border |
|-------|---------|------|------|--------|
| 0 — Background | Cosmic Void | — | `BgDeep` radial gradient | — |
| 1 — Glass Panel | Standard surface | 40px | `GlassSurface` (60% opaque) | `GlassBorder` (12% white) |
| 2 — Modal/Popover | Elevated popup | 50px | `GlassSurfaceLight` (10%) | `GlassBorderHighlight` |
| 3 — Interactive Node | Floating button | — | `color.copy(0.15f)` | `color.copy(0.6f)` |

Shadows are never pure black — ambient shadows use `Color.Black.copy(0.4f)`, spot shadows `Color.Black.copy(0.3f)`. Legendary content uses `SystemGold.copy(0.2f)` ambient shadow.

---

## Shapes

| Context | Shape | Compose |
|---------|-------|---------|
| Standard cards/panels | Rounded 16dp | `RoundedCornerShape(16.dp)` |
| Buttons | Rounded 8dp | `RoundedCornerShape(8.dp)` |
| CTA buttons | Rounded 10dp | `RoundedCornerShape(10.dp)` |
| Badges/chips | Rounded 4dp | `RoundedCornerShape(4.dp)` |
| Stat/progress bars | Rounded 2.5dp | `RoundedCornerShape(2.5.dp)` |
| Avatar/node | Circular | `CircleShape` |

---

## Components Reference

All shared components live in `Glassmorphism.kt`. Import from there — do not redefine locally.

### Modifier Extensions

```kotlin
// Standard glass panel (cards, quest items, stat containers)
Modifier.glassmorphism(shape, backgroundColor, borderColor, borderWidth)

// Legendary/rare panel (gold border)
Modifier.glassmorphismRare(shape, backgroundColor)
```

### Card Variants

| Component | Border | Shadow | When to Use |
|-----------|--------|--------|-------------|
| `GlassCard` | White frost | Black | Default panel — stats, quests, inventory |
| `GlassCardRare` | Gold | Gold ambient | Character panel, rare achievements |
| `GlassCardEvolved` | Animated rainbow | — | GRAND_MASTER skills, max-rank items |

### Buttons

```kotlin
// Standard action button with optional pulse animation
SystemButton(label, onClick, color = SystemBlue, pulsing = false)

// Full-width CTA button with gradient + border glow
SystemButtonCta(label, onClick, color = SystemBlue)
```

### Labels & Badges

```kotlin
// Section header (Rajdhani bold caps)
SystemLabel(text, color = SystemBlueDim, fontSize = 18f)

// Status chip (tier, class, rank)
SystemBadge(text, color = SystemBlue)
```

### Progress Bars

```kotlin
// Character attribute bars (STR/INT/AGI/VIT/WIS/CHA)
StatProgressBar(name, value, maxValue = 100, color)

// Skill SP bar with animated glow dot
SpProgressBar(currentSp, maxSp, color, isEvolved = false)

// EXP bar — gold gradient, HomeScreen character panel
ExpProgressBar(currentExp, maxExp)
```

### Utility

```kotlin
// Gold/streak/debt inline badge with emoji icon
CurrencyBadge(icon, label, color)

// CRT scanline overlay — place as first child of root Box
ScanlineOverlay()
```

---

## Layout & Spacing

- **Base unit:** 4px
- **Mobile gutter:** 16dp horizontal margin
- **Panel gap:** 24dp between glassmorphic cards (lets background gradient breathe)
- **Card padding:** 16dp internal padding standard; 12dp for compact rows

---

## Animation Conventions

| Effect | Duration | Easing | Repeat |
|--------|----------|--------|--------|
| Button pulse / border glow | 800–900ms | `EaseInOutSine` | Reverse |
| SP bar glow dot | 1200ms | `EaseInOutSine` | Reverse |
| Evolved rainbow border | 1200ms | `EaseInOutSine` | Reverse |
| Scanline scroll | CSS only | — | — |

Always use `rememberInfiniteTransition(label = "...")` with a meaningful label for all looping animations.

---

## Screen Inventory

| Screen | File | Stitch Reference |
|--------|------|-----------------|
| Splash / Awakening | `OnboardingScreen.kt` step 1 | `Thuc_Tinh_Chuc_Nghiep_Cinematic.html` |
| Character Creation | `OnboardingScreen.kt` | `Khoi_Tao_Nhan_Vat.html` |
| Home / Status | `HomeScreen.kt` | `Bang_Trang_Thai_Home.html` |
| Daily Quests | — | `Nhiem_Vu_Hang_Ngay.html` |
| Skill Inventory | `SkillTreeScreen.kt` | `Kho_Ky_Nang_Skill_Inventory.html` |
| Finance | — | `Quan_Ly_Tai_Chinh_Finance_Dashboard.html` |
| Inventory | — | `Kho_Vat_Pham_Inventory.html` |
| Titles | — | `Kho_Danh_Hieu_Titles.html` |
| Calendar | — | `Lich_trinh_Calendar.html` |

---

## Tier System (Skills)

```kotlin
enum class SkillLevel(val title: String, val maxSp: Int) {
    NOVICE("Nhập Môn", 100),
    APPRENTICE("Sơ Cấp", 300),
    INTERMEDIATE("Trung Sơ Cấp", 600),
    ADVANCED("Trung Cấp", 1000),
    EXPERT("Tiền Cao Cấp", 1800),
    MASTER("Cao Cấp", 3000),
    GRAND_MASTER("Grand Master", 9999)
}
```

| SkillLevel | Tier | Color |
|-----------|------|-------|
| NOVICE, APPRENTICE | Sơ Cấp | `TierBeginner` |
| INTERMEDIATE, ADVANCED | Trung Cấp | `TierIntermediate` |
| EXPERT, MASTER | Master | `TierMaster` |
| GRAND_MASTER | ⚡ Tiến Hóa | `TierEvolved` + rainbow border |
