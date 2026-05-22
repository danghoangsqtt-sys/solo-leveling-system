package com.systemleveling.feature.quests.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.core.model.WorkPlanItem
import com.systemleveling.core.model.WorkPriority

// ── Palette ───────────────────────────────────────────────────────────────────
private val BG_SHEET     = Color(0xFF14142A)
private val GLASS_BORDER = Color(0x1FFFFFFF)
private val TEXT         = Color(0xFFE3E0F8)
private val TEXT_MUTED   = Color(0xFFC0C7D4)
private val PRIMARY      = Color(0xFF4A9EFF)
private val RED          = Color(0xFFFF4757)
private val ORANGE       = Color(0xFFFF7F50)
private val GOLD         = Color(0xFFFFD700)
private val GREEN        = Color(0xFF2ED573)

private fun WorkPriority.color(): Color = when (this) {
    WorkPriority.CRITICAL -> RED
    WorkPriority.HIGH     -> ORANGE
    WorkPriority.NORMAL   -> PRIMARY
    WorkPriority.LOW      -> GREEN
}

private fun WorkPriority.icon(): String = when (this) {
    WorkPriority.CRITICAL -> "🔥"
    WorkPriority.HIGH     -> "⚡"
    WorkPriority.NORMAL   -> "📋"
    WorkPriority.LOW      -> "💤"
}

// ── Main Sheet ────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkPlanInputSheet(
    items: List<WorkPlanItem>,
    onAdd: (WorkPlanItem) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit,
    onRegenerateQuests: () -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BG_SHEET,
        dragHandle = {
            Box(
                modifier = Modifier.padding(vertical = 10.dp)
                    .width(40.dp).height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.horizontalGradient(listOf(RED, ORANGE, PRIMARY)))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "📋 KẾ HOẠCH CÔNG VIỆC",
                        color = TEXT, fontSize = 16.sp,
                        fontWeight = FontWeight.Black, letterSpacing = 0.06f.em
                    )
                    Text(
                        "AI sẽ dùng danh sách này để sinh quest phù hợp",
                        color = TEXT_MUTED, fontSize = 11.sp
                    )
                }
                // Add button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PRIMARY.copy(0.15f))
                        .border(1.dp, PRIMARY.copy(0.5f), CircleShape)
                        .clickable { showAddForm = !showAddForm }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (showAddForm) "✕" else "+", color = PRIMARY, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Add form (collapsible)
            if (showAddForm) {
                AddWorkItemForm(
                    onConfirm = { item ->
                        onAdd(item)
                        showAddForm = false
                    },
                    onCancel = { showAddForm = false }
                )
                Spacer(Modifier.height(16.dp))
            }

            // Existing items
            if (items.isEmpty() && !showAddForm) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GLASS_BORDER.copy(0.05f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌟", fontSize = 32.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Chưa có công việc nào", color = TEXT_MUTED, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Thêm công việc để AI sinh quest phù hợp hơn", color = TEXT_MUTED.copy(0.6f), fontSize = 11.sp)
                    }
                }
            } else if (items.isNotEmpty()) {
                // Priority legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WorkPriority.entries.forEach { p ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(p.color().copy(0.1f))
                                .border(0.5.dp, p.color().copy(0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("${p.icon()} ${p.label}", color = p.color(), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Items list (sorted by priority score desc)
                val sorted = items.sortedByDescending { it.workPriority.score }
                sorted.forEach { item ->
                    WorkItemRow(item = item, onRemove = { onRemove(item.id) })
                    Spacer(Modifier.height(6.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Regenerate button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(RED.copy(0.25f), ORANGE.copy(0.2f), PRIMARY.copy(0.15f))))
                    .border(1.dp, PRIMARY.copy(0.5f), RoundedCornerShape(12.dp))
                    .clickable {
                        onRegenerateQuests()
                        onDismiss()
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🔄", fontSize = 16.sp)
                    Text(
                        "TẠNH SINH QUEST MỚI THEO KẾ HOẠCH",
                        color = PRIMARY, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.06f.em
                    )
                }
            }
        }
    }
}

// ── Add form ──────────────────────────────────────────────────────────────────
@Composable
private fun AddWorkItemForm(
    onConfirm: (WorkPlanItem) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var estimatedStr by remember { mutableStateOf("30") }
    var selectedPriority by remember { mutableStateOf(WorkPriority.NORMAL) }
    var note by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E1E35))
            .border(1.dp, PRIMARY.copy(0.3f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("+ Thêm công việc", color = PRIMARY, fontSize = 13.sp, fontWeight = FontWeight.Bold)

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tên công việc *", fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1A1A2B),
                    unfocusedContainerColor = Color(0xFF1A1A2B),
                    focusedTextColor = TEXT, unfocusedTextColor = TEXT,
                    focusedIndicatorColor = PRIMARY, unfocusedIndicatorColor = GLASS_BORDER
                ),
                singleLine = true
            )

            // Priority selector
            Text("Mức ưu tiên:", color = TEXT_MUTED, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WorkPriority.entries.forEach { p ->
                    val isSelected = selectedPriority == p
                    val bgColor by animateColorAsState(
                        if (isSelected) p.color().copy(0.2f) else Color.Transparent,
                        label = "prioBg"
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .border(
                                1.dp,
                                if (isSelected) p.color().copy(0.8f) else GLASS_BORDER,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedPriority = p }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${p.icon()}",
                            fontSize = 16.sp
                        )
                    }
                }
            }
            Text(
                "${selectedPriority.icon()} ${selectedPriority.label}",
                color = selectedPriority.color(), fontSize = 10.sp, fontWeight = FontWeight.Bold
            )

            // Deadline + Duration row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Deadline (HH:mm)", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1A1A2B), unfocusedContainerColor = Color(0xFF1A1A2B),
                        focusedTextColor = TEXT, unfocusedTextColor = TEXT,
                        focusedIndicatorColor = PRIMARY, unfocusedIndicatorColor = GLASS_BORDER
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = estimatedStr,
                    onValueChange = { if (it.all { c -> c.isDigit() }) estimatedStr = it },
                    label = { Text("Phút cần", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1A1A2B), unfocusedContainerColor = Color(0xFF1A1A2B),
                        focusedTextColor = TEXT, unfocusedTextColor = TEXT,
                        focusedIndicatorColor = PRIMARY, unfocusedIndicatorColor = GLASS_BORDER
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }

            // Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú (tùy chọn)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1A1A2B), unfocusedContainerColor = Color(0xFF1A1A2B),
                    focusedTextColor = TEXT, unfocusedTextColor = TEXT,
                    focusedIndicatorColor = PRIMARY, unfocusedIndicatorColor = GLASS_BORDER
                ),
                maxLines = 2
            )

            // Confirm / Cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                        .background(GLASS_BORDER.copy(0.05f))
                        .border(0.5.dp, GLASS_BORDER, RoundedCornerShape(8.dp))
                        .clickable { onCancel() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Huỷ", color = TEXT_MUTED, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier.weight(2f).clip(RoundedCornerShape(8.dp))
                        .background(if (title.isNotBlank()) PRIMARY.copy(0.2f) else GLASS_BORDER.copy(0.03f))
                        .border(1.dp, if (title.isNotBlank()) PRIMARY.copy(0.7f) else GLASS_BORDER, RoundedCornerShape(8.dp))
                        .clickable(enabled = title.isNotBlank()) {
                            onConfirm(
                                WorkPlanItem(
                                    title = title.trim(),
                                    priority = selectedPriority.name,
                                    deadline = deadline.trim(),
                                    estimatedMinutes = estimatedStr.toIntOrNull() ?: 30,
                                    note = note.trim()
                                )
                            )
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✅ Thêm vào kế hoạch",
                        color = if (title.isNotBlank()) PRIMARY else TEXT_MUTED.copy(0.4f),
                        fontSize = 12.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Work item row ─────────────────────────────────────────────────────────────
@Composable
private fun WorkItemRow(item: WorkPlanItem, onRemove: () -> Unit) {
    val pColor = item.workPriority.color()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(pColor.copy(0.06f))
            .border(0.5.dp, pColor.copy(0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Priority icon
            Text(item.workPriority.icon(), fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, color = TEXT, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(item.workPriority.label, color = pColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    if (item.deadline.isNotBlank()) Text("⏰ ${item.deadline}", color = GOLD, fontSize = 9.sp)
                    Text("~${item.estimatedMinutes}min", color = TEXT_MUTED, fontSize = 9.sp)
                }
                if (item.note.isNotBlank()) {
                    Text(item.note, color = TEXT_MUTED.copy(0.7f), fontSize = 10.sp, lineHeight = 13.sp)
                }
            }

            // Remove
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onRemove() }
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("✕", color = TEXT_MUTED.copy(0.5f), fontSize = 12.sp)
            }
        }
    }
}
