package com.systemleveling.feature.onboarding.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import com.systemleveling.core.ai.AiSurveyData
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    val height: String = "",
    val weight: String = "",
    val pushUps: Int = -1,
    val lifting: Int = -1,
    val runningPace: Int = -1,
    val studyHours: Float = 2f,
    val languageLevel: Int = -1,
    val sleepHours: Float = 7f,
    val workStyle: Int = -1,
    val wakeTime: String = "06:00",
    val sleepTime: String = "23:00",
    val workTime: String = "08:00 - 17:00",
    val lunchTime: String = "12:00 - 13:00",
    val workoutTime: String = "17:30 - 18:30"
)

// ── Survey Screen ─────────────────────────────────────────────────────────────
@Composable
fun SurveyScreen(
    onBack: () -> Unit,
    onNext: (AiSurveyData) -> Unit
) {
    var phase by remember { mutableStateOf(1) }      // 1: Cơ bản, 2: Thể chất, 3: Trí tuệ & Kỹ năng, 4: Lịch trình
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
                        targetValue = phase / 4f,
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
                    Text("Phase $phase / 4", color = GOLD, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("${(phase * 25f).toInt()}%", color = PRIMARY.copy(0.7f), fontSize = 9.sp)
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

                val phaseTitle = when (phase) {
                    1 -> "THÔNG TIN CƠ BẢN"
                    2 -> "THỂ CHẤT"
                    3 -> "TRÍ TUỆ & KỸ NĂNG"
                    else -> "LỊCH TRÌNH SINH HỌC"
                }
                val phaseIcon = when (phase) {
                    1 -> "🧬"; 2 -> "💪"; 3 -> "🧠"; else -> "🕒"
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
                    "Hệ thống cần dữ liệu chính xác để tính toán chỉ số (STAT) khởi điểm. Hãy nhập trung thực.",
                    color = TEXT_MUTED,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(20.dp))

                when (phase) {
                    1 -> PhaseBasic(data) { data = it }
                    2 -> PhasePhysical(data) { data = it }
                    3 -> PhaseMental(data) { data = it }
                    4 -> PhaseSchedule(data) { data = it }
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
                    1 -> data.height.isNotBlank() && data.weight.isNotBlank()
                    2 -> data.pushUps != -1 && data.lifting != -1 && data.runningPace != -1
                    3 -> data.languageLevel != -1 && data.workStyle != -1
                    4 -> data.wakeTime.isNotBlank() && data.sleepTime.isNotBlank() && data.workTime.isNotBlank()
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
                            if (phase < 4) {
                                phase++
                            } else {
                                val pushUpsStr = listOf("Dưới 10 cái", "10-20 cái", "20-40 cái", "Hơn 40 cái")[data.pushUps.coerceAtLeast(0)]
                                val liftingStr = listOf("Nhẹ (dưới 10kg)", "Vừa (10-30kg)", "Nặng (30-60kg)", "Rất nặng (>60kg)")[data.lifting.coerceAtLeast(0)]
                                val runningStr = listOf("Không chạy", "Chạy chậm/Đi bộ", "Chạy bền trung bình", "Chạy rất nhanh")[data.runningPace.coerceAtLeast(0)]
                                val langStr = listOf("Cơ bản", "Khá (giao tiếp được)", "Tốt (Làm việc được)", "Trôi chảy (như bản xứ)")[data.languageLevel.coerceAtLeast(0)]
                                val workStr = listOf("Nhanh nhạy, linh hoạt", "Cẩn thận, chi tiết", "Sáng tạo, đột phá", "Cần cù, kỷ luật")[data.workStyle.coerceAtLeast(0)]

                                val aiData = AiSurveyData(
                                    height = data.height,
                                    weight = data.weight,
                                    pushUps = pushUpsStr,
                                    lifting = liftingStr,
                                    runningPace = runningStr,
                                    studyHours = "${data.studyHours.toInt()} giờ/ngày",
                                    languageLevel = langStr,
                                    sleepHours = "${data.sleepHours.toInt()} giờ/ngày",
                                    workStyle = workStr,
                                    wakeTime = data.wakeTime,
                                    sleepTime = data.sleepTime,
                                    workTime = data.workTime,
                                    lunchTime = data.lunchTime,
                                    workoutTime = data.workoutTime
                                )
                                onNext(aiData)
                            }
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (phase < 4) "Tiếp theo  ›" else "Khởi tạo STAT  ›",
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

// ── Phase 1: Cơ Bản ─────────────────────────────────────────────────────────
@Composable
private fun PhaseBasic(data: SurveyData, onDataChange: (SurveyData) -> Unit) {
    SurveyCard(
        icon = "📏", label = "VIT", labelColor = Color(0xFF2ED573),
        title = "Chỉ số cơ thể", question = "Nhập chiều cao và cân nặng để tính toán BMI."
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = data.height,
                onValueChange = { onDataChange(data.copy(height = it)) },
                label = { Text("Chiều cao (cm)", fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GLASS,
                    unfocusedContainerColor = GLASS,
                    focusedTextColor = TEXT,
                    unfocusedTextColor = TEXT,
                    focusedIndicatorColor = PRIMARY,
                    unfocusedIndicatorColor = GLASS_BORDER
                )
            )
            OutlinedTextField(
                value = data.weight,
                onValueChange = { onDataChange(data.copy(weight = it)) },
                label = { Text("Cân nặng (kg)", fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GLASS,
                    unfocusedContainerColor = GLASS,
                    focusedTextColor = TEXT,
                    unfocusedTextColor = TEXT,
                    focusedIndicatorColor = PRIMARY,
                    unfocusedIndicatorColor = GLASS_BORDER
                )
            )
        }
    }

    Spacer(Modifier.height(12.dp))

    SurveyCard(
        icon = "💤", label = "RECOVERY", labelColor = Color(0xFFA4C9FF),
        title = "Thời gian nghỉ ngơi", question = "Bạn ngủ bao nhiêu tiếng mỗi ngày?"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("4h", color = TEXT_MUTED, fontSize = 10.sp)
            Text("${data.sleepHours.toInt()} giờ", color = PRIMARY, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("12h", color = TEXT_MUTED, fontSize = 10.sp)
        }
        Slider(
            value = data.sleepHours,
            onValueChange = { onDataChange(data.copy(sleepHours = it)) },
            valueRange = 4f..12f,
            colors = SliderDefaults.colors(thumbColor = PRIMARY, activeTrackColor = PRIMARY, inactiveTrackColor = GLASS_BORDER)
        )
    }
}

// ── Phase 2: Thể Chất ─────────────────────────────────────────────────────────
@Composable
private fun PhasePhysical(data: SurveyData, onDataChange: (SurveyData) -> Unit) {
    McqCard(
        icon = "💪", label = "STR", labelColor = Color(0xFFFF4757),
        title = "Sức mạnh Cơ Bắp", question = "Số cái hít đất (push-ups) tối đa bạn có thể làm trong 1 lần liên tục?",
        options = listOf("Dưới 10 cái", "10-20 cái", "20-40 cái", "Hơn 40 cái"),
        selected = data.pushUps, onSelect = { onDataChange(data.copy(pushUps = it)) }
    )
    Spacer(Modifier.height(12.dp))
    McqCard(
        icon = "🏋️", label = "STR", labelColor = Color(0xFFFF4757),
        title = "Khả năng Nâng Vác", question = "Mức tạ hoặc vật nặng tối đa bạn thường xuyên bê vác?",
        options = listOf("Nhẹ (dưới 10kg)", "Vừa (10-30kg)", "Nặng (30-60kg)", "Rất nặng (>60kg)"),
        selected = data.lifting, onSelect = { onDataChange(data.copy(lifting = it)) }
    )
    Spacer(Modifier.height(12.dp))
    McqCard(
        icon = "🏃", label = "AGI", labelColor = Color(0xFF2ED573),
        title = "Tốc Độ & Sức Bền", question = "Khả năng chạy bộ của bạn như thế nào?",
        options = listOf("Không bao giờ chạy", "Chỉ đi bộ hoặc chạy rất chậm", "Có thể chạy bền ở mức trung bình", "Chạy rất nhanh và lâu"),
        selected = data.runningPace, onSelect = { onDataChange(data.copy(runningPace = it)) }
    )
}

// ── Phase 3: Trí Tuệ ─────────────────────────────────────────────────────────
@Composable
private fun PhaseMental(data: SurveyData, onDataChange: (SurveyData) -> Unit) {
    SurveyCard(
        icon = "📚", label = "INT", labelColor = Color(0xFFFFD700),
        title = "Học thuật & Nghiên cứu", question = "Thời gian bạn dành để học hỏi, đọc sách hoặc làm việc trí óc mỗi ngày?"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("0h", color = TEXT_MUTED, fontSize = 10.sp)
            Text("${data.studyHours.toInt()} giờ", color = PRIMARY, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("16h+", color = TEXT_MUTED, fontSize = 10.sp)
        }
        Slider(
            value = data.studyHours,
            onValueChange = { onDataChange(data.copy(studyHours = it)) },
            valueRange = 0f..16f,
            colors = SliderDefaults.colors(thumbColor = PRIMARY, activeTrackColor = PRIMARY, inactiveTrackColor = GLASS_BORDER)
        )
    }
    Spacer(Modifier.height(12.dp))
    McqCard(
        icon = "🗣️", label = "WIS/CHA", labelColor = Color(0xFFB48EFF),
        title = "Trình độ Ngoại Ngữ", question = "Khả năng sử dụng ngoại ngữ (VD: Tiếng Anh) của bạn?",
        options = listOf("Chỉ biết cơ bản", "Khá (Giao tiếp đơn giản)", "Tốt (Làm việc, nghiên cứu tài liệu)", "Trôi chảy (Như người bản xứ)"),
        selected = data.languageLevel, onSelect = { onDataChange(data.copy(languageLevel = it)) }
    )
    Spacer(Modifier.height(12.dp))
    McqCard(
        icon = "⚡", label = "INT/AGI", labelColor = Color(0xFF4A9EFF),
        title = "Phong cách Xử lý vấn đề", question = "Điểm mạnh nhất của bạn trong công việc/học tập là gì?",
        options = listOf("Nhanh nhạy, linh hoạt ứng biến", "Cẩn thận, chi tiết từng bước", "Sáng tạo, hay có ý tưởng đột phá", "Cần cù, kỷ luật, đúng hạn"),
        selected = data.workStyle, onSelect = { onDataChange(data.copy(workStyle = it)) }
    )
}

// ── Phase 4: Lịch Trình ──────────────────────────────────────────────────────
@Composable
private fun PhaseSchedule(data: SurveyData, onDataChange: (SurveyData) -> Unit) {
    SurveyCard(
        icon = "🕒", label = "TIME", labelColor = Color(0xFF4A9EFF),
        title = "Đồng Bộ Sinh Học", question = "Hệ thống cần lịch trình của bạn để giao nhiệm vụ vào đúng mốc thời gian."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = data.wakeTime,
                    onValueChange = { onDataChange(data.copy(wakeTime = it)) },
                    label = { Text("Giờ thức (VD: 06:00)", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = GLASS, unfocusedContainerColor = GLASS,
                        focusedTextColor = TEXT, unfocusedTextColor = TEXT,
                        focusedIndicatorColor = PRIMARY, unfocusedIndicatorColor = GLASS_BORDER
                    )
                )
                OutlinedTextField(
                    value = data.sleepTime,
                    onValueChange = { onDataChange(data.copy(sleepTime = it)) },
                    label = { Text("Giờ ngủ (VD: 23:00)", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = GLASS, unfocusedContainerColor = GLASS,
                        focusedTextColor = TEXT, unfocusedTextColor = TEXT,
                        focusedIndicatorColor = PRIMARY, unfocusedIndicatorColor = GLASS_BORDER
                    )
                )
            }
            
            OutlinedTextField(
                value = data.workTime,
                onValueChange = { onDataChange(data.copy(workTime = it)) },
                label = { Text("Giờ làm việc (VD: 08:00 - 17:00)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GLASS, unfocusedContainerColor = GLASS,
                    focusedTextColor = TEXT, unfocusedTextColor = TEXT,
                    focusedIndicatorColor = PRIMARY, unfocusedIndicatorColor = GLASS_BORDER
                )
            )

            OutlinedTextField(
                value = data.lunchTime,
                onValueChange = { onDataChange(data.copy(lunchTime = it)) },
                label = { Text("Nghỉ trưa (VD: 12:00 - 13:00)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GLASS, unfocusedContainerColor = GLASS,
                    focusedTextColor = TEXT, unfocusedTextColor = TEXT,
                    focusedIndicatorColor = PRIMARY, unfocusedIndicatorColor = GLASS_BORDER
                )
            )

            OutlinedTextField(
                value = data.workoutTime,
                onValueChange = { onDataChange(data.copy(workoutTime = it)) },
                label = { Text("Tập luyện (VD: 17:30 - 18:30)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GLASS, unfocusedContainerColor = GLASS,
                    focusedTextColor = TEXT, unfocusedTextColor = TEXT,
                    focusedIndicatorColor = PRIMARY, unfocusedIndicatorColor = GLASS_BORDER
                )
            )
        }
    }
}

// ── Shared Card Components ────────────────────────────────────────────────────
@Composable
private fun SurveyCard(
    icon: String, label: String, labelColor: Color, title: String, question: String,
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
                Text(title, color = TEXT, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
    icon: String, label: String, labelColor: Color, title: String, question: String,
    options: List<String>, selected: Int, onSelect: (Int) -> Unit
) {
    SurveyCard(icon = icon, label = label, labelColor = labelColor, title = title, question = question) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEachIndexed { idx, opt ->
                val isSelected = selected == idx
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) labelColor.copy(0.1f) else Color(0xFF1A1A2B))
                        .border(
                            width = if (isSelected) 1.dp else 0.5.dp,
                            color = if (isSelected) labelColor.copy(0.7f) else GLASS_BORDER,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelect(idx) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp).clip(CircleShape)
                            .background(if (isSelected) labelColor else Color.Transparent)
                            .border(1.5.dp, if (isSelected) labelColor else GLASS_BORDER.copy(0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) Box(Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        opt, color = if (isSelected) TEXT else TEXT_MUTED,
                        fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
