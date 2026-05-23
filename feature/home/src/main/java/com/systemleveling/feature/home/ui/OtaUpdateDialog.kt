package com.systemleveling.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.systemleveling.core.ota.OtaUpdateInfo

private val BG_DEEP      = Color(0xFF0A0A1A)
private val PRIMARY      = Color(0xFF4A9EFF)
private val PRIMARY_DIM  = Color(0xFFA4C9FF)
private val GOLD         = Color(0xFFFFD700)
private val GLASS_BORDER = Color(0x1FFFFFFF)
private val TEXT_MUTED   = Color(0xFFC0C7D4)

@Composable
fun OtaUpdateDialog(
    info: OtaUpdateInfo,
    isDownloading: Boolean,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        containerColor = BG_DEEP,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GOLD.copy(alpha = 0.12f))
                            .border(0.5.dp, GOLD.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "SYSTEM UPDATE",
                            color = GOLD, fontSize = 10.sp,
                            fontWeight = FontWeight.Black, letterSpacing = 0.1f.em
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("v${info.versionCode}", color = TEXT_MUTED, fontSize = 12.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    info.displayName.ifBlank { "Bản cập nhật mới" },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                HorizontalDivider(color = GLASS_BORDER)
                Spacer(Modifier.height(10.dp))

                if (info.releaseNotes.isNotBlank()) {
                    Text(
                        "THAY ĐỔI",
                        color = PRIMARY_DIM, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.1f.em
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0x0AFFFFFF), Color(0x05FFFFFF))
                                )
                            )
                            .border(0.5.dp, GLASS_BORDER, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            info.releaseNotes,
                            color = TEXT_MUTED,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }

                if (isDownloading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = PRIMARY,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Đang tải xuống...", color = TEXT_MUTED, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    if (info.downloadUrl != null)
                        "Tải xuống và cài đặt tự động trên thiết bị của bạn."
                    else
                        "Không tìm thấy file APK trong release. Tải thủ công từ GitHub.",
                    color = TEXT_MUTED.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onUpdate,
                enabled = !isDownloading && info.downloadUrl != null
            ) {
                Text(
                    if (isDownloading) "Đang tải..." else "Cập nhật ngay",
                    color = if (isDownloading) TEXT_MUTED else GOLD,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            if (!isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text("Để sau", color = TEXT_MUTED)
                }
            }
        }
    )
}
