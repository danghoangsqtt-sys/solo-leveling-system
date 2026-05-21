package com.systemleveling.feature.onboarding.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.core.designsystem.components.GlassCard
import com.systemleveling.core.designsystem.theme.SystemLevelingTheme
import kotlinx.coroutines.delay

// ── Palette ─────────────────────────────────────────────────────────────────
private val BG = Color(0xFF121222)
private val BG_DEEP = Color(0xFF0A0A1A)
private val PRIMARY = Color(0xFF4A9EFF)
private val PRIMARY_DIM = Color(0xFFA4C9FF)
private val GOLD = Color(0xFFFFD700)
private val GREEN = Color(0xFF2ED573)
private val GLASS = Color(0x1AFFFFFF)
private val GLASS_BORDER = Color(0x1FFFFFFF)
private val TEXT_MUTED = Color(0xFFC0C7D4)

// ── Root ────────────────────────────────────────────────────────────────────
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    var nickname by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("Warrior") }
    var surveyStats by remember { mutableStateOf<com.systemleveling.core.database.entity.StatEntity?>(null) }

    // Survey screen bypasses the Box wrapper — it has its own full-screen layout
    if (currentStep == 2) {
        SurveyScreen(
            onBack = { currentStep = 1 },
            onNext = { stats ->
                surveyStats = stats
                currentStep = 3
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1A1A2E), BG_DEEP),
                    radius = 1200f
                )
            )
    ) {
        ScanlineOverlay()

        when (currentStep) {
            0 -> IntroCinematicStep { currentStep = 1 }
            1 -> BasicInfoStep(nickname, { nickname = it }) { currentStep = 2 }
            3 -> ClassSelectionStep(selectedClass, { selectedClass = it }) { currentStep = 4 }
            4 -> GoalSelectionStep {
                viewModel.completeOnboarding(
                    nickname = nickname.ifBlank { "Shadow Monarch" },
                    selectedClass = selectedClass,
                    surveyStats = surveyStats
                )
                onFinish()
            }
        }
    }
}

// ── Scanline overlay ─────────────────────────────────────────────────────────
@Composable
private fun ScanlineOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineSpacing = 4.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color.Black.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += lineSpacing
        }
    }
}

// ── Step progress bar ─────────────────────────────────────────────────────────
@Composable
private fun StepIndicator(current: Int, total: Int = 4) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(total) { idx ->
            val active = idx < current
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (active) PRIMARY else GLASS_BORDER)
            )
        }
    }
}

// ── Step 0 — Cinematic intro ─────────────────────────────────────────────────
@Composable
fun IntroCinematicStep(onNext: () -> Unit) {
    val messages = listOf(
        "[SYSTEM INITIALIZING...]",
        "[SCANNING HOST...]",
        "[COMPATIBLE HOST DETECTED]",
        "[AWAKENING PROTOCOL ENGAGED]",
        "Bạn đã được chọn. Hệ thống đang khởi tạo..."
    )
    var visibleCount by remember { mutableStateOf(0) }
    var showButton by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "glow"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "ring"
    )

    LaunchedEffect(Unit) {
        messages.forEachIndexed { i, _ ->
            delay(if (i == 0) 400 else 900)
            visibleCount = i + 1
        }
        delay(600)
        showButton = true
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pulsing system orb
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Outer glow ring
                drawCircle(
                    color = PRIMARY.copy(alpha = glowAlpha * 0.2f),
                    radius = size.minDimension / 2f
                )
                // Rotating dashed ring
                drawArc(
                    color = PRIMARY.copy(alpha = 0.6f),
                    startAngle = ringRotation,
                    sweepAngle = 240f,
                    useCenter = false,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawArc(
                    color = GOLD.copy(alpha = 0.5f),
                    startAngle = ringRotation + 180f,
                    sweepAngle = 120f,
                    useCenter = false,
                    style = Stroke(width = 1.5f.dp.toPx())
                )
            }
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF1E3A5F), Color(0xFF0A1628))
                        )
                    )
                    .border(1.dp, PRIMARY.copy(alpha = glowAlpha), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚡",
                    fontSize = 36.sp,
                    modifier = Modifier.alpha(glowAlpha)
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        // Typewriter messages
        messages.take(visibleCount).forEachIndexed { i, msg ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
            ) {
                Text(
                    text = msg,
                    color = if (i == messages.lastIndex) PRIMARY_DIM else PRIMARY,
                    fontWeight = if (i == messages.lastIndex) FontWeight.Normal else FontWeight.SemiBold,
                    fontSize = if (i == messages.lastIndex) 14.sp else 13.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.08f.em,
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .then(
                            if (i == messages.lastIndex) Modifier.alpha(glowAlpha) else Modifier
                        )
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        AnimatedVisibility(visible = showButton, enter = fadeIn(tween(600))) {
            SystemButton(label = "[ TAP TO AWAKEN ]", onClick = onNext)
        }
    }
}

// ── Step 1 — Character registration ─────────────────────────────────────────
@Composable
fun BasicInfoStep(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StepIndicator(current = 1)
        Spacer(Modifier.height(32.dp))

        SystemLabel(text = "ĐĂNG KÝ NHÂN VẬT")
        Spacer(Modifier.height(8.dp))
        Text(
            "Nhập thông tin để hệ thống định danh chiến binh",
            color = TEXT_MUTED, fontSize = 13.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "TÊN CHIẾN BINH",
                    color = PRIMARY, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 0.12f.em
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = onNicknameChange,
                    placeholder = { Text("Shadow Monarch...", color = TEXT_MUTED, fontSize = 14.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PRIMARY,
                        unfocusedBorderColor = GLASS_BORDER,
                        cursorColor = PRIMARY,
                        focusedContainerColor = Color(0x0AFFFFFF),
                        unfocusedContainerColor = Color(0x05FFFFFF)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tên sẽ hiển thị trên HUD và bảng xếp hạng",
                    color = TEXT_MUTED, fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        SystemButton(label = "TIẾP THEO →", onClick = onNext)
    }
}

// ── Step 3 — Class selection ─────────────────────────────────────────────────
@Composable
fun ClassSelectionStep(
    selectedClass: String,
    onClassSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    data class ClassInfo(
        val id: String, val name: String, val icon: String,
        val desc: String, val stat: String, val color: Color
    )
    val classes = listOf(
        ClassInfo("Warrior", "CHIẾN BINH", "⚔️", "Tiền tuyến bất khuất. Sức mạnh và sức bền vượt trội.", "STR • VIT", PRIMARY),
        ClassInfo("Mage", "PHÁP SƯ", "🔮", "Trí tuệ vô song. Kiểm soát năng lượng và phép thuật.", "INT • MP", GOLD),
        ClassInfo("Ranger", "TỐC XẠ", "🏹", "Linh hoạt như gió. Tốc độ và chính xác tuyệt đỉnh.", "AGI • DEX", GREEN)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(56.dp))
        StepIndicator(current = 3)
        Spacer(Modifier.height(32.dp))
        SystemLabel(text = "THỨC TỈNH CHỨC NGHIỆP")
        Spacer(Modifier.height(8.dp))
        Text(
            "Chọn con đường chiến đấu của bạn",
            color = TEXT_MUTED, fontSize = 13.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))

        classes.forEach { cls ->
            val isSelected = selectedClass == cls.id
            val borderColor = if (isSelected) cls.color else GLASS_BORDER
            val bgAlpha = if (isSelected) 0.15f else 0.05f
            val infiniteTransition = rememberInfiniteTransition(label = cls.id)
            val selectedGlow by infiniteTransition.animateFloat(
                initialValue = 0.6f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                label = "glow"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(cls.color.copy(alpha = bgAlpha))
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = borderColor.copy(alpha = if (isSelected) selectedGlow else 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onClassSelected(cls.id) }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cls.color.copy(alpha = 0.12f))
                            .border(1.dp, cls.color.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(cls.icon, fontSize = 24.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            cls.name, color = if (isSelected) cls.color else Color.White,
                            fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.06f.em
                        )
                        Text(cls.desc, color = TEXT_MUTED, fontSize = 12.sp, lineHeight = 16.sp)
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(cls.color.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                cls.stat, color = cls.color,
                                fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1f.em
                            )
                        }
                    }
                    if (isSelected) {
                        Text("✓", color = cls.color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        SystemButton(label = "TIẾP THEO →", onClick = onNext, color = PRIMARY)
        Spacer(Modifier.height(32.dp))
    }
}

// ── Step 4 — System activation ───────────────────────────────────────────────
@Composable
fun GoalSelectionStep(onFinish: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "activate")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "alpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StepIndicator(current = 4)
        Spacer(Modifier.height(48.dp))

        Text(
            "⚠",
            color = GOLD,
            fontSize = 48.sp,
            modifier = Modifier.alpha(pulseAlpha)
        )
        Spacer(Modifier.height(16.dp))
        SystemLabel(text = "KÍCH HOẠT HỆ THỐNG")
        Spacer(Modifier.height(12.dp))
        Text(
            "Bằng cách xác nhận, bạn đồng ý tham gia hệ thống Solo Leveling. Hành trình của bạn bắt đầu ngay bây giờ.",
            color = TEXT_MUTED, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 20.sp
        )

        Spacer(Modifier.height(40.dp))

        // Warning panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(GOLD.copy(alpha = 0.06f))
                .border(1.dp, GOLD.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                listOf(
                    "Hệ thống sẽ theo dõi tiến trình hàng ngày",
                    "Nhiệm vụ có thể kích hoạt hình phạt nếu bỏ lỡ",
                    "Dữ liệu được lưu trữ cục bộ trên thiết bị"
                ).forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("▸", color = GOLD, fontSize = 12.sp, modifier = Modifier.padding(top = 1.dp, end = 8.dp))
                        Text(item, color = TEXT_MUTED, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .scale(pulseScale)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF1B4F8A), PRIMARY, Color(0xFF1B4F8A)))
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(PRIMARY_DIM, PRIMARY, PRIMARY_DIM)),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(onClick = onFinish)
                .padding(horizontal = 40.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "[ KÍCH HOẠT HỆ THỐNG ]",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1f.em
            )
        }
    }
}

// ── Shared components ────────────────────────────────────────────────────────
@Composable
private fun SystemLabel(text: String) {
    Text(
        text,
        color = PRIMARY_DIM,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.06f.em,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun SystemButton(
    label: String,
    onClick: () -> Unit,
    color: Color = PRIMARY
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08f.em)
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFF0A0A1A)
@Composable
private fun PreviewIntro() = SystemLevelingTheme { IntroCinematicStep {} }

@Preview(showBackground = true, backgroundColor = 0xFF121222)
@Composable
private fun PreviewBasicInfo() = SystemLevelingTheme {
    BasicInfoStep("Sung Jin-Woo", {}) {}
}

@Preview(showBackground = true, backgroundColor = 0xFF121222)
@Composable
private fun PreviewClassSelect() = SystemLevelingTheme {
    ClassSelectionStep("Mage", {}) {}
}
