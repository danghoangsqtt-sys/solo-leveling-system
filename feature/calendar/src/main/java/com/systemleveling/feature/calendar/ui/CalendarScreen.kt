package com.systemleveling.feature.calendar.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.systemleveling.core.database.entity.CalendarEventEntity
import com.systemleveling.core.model.QuestRank
import com.systemleveling.core.model.QuestStatus
import com.systemleveling.core.model.RecurrenceType
import java.text.SimpleDateFormat
import java.util.*

// ── Colors ─────────────────────────────────────────────────────────────────────
private val BG       = Color(0xFF090912)
private val BG_CARD  = Color(0xFF0F0F20)
private val PRIMARY  = Color(0xFF4A9EFF)
private val GOLD     = Color(0xFFFFD700)
private val SURFACE  = Color(0xFF1A1A2E)

// ── Rank color map (same as QuestListScreen) ───────────────────────────────────
private fun rankColor(rank: QuestRank) = when (rank) {
    QuestRank.E -> Color(0xFFAAAAAA)
    QuestRank.D -> Color(0xFF2ED573)
    QuestRank.C -> Color(0xFF4A9EFF)
    QuestRank.B -> Color(0xFFE040FB)
    QuestRank.A -> Color(0xFFFFAB40)
    QuestRank.S -> Color(0xFFFF5252)
}

// ── Root screen ────────────────────────────────────────────────────────────────
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onBack: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val items by viewModel.itemsForDate.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // 30-day window centered on today
    val days = remember {
        val list = mutableListOf<Long>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -14)
        repeat(45) {
            list.add(cal.timeInMillis)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }
    val todayIndex = 14 // offset start
    val rowState = rememberLazyListState(initialFirstVisibleItemIndex = todayIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF0D0D22), Color(0xFF050508)), radius = 1600f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xAA090912))
                    .border(BorderStroke(0.5.dp, Color(0x1FFFFFFF)))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onBack() }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("◀", color = PRIMARY, fontSize = 10.sp)
                        Spacer(Modifier.width(4.dp))
                        Text("HOME", color = PRIMARY, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08f.em)
                    }
                    Text(
                        "📅 LỊCH TRÌNH",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.1f.em
                    )
                    // Add event button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0D1A2E))
                            .border(0.5.dp, PRIMARY.copy(0.5f), RoundedCornerShape(8.dp))
                            .clickable { showAddDialog = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("+ Thêm", color = PRIMARY, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Week strip ────────────────────────────────────────────────────
            LazyRow(
                state = rowState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(days) { ts ->
                    val isToday = isSameDay(ts, System.currentTimeMillis())
                    val isSelected = isSameDay(ts, selectedDate)
                    DayChip(
                        timestamp = ts,
                        isSelected = isSelected,
                        isToday = isToday,
                        onClick = { viewModel.selectDate(ts) }
                    )
                }
            }

            // ── Date header ───────────────────────────────────────────────────
            val dateStr = remember(selectedDate) {
                SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi", "VN"))
                    .format(Date(selectedDate)).uppercase()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(32.dp).height(0.5.dp).background(Color(0x33FFFFFF)))
                    Spacer(Modifier.width(8.dp))
                    Text(dateStr, color = Color(0xFF8899CC), fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 0.1f.em)
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.width(32.dp).height(0.5.dp).background(Color(0x33FFFFFF)))
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Item count ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "HOẠT ĐỘNG",
                    color = PRIMARY.copy(0.7f), fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 0.1f.em
                )
                Text(
                    "${items.size} mục",
                    color = GOLD, fontSize = 10.sp, fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Timeline ──────────────────────────────────────────────────────
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 36.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Không có hoạt động nào", color = Color(0xFF3A3A5A), fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Nhấn + Thêm để tạo sự kiện mới",
                            color = PRIMARY.copy(0.5f), fontSize = 11.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        when (item) {
                            is CalendarItem.QuestItem ->
                                CalendarQuestRow(item.quest)
                            is CalendarItem.EventItem ->
                                CalendarEventRow(
                                    event = item.event,
                                    onDelete = { viewModel.deleteEvent(item.event.id) }
                                )
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }

        // ── Add event dialog ──────────────────────────────────────────────────
        if (showAddDialog) {
            AddEventDialog(
                defaultDateMs = selectedDate,
                onSave = { event ->
                    viewModel.addEvent(event)
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false }
            )
        }
    }
}

// ── Day chip ───────────────────────────────────────────────────────────────────
@Composable
private fun DayChip(timestamp: Long, isSelected: Boolean, isToday: Boolean, onClick: () -> Unit) {
    val dow = remember(timestamp) { SimpleDateFormat("EEE", Locale("vi", "VN")).format(Date(timestamp)) }
    val dom = remember(timestamp) { SimpleDateFormat("dd",  Locale.getDefault()).format(Date(timestamp)) }
    val bgColor = when {
        isSelected -> PRIMARY
        isToday    -> Color(0xFF152030)
        else       -> Color(0xFF0F0F1E)
    }
    val textColor = if (isSelected) Color.Black else Color.White
    val borderColor = when {
        isSelected -> PRIMARY
        isToday    -> PRIMARY.copy(0.5f)
        else       -> Color(0xFF1E1E30)
    }

    Column(
        modifier = Modifier
            .width(52.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(dow, fontSize = 9.sp, color = if (isSelected) Color.DarkGray else Color(0xFF7788AA),
            fontWeight = FontWeight.SemiBold, letterSpacing = 0.06f.em)
        Spacer(Modifier.height(3.dp))
        Text(dom, fontSize = 16.sp, color = textColor, fontWeight = FontWeight.Black)
        if (isToday && !isSelected) {
            Spacer(Modifier.height(3.dp))
            Box(Modifier.size(4.dp).clip(CircleShape).background(PRIMARY))
        }
    }
}

// ── Quest row ──────────────────────────────────────────────────────────────────
@Composable
private fun CalendarQuestRow(quest: com.systemleveling.core.database.entity.QuestEntity) {
    val isCompleted = quest.status == QuestStatus.COMPLETED
    val isFailed = quest.status == QuestStatus.FAILED
    val rankColor = rankColor(quest.rank)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Time + spine
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(50.dp).padding(top = 2.dp)
        ) {
            Text(
                quest.timeStart ?: "--",
                fontSize = 9.sp, fontWeight = FontWeight.Bold,
                color = if (isCompleted) Color(0xFF2A2A3A) else Color(0xFF7788AA),
                letterSpacing = 0.03f.em
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(2.dp).height(44.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        if (isCompleted) SolidColor(Color(0xFF2A2A40))
                        else Brush.verticalGradient(listOf(rankColor.copy(0.9f), rankColor.copy(0.1f)))
                    )
            )
        }
        Spacer(Modifier.width(10.dp))
        // Card
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isCompleted) Color(0xFF0A0A14) else BG_CARD)
                .border(
                    1.dp,
                    if (isCompleted) Color(0xFF1E1E30) else rankColor.copy(0.4f),
                    RoundedCornerShape(10.dp)
                )
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(rankColor.copy(0.1f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(quest.rank.name, color = rankColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        quest.title,
                        color = if (isCompleted) Color(0xFF2A2A40) else Color.White,
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    quest.timeEnd?.let { end ->
                        Text(
                            "${quest.timeStart ?: "?"} – $end",
                            color = if (isCompleted) Color(0xFF1A1A2A) else PRIMARY.copy(0.7f),
                            fontSize = 9.sp
                        )
                    }
                }
                when {
                    isCompleted -> Text("✅", fontSize = 13.sp)
                    isFailed    -> Text("💀", fontSize = 13.sp)
                    else        -> Box(
                        modifier = Modifier
                            .size(18.dp).clip(CircleShape)
                            .border(1.5.dp, rankColor.copy(0.6f), CircleShape)
                    )
                }
            }
        }
    }
}

// ── User event row ─────────────────────────────────────────────────────────────
@Composable
private fun CalendarEventRow(event: CalendarEventEntity, onDelete: () -> Unit) {
    val color = runCatching { Color(android.graphics.Color.parseColor(event.colorHex)) }
        .getOrDefault(PRIMARY)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val recurrenceLabel = when (event.recurrenceType) {
        RecurrenceType.NONE    -> null
        RecurrenceType.DAILY   -> "Hàng ngày"
        RecurrenceType.WEEKLY  -> "Hàng tuần"
        RecurrenceType.MONTHLY -> "Hàng tháng"
        RecurrenceType.YEARLY  -> "Hàng năm"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Time + spine
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(50.dp).padding(top = 2.dp)
        ) {
            Text(
                event.timeStart ?: "--",
                fontSize = 9.sp, fontWeight = FontWeight.Bold,
                color = color.copy(0.7f), letterSpacing = 0.03f.em
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(2.dp).height(44.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Brush.verticalGradient(listOf(color.copy(0.8f), color.copy(0.1f))))
            )
        }
        Spacer(Modifier.width(10.dp))
        // Card
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(BG_CARD)
                .border(1.dp, color.copy(0.5f), RoundedCornerShape(10.dp))
                .clickable(enabled = false) {}
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(0.12f))
                        .border(0.5.dp, color.copy(0.35f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(event.emoji, fontSize = 15.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        event.title, color = Color.White,
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (event.timeStart != null) {
                            Text(
                                "${event.timeStart}${event.timeEnd?.let { " – $it" } ?: ""}",
                                color = color.copy(0.7f), fontSize = 9.sp
                            )
                        }
                        if (recurrenceLabel != null) {
                            if (event.timeStart != null) Text(" · ", color = Color(0xFF3A3A5A), fontSize = 9.sp)
                            Text("🔁 $recurrenceLabel", color = color.copy(0.6f), fontSize = 9.sp)
                        }
                    }
                }
                // Delete button
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E0A0A))
                        .border(0.5.dp, Color(0xFFFF5252).copy(0.3f), RoundedCornerShape(6.dp))
                        .clickable { showDeleteConfirm = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🗑", fontSize = 11.sp)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF0F0F20),
            title = { Text("Xóa sự kiện?", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = { Text(event.title, color = Color(0xFF8899CC), fontSize = 12.sp) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Xóa", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Huỷ", color = PRIMARY)
                }
            }
        )
    }
}

// ── Add Event Dialog ───────────────────────────────────────────────────────────
private val EMOJI_OPTIONS = listOf("📌","📅","⏰","🏋️","📚","💼","🎯","🍽️","💊","🎮","🏃","💡","🔔","✈️","🎂","💰")
private val COLOR_OPTIONS = listOf(
    "#4A9EFF" to Color(0xFF4A9EFF),
    "#2ED573" to Color(0xFF2ED573),
    "#FFD700" to Color(0xFFFFD700),
    "#E040FB" to Color(0xFFE040FB),
    "#FF6B6B" to Color(0xFFFF6B6B),
    "#FF9F43" to Color(0xFFFF9F43)
)
private val REMINDER_OPTIONS = listOf(0 to "Không nhắc", 5 to "5 phút trước", 10 to "10 phút", 15 to "15 phút", 30 to "30 phút", 60 to "1 tiếng")

@Composable
fun AddEventDialog(
    defaultDateMs: Long,
    onSave: (CalendarEventEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("📌") }
    var timeStart by remember { mutableStateOf("") }
    var timeEnd by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf(RecurrenceType.NONE) }
    var reminderIdx by remember { mutableIntStateOf(0) }
    var selectedColorHex by remember { mutableStateOf("#4A9EFF") }
    var description by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF080814))
                .border(1.5.dp, PRIMARY.copy(0.5f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Text(
                    "✨ TẠO SỰ KIỆN MỚI",
                    color = PRIMARY, fontSize = 13.sp,
                    fontWeight = FontWeight.Black, letterSpacing = 0.12f.em
                )

                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề *", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PRIMARY,
                        unfocusedBorderColor = Color(0xFF2A2A40),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = PRIMARY,
                        focusedLabelColor = PRIMARY,
                        unfocusedLabelColor = Color(0xFF5566AA)
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Emoji picker
                DialogSectionLabel("BIỂU TƯỢNG")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(EMOJI_OPTIONS) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedEmoji == emoji) PRIMARY.copy(0.2f) else Color(0xFF111122))
                                .border(
                                    1.dp,
                                    if (selectedEmoji == emoji) PRIMARY else Color(0xFF1E1E30),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 18.sp)
                        }
                    }
                }

                // Color picker
                DialogSectionLabel("MÀU SẮC")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    COLOR_OPTIONS.forEach { (hex, color) ->
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    if (selectedColorHex == hex) 2.5.dp else 0.dp,
                                    Color.White,
                                    CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                // Time row
                DialogSectionLabel("THỜI GIAN")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = timeStart,
                        onValueChange = { if (it.length <= 5) timeStart = it },
                        label = { Text("Bắt đầu", fontSize = 10.sp) },
                        placeholder = { Text("HH:MM", fontSize = 11.sp, color = Color(0xFF3A3A5A)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PRIMARY, unfocusedBorderColor = Color(0xFF2A2A40),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            cursorColor = PRIMARY, focusedLabelColor = PRIMARY,
                            unfocusedLabelColor = Color(0xFF5566AA)
                        ),
                        singleLine = true, shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = timeEnd,
                        onValueChange = { if (it.length <= 5) timeEnd = it },
                        label = { Text("Kết thúc", fontSize = 10.sp) },
                        placeholder = { Text("HH:MM", fontSize = 11.sp, color = Color(0xFF3A3A5A)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PRIMARY, unfocusedBorderColor = Color(0xFF2A2A40),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            cursorColor = PRIMARY, focusedLabelColor = PRIMARY,
                            unfocusedLabelColor = Color(0xFF5566AA)
                        ),
                        singleLine = true, shape = RoundedCornerShape(10.dp)
                    )
                }

                // Recurrence
                DialogSectionLabel("LẶP LẠI")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    RecurrenceType.entries.forEach { type ->
                        val label = when (type) {
                            RecurrenceType.NONE    -> "Một lần"
                            RecurrenceType.DAILY   -> "Ngày"
                            RecurrenceType.WEEKLY  -> "Tuần"
                            RecurrenceType.MONTHLY -> "Tháng"
                            RecurrenceType.YEARLY  -> "Năm"
                        }
                        val isSelected = recurrence == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (isSelected) PRIMARY.copy(0.2f) else Color(0xFF111122))
                                .border(1.dp, if (isSelected) PRIMARY else Color(0xFF1E1E30), RoundedCornerShape(7.dp))
                                .clickable { recurrence = type }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = if (isSelected) PRIMARY else Color(0xFF5566AA),
                                fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Reminder
                DialogSectionLabel("NHẮC HẸN")
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    REMINDER_OPTIONS.forEachIndexed { idx, (_, label) ->
                        val isSelected = reminderIdx == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (isSelected) GOLD.copy(0.15f) else Color(0xFF111122))
                                .border(1.dp, if (isSelected) GOLD.copy(0.6f) else Color(0xFF1E1E30), RoundedCornerShape(7.dp))
                                .clickable { reminderIdx = idx }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(label, color = if (isSelected) GOLD else Color(0xFF5566AA),
                                fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Ghi chú (tuỳ chọn)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PRIMARY, unfocusedBorderColor = Color(0xFF2A2A40),
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        cursorColor = PRIMARY, focusedLabelColor = PRIMARY,
                        unfocusedLabelColor = Color(0xFF5566AA)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color(0xFF2A2A40)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Huỷ", color = Color(0xFF5566AA), fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            if (title.isBlank()) return@Button
                            onSave(
                                CalendarEventEntity(
                                    title = title.trim(),
                                    description = description.trim(),
                                    emoji = selectedEmoji,
                                    baseDateMs = defaultDateMs,
                                    timeStart = timeStart.takeIf { it.matches(Regex("\\d{2}:\\d{2}")) },
                                    timeEnd = timeEnd.takeIf { it.matches(Regex("\\d{2}:\\d{2}")) },
                                    recurrenceType = recurrence,
                                    reminderMinutesBefore = REMINDER_OPTIONS[reminderIdx].first,
                                    colorHex = selectedColorHex
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PRIMARY),
                        shape = RoundedCornerShape(10.dp),
                        enabled = title.isNotBlank()
                    ) {
                        Text("Lưu", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogSectionLabel(text: String) {
    Text(text, color = Color(0xFF5566AA), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1f.em)
}

private fun isSameDay(ts1: Long, ts2: Long): Boolean {
    val c1 = Calendar.getInstance().apply { timeInMillis = ts1 }
    val c2 = Calendar.getInstance().apply { timeInMillis = ts2 }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
           c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}
