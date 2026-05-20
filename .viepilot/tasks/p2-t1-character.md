# Task P2-T1: Dynamic Low-Poly SVG Character Model & Aura System

## Objective
Implement a parametric, stylized, faceless silhouette character model using SVG. The character body must adapt dynamically based on the user's height, weight, BMI, and STR (Strength) stats. It must also have a glowing, animating aura reflecting their level and Highest Stat.

## Paths
- `components/character/types.ts`
- `components/character/useBodyParams.ts`
- `components/character/BodyParts.tsx`
- `components/character/AuraEffect.tsx`
- `components/character/CharacterPreview.tsx`
- `app/(tabs)/index.tsx` (Integration)
- `app/onboarding.tsx` (Integration)

## File-Level Plan

### 1. `components/character/types.ts`
Define user stat input types, BMI categories, and calculated body dimensions (shoulder width, arm thickness, hip/waist size, height ratio).

### 2. `components/character/useBodyParams.ts`
A custom hook that takes the user's height, weight, STR, and level and returns specific dimensional multipliers for rendering the SVG body parts.
- Calculates BMI to classify body type (Ectomorph, Lean, Athletic, Stocky, Heavy, Large).
- Uses STR to inflate shoulder/chest/arm scale.
- Adjusts leg height ratio based on height.

### 3. `components/character/BodyParts.tsx`
Vẽ các mảnh đa giác (low-poly silhouette style):
- Đầu (Head)
- Cổ (Neck)
- Ngực & Thân trên (Chest/Torso)
- Eo & Thân dưới (Waist/Hips)
- Bắp tay, cẳng tay (Left/Right Arms)
- Đùi, bắp chân (Left/Right Legs)
SVG paths should dynamically scale coordinates based on calculated body parameter widths/heights, with a cool dark cyber shadow style.

### 4. `components/character/AuraEffect.tsx`
Creates an animated backdrop representing the character's aura.
- Animated with `react-native-reanimated` or standard SVG gradient transitions.
- Lowest level = faint glow.
- Higher level = large pulsing glow with particles.

### 5. `components/character/CharacterPreview.tsx`
Combines `CharacterModel` and `AuraEffect` inside a flex container, showing a complete glowing character.

### 6. Integration
- Update `app/(tabs)/index.tsx` to display `CharacterPreview` instead of the static character image.
- Update `app/onboarding.tsx` to show the preview during step 1 (height/weight input) so the user sees the silhouette morphing in real time!

## Verification
- Run type check: `npx tsc --noEmit`
- Run the app and verify visual rendering in browser.
