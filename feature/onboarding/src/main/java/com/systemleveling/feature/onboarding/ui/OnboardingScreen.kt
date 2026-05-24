package com.systemleveling.feature.onboarding.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import com.systemleveling.core.ai.AiSurveyData
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.core.designsystem.components.GlassCard
import kotlinx.coroutines.delay

// ── Palette ─────────────────────────────────────────────────────────────────
private val BG = Color(0xFF121222)
private val BG_DEEP = Color(0xFF0A0A1A)
private val PRIMARY = Color(0xFF4A9EFF)
private val PRIMARY_DIM = Color(0xFFA4C9FF)
private val GOLD = Color(0xFFFFD700)
private val GLASS_BORDER = Color(0x1FFFFFFF)
private val TEXT_MUTED = Color(0xFFC0C7D4)

data class GoalItem(val text: String = "", val timeline: String = "")

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var currentStep by remember { mutableStateOf(1) } // Start directly at step 1
    var nickname by remember { mutableStateOf("") }
    var goals by remember { mutableStateOf(List(3) { GoalItem() }) }
    var apiKey by remember { mutableStateOf("") }
    var supabaseUrl by remember { mutableStateOf("") }
    var supabaseAnonKey by remember { mutableStateOf("") }
    var surveyData by remember { mutableStateOf<AiSurveyData?>(null) }
    
    val combinedGoal = remember(goals) {
        goals.mapIndexed { i, g -> "Mục tiêu ${i + 1}: ${g.text} (Thời hạn: ${g.timeline})" }.joinToString("\n")
    }

    LaunchedEffect(uiState) {
        if (uiState is OnboardingUiState.Success) {
            onFinish()
        }
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

        when (val state = uiState) {
            is OnboardingUiState.Loading -> {
                AiLoadingStep(state.message)
            }
            is OnboardingUiState.Result -> {
                ResultStep(
                    data = state.data,
                    onAccept = { selectedClassName ->
                        viewModel.acceptAndComplete(nickname, combinedGoal, selectedClassName, state.data, surveyData!!)
                    },
                    onReroll = {
                        surveyData?.let {
                            viewModel.generateRoadmapAndComplete(nickname, combinedGoal, it, apiKey)
                        }
                    },
                    onEditGoals = {
                        viewModel.resetToIdle()
                        currentStep = 2
                    }
                )
            }
            else -> {
                when (currentStep) {
                    0 -> IntroCinematicStep { currentStep = 1 } 
                    1 -> SurveyScreen(
                        onBack = { /* Optional */ },
                        onNext = { data ->
                            surveyData = data
                            currentStep = 2
                        }
                    )
                    2 -> SetupInfoStep(
                        nickname = nickname,
                        goals = goals,
                        apiKey = apiKey,
                        supabaseUrl = supabaseUrl,
                        supabaseAnonKey = supabaseAnonKey,
                        onNicknameChange = { nickname = it },
                        onGoalChange = { index, newGoal ->
                            goals = goals.toMutableList().also { it[index] = newGoal }
                        },
                        onApiKeyChange = { apiKey = it },
                        onSupabaseUrlChange = { supabaseUrl = it },
                        onSupabaseAnonKeyChange = { supabaseAnonKey = it },
                        onNext = {
                            surveyData?.let {
                                viewModel.generateRoadmapAndComplete(
                                    nickname, combinedGoal, it, apiKey,
                                    supabaseUrl, supabaseAnonKey
                                )
                            }
                        },
                        error = if (state is OnboardingUiState.Error) state.message else null
                    )
                }
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

// ── Step 0 — Cinematic intro (Faster) ────────────────────────────────────────
@Composable
fun IntroCinematicStep(onNext: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(1500) // Fast intro
        onNext()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SystemLabel(text = "[AWAKENING PROTOCOL ENGAGED]")
    }
}

// ── Step 1 — Setup Info ─────────────────────────────────────────
@Composable
fun SetupInfoStep(
    nickname: String,
    goals: List<GoalItem>,
    apiKey: String = "",
    supabaseUrl: String = "",
    supabaseAnonKey: String = "",
    onNicknameChange: (String) -> Unit,
    onGoalChange: (Int, GoalItem) -> Unit,
    onApiKeyChange: (String) -> Unit = {},
    onSupabaseUrlChange: (String) -> Unit = {},
    onSupabaseAnonKeyChange: (String) -> Unit = {},
    onNext: () -> Unit,
    error: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SystemLabel(text = "ĐỊNH HƯỚNG PHÁT TRIỂN")
        Spacer(Modifier.height(8.dp))
        Text(
            "Hệ thống AI sẽ đánh giá và thiết lập lộ trình cho bạn",
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
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    "3 MỤC TIÊU PHÁT TRIỂN & THỜI HẠN",
                    color = GOLD, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 0.12f.em
                )
                Spacer(Modifier.height(8.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    goals.forEachIndexed { index, goal ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = goal.text,
                                onValueChange = { onGoalChange(index, goal.copy(text = it)) },
                                placeholder = { Text("Mục tiêu ${index + 1}...", color = TEXT_MUTED, fontSize = 12.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = GOLD,
                                    unfocusedBorderColor = GLASS_BORDER,
                                    cursorColor = GOLD,
                                    focusedContainerColor = Color(0x0AFFFFFF),
                                    unfocusedContainerColor = Color(0x05FFFFFF)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(2f)
                            )
                            OutlinedTextField(
                                value = goal.timeline,
                                onValueChange = { onGoalChange(index, goal.copy(timeline = it)) },
                                placeholder = { Text("Thời hạn...", color = TEXT_MUTED, fontSize = 12.sp) },
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
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))

        // API Key field — required for AI analysis
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔑", fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "GEMINI API KEY",
                        color = Color(0xFF00FF87), fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.12f.em
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "bắt buộc",
                        color = Color(0xFFFF5252).copy(0.7f), fontSize = 9.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Nhập key tại aistudio.google.com → Get API Key",
                    color = TEXT_MUTED, fontSize = 10.sp
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = onApiKeyChange,
                    placeholder = { Text("AIzaSy...", color = TEXT_MUTED, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00FF87),
                        unfocusedBorderColor = if (apiKey.isBlank()) Color(0x55FF5252) else GLASS_BORDER,
                        cursorColor = Color(0xFF00FF87),
                        focusedContainerColor = Color(0x0A00FF87),
                        unfocusedContainerColor = Color(0x05FFFFFF)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Cloud Backup fields (optional) ───────────────────────────────────
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("☁️", fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "SUPABASE CLOUD BACKUP",
                        color = Color(0xFF4A9EFF), fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.12f.em
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "tùy chọn",
                        color = TEXT_MUTED.copy(0.6f), fontSize = 9.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Lưu dữ liệu lên cloud — tạo project miễn phí tại supabase.com",
                    color = TEXT_MUTED, fontSize = 10.sp
                )
                Spacer(Modifier.height(10.dp))

                Text("Project URL:", color = Color.White.copy(0.8f), fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = supabaseUrl,
                    onValueChange = onSupabaseUrlChange,
                    placeholder = { Text("https://xxxx.supabase.co", color = TEXT_MUTED, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4A9EFF),
                        unfocusedBorderColor = GLASS_BORDER,
                        cursorColor = Color(0xFF4A9EFF),
                        focusedContainerColor = Color(0x0A4A9EFF),
                        unfocusedContainerColor = Color(0x05FFFFFF)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                Text("Anon Key:", color = Color.White.copy(0.8f), fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = supabaseAnonKey,
                    onValueChange = onSupabaseAnonKeyChange,
                    placeholder = { Text("eyJ...", color = TEXT_MUTED, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4A9EFF),
                        unfocusedBorderColor = GLASS_BORDER,
                        cursorColor = Color(0xFF4A9EFF),
                        focusedContainerColor = Color(0x0A4A9EFF),
                        unfocusedContainerColor = Color(0x05FFFFFF)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(error, color = Color(0xFFFF5252), fontSize = 12.sp, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(28.dp))
        val canProceed = goals.all { it.text.isNotBlank() && it.timeline.isNotBlank() } && apiKey.isNotBlank()
        SystemButton(
            label = "[ PHÂN TÍCH VÀ KHỞI TẠO ]", 
            onClick = {
                if (canProceed) onNext()
            },
            color = if (canProceed) PRIMARY else TEXT_MUTED
        )
    }
}

// ── Loading Step (AI Processing) ───────────────────────────────────────────
@Composable
fun AiLoadingStep(message: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "alpha"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "ring"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = PRIMARY.copy(alpha = 0.8f),
                    startAngle = ringRotation,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            Text(
                text = "⚡",
                fontSize = 32.sp,
                modifier = Modifier.alpha(pulseAlpha)
            )
        }
        Spacer(Modifier.height(32.dp))
        SystemLabel(text = "HỆ THỐNG ĐANG PHÂN TÍCH...")
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            color = TEXT_MUTED, fontSize = 14.sp, textAlign = TextAlign.Center,
            modifier = Modifier.alpha(pulseAlpha)
        )
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
    color: Color = PRIMARY,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.06f.em, textAlign = TextAlign.Center)
    }
}

// ── Animated stat bar ─────────────────────────────────────────────────────────
@Composable
fun AnimatedStatBar(label: String, value: Int, color: Color) {
    val targetFraction = (value.coerceIn(0, 100) / 100f)
    val animFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "stat_$label"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(72.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animFraction)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(listOf(color.copy(0.6f), color))
                    )
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$value",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.End
        )
    }
}

// ── Step 4 — Result / Review ──────────────────────────────────────────────
@Composable
fun ResultStep(
    data: com.systemleveling.core.ai.AiCompleteOnboardingResponse,
    onAccept: (String) -> Unit,
    onReroll: () -> Unit,
    onEditGoals: () -> Unit = {}
) {
    var selectedClassIdx by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        SystemLabel(text = "KẾT QUẢ KHỞI TẠO")
        Spacer(Modifier.height(24.dp))

        // STATS — animated bars
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("CHỈ SỐ KHỞI ĐIỂM", color = GOLD, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.12f.em)
                Spacer(Modifier.height(12.dp))
                AnimatedStatBar("⚔ STR", data.stats.str,     Color(0xFFFF5252))
                Spacer(Modifier.height(8.dp))
                AnimatedStatBar("📚 INT", data.stats.intStat, Color(0xFFFFD700))
                Spacer(Modifier.height(8.dp))
                AnimatedStatBar("⚡ AGI", data.stats.agi,     Color(0xFF2ED573))
                Spacer(Modifier.height(8.dp))
                AnimatedStatBar("💚 VIT", data.stats.vit,     Color(0xFF4A9EFF))
                Spacer(Modifier.height(8.dp))
                AnimatedStatBar("🔮 WIS", data.stats.wis,     Color(0xFFB48EFF))
                Spacer(Modifier.height(8.dp))
                AnimatedStatBar("✨ CHA", data.stats.cha,     Color(0xFFFF9F43))
            }
        }

        Spacer(Modifier.height(16.dp))

        // JOB CLASS SELECTION
        Text("CHỌN NGHỀ NGHIỆP KHỞI ĐẦU", color = PRIMARY, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.suggestedClasses.forEachIndexed { index, jobClass ->
                val isSelected = selectedClassIdx == index
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) PRIMARY.copy(0.15f) else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) PRIMARY else GLASS_BORDER,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedClassIdx = index }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(jobClass.iconEmoji, fontSize = 32.sp)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(jobClass.className, color = if (isSelected) GOLD else Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(jobClass.description, color = TEXT_MUTED, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ROADMAP PREVIEW
        val activeClassIdx = selectedClassIdx ?: 0
        val activeClass = data.suggestedClasses.getOrNull(activeClassIdx)
        val activeRoadmap = activeClass?.roadmap ?: emptyList()
        
        if (selectedClassIdx != null) {
            Text("KỸ NĂNG CỦA ${activeClass?.className?.uppercase()}", color = GOLD, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        } else {
            Text("KỸ NĂNG XEM TRƯỚC (HÃY CHỌN 1 NGHỀ)", color = TEXT_MUTED, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            activeRoadmap.forEach { skill ->
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0x1AFFFFFF), RoundedCornerShape(8.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(skill.iconEmoji, fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(skill.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(skill.description, color = TEXT_MUTED, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Row 1 — secondary actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SystemButton(
                    label = "[ ← SỬA MỤC TIÊU ]",
                    onClick = onEditGoals,
                    color = GOLD.copy(0.7f)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                SystemButton(label = "[ ↺ TÁI TẠO ]", onClick = onReroll, color = TEXT_MUTED)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Row 2 — primary confirm (full width)
        SystemButton(
            label = if (selectedClassIdx != null) "[ ✅ CHẤP NHẬN NGHỀ ĐÃ CHỌN ]" else "[ Chọn một nghề để tiếp tục ]",
            onClick = {
                if (selectedClassIdx != null) {
                    onAccept(data.suggestedClasses[selectedClassIdx!!].className)
                }
            },
            color = if (selectedClassIdx != null) PRIMARY else TEXT_MUTED.copy(0.3f)
        )
    }
}
