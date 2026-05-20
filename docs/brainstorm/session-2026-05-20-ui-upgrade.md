# 🎮 Brainstorm Session — UI Upgrade Plan
> **Ngày:** 2026-05-20  
> **Trạng thái:** Active  
> **Chủ đề:** Nâng cấp giao diện toàn diện + Hệ thống nhân vật 3D mới  
> **Platform:** React Native (Expo SDK 54) + TypeScript

---

## Vấn đề hiện tại

### Đánh giá UI hiện tại — Thiếu chuyên nghiệp

| # | Vấn đề | Mức độ | Chi tiết |
|---|--------|--------|----------|
| 1 | Typography quá nhỏ/chật | 🔴 Critical | h1=18px, body=13px, label=10px — khó đọc trên mobile |
| 2 | Thiếu visual hierarchy | 🔴 Critical | Các card, section flat, không depth, không phân biệt rõ |
| 3 | Hình nhân vật không phù hợp | 🔴 Critical | PNG anime chi tiết ~500KB-900KB, không phản ánh user |
| 4 | Thiếu micro-animations | 🟠 High | Chỉ có progress bar animation, thiếu sống động |
| 5 | Bottom tab bar đơn giản | 🟠 High | FontAwesome icon nhỏ, không personality |
| 6 | Color scheme chưa harmony | 🟡 Medium | Raw Tailwind colors, chưa có palette tinh tế |
| 7 | Thiếu empty/loading states | 🟡 Medium | Chỉ mock data tĩnh |
| 8 | Header sections lặp lại | 🟡 Medium | Mỗi tab có gradient header giống nhau |
| 9 | Onboarding UX chưa mượt | 🟡 Medium | Survey dài nhưng transition đơn giản |
| 10 | Thiếu feedback visual | 🟡 Medium | Tap/press không có ripple/bounce đủ rõ |

---

## Decisions

| # | Quyết định | Lý do |
|---|-----------|-------|
| D1 | **Nhân vật: Low-poly 3D body morph** — hình người geometric, body form thay đổi theo chiều cao/cân nặng/BMI/STR thực của user | Phản ánh thể trạng thực → motivate cải thiện bản thân |
| D2 | **Không cần quần áo/trang phục/equipment** — chỉ body form + aura effects | Đơn giản hóa, tập trung vào "hình dạng thật" |
| D3 | **UI Style: MMORPG nền sáng kiểu MapleStory** — vibrant, colorful, cheerful, cute nhưng game-like | User chọn, phù hợp tone motivational |
| D4 | **Approach: Character-first** — xây hệ thống nhân vật 3D trước, sau đó polish UI dần | Ưu tiên element khác biệt nhất |
| D5 | **Aura system** — aura quanh nhân vật thay đổi theo level/tổng stat | Visual indicator sức mạnh tổng |
| D6 | **Body parameters mapping** — Height→tỷ lệ, BMI→body type, STR→cơ bắp/vai | 3 trục thay đổi body rõ ràng |

---

## Kế hoạch nâng cấp chi tiết

### Phase 1 — Character System 3D (ưu tiên #1)

#### 1A. Low-Poly Body Model System
**Mục tiêu:** Tạo hệ thống nhân vật 3D cơ bản bằng SVG/Canvas, body form thay đổi theo chỉ số

**Approach kỹ thuật:**
- Sử dụng **React Native SVG** (`react-native-svg`) để vẽ nhân vật low-poly
- Body được chia thành các polygon segments: đầu, cổ, vai, thân, tay, chân
- Mỗi segment có **parametric control** dựa trên chỉ số user:
  - `height` (cm) → tỷ lệ tổng thể cao/thấp (scale Y)
  - `weight/BMI` → body width, belly size, thigh thickness
  - `STR` → shoulder width, arm thickness, chest size
- Render bằng SVG paths với smooth interpolation

**Body Types (dựa trên BMI):**
```
BMI < 18.5  → Ectomorph (gầy, thanh mảnh)
BMI 18.5-22 → Lean (thon gọn, cân đối)
BMI 22-25   → Athletic (cân đối, có cơ)
BMI 25-28   → Stocky (đầy đặn, chắc khỏe)
BMI 28-32   → Heavy (nặng, to)
BMI > 32    → Large (rất to)
```

**STR Modifier:**
```
STR 0-20   → Cơ bắp minimal (body mềm mại)
STR 20-40  → Cơ bắp nhẹ (bắt đầu có definition)
STR 40-60  → Cơ bắp trung bình (vai rộng hơn, tay có cơ)
STR 60-80  → Cơ bắp rõ (vai rộng, ngực nở, tay to)
STR 80-100 → Cơ bắp tối đa (body builder form)
```

**Biến đổi Body SVG:**
```
shoulder_width  = base * (1 + STR * 0.005) * bmi_factor
arm_thickness   = base * (1 + STR * 0.004)
chest_width     = base * (1 + STR * 0.003) * bmi_factor
waist_width     = base * bmi_factor * (1 - STR * 0.001)
leg_thickness   = base * (1 + STR * 0.002) * bmi_factor
total_height    = base * (height_cm / 170)  // normalize to 170cm
```

#### 1B. Aura & Glow System
**Mục tiêu:** Visual effects quanh nhân vật phản ánh power level

**Aura Tiers:**
| Level Range | Aura Style | Color | Effect |
|-------------|-----------|-------|--------|
| 1-10 | Không có aura | — | Chỉ subtle shadow |
| 11-20 | Faint glow | Soft blue | Nhẹ nhàng, shimmer |
| 21-30 | Steady aura | Blue-purple | Glow ổn định, pulse chậm |
| 31-40 | Strong aura | Purple-gold | Particles bay lên, glow mạnh |
| 41-50 | Blazing aura | Gold | Flame-like particles, intense glow |
| 51+ | Divine aura | Rainbow/White | Multi-layer particles, screen glow |

**Kỹ thuật:**
- Aura vẽ bằng SVG radial gradients + animated opacity
- Particles bằng `react-native-reanimated` shared values
- Pulse animation với `withRepeat(withTiming(...))` — hiện đã có reanimated trong deps

#### 1C. Character Component Architecture
```
components/
  character/
    CharacterModel.tsx      — Main SVG body renderer
    BodyParts.tsx           — Individual body segment SVG paths
    AuraEffect.tsx          — Glow/particle effects
    CharacterPreview.tsx    — Full character with aura (dùng ở Home, Onboarding)
    useBodyParams.ts        — Hook tính body parameters từ stats
    constants.ts            — Body proportions, aura configs
```

---

### Phase 2 — MapleStory-Style Design System Overhaul

#### 2A. Color Palette Upgrade
**Từ raw Tailwind colors → Curated MapleStory-inspired palette:**

```
// MAPLESTORY VIBRANT PALETTE — Nền sáng, cheerful
Primary:     #4A90D9 → #6BB5FF (sky blue gradient, softer than raw blue)  
Secondary:   #9B6BFF → #B794FF (lavender purple)
Accent Gold: #FFB347 → #FFD700 (warm gold, treasure feel)
Success:     #5CD685 → #7FE5A0 (mint green, không harsh)
Danger:      #FF6B6B → #FF8A8A (coral red, cute not aggressive)
Orange:      #FF9F43 → #FFBE76 (peach orange)
Pink:        #FF6B9D → #FF9BC0 (sakura pink)
Cyan:        #4ECDC4 → #72F2EB (teal, fresh)

Background:  #FFF8F0 (warm cream, không harsh white)
Card:        #FFFFFF (pure white with shadow)
Surface:     #FFF2E8 (warm peach tint)
```

#### 2B. Typography Upgrade
```
// TĂNG SIZE — dễ đọc, thoáng
h1:      22px → bold, letter-spacing 0.5
h2:      18px → semibold  
h3:      16px → bold
body:    15px → regular (tăng từ 13)
caption: 13px → medium (tăng từ 11)
label:   12px → semibold (tăng từ 10)
mono:    14px → monospace
stat:    18px → extra-bold monospace

// FONT: thêm custom game-like font
Headings: "Outfit" hoặc "Nunito" (rounded, friendly, game-like)
Body: "Inter" (clean, readable)
Numbers/Stats: "JetBrains Mono" hoặc "Space Mono"
```

#### 2C. Card & Component Style
**Từ flat cards → MapleStory UI panels:**
```
Card Style:
  - borderRadius: 20px (rounded hơn)
  - backgroundColor: #FFFFFF  
  - Shadow: 0 4px 20px rgba(0,0,0,0.06) (soft, deep shadow)
  - Border: 2px solid rgba(74,144,217,0.12) (subtle blue tint)
  - Khi active: border glow animation

Game Panel Style (cho stat cards, hero section):
  - Double border: outer border + inner glow
  - Corner decorations (SVG ornaments ở 4 góc)
  - Header bar gradient (mini bar trên mỗi panel)
```

#### 2D. Micro-Animations Everywhere
```
// ANIMATIONS CẦN THÊM
1. Tab switch    → slide + fade transition (300ms ease)
2. Card appear   → stagger fadeInUp khi scroll vào view  
3. Stat bar fill → spring animation với overshoot
4. Button press  → scale(0.95) + ripple color + haptic
5. Level up      → full-screen particle burst + flash
6. Quest done    → confetti + card fly-away  
7. Number change → counting animation (odometer style)
8. Pull refresh  → character animation (nhân vật tập thể dục)
9. Page scroll   → parallax trên hero section
10. Skeleton     → shimmer loading cho mọi data fetch
```

---

### Phase 3 — Screen-by-Screen UI Polish

#### 3A. Home Screen Redesign
```
Layout mới:
┌─────────────────────────────┐
│  ┌── HERO SECTION ────────┐ │
│  │  Character 3D (center)  │ │
│  │  Aura + Particles       │ │
│  │  Name + Level badge     │ │  ← Parallax scroll
│  │  EXP ring (circular)    │ │
│  └─────────────────────────┘ │
│                               │
│  ┌── QUICK STATS ──────────┐ │
│  │ 💪 STR  🧠 INT  ⚡ AGI  │ │  ← Horizontal scroll stat chips
│  │ ❤️ VIT  🔮 WIS  ✨ CHA  │ │     mỗi chip có mini bar + glow
│  └─────────────────────────┘ │
│                               │
│  ┌── TODAY'S PROGRESS ─────┐ │
│  │  Circular quest progress │ │  ← Animated ring chart
│  │  3/8 quests • 37%       │ │
│  │  [View Quest Board →]   │ │
│  └─────────────────────────┘ │
│                               │
│  ┌── RESOURCE BAR ─────────┐ │
│  │ 💰 2,350  💎 15  🔥 12d │ │  ← Compact resource row
│  └─────────────────────────┘ │
│                               │
│  ┌── DAILY INSIGHT ────────┐ │
│  │  AI generated tip        │ │  ← Motivational card
│  │  "Hôm nay STR +3! 💪"   │ │
│  └─────────────────────────┘ │
└─────────────────────────────┘
```

**Thay đổi key:**
- EXP bar → **Circular ring** quanh character (không dùng linear bar)
- Stats → **Horizontal chip scroll** thay vì table cũ
- Character chiếm center stage với aura effects
- Thêm "Daily Insight" card từ AI

#### 3B. Quest Screen Redesign
```
Layout mới:
- Header: giữ timeline nhưng thêm **quest type filter chips**
- Quest cards: thêm **rank glow border** (S rank = gold shimmer)
- Complete animation: **confetti + card slide away**
- Add **mini-map** ở top (horizontal timeline dots)
- Filter: Daily | Weekly | Boss | All
```

#### 3C. Skills Screen Redesign
```
Layout mới:
- Từ grid → **Node graph** (skill nodes kết nối bằng lines)
- Mỗi node: circular icon + glow khi unlocked
- Lines: animated dashes khi unlockable
- Zoom/Pan gesture handler
- Skill detail: bottom sheet popup
```

#### 3D. Finance Screen Redesign
```
Layout mới:
- Hero card: **3D flip card** mặt trước=NET, mặt sau=chi tiết
- Charts: **animated pie/bar** thay vì text-only
- Transaction list: **swipe actions** (delete, edit, tag)
- Thêm **monthly comparison** mini chart
```

#### 3E. Tab Bar Redesign
```
Tab bar mới — MapleStory style:
- Background: warm cream (#FFF8F0) + top border ornament
- Active tab: bouncing icon + color fill + label slide-in
- Center tab: floating action button style (Home = character icon)
- Icons: custom SVG icons thay FontAwesome
- Badge: game-style notification dot (gold with glow)
```

---

### Phase 4 — Progressive Enhancement (liên tục)

#### 4A. Onboarding Upgrade
- **Step-by-step character preview** — nhân vật 3D thay đổi realtime khi user nhập chiều cao, cân nặng, trả lời survey
- **"Transformation preview"** — hiện body hiện tại vs body mục tiêu
- **Cinematic intro** cải thiện — text typing animation + particle background

#### 4B. Achievement & Progress Visualization
- **Weekly recap screen** — chart so sánh tuần này vs tuần trước
- **Body evolution timeline** — lịch sử thay đổi body model theo thời gian
- **Milestone celebrations** — full-screen animation khi đạt mốc quan trọng

#### 4C. Smart Notifications UI
- **In-app notification** kiểu game — slide from top với icon + sound
- **Quest reminder** với character chibi nhỏ trong notification
- **Streak warning** — character buồn khi sắp mất streak

---

## Kế hoạch thực thi theo thứ tự

### Sprint 1 (Tuần 1-2): Character Foundation
- [ ] Thiết kế SVG body template (6 body types × 5 STR levels)
- [ ] Implement `CharacterModel` component với SVG paths
- [ ] Implement `useBodyParams` hook (height/weight/BMI/STR → body params)
- [ ] Tích hợp vào Home Screen thay thế character PNG hiện tại
- [ ] Basic aura system (3 tiers: none, glow, strong)

### Sprint 2 (Tuần 3-4): Design System v2
- [ ] Update Color palette → MapleStory vibrant
- [ ] Update Typography scale (tăng size)
- [ ] Create new Card/Panel components (rounded, shadow, border ornaments)
- [ ] Tab bar redesign (custom icons, animation)
- [ ] Thêm micro-animations cơ bản (button press, card appear)

### Sprint 3 (Tuần 5-6): Screen Polish
- [ ] Home Screen redesign (circular EXP, stat chips, daily insight)
- [ ] Quest Screen polish (filter, rank glow, completion animation)
- [ ] Skills Screen layout improve
- [ ] Finance Screen chart + hero card

### Sprint 4 (Tuần 7-8): Animation & Polish
- [ ] Full aura system (6 tiers + particles)
- [ ] Level-up celebration animation
- [ ] Skeleton loading screens
- [ ] Onboarding với live character preview
- [ ] Body evolution tracking setup

---

## Tài liệu tham khảo

- **MapleStory UI**: Vibrant colors, cute panels, rounded corners, cheerful feel
- **Solo Leveling: Arise**: Aura effects, stat display, power visualization
- **Apple Health**: Clean data visualization, progressive charts
- **Habitica**: Gamified character that changes with user progress

---

## Open Questions

1. **Gender cho body model?** — Có cần phân biệt nam/nữ cho body SVG không? (Hiện tại survey có hỏi giới tính)
2. **Sound effects?** — Có muốn thêm audio feedback (tap sound, level up jingle)?
3. **Offline character rendering?** — SVG + reanimated là fully offline, có OK không?
4. **Goal visualization?** — Có muốn hiện "target body" (body khi đạt goal) bên cạnh body hiện tại?

---

## Next Steps

→ `/vp-crystallize` để chuyển brainstorm thành implementation plan chi tiết
→ Hoặc bắt đầu **Sprint 1** ngay: xây dựng Character SVG system
