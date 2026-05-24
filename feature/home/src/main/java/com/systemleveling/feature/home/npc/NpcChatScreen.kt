package com.systemleveling.feature.home.npc

import android.Manifest
import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.systemleveling.core.ai.ChatMessage
import com.systemleveling.core.ai.MessageRole

private val BgDeep       = Color(0xFF0D0D1A)
private val GlassSurface = Color(0x28FFFFFF)
private val GlassBorder  = Color(0x40FFFFFF)
private val TextMuted    = Color(0xB3FFFFFF)
private val Purple       = Color(0xFFB48EFF)
private val Primary      = Color(0xFF4A9EFF)
private val RecordRed    = Color(0xFFFF4444)
private val UserBubbleBg = Color(0x55B48EFF)
private val AiBubbleBg   = Color(0x33000000)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NpcChatScreen(
    viewModel: NpcChatViewModel,
    onBack: () -> Unit
) {
    val apiKey           by viewModel.apiKey.collectAsState()
    val messages         by viewModel.messages.collectAsState()
    val isLoading        by viewModel.isLoading.collectAsState()
    val error            by viewModel.error.collectAsState()
    val isRecording      by viewModel.isRecording.collectAsState()
    val recordMode       by viewModel.recordMode.collectAsState()
    val recordingSeconds by viewModel.recordingSeconds.collectAsState()
    val targetLanguage   by viewModel.targetLanguage.collectAsState()
    val processingAudio  by viewModel.processingAudio.collectAsState()

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var webViewRef       by remember { mutableStateOf<WebView?>(null) }
    var inputText        by remember { mutableStateOf("") }
    var targetLangInput  by remember { mutableStateOf("Tiếng Việt") }
    val listState        = rememberLazyListState()

    // All launchers at top level — required by Compose rules
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startRecording()
    }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
                ?.getOrNull(0) ?: ""
            if (spoken.isNotBlank()) viewModel.sendMessage(spoken)
        }
    }

    // Auto-scroll to newest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    // Trigger proactive greeting on open
    LaunchedEffect(apiKey) {
        if (apiKey.isNotBlank() && messages.isEmpty()) {
            viewModel.triggerProactiveGreeting()
        }
    }

    // Sync target language field
    LaunchedEffect(targetLanguage) { targetLangInput = targetLanguage }

    // Animate Live2D mouth while AI is busy
    val isBusy = isLoading || processingAudio
    LaunchedEffect(isBusy) {
        if (isBusy) {
            while (true) {
                val vol = (10..70).random() / 10f
                webViewRef?.evaluateJavascript(
                    "if(typeof window.setVolume==='function')window.setVolume($vol);", null
                )
                kotlinx.coroutines.delay(120)
            }
        } else {
            webViewRef?.evaluateJavascript(
                "if(typeof window.setVolume==='function')window.setVolume(0);", null
            )
        }
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(
            onDismiss = { showApiKeyDialog = false },
            onConfirm = { key -> viewModel.saveApiKey(key); showApiKeyDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDeep)) {

        // ── 1. Full-screen Live2D WebView ────────────────────────────────────────
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.parseColor("#0D0D1A"))
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    @Suppress("DEPRECATION")
                    settings.allowFileAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    settings.allowUniversalAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val url = request?.url.toString()
                            if (url.startsWith("https://appassets.local/")) {
                                try {
                                    val assetPath = url.substringAfter("https://appassets.local/")
                                    val mimeType = when {
                                        assetPath.endsWith(".html") -> "text/html"
                                        assetPath.endsWith(".js")   -> "application/javascript"
                                        assetPath.endsWith(".json") -> "application/json"
                                        assetPath.endsWith(".png")  -> "image/png"
                                        assetPath.endsWith(".moc3") -> "application/octet-stream"
                                        else -> "application/octet-stream"
                                    }
                                    val inputStream = ctx.assets.open(assetPath)
                                    return WebResourceResponse(mimeType, "UTF-8", inputStream).apply {
                                        responseHeaders = mapOf("Access-Control-Allow-Origin" to "*")
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            return super.shouldInterceptRequest(view, request)
                        }
                    }
                    webChromeClient = WebChromeClient()
                    addJavascriptInterface(object : Any() {
                        @JavascriptInterface fun onModelLoaded() {}
                    }, "AndroidBridge")
                    loadUrl("https://appassets.local/live2d.html")
                    webViewRef = this
                }
            }
        )

        // ── 2. Gradient overlay ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC0D0D1A), BgDeep)
                    )
                )
        )

        // ── 3. UI overlay ───────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            NpcTopBar(
                apiKeySet    = apiKey.isNotBlank(),
                recordMode   = recordMode,
                onBack       = onBack,
                onSettings   = { showApiKeyDialog = true },
                onClearHistory = { viewModel.clearHistory() },
                onModeChange = { viewModel.setRecordMode(it) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Suggestion chips (chat mode, no messages)
            if (messages.isEmpty() && apiKey.isNotBlank() && recordMode == RecordMode.CHAT) {
                val suggestions = listOf(
                    "Xin chào Aura! 👋",
                    "Bạn có thể làm gì?",
                    "Kể cho tôi một câu chuyện",
                    "Giao cho tôi một nhiệm vụ",
                    "Hôm nay bạn thấy thế nào?"
                )
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    items(suggestions, key = { it }) { text ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(GlassSurface)
                                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                                .clickable { inputText = text }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(text, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Chat messages
            if (messages.isNotEmpty()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(messages, key = { "${it.role}-${it.content.hashCode()}" }) { msg ->
                        ChatBubble(msg)
                    }
                }
            }

            // Error banner
            error?.let { errMsg ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠ $errMsg",
                    color = Color(0xFFFF6B6B),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33FF0000))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable { viewModel.clearError() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Bottom action area ─────────────────────────────────────────────
            when {
                isRecording -> RecordingActivePanel(
                    seconds  = recordingSeconds,
                    onStop   = { viewModel.stopAndProcess() },
                    onCancel = { viewModel.cancelRecording() }
                )

                processingAudio -> ProcessingPanel(
                    label = if (recordMode == RecordMode.NOTES) "Đang xử lý ghi chú..."
                            else "Đang nhận dạng & dịch..."
                )

                recordMode == RecordMode.TRANSLATE -> Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetLangInput,
                        onValueChange = {
                            targetLangInput = it
                            viewModel.setTargetLanguage(it)
                        },
                        label = { Text("Ngôn ngữ đích", color = TextMuted, fontSize = 11.sp) },
                        placeholder = { Text("VD: Tiếng Việt, English...", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = Purple,
                            unfocusedBorderColor    = GlassBorder,
                            focusedTextColor        = Color.White,
                            unfocusedTextColor      = Color.White,
                            focusedContainerColor   = Color(0x28000000),
                            unfocusedContainerColor = Color(0x1A000000)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    RecordStartButton(
                        label     = "🌐 Nhấn để dịch ngoại ngữ",
                        hasApiKey = apiKey.isNotBlank(),
                        onApiKeyClick = { showApiKeyDialog = true },
                        onRecord = { recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                    )
                }

                recordMode == RecordMode.NOTES -> RecordStartButton(
                    label     = "🎙 Nhấn để ghi chú cuộc họp / học",
                    hasApiKey = apiKey.isNotBlank(),
                    onApiKeyClick = { showApiKeyDialog = true },
                    onRecord = { recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                )

                else -> ChatInputRow(
                    text          = inputText,
                    onTextChange  = { inputText = it },
                    isLoading     = isLoading,
                    hasApiKey     = apiKey.isNotBlank(),
                    onSend        = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    onApiKeyClick = { showApiKeyDialog = true },
                    onMicClick    = {
                        val intent = android.content.Intent(
                            android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                        ).apply {
                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
                            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Nói gì đó với Aura...")
                        }
                        try { speechLauncher.launch(intent) } catch (_: Exception) {}
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ── Top bar ────────────────────────────────────────────────────────────────────
@Composable
private fun NpcTopBar(
    apiKeySet: Boolean,
    recordMode: RecordMode,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onClearHistory: () -> Unit,
    onModeChange: (RecordMode) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp).clip(CircleShape).background(GlassSurface)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back",
                    tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("AURA", color = Color.White, fontSize = 18.sp,
                    fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(6.dp).clip(CircleShape)
                            .background(if (apiKeySet) Color(0xFF44FF88) else Color(0xFFFF4444))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (apiKeySet) "Online" else "API Key Required",
                        color = TextMuted, fontSize = 10.sp, letterSpacing = 0.5.sp
                    )
                }
            }

            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(GlassSurface)
                    .clickable(onClick = onClearHistory),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = "Clear history",
                    tint = TextMuted, modifier = Modifier.size(17.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(GlassSurface)
                    .clickable(onClick = onSettings),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings",
                    tint = TextMuted, modifier = Modifier.size(17.dp))
            }
        }

        // Mode selector chips
        if (apiKeySet) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeChip("💬 Chat",    selected = recordMode == RecordMode.CHAT)
                    { onModeChange(RecordMode.CHAT) }
                ModeChip("📝 Ghi chú", selected = recordMode == RecordMode.NOTES)
                    { onModeChange(RecordMode.NOTES) }
                ModeChip("🌐 Dịch",    selected = recordMode == RecordMode.TRANSLATE)
                    { onModeChange(RecordMode.TRANSLATE) }
            }
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Purple.copy(alpha = 0.3f) else GlassSurface)
            .border(1.dp, if (selected) Purple else GlassBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected) Purple else TextMuted,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ── Chat bubble ────────────────────────────────────────────────────────────────
@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        // Wider bubble for AI results (may contain transcripts + translations)
        val bubbleWidthFraction = if (isUser) 0.82f else 0.95f
        Box(
            modifier = Modifier
                .fillMaxWidth(bubbleWidthFraction)
                .clip(RoundedCornerShape(
                    topStart    = if (isUser) 16.dp else 4.dp,
                    topEnd      = if (isUser) 4.dp  else 16.dp,
                    bottomStart = 16.dp, bottomEnd = 16.dp
                ))
                .background(if (isUser) UserBubbleBg else AiBubbleBg)
                .border(1.dp, if (isUser) Purple.copy(alpha = 0.5f) else GlassBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(message.content, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

// ── Chat input row ─────────────────────────────────────────────────────────────
@Composable
private fun ChatInputRow(
    text: String,
    onTextChange: (String) -> Unit,
    isLoading: Boolean,
    hasApiKey: Boolean,
    onSend: () -> Unit,
    onApiKeyClick: () -> Unit,
    onMicClick: () -> Unit
) {
    if (!hasApiKey) {
        ApiKeyButton(onClick = onApiKeyClick)
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Nói gì đó với Aura...", color = TextMuted, fontSize = 14.sp) },
            modifier = Modifier.weight(1f),
            maxLines = 4,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Purple,
                unfocusedBorderColor    = GlassBorder,
                focusedTextColor        = Color.White,
                unfocusedTextColor      = Color.White,
                focusedContainerColor   = Color(0x28000000),
                unfocusedContainerColor = Color(0x1A000000)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() })
        )

        val isTyping = text.isNotBlank()
        Box(
            modifier = Modifier
                .size(52.dp).clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        if (isTyping || isLoading) listOf(Purple, Primary)
                        else listOf(GlassSurface, GlassSurface)
                    )
                )
                .clickable(enabled = !isLoading) {
                    if (isTyping) onSend() else onMicClick()
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp
                )
                isTyping  -> Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send",
                    tint = Color.White, modifier = Modifier.size(20.dp))
                else      -> Icon(Icons.Rounded.Mic, contentDescription = "Mic",
                    tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

// ── Record start button ────────────────────────────────────────────────────────
@Composable
private fun RecordStartButton(
    label: String,
    hasApiKey: Boolean,
    onApiKeyClick: () -> Unit,
    onRecord: () -> Unit
) {
    if (!hasApiKey) { ApiKeyButton(onClick = onApiKeyClick); return }

    Button(
        onClick = onRecord,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RecordRed.copy(alpha = 0.18f))
    ) {
        Icon(Icons.Rounded.FiberManualRecord, contentDescription = null,
            tint = RecordRed, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

// ── Recording active panel ─────────────────────────────────────────────────────
@Composable
private fun RecordingActivePanel(seconds: Int, onStop: () -> Unit, onCancel: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "rec-pulse")
    val dotScale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    val min = seconds / 60
    val sec = seconds % 60

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RecordRed.copy(alpha = 0.12f))
            .border(1.dp, RecordRed.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Rounded.FiberManualRecord,
                    contentDescription = null,
                    tint = RecordRed,
                    modifier = Modifier.size(12.dp).scale(dotScale)
                )
                Text(
                    "REC  %d:%02d".format(min, sec),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(GlassSurface).clickable(onClick = onCancel),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Cancel",
                        tint = TextMuted, modifier = Modifier.size(18.dp))
                }
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(RecordRed).clickable(onClick = onStop),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Stop, contentDescription = "Stop",
                        tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ── Processing panel ───────────────────────────────────────────────────────────
@Composable
private fun ProcessingPanel(label: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp), color = Purple, strokeWidth = 2.dp
            )
            Text(label, color = TextMuted, fontSize = 14.sp)
        }
    }
}

// ── Shared API key prompt button ───────────────────────────────────────────────
@Composable
private fun ApiKeyButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GlassSurface)
    ) {
        Icon(Icons.Rounded.AutoAwesome, contentDescription = null,
            tint = Purple, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("NHẬP API KEY", color = Color.White, fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp, fontSize = 13.sp)
    }
}

// ── API Key dialog ─────────────────────────────────────────────────────────────
@Composable
private fun ApiKeyDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var keyText by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF12102A),
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✦", fontSize = 32.sp, color = Purple)
                Spacer(Modifier.height(8.dp))
                Text("Kết Nối Aura", color = Purple, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Nhập Google Gemini API Key để kích hoạt",
                    color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("AIzaSy...", color = TextMuted, fontSize = 12.sp) },
                    placeholder = { Text("Dán Gemini API Key vào đây", color = TextMuted) },
                    visualTransformation = if (showKey) VisualTransformation.None
                                          else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Purple,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White
                    )
                )
                Text(
                    if (showKey) "Ẩn key" else "Hiện key",
                    color = Primary, fontSize = 11.sp,
                    modifier = Modifier.clickable { showKey = !showKey }
                )
                Text(
                    "Lấy key tại aistudio.google.com → Get API key.\nKey chỉ lưu cục bộ trên thiết bị.",
                    color = TextMuted, fontSize = 10.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (keyText.isNotBlank()) onConfirm(keyText) },
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(10.dp)
            ) { Text("KÍCH HOẠT", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ĐỂ SAU", color = TextMuted) }
        }
    )
}
