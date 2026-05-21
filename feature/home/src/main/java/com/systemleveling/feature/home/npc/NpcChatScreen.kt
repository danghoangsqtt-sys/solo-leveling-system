package com.systemleveling.feature.home.npc

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.systemleveling.feature.home.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.core.ai.ChatMessage
import com.systemleveling.core.ai.MessageRole
import kotlinx.coroutines.launch

private val BgDeep    = Color(0xFF060612)
private val BgMid     = Color(0xFF0E0E20)
private val Primary   = Color(0xFF4A9EFF)
private val Gold      = Color(0xFFFFD700)
private val Purple    = Color(0xFFB48EFF)
private val PurpleDim = Color(0xFF7A5EBD)
private val GlassBorder = Color(0x1FFFFFFF)
private val GlassSurface = Color(0x991E1E2F)
private val TextMuted   = Color(0xFFC0C7D4)
private val Green       = Color(0xFF2ED573)

private val QuickPrompts = listOf(
    "Phân tích chỉ số của ta",
    "Nhiệm vụ nào quan trọng nhất?",
    "Lời khuyên luyện tập hôm nay",
    "Cách tăng EXP nhanh nhất",
    "Đánh giá tiến độ streak"
)

@Composable
fun NpcChatScreen(
    viewModel: NpcChatViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    LaunchedEffect(apiKey) {
        if (apiKey.isBlank() && messages.isEmpty()) showApiKeyDialog = true
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(
            onDismiss = { showApiKeyDialog = false },
            onConfirm = { key ->
                viewModel.saveApiKey(key)
                showApiKeyDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF12102A), BgDeep, BgMid))
            )
    ) {
        // Character layer
        AuraCharacterView()

        Column(modifier = Modifier.fillMaxSize()) {

            AuraHeader(
                messageCount = messages.size,
                hasKey = apiKey.isNotBlank(),
                onBack = onBack,
                onSettings = { showApiKeyDialog = true },
                onClear = { viewModel.clearHistory() }
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 180.dp, bottom = 16.dp) // Leave space for character
            ) {
                if (messages.isEmpty()) {
                    item {
                        QuickPromptsRow { prompt ->
                            viewModel.sendMessage(prompt)
                        }
                    }
                }
                items(messages) { msg ->
                    MessageBubble(msg)
                }
                if (isLoading) {
                    item { TypingIndicator() }
                }
            }

            error?.let { err ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFF6B6B).copy(0.12f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(err, color = Color(0xFFFF6B6B), fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text(
                            "✕", color = Color(0xFFFF6B6B), fontSize = 14.sp,
                            modifier = Modifier.clickable { viewModel.clearError() }
                        )
                    }
                }
            }

            ChatInputBar(
                value = inputText,
                isLoading = isLoading,
                onValueChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                        scope.launch { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size) }
                    }
                }
            )
        }
    }
}

@Composable
private fun AuraHeader(
    messageCount: Int,
    hasKey: Boolean,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onClear: () -> Unit
) {
    val glow by rememberInfiniteTransition(label = "hdr").animateFloat(
        0.5f, 1f,
        infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xBB060612))
            .border(BorderStroke(0.5.dp, Purple.copy(0.15f)))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GlassSurface.copy(0.4f))
                    .border(0.5.dp, GlassBorder, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text("◀", color = Primary, fontSize = 14.sp)
            }

            // Center: Aura identity
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Animated orb
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(Purple.copy(glow * 0.6f), Color(0xFF1A0A2E)))
                            )
                            .border(1.dp, Purple.copy(glow), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text("✦", fontSize = 12.sp, color = Purple.copy(glow)) }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "AURA",
                        color = Purple,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.15f.em
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (hasKey) Green else Color(0xFFFF6B6B))
                    )
                    Text(
                        if (hasKey) "Đang hoạt động · gemini-2.0-flash" else "Chưa kết nối",
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                    if (messageCount > 0) {
                        Text("·", color = TextMuted, fontSize = 9.sp)
                        Text("$messageCount tin", color = TextMuted, fontSize = 9.sp)
                    }
                }
            }

            // Right actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (messageCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GlassSurface.copy(0.4f))
                            .border(0.5.dp, GlassBorder, CircleShape)
                            .clickable { onClear() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🗑", fontSize = 14.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GlassSurface.copy(0.4f))
                        .border(0.5.dp, GlassBorder, CircleShape)
                        .clickable { onSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚙", color = TextMuted, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun AuraCharacterView() {
    val breath by rememberInfiniteTransition(label = "breath").animateFloat(
        0.98f, 1.02f,
        infiniteRepeatable(tween(2500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "br"
    )
    val glow by rememberInfiniteTransition(label = "glow_char").animateFloat(
        0.2f, 0.6f,
        infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "gc"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Back glow
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(y = 40.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Purple.copy(glow), Color.Transparent)
                    )
                )
        )
        
        // Character Image
        Image(
            painter = painterResource(id = R.drawable.aura_npc),
            contentDescription = "Aura NPC",
            modifier = Modifier
                .padding(top = 60.dp)
                .height(280.dp)
                .scale(1f, breath) // Breathing effect
        )
        
        // Front fade to blend with chat
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xFF12102A))
                    )
                )
        )
    }
}

@Composable
private fun QuickPromptsRow(onPrompt: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(QuickPrompts) { prompt ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Purple.copy(0.08f))
                    .border(0.5.dp, Purple.copy(0.3f), RoundedCornerShape(20.dp))
                    .clickable { onPrompt(prompt) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    "✦  $prompt",
                    color = Purple.copy(0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage) {
    val isUser = msg.role == MessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Purple.copy(0.35f), Color(0xFF1A0A2E))))
                    .border(0.5.dp, Purple.copy(0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("✦", fontSize = 12.sp, color = Purple) }
            Spacer(Modifier.width(8.dp))
        }
        Box(
            modifier = Modifier
                .widthIn(max = 285.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (isUser) 18.dp else 4.dp,
                        topEnd = if (isUser) 4.dp else 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    )
                )
                .background(
                    if (isUser)
                        Brush.linearGradient(listOf(Primary.copy(0.22f), Primary.copy(0.12f)))
                    else
                        Brush.linearGradient(listOf(Purple.copy(0.14f), GlassSurface.copy(0.5f)))
                )
                .border(
                    0.5.dp,
                    if (isUser) Primary.copy(0.3f) else Purple.copy(0.22f),
                    RoundedCornerShape(
                        topStart = if (isUser) 18.dp else 4.dp,
                        topEnd = if (isUser) 4.dp else 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = msg.content,
                color = if (isUser) Color.White else Color(0xFFEEE8FF),
                fontSize = 13.sp,
                lineHeight = 1.55.em
            )
        }
        if (isUser) Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun TypingIndicator() {
    val phase by rememberInfiniteTransition(label = "typing").animateFloat(
        0f, 3f,
        infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )
    Row(verticalAlignment = Alignment.Bottom) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Purple.copy(0.12f))
                .border(0.5.dp, Purple.copy(0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Text("✦", fontSize = 12.sp, color = Purple) }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Purple.copy(0.1f))
                .border(0.5.dp, Purple.copy(0.2f), RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { i ->
                    val alpha = if (phase.toInt() == i) 1f else 0.3f
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Purple.copy(alpha))
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    isLoading: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val canSend = value.isNotBlank() && !isLoading
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xEE060612))
            .border(BorderStroke(0.5.dp, Purple.copy(0.12f)))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Nhắn tin với Aura...", color = TextMuted.copy(0.6f), fontSize = 13.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple.copy(0.45f),
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Purple,
                focusedContainerColor = GlassSurface.copy(0.3f),
                unfocusedContainerColor = GlassSurface.copy(0.15f)
            ),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    if (canSend)
                        Brush.radialGradient(listOf(Purple, Color(0xFF6A3FA0)))
                    else
                        Brush.radialGradient(listOf(GlassSurface.copy(0.5f), GlassSurface.copy(0.3f)))
                )
                .border(1.dp, if (canSend) Purple.copy(0.6f) else GlassBorder, CircleShape)
                .clickable(enabled = canSend) { onSend() },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Text("✦", fontSize = 16.sp, color = Purple)
            } else {
                Text("▶", color = if (canSend) Color.White else TextMuted.copy(0.3f), fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ApiKeyDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var keyText by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF12102A),
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("✦", fontSize = 32.sp, color = Purple)
                Spacer(Modifier.height(8.dp))
                Text("Kết Nối Aura", color = Purple, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(
                    "Nhập Google Gemini API Key để kích hoạt",
                    color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("AIzaSy...", color = TextMuted, fontSize = 12.sp) },
                    placeholder = { Text("Dán Gemini API Key vào đây", color = TextMuted) },
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Text(
                    if (showKey) "🙈 Ẩn key" else "👁 Hiện key",
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
