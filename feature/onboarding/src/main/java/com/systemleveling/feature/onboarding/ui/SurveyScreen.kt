package com.systemleveling.feature.onboarding.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.core.database.entity.StatEntity

// ── Palette ───────────────────────────────────────────────────────────────────
private val BG       = Color(0xFF0C0C1D)
private val PRIMARY  = Color(0xFF4A9EFF)
private val PRIMARY_DIM = Color(0xFFA4C9FF)
private val GOLD     = Color(0xFFFFE16D)
private val GLASS    = Color(0xFF1E1E2F)
private val GLASS_BORDER = Color(0x1FFFFFFF)
private val TEXT     = Color(0xFFE3E0F8)
private val TEXT_MUTED = Color(0xFFC0C7D4)

// ── Survey Data ───────────────────────────────────────────────────────────────
private data class SurveyData(
    val workoutHours: Float = 4f,           // 0-20
    val workoutStyle: Int = -1,             // 0-3
    val problemSolving: Int = -1,           // 0-3
    val learningStyle: Int = -1,            // 0-3
    val workPace: Int = -1,                 // 0-3
    val groupRole: Int = -1                 // 0-3
)

private fun computeStats(d: SurveyData): StatEntity {
    var str = 10; var int_ = 10; var agi = 10; var vit = 10; var wis = 10; var cha = 10

    // Q1: workout hours → STR + VIT
    val hrs = d.workoutHours
    str += when {
        hrs >= 13 -> 20; hrs >= 8 -> 15; hrs >= 4 -> 10; else -> 3
    }
    vit += when {
        hrs >= 13 -> 15; hrs >= 8 -> 12; hrs >= 4 -> 8; else -> 2
    }

    // Q2: workout style
    when (d.workoutStyle) {
        0 -> { str += 10; vit += 5 }             // Tập tạ
        1 -> { vit += 10; agi += 8 }             // Cardio/chạy
        2 -> { vit += 8; wis += 7 }              // Yoga/thiền
        3 -> { /* Không tập */ }
    }

    // Q3: problem solving → INT + AGI
    when (d.problemSolving) {
        0 -> { int_ += 20 }                      // Phân tích toàn bộ
        1 -> { agi += 15; int_ += 8 }            // Trực giác
        2 -> { wis += 15; cha += 8 }             // Tìm hướng dẫn
        3 -> { vit += 8; int_ += 10 }            // Thử sai
    }

    // Q4: learning style → INT + WIS
    when (d.learningStyle) {
        0 -> { int_ += 15; wis += 10 }           // Đọc sách
        1 -> { int_ += 10; wis += 8 }            // Video/ví dụ
        2 -> { wis += 15; cha += 8 }             // Học từ người khác
        3 -> { agi += 10; vit += 5 }             // Thực hành
    }

    // Q5: work pace → AGI + WIS
    when (d.workPace) {
        0 -> { agi += 20; str += 5 }             // Nhanh, quyết đoán
        1 -> { wis += 15; int_ += 8 }            // Cẩn thận, kế hoạch
        2 -> { agi += 10; cha += 8 }             // Linh hoạt
        3 -> { vit += 15; wis += 8 }             // Chậm, chắc chắn
    }

    // Q6: group role → CHA
    when (d.groupRole) {
        0 -> { cha += 20; str += 5 }             // Dẫn dắt
        1 -> { int_ += 10; cha += 10 }           // Ý tưởng sáng tạo
        2 -> { wis += 10; cha += 15 }            // Hỗ trợ, giúp đỡ
        3 -> { int_ += 15; agi += 8 }            // Độc lập
    }

    return StatEntity(
        str = str.coerceIn(10, 80),
        intStat = int_.coerceIn(10, 80),
        agi = agi.coerceIn(10, 80),
        vit = vit.coerceIn(10, 80),
        wis = wis.coerceIn(10, 80),
        cha = cha.coerceIn(10, 80)
    )
}

// ── Survey Screen ─────────────────────────────────────────────────────────────
@Composable
fun SurveyScreen(
    onBack: () -> Unit,
    onNext: (StatEntity) -> Unit
) {
    var phase by remember { mutableStateOf(1) }      // 1, 2, 3
    var data by remember { mutableStateOf(SurveyData()) }

    val scanPulse by rememberInfiniteTransition(label = "scan").animateFloat(
        0.7f, 1f,
        infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "p"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0F0F25), BG))
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Fixed Header ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xBB0A0A1A))
                    .border(BorderStroke(0.5.dp, GLASS_BORDER))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "SYSTEM LEVELING",
                    color = PRIMARY_DIM.copy(scanPulse),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.18f.em
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Scanning Host Compatibility...",
                    color = PRIMARY.copy(0.7f),
                    fontSize = 9.sp,
                    letterSpacing = 0.14f.em
                )
                Spacer(Modifier.height(6.dp))
                // Scanning progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(GLASS_BORDER)
                ) {
                    val animProg by animateFloatAsState(
                        targetValue = phase / 3f,
                        animationSpec = tween(600),
                        label = "scan_prog"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animProg)
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF4A9EFF), Color(0xFF00FF87)))
                            )
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Phase $phase / 3", color = GOLD, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("${(phase * 33.3f).toInt()}%", color = PRIMARY.copy(0.7f), fontSize = 9.sp)
                }
            }

            // ── Scrollable Content ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))

                // Phase title
                val phaseTitle = when (phase) {
                    1 -> "THỂ CHẤT"
                    2 -> "TRÍ TUỆ"
                    else -> "TÍNH CÁCH"
                }
                val phaseIcon = when (phase) {
                    1 -> "💪"; 2 -> "🧠"; else -> "⚡"
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(phaseIcon, fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Phase $phase: $phaseTitle",
                        color = GOLD,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.06f.em
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Hệ thống cần dữ liệu chính xác để hiệu chỉnh thông số ban đầu. Hãy trả lời trung thực.",
                    color = TEXT_MUTED,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(20.dp))

                when (phase) {
                    1 -> PhasePhysical(
                        data = data,
                        onDataChange = { data = it }
                    )
                    2 -> PhaseMental(
                        data = data,
                        onDataChange = { data = it }
                    )
                    3 -> PhaseCharacter(
                        data = data,
                        onDataChange = { data = it }
                    )
                }

                Spacer(Modifier.height(32.dp))
            }

            // ── Action Bar ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xCC0A0A1A))
                    .border(BorderStroke(0.5.dp, GLASS_BORDER))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "◀  Quay lại",
                    color = TEXT_MUTED,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        if (phase > 1) phase-- else onBack()
                    }
                )

                val canProceed = when (phase) {
                    1 -> data.workoutStyle != -1
                    2 -> data.problemSolving != -1 && data.learningStyle != -1
                    3 -> data.workPace != -1 && data.groupRole != -1
                    else -> true
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (canProceed)
                                Brush.horizontalGradient(listOf(Color(0xFF004884), PRIMARY))
                            else
                                Brush.horizontalGradient(listOf(GLASS, GLASS))
                        )
                        .clickable(enabled = canProceed) {
                            if (phase < 3) {
                                phase++
                            } else {
                                onNext(computeStats(data))
                            }
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (phase < 3) "Tiếp theo  ›" else "Xác nhận dữ liệu  ›",
                        color = if (canProceed) Color.White else TEXT_MUTED.copy(0.4f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.06f.em
                    )
                }
            }
        }
    }
}

// ── Phase 1: Thể Chất ─────────────────────────────────────────────────────────
@Composable
private fun PhasePhysical(data: SurveyData, onDataChange: (SurveyData) -> Unit) {
    // Q1: Workout hours slider
    SurveyCard(
        icon = "🏋️",
        label = "STR • VIT",
        labelColor = Color(0xFF4A9EFF),
        title = "Physical Exertion Quotient",
        question = "Bạn tập luyện thể thao bao nhiêu giờ mỗi tuần?"
    ) {
        val hrLabel = when {
            data.workoutHours < 2 -> "Ít / Không tập"
            data.workoutHours < 6 -> "${data.workoutHours.toInt()} giờ / tuần"
            data.workoutHours < 12 -> "${data.workoutHours.toInt()} giờ / tuần"
            else -> "${data.workoutHours.toInt()} giờ / tuần — Vận động viên"
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("0h", color = TEXT_MUTED, fontSize = 10.sp)
            Text(
                hrLabel,
                color = PRIMARY,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text("20h+", color = TEXT_MUTED, fontSize = 10.sp)
        }
        Slider(
            value = data.workoutHours,
            onValueChange = { onDataChange(data.copy(workoutHours = it)) },
            valueRange = 0f..20f,
            colors = SliderDefaults.colors(
                thumbColor = PRIMARY,
                activeTrackColor = PRIMARY,
                inactiveTrackColor = GLASS_BORDER
            )
        )
    }

    Spacer(Modifier.height(12.dp))

    // Q2: Workout style MCQ
    McqCard(
        icon = "🏃",
        label = "STR • VIT • AGI",
        labelColor = Color(0xFF2ED573),
        title = "Training Protocol",
        question = "Loại hình vận động bạn thường xuyên nhất:",
        options = listOf(
            "Tập tạ / Gym — Xây dựng sức mạnh",
            "Chạy bộ / Cardio — Tăng sức bền",
            "Yoga / Thiền — Cân bằng thân tâm",
            "Không tập luyện thường xuyên"
        ),
        selected = data.workoutStyle,
        onSelect = { onDataChange(data.copy(workoutStyle = it)) }
    )
}

// ── Phase 2: Trí Tuệ ─────────────────────────────────────────────────────────
@Composable
private fun PhaseMental(data: SurveyData, onDataChange: (SurveyData) -> Unit) {
    McqCard(
        icon = "🧩",
        label = "INT",
        labelColor = Color(0xFFFFD700),
        title = "Cognitive Load Preference",
        question = "Khi đối mặt với vấn đề phức tạp, bạn thường:",
        options = listOf(
            "Phân tích toàn bộ biến số trước khi hành động",
            "Dựa vào trực giác và thích nghi nhanh",
            "Tìm kiếm hướng dẫn từ nguồn bên ngoài",
            "Thử sai cho đến khi tìm được giải pháp"
        ),
        selected = data.problemSolving,
        onSelect = { onDataChange(data.copy(problemSolving = it)) }
    )

    Spacer(Modifier.height(12.dp))

    McqCard(
        icon = "📚",
        label = "WIS",
        labelColor = Color(0xFFB48EFF),
        title = "Knowledge Acquisition Method",
        question = "Cách bạn tiếp thu kiến thức và kỹ năng mới:",
        options = listOf(
            "Đọc sách, tài liệu chuyên sâu",
            "Xem video, học qua ví dụ thực tế",
            "Học từ người khác — mentor, đồng nghiệp",
            "Thực hành trực tiếp, học qua thực chiến"
        ),
        selected = data.learningStyle,
        onSelect = { onDataChange(data.copy(learningStyle = it)) }
    )
}

// ── Phase 3: Tính Cách ───────────────────────────────────────────────────────
@Composable
private fun PhaseCharacter(data: SurveyData, onDataChange: (SurveyData) -> Unit) {
    McqCard(
        icon = "⚡",
        label = "AGI",
        labelColor = Color(0xFF2ED573),
        title = "Operational Tempo",
        question = "Nhịp độ và phong cách làm việc của bạn:",
        options = listOf(
            "Nhanh, quyết đoán — không ngại rủi ro",
            "Cẩn thận, có kế hoạch rõ ràng trước khi bắt đầu",
            "Linh hoạt, thích nghi theo tình huống",
            "Chậm rãi, chắc chắn — ưu tiên độ chính xác"
        ),
        selected = data.workPace,
        onSelect = { onDataChange(data.copy(workPace = it)) }
    )

    Spacer(Modifier.height(12.dp))

    McqCard(
        icon = "👥",
        label = "CHA",
        labelColor = Color(0xFFFF9F43),
        title = "Social Dynamics Role",
        question = "Trong một nhóm làm việc, bạn thường:",
        options = listOf(
            "Dẫn dắt và định hướng cho cả nhóm",
            "Đóng góp ý tưởng sáng tạo, giải pháp mới",
            "Hỗ trợ và giúp đỡ thành viên khác",
            "Làm việc độc lập với kết quả riêng"
        ),
        selected = data.groupRole,
        onSelect = { onDataChange(data.copy(groupRole = it)) }
    )
}

// ── Shared Card Components ────────────────────────────────────────────────────
@Composable
private fun SurveyCard(
    icon: String,
    label: String,
    labelColor: Color,
    title: String,
    question: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GLASS.copy(0.65f))
            .border(0.5.dp, GLASS_BORDER, RoundedCornerShape(14.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    color = TEXT,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(labelColor.copy(0.15f))
                        .border(0.5.dp, labelColor.copy(0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(label, color = labelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1f.em)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(question, color = TEXT_MUTED, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun McqCard(
    icon: String,
    label: String,
    labelColor: Color,
    title: String,
    question: String,
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    SurveyCard(
        icon = icon,
        label = label,
        labelColor = labelColor,
        title = title,
        question = question
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEachIndexed { idx, opt ->
                val isSelected = selected == idx
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) labelColor.copy(0.1f)
                            else Color(0xFF1A1A2B)
                        )
                        .border(
                            width = if (isSelected) 1.dp else 0.5.dp,
                            color = if (isSelected) labelColor.copy(0.7f) else GLASS_BORDER,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelect(idx) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Radio dot
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) labelColor else Color.Transparent)
                            .border(1.5.dp, if (isSelected) labelColor else GLASS_BORDER.copy(0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        opt,
                        color = if (isSelected) TEXT else TEXT_MUTED,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
