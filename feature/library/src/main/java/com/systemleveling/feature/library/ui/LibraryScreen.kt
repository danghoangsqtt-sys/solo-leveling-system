package com.systemleveling.feature.library.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemleveling.core.database.entity.CourseEntity
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.designsystem.theme.md_theme_dark_primary
import com.systemleveling.core.model.CourseContentType
import com.systemleveling.core.model.ItemRarity
import kotlin.math.roundToInt

// ── Drag state ────────────────────────────────────────────────────────────────
private data class DragState(
    val draggedId: String,
    val ghostY: Float,
    val dropTargetId: String? = null
)

// ── Depth helper ──────────────────────────────────────────────────────────────
private fun courseDepth(course: CourseEntity, map: Map<String, CourseEntity>): Int {
    var depth = 0
    var cur = course
    val visited = mutableSetOf<String>()
    while (cur.parentId != null) {
        if (cur.parentId in visited) break
        visited += cur.id
        cur = map[cur.parentId] ?: break
        depth++
    }
    return depth
}

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    onNavigateToCourse: (String) -> Unit
) {
    val courses by viewModel.displayedCourses.collectAsState()
    val courseMap by viewModel.courseMap.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val expandedFolderIds by viewModel.expandedFolderIds.collectAsState()
    val folderIds by viewModel.folderIds.collectAsState()
    val selectedCourseIds by viewModel.selectedCourseIds.collectAsState()
    val isSelectMode by viewModel.isSelectMode.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var courseToEdit by remember { mutableStateOf<CourseEntity?>(null) }

    // Drag & drop state
    var dragState by remember { mutableStateOf<DragState?>(null) }
    var screenTopY by remember { mutableFloatStateOf(0f) }
    val rowTopY = remember { mutableStateMapOf<String, Float>() }
    val rowHeights = remember { mutableStateMapOf<String, Float>() }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    if (showAddDialog) {
        AddCourseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, author, desc, url, type, category ->
                viewModel.addCourse(title, author, desc, url, type, category)
                showAddDialog = false
            }
        )
    }
    if (showAddFolderDialog) {
        AddFolderDialog(
            onDismiss = { showAddFolderDialog = false },
            onConfirm = { name ->
                viewModel.createFolder(name)
                showAddFolderDialog = false
            }
        )
    }
    if (showSyncDialog) {
        SyncFromWebDialog(
            isSyncing = isSyncing,
            onDismiss = { showSyncDialog = false },
            onSyncAppwrite = { key -> viewModel.syncFromAppwrite(key); showSyncDialog = false },
            onImportJson = { json -> viewModel.importFromJson(json); showSyncDialog = false }
        )
    }
    courseToEdit?.let { course ->
        EditCourseDialog(
            course = course,
            onDismiss = { courseToEdit = null },
            onConfirm = { title, desc, url ->
                viewModel.editCourse(course, title, desc, url)
                courseToEdit = null
            }
        )
    }
    if (showGroupDialog) {
        GroupCoursesDialog(
            count = selectedCourseIds.size,
            onDismiss = { showGroupDialog = false },
            onConfirm = { name ->
                viewModel.groupSelectedIntoFolder(name)
                showGroupDialog = false
            }
        )
    }
    if (showMoveDialog) {
        val targets = courseMap.values
            .filter { it.id in folderIds && it.id !in selectedCourseIds }
            .sortedBy { it.title }
        FolderPickerDialog(
            folders = targets,
            onDismiss = { showMoveDialog = false },
            onPickFolder = { id -> viewModel.moveToFolder(selectedCourseIds, id); showMoveDialog = false },
            onMoveToRoot = { viewModel.moveToFolder(selectedCourseIds, null); showMoveDialog = false }
        )
    }
    syncMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearSyncMessage() },
            containerColor = Color(0xFF1E1E2F),
            title = { Text("Kết quả", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(msg, color = Color.LightGray, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSyncMessage() }) {
                    Text("OK", color = md_theme_dark_primary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(md_theme_dark_background)
            .onGloballyPositioned { screenTopY = it.positionInRoot().y }
    ) {
        Column(Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0D1A))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "◀", color = md_theme_dark_primary, fontSize = 15.sp,
                    modifier = Modifier.clickable { onBack() }
                )
                Spacer(Modifier.width(10.dp))
                Text("📚", fontSize = 13.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    "Kho Học Thuật",
                    color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF1A1A2E))
                        .border(0.5.dp, md_theme_dark_primary.copy(0.35f), RoundedCornerShape(5.dp))
                        .clickable(enabled = !isSyncing) { showSyncDialog = true }
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    if (isSyncing)
                        CircularProgressIndicator(Modifier.size(11.dp), strokeWidth = 1.5.dp, color = md_theme_dark_primary)
                    else
                        Text("⟳", color = md_theme_dark_primary, fontSize = 11.sp)
                }
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF1A2A1A))
                        .border(0.5.dp, Color(0xFF40A060).copy(0.5f), RoundedCornerShape(5.dp))
                        .clickable { showAddFolderDialog = true }
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📁+", fontSize = 11.sp)
                }
                Spacer(Modifier.width(4.dp))
                Box(
                    Modifier.size(26.dp).clip(CircleShape).background(md_theme_dark_primary)
                        .clickable { showAddDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            // ── Category filter ───────────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                item {
                    SmallCategoryChip("🗂", "Tất cả", selectedCategory == null) { viewModel.setCategory(null) }
                }
                items(CourseContentType.entries) { type ->
                    SmallCategoryChip(type.icon, type.label, selectedCategory == type) { viewModel.setCategory(type) }
                }
            }
            HorizontalDivider(color = Color(0xFF1A1A28), thickness = 0.5.dp)

            // ── Select-mode action bar ────────────────────────────────────────
            if (isSelectMode) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141422))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "✅ ${selectedCourseIds.size} đã chọn",
                        color = md_theme_dark_primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (selectedCourseIds.size >= 2) {
                            TextButton(
                                onClick = { showGroupDialog = true },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) { Text("📁 Nhóm", color = md_theme_dark_primary, fontSize = 10.sp) }
                        }
                        TextButton(
                            onClick = { showMoveDialog = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) { Text("📤 Di chuyển", color = Color(0xFFFF9F43), fontSize = 10.sp) }
                        TextButton(
                            onClick = { viewModel.clearSelection() },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) { Text("Hủy", color = Color.Gray, fontSize = 10.sp) }
                    }
                }
                HorizontalDivider(color = Color(0xFF1A1A28), thickness = 0.5.dp)
            }

            // ── Tree list ─────────────────────────────────────────────────────
            if (courses.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 36.sp)
                        Spacer(Modifier.height(10.dp))
                        Text("Chưa có tài liệu nào.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                        Text("Nhấn + để thêm.", color = Color.DarkGray, fontSize = 11.sp)
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(courses, key = { it.id }) { course ->
                        val depth = courseDepth(course, courseMap)
                        val isFolder = course.id in folderIds
                        val isExpanded = course.id in expandedFolderIds
                        val isSelected = course.id in selectedCourseIds
                        val isDragTarget = dragState?.dropTargetId == course.id && isFolder
                        val isBeingDragged = dragState?.draggedId == course.id

                        TreeRow(
                            course = course,
                            depth = depth,
                            isFolder = isFolder,
                            isExpanded = isExpanded,
                            isSelected = isSelected,
                            isSelectMode = isSelectMode,
                            isDragTarget = isDragTarget,
                            isBeingDragged = isBeingDragged,
                            onPositioned = { y, h -> rowTopY[course.id] = y; rowHeights[course.id] = h },
                            onClick = {
                                when {
                                    isSelectMode -> viewModel.toggleSelect(course.id)
                                    isFolder     -> viewModel.toggleFolderExpansion(course.id)
                                    else         -> onNavigateToCourse(course.id)
                                }
                            },
                            onLongPress = { viewModel.toggleSelect(course.id) },
                            onToggleExpand = { viewModel.toggleFolderExpansion(course.id) },
                            onPin = { viewModel.togglePin(course) },
                            onDelete = { viewModel.deleteCourse(course.id) },
                            onEdit = { courseToEdit = course },
                            onUngroup = if (course.parentId != null) ({ viewModel.ungroupCourse(course) }) else null,
                            onDragStart = { startY ->
                                dragState = DragState(draggedId = course.id, ghostY = startY)
                            },
                            onDragDelta = { dy ->
                                dragState = dragState?.let { ds ->
                                    val newY = ds.ghostY + dy
                                    val target = rowTopY.entries
                                        .filter { (id, _) -> id in folderIds && id != ds.draggedId }
                                        .firstOrNull { (id, top) ->
                                            val h = rowHeights[id] ?: 0f
                                            newY >= top && newY <= top + h
                                        }?.key
                                    ds.copy(ghostY = newY, dropTargetId = target)
                                }
                            },
                            onDragEnd = {
                                dragState?.let { ds ->
                                    if (ds.dropTargetId != null) {
                                        viewModel.moveToFolder(setOf(ds.draggedId), ds.dropTargetId)
                                    }
                                }
                                dragState = null
                            }
                        )
                    }
                }
            }
        }

        // ── Drag ghost overlay ────────────────────────────────────────────────
        dragState?.let { ds ->
            courseMap[ds.draggedId]?.let { ghost ->
                DragGhost(
                    course = ghost,
                    isFolder = ghost.id in folderIds,
                    y = (ds.ghostY - screenTopY).roundToInt(),
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }
        }
    }
}

// ── Tree Row ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TreeRow(
    course: CourseEntity,
    depth: Int,
    isFolder: Boolean,
    isExpanded: Boolean,
    isSelected: Boolean,
    isSelectMode: Boolean,
    isDragTarget: Boolean,
    isBeingDragged: Boolean,
    onPositioned: (y: Float, h: Float) -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onToggleExpand: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onUngroup: (() -> Unit)?,
    onDragStart: (startGlobalY: Float) -> Unit,
    onDragDelta: (dy: Float) -> Unit,
    onDragEnd: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var itemTopY by remember { mutableFloatStateOf(0f) }

    val bgColor = when {
        isDragTarget    -> md_theme_dark_primary.copy(alpha = 0.12f)
        isSelected      -> md_theme_dark_primary.copy(alpha = 0.07f)
        isBeingDragged  -> Color(0xFF141424).copy(alpha = 0.5f)
        else            -> Color.Transparent
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .then(if (isDragTarget) Modifier.border(0.5.dp, md_theme_dark_primary.copy(0.6f)) else Modifier)
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    itemTopY = pos.y
                    onPositioned(pos.y, coords.size.height.toFloat())
                }
                .pointerInput(course.id) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset -> onDragStart(itemTopY + offset.y) },
                        onDrag = { change, delta -> change.consume(); onDragDelta(delta.y) },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() }
                    )
                }
                .combinedClickable(onClick = onClick, onLongClick = onLongPress)
                .padding(
                    start = (12 + depth * 14).dp,
                    end = 10.dp,
                    top = 6.dp,
                    bottom = 6.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Depth guide line
            if (depth > 0) {
                Box(Modifier.width(1.dp).height(28.dp).background(Color(0xFF262636)))
                Spacer(Modifier.width(7.dp))
            }

            // Expand chevron (folders only) or spacer
            if (isFolder) {
                Text(
                    text = if (isExpanded) "▾" else "▸",
                    color = Color(0xFF5A5A7A),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onToggleExpand() }
                        .wrapContentSize()
                )
                Spacer(Modifier.width(3.dp))
            } else {
                Spacer(Modifier.width(19.dp))
            }

            // Icon
            Text(
                text = if (isFolder) (if (isExpanded) "📂" else "📁") else course.contentType.icon,
                fontSize = if (isFolder) 15.sp else 12.sp
            )
            Spacer(Modifier.width(7.dp))

            // Title + subtitle
            Column(Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    color = if (isBeingDragged) Color(0xFF666680) else Color.White,
                    fontSize = 13.sp,
                    fontWeight = if (isFolder) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isFolder && course.author.isNotBlank()) {
                    Text(
                        text = course.author,
                        color = Color(0xFF5A5A7A),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (isFolder && course.category.isNotBlank()) {
                    Text(course.category, color = Color(0xFF5A5A7A), fontSize = 10.sp, maxLines = 1)
                }
            }

            // Right-side: actions or select indicator
            if (isSelectMode) {
                Box(
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) md_theme_dark_primary else Color(0xFF252535))
                        .border(0.5.dp, if (isSelected) md_theme_dark_primary else Color(0xFF3A3A4A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) Text("✓", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (course.isPinned) {
                        Text("📌", fontSize = 9.sp, modifier = Modifier.clickable { onPin() })
                    }
                    Text("✏️", fontSize = 9.sp, modifier = Modifier.clickable { onEdit() })
                    if (onUngroup != null) {
                        Text("⬆", fontSize = 9.sp, color = Color(0xFFFF9F43), modifier = Modifier.clickable { onUngroup() })
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (confirmDelete) Color(0xFFFF5252).copy(0.12f) else Color.Transparent)
                            .clickable {
                                if (confirmDelete) { onDelete(); confirmDelete = false }
                                else confirmDelete = true
                            }
                            .padding(1.dp)
                    ) {
                        Text(
                            text = if (confirmDelete) "✓" else "🗑",
                            fontSize = 9.sp,
                            color = if (confirmDelete) Color(0xFFFF5252) else Color(0xFF444455)
                        )
                    }
                }
            }
        }

        // Row divider (indented to match content)
        HorizontalDivider(
            color = Color(0xFF16161E),
            thickness = 0.5.dp,
            modifier = Modifier.padding(start = (12 + depth * 14 + 40).dp)
        )
    }
}

// ── Drag ghost ────────────────────────────────────────────────────────────────
@Composable
private fun DragGhost(
    course: CourseEntity,
    isFolder: Boolean,
    y: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .offset { IntOffset(16, y - 18) }
            .fillMaxWidth(0.6f)
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xFF252535).copy(alpha = 0.95f))
            .border(0.5.dp, md_theme_dark_primary.copy(0.5f), RoundedCornerShape(7.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (isFolder) "📁" else course.contentType.icon, fontSize = 12.sp)
        Spacer(Modifier.width(7.dp))
        Text(
            course.title,
            color = Color.White, fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Folder picker dialog ──────────────────────────────────────────────────────
@Composable
private fun FolderPickerDialog(
    folders: List<CourseEntity>,
    onDismiss: () -> Unit,
    onPickFolder: (String) -> Unit,
    onMoveToRoot: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A2A),
        title = { Text("Di chuyển vào thư mục", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(5.dp))
                            .clickable { onMoveToRoot() }
                            .padding(horizontal = 8.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏠", fontSize = 14.sp)
                        Spacer(Modifier.width(10.dp))
                        Text("Thư mục gốc", color = Color(0xFFAAAAAA), fontSize = 13.sp)
                    }
                    HorizontalDivider(color = Color(0xFF252530))
                }
                items(folders, key = { it.id }) { folder ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(5.dp))
                            .clickable { onPickFolder(folder.id) }
                            .padding(horizontal = 8.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📁", fontSize = 14.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(folder.title, color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy", color = Color.Gray, fontSize = 12.sp) } }
    )
}

// ── Small composables ─────────────────────────────────────────────────────────
@Composable
private fun SmallCategoryChip(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) md_theme_dark_primary.copy(0.18f) else Color(0xFF161626))
            .border(0.5.dp, if (selected) md_theme_dark_primary else Color(0xFF242434), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            "$icon $label",
            color = if (selected) md_theme_dark_primary else Color(0xFF7A7A9A),
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = md_theme_dark_primary,
    unfocusedBorderColor = Color.Gray,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)

private fun rarityColor(rarity: ItemRarity): Color = when (rarity) {
    ItemRarity.COMMON    -> Color(0xFFAAAAAA)
    ItemRarity.UNCOMMON  -> Color(0xFF40E17E)
    ItemRarity.RARE      -> Color(0xFF4A9EFF)
    ItemRarity.EPIC      -> Color(0xFFE040FB)
    ItemRarity.LEGENDARY -> Color(0xFFFFAB40)
    ItemRarity.MYTHIC    -> Color(0xFFFF5252)
}

// ── Add Folder Dialog ─────────────────────────────────────────────────────────
@Composable
private fun AddFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2F),
        title = { Text("📁 Tạo thư mục mới", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tạo thư mục rỗng để gom khóa học và tài liệu vào.", color = Color.LightGray, fontSize = 12.sp)
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Tên thư mục *", color = Color.Gray) },
                    placeholder = { Text("VD: ESP32, Python cơ bản, Toán cao cấp...", color = Color.DarkGray, fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (folderName.isNotBlank()) onConfirm(folderName.trim()) },
                enabled = folderName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary)
            ) { Text("TẠO", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("HỦY", color = Color.Gray, fontSize = 12.sp) } }
    )
}

// ── Add Course Dialog ─────────────────────────────────────────────────────────
@Composable
private fun AddCourseDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, author: String, desc: String, url: String, type: CourseContentType, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CourseContentType.GENERAL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2F),
        title = { Text("Thêm Tài Liệu Mới", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("Tên *", color = Color.Gray) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(), colors = outlinedColors()
                    )
                }
                item {
                    OutlinedTextField(
                        value = author, onValueChange = { author = it },
                        label = { Text("Tác giả / Nguồn", color = Color.Gray) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(), colors = outlinedColors()
                    )
                }
                item {
                    OutlinedTextField(
                        value = desc, onValueChange = { desc = it },
                        label = { Text("Mô tả", color = Color.Gray) },
                        maxLines = 2, modifier = Modifier.fillMaxWidth(), colors = outlinedColors()
                    )
                }
                item {
                    OutlinedTextField(
                        value = url, onValueChange = { url = it },
                        label = { Text("Link (URL)", color = Color.Gray) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(), colors = outlinedColors()
                    )
                }
                item {
                    OutlinedTextField(
                        value = category, onValueChange = { category = it },
                        label = { Text("Danh mục", color = Color.Gray) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(), colors = outlinedColors()
                    )
                }
                item {
                    Text("Loại nội dung", color = Color.Gray, fontSize = 11.sp)
                    Spacer(Modifier.height(5.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        CourseContentType.entries.forEach { type ->
                            val selected = selectedType == type
                            val color = contentTypeColor(type)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(if (selected) color.copy(0.18f) else Color(0xFF121222))
                                    .border(0.5.dp, if (selected) color else Color.DarkGray, RoundedCornerShape(7.dp))
                                    .clickable { selectedType = type }
                                    .padding(6.dp)
                            ) {
                                Text(type.icon, fontSize = 16.sp)
                                Text(type.label, color = if (selected) color else Color.Gray, fontSize = 8.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim(), author.trim(), desc.trim(), url.trim(), selectedType, category.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary)
            ) { Text("LƯU", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("HỦY", color = Color.Gray, fontSize = 12.sp) } }
    )
}

// ── Sync / Import dialog ──────────────────────────────────────────────────────
@Composable
private fun SyncFromWebDialog(
    isSyncing: Boolean,
    onDismiss: () -> Unit,
    onSyncAppwrite: (apiKey: String) -> Unit,
    onImportJson: (json: String) -> Unit
) {
    var useJsonMode by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf("") }
    var jsonInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2F),
        title = { Text("Import dữ liệu học tập", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(7.dp)).background(Color(0xFF121222)).padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf("Appwrite API" to false, "Paste JSON" to true).forEach { (label, isJson) ->
                            val sel = useJsonMode == isJson
                            Box(
                                Modifier.weight(1f).clip(RoundedCornerShape(5.dp))
                                    .background(if (sel) md_theme_dark_primary.copy(0.18f) else Color.Transparent)
                                    .border(if (sel) 0.5.dp else 0.dp, if (sel) md_theme_dark_primary else Color.Transparent, RoundedCornerShape(5.dp))
                                    .clickable { useJsonMode = isJson }.padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (sel) md_theme_dark_primary else Color.Gray, fontSize = 11.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal)
                            }
                        }
                    }
                }
                if (!useJsonMode) {
                    item { Text("Nhập Appwrite API Key để import khóa học từ web.", color = Color.LightGray, fontSize = 12.sp) }
                    item {
                        OutlinedTextField(
                            value = apiKey, onValueChange = { apiKey = it },
                            label = { Text("Appwrite API Key", color = Color.Gray) },
                            singleLine = true, modifier = Modifier.fillMaxWidth(), colors = outlinedColors()
                        )
                    }
                    item { Text("Console → Settings → API Keys → Create (databases.read)", color = Color.Gray, fontSize = 10.sp) }
                } else {
                    item { Text("Dán JSON array từ web app dhebook.", color = Color.LightGray, fontSize = 12.sp) }
                    item {
                        OutlinedTextField(
                            value = jsonInput, onValueChange = { jsonInput = it },
                            label = { Text("JSON Array", color = Color.Gray) },
                            maxLines = 6, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), colors = outlinedColors()
                        )
                    }
                    item { Text("Web App → DevTools → Network → copy response body", color = Color.Gray, fontSize = 10.sp) }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!useJsonMode && apiKey.isNotBlank()) onSyncAppwrite(apiKey.trim())
                    else if (useJsonMode && jsonInput.isNotBlank()) onImportJson(jsonInput.trim())
                },
                enabled = (if (useJsonMode) jsonInput.isNotBlank() else apiKey.isNotBlank()) && !isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary)
            ) { Text(if (useJsonMode) "IMPORT" else "ĐỒNG BỘ", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("HỦY", color = Color.Gray, fontSize = 12.sp) } }
    )
}

// ── Edit Course Dialog ────────────────────────────────────────────────────────
@Composable
private fun EditCourseDialog(
    course: CourseEntity,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, url: String) -> Unit
) {
    var title by remember { mutableStateOf(course.title) }
    var desc by remember { mutableStateOf(course.description) }
    var url by remember { mutableStateOf(course.contentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2F),
        title = { Text("Chỉnh sửa", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tên", color = Color.Gray) }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = outlinedColors())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Mô tả", color = Color.Gray) }, maxLines = 3, modifier = Modifier.fillMaxWidth(), colors = outlinedColors())
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Link (URL)", color = Color.Gray) }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = outlinedColors())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title.trim(), desc.trim(), url.trim()) }, enabled = title.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary)) {
                Text("LƯU", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("HỦY", color = Color.Gray, fontSize = 12.sp) } }
    )
}

// ── Group Courses Dialog ──────────────────────────────────────────────────────
@Composable
private fun GroupCoursesDialog(
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: (folderName: String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2F),
        title = { Text("Tạo thư mục nhóm", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Nhóm $count mục vào một thư mục.", color = Color.LightGray, fontSize = 12.sp)
                OutlinedTextField(
                    value = folderName, onValueChange = { folderName = it },
                    label = { Text("Tên thư mục", color = Color.Gray) },
                    placeholder = { Text("VD: ESP32, Python cơ bản...", color = Color.DarkGray, fontSize = 11.sp) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), colors = outlinedColors()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(folderName.trim()) }, enabled = folderName.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary)) {
                Text("TẠO", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("HỦY", color = Color.Gray, fontSize = 12.sp) } }
    )
}
