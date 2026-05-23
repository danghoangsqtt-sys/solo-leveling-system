package com.systemleveling.feature.library.ui

import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.systemleveling.core.database.entity.LessonEntity
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.designsystem.theme.md_theme_dark_primary
import com.systemleveling.core.model.CourseContentType

@Composable
fun CourseDetailScreen(
    viewModel: CourseDetailViewModel,
    onBack: () -> Unit
) {
    val course by viewModel.course.collectAsState()
    val lessons by viewModel.lessons.collectAsState()
    val viewingLesson by viewModel.viewingLesson.collectAsState()

    val config = LocalConfiguration.current
    val isTablet = config.screenWidthDp >= 600

    // Handle back press when viewer is open
    BackHandler(enabled = viewingLesson != null) {
        viewModel.closeViewer()
    }

    if (isTablet) {
        // ── Tablet: Two-pane layout ──────────────────────────────────────────
        Row(modifier = Modifier.fillMaxSize().background(md_theme_dark_background)) {
            // Left pane — lesson list (40%)
            Column(modifier = Modifier.fillMaxHeight().weight(0.4f)) {
                LessonListPane(
                    course = course,
                    lessons = lessons,
                    viewModel = viewModel,
                    onBack = onBack,
                    selectedLesson = viewingLesson
                )
            }
            // Divider
            Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFF2F2F40)))
            // Right pane — WebView (60%)
            Box(modifier = Modifier.fillMaxHeight().weight(0.6f)) {
                if (viewingLesson != null) {
                    WebViewPane(lesson = viewingLesson!!)
                } else {
                    EmptyViewerPlaceholder()
                }
            }
        }
    } else {
        // ── Phone: Full-screen switch ────────────────────────────────────────
        if (viewingLesson != null) {
            PhoneWebViewScreen(
                lesson = viewingLesson!!,
                onBack = { viewModel.closeViewer() }
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(md_theme_dark_background)) {
                LessonListPane(
                    course = course,
                    lessons = lessons,
                    viewModel = viewModel,
                    onBack = onBack,
                    selectedLesson = null
                )
            }
        }
    }
}

// ── Lesson list pane ─────────────────────────────────────────────────────────

@Composable
private fun LessonListPane(
    course: com.systemleveling.core.database.entity.CourseEntity?,
    lessons: List<LessonEntity>,
    viewModel: CourseDetailViewModel,
    onBack: () -> Unit,
    selectedLesson: LessonEntity?
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var lessonToEdit by remember { mutableStateOf<LessonEntity?>(null) }

    if (showAddDialog) {
        AddLessonDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, url, type, notes ->
                viewModel.addLesson(title, url, type, notes)
                showAddDialog = false
            }
        )
    }

    lessonToEdit?.let { lesson ->
        EditLessonDialog(
            lesson = lesson,
            onDismiss = { lessonToEdit = null },
            onConfirm = { title, url, type, notes ->
                viewModel.editLesson(lesson, title, url, type, notes)
                lessonToEdit = null
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF1E1E2F),
            title = { Text("Xóa khóa học?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Sẽ xóa toàn bộ khóa học và tất cả bài học. Không thể hoàn tác.", color = Color.Gray) },
            confirmButton = {
                Button(
                    onClick = { onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) { Text("XÓA", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("HỦY", color = Color.Gray) }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(top = 32.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("◀ BACK", color = md_theme_dark_primary, modifier = Modifier.clickable { onBack() })
            Text("+", color = md_theme_dark_primary, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { showAddDialog = true })
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Course info card
        if (course != null) {
            CourseInfoCard(course = course)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Lessons header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DANH SÁCH BÀI HỌC",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = "${lessons.count { it.isCompleted }}/${lessons.size} hoàn thành",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (lessons.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📂", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Chưa có bài học nào.", color = Color.Gray, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Nhấn + để thêm bài học mới.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(lessons, key = { _, it -> it.id }) { idx, lesson ->
                    LessonItem(
                        lesson = lesson,
                        isSelected = selectedLesson?.id == lesson.id,
                        onOpen = { viewModel.openLesson(lesson) },
                        onToggle = { viewModel.toggleLesson(lesson) },
                        onDelete = { viewModel.deleteLesson(lesson) },
                        onEdit = { lessonToEdit = lesson },
                        onMoveUp = if (idx > 0) ({ viewModel.moveLessonUp(lesson) }) else null,
                        onMoveDown = if (idx < lessons.size - 1) ({ viewModel.moveLessonDown(lesson) }) else null
                    )
                }
            }
        }
    }
}

// ── Course info card ─────────────────────────────────────────────────────────

@Composable
private fun CourseInfoCard(course: com.systemleveling.core.database.entity.CourseEntity) {
    val progress = if (course.totalModules > 0) course.completedModules.toFloat() / course.totalModules else 0f
    val typeColor = contentTypeColor(course.contentType)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E2E))
            .border(1.dp, typeColor.copy(0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(course.contentType.icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(course.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(course.author, color = Color.Gray, fontSize = 11.sp)
                }
                if (course.isCompleted) Text("✅", fontSize = 18.sp)
            }
            if (course.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(course.description, color = Color.LightGray, fontSize = 12.sp)
            }
            if (course.totalModules > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = typeColor,
                    trackColor = Color(0xFF2A2A3E)
                )
                Text(
                    text = "${course.completedModules}/${course.totalModules} bài",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// ── Lesson item ───────────────────────────────────────────────────────────────

@Composable
private fun LessonItem(
    lesson: LessonEntity,
    isSelected: Boolean,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val typeColor = contentTypeColor(lesson.contentType)
    val hasContent = lesson.contentUrl.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF1A2A1A) else Color(0xFF121222))
            .border(1.dp, if (isSelected) md_theme_dark_primary.copy(0.6f) else Color(0xFF2A2A3E), RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = hasContent) { onOpen() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completion checkbox
            Checkbox(
                checked = lesson.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = typeColor, uncheckedColor = Color.Gray),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(lesson.contentType.icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    color = if (lesson.isCompleted) Color.Gray else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (lesson.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (lesson.notes.isNotBlank()) {
                    Text(lesson.notes, color = Color.DarkGray, fontSize = 10.sp)
                }
                if (!hasContent) {
                    Text("Chưa có link", color = Color.DarkGray, fontSize = 10.sp)
                }
            }
            if (hasContent) {
                Text("▶", color = typeColor, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
            }
        }

        // ── Action toolbar ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0E0E1A))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reorder up
            if (onMoveUp != null) {
                Text(
                    "↑",
                    color = Color(0xFF5A5A8A), fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onMoveUp() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            // Reorder down
            if (onMoveDown != null) {
                Text(
                    "↓",
                    color = Color(0xFF5A5A8A), fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onMoveDown() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            // Edit
            Text(
                "✏️",
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onEdit() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            // Delete
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (confirmDelete) Color(0xFFFF5252).copy(0.2f) else Color.Transparent)
                    .clickable {
                        if (confirmDelete) { onDelete(); confirmDelete = false }
                        else confirmDelete = true
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    if (confirmDelete) "✓ Xóa?" else "🗑",
                    fontSize = 11.sp,
                    color = if (confirmDelete) Color(0xFFFF5252) else Color.DarkGray
                )
            }
        }
    }
}

// ── WebView pane (tablet side or phone full) ─────────────────────────────────

@Composable
fun WebViewPane(lesson: LessonEntity) {
    val embedUrl = remember(lesson.id, lesson.contentUrl) {
        buildEmbedUrl(lesson.contentUrl, lesson.contentType)
    }
    var isLoading by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D1A))) {
        if (embedUrl.isBlank()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có nội dung để hiển thị", color = Color.Gray)
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            mediaPlaybackRequiresUserGesture = false
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                        }
                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = false
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }
                        }
                        loadUrl(embedUrl)
                    }
                },
                update = { webView ->
                    if (webView.url != embedUrl) {
                        webView.loadUrl(embedUrl)
                        isLoading = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = md_theme_dark_primary)
                        Spacer(Modifier.height(8.dp))
                        Text("Đang tải nội dung...", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneWebViewScreen(lesson: LessonEntity, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D1A))) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF121222)).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("◀", color = md_theme_dark_primary, fontSize = 18.sp, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text(lesson.contentType.label, color = Color.Gray, fontSize = 11.sp)
            }
        }
        WebViewPane(lesson = lesson)
    }
}

@Composable
private fun EmptyViewerPlaceholder() {
    Box(Modifier.fillMaxSize().background(Color(0xFF0D0D1A)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📖", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text("Chọn một bài học để bắt đầu xem", color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

// ── Add Lesson Dialog ─────────────────────────────────────────────────────────

@Composable
private fun AddLessonDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, url: String, type: CourseContentType, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CourseContentType.GENERAL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2F),
        title = { Text("Thêm Bài Học", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Tên bài học *", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedFieldColors()
                )
                OutlinedTextField(
                    value = url, onValueChange = { url = it },
                    label = { Text("Link nội dung (URL)", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedFieldColors()
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Ghi chú", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedFieldColors()
                )
                Text("Loại nội dung", color = Color.Gray, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CourseContentType.entries.forEach { type ->
                        val selected = selectedType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) md_theme_dark_primary.copy(0.25f) else Color(0xFF121222))
                                .border(1.dp, if (selected) md_theme_dark_primary else Color.Gray, RoundedCornerShape(8.dp))
                                .clickable { selectedType = type }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(type.icon, fontSize = 18.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    onConfirm(title.trim(), url.trim(), selectedType, notes.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary)
            ) { Text("THÊM", color = Color.Black, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("HỦY", color = Color.Gray) }
        }
    )
}

// ── Edit Lesson Dialog ────────────────────────────────────────────────────────

@Composable
private fun EditLessonDialog(
    lesson: LessonEntity,
    onDismiss: () -> Unit,
    onConfirm: (title: String, url: String, type: CourseContentType, notes: String) -> Unit
) {
    var title by remember { mutableStateOf(lesson.title) }
    var url by remember { mutableStateOf(lesson.contentUrl) }
    var notes by remember { mutableStateOf(lesson.notes) }
    var selectedType by remember { mutableStateOf(lesson.contentType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2F),
        title = { Text("Chỉnh sửa bài học", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Tên bài học *", color = Color.Gray) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), colors = outlinedFieldColors()
                )
                OutlinedTextField(
                    value = url, onValueChange = { url = it },
                    label = { Text("Link nội dung (URL)", color = Color.Gray) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), colors = outlinedFieldColors()
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Ghi chú", color = Color.Gray) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), colors = outlinedFieldColors()
                )
                Text("Loại nội dung", color = Color.Gray, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CourseContentType.entries.forEach { type ->
                        val selected = selectedType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) md_theme_dark_primary.copy(0.25f) else Color(0xFF121222))
                                .border(1.dp, if (selected) md_theme_dark_primary else Color.Gray, RoundedCornerShape(8.dp))
                                .clickable { selectedType = type }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(type.icon, fontSize = 18.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim(), url.trim(), selectedType, notes.trim()) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_primary)
            ) { Text("LƯU", color = Color.Black, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("HỦY", color = Color.Gray) } }
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

fun buildEmbedUrl(url: String, type: CourseContentType): String {
    if (url.isBlank()) return ""
    return when (type) {
        CourseContentType.PDF -> {
            if (url.contains("drive.google.com")) {
                url.replace("/view?usp=sharing", "/preview")
                    .replace("/view", "/preview")
                    .replace("?usp=sharing", "/preview")
            } else {
                "https://docs.google.com/viewer?url=${Uri.encode(url)}&embedded=true"
            }
        }
        CourseContentType.VIDEO -> {
            val youtubePattern = Regex("""(?:youtu\.be/|youtube\.com/watch\?v=|youtube\.com/embed/)([a-zA-Z0-9_-]{11})""")
            val match = youtubePattern.find(url)
            if (match != null) {
                "https://www.youtube.com/embed/${match.groupValues[1]}?rel=0&showinfo=0"
            } else if (url.contains("drive.google.com")) {
                url.replace("/view?usp=sharing", "/preview").replace("/view", "/preview")
            } else url
        }
        else -> url
    }
}

fun contentTypeColor(type: CourseContentType): Color = when (type) {
    CourseContentType.EBOOK -> Color(0xFF40E17E)
    CourseContentType.VIDEO -> Color(0xFFFF5252)
    CourseContentType.PDF -> Color(0xFF4A9EFF)
    CourseContentType.ARTICLE -> Color(0xFFFFD700)
    CourseContentType.GENERAL -> Color(0xFFAAAAAA)
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = md_theme_dark_primary,
    unfocusedBorderColor = Color.Gray,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
