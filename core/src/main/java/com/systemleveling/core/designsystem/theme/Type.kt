package com.systemleveling.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.systemleveling.core.R

// Provider wrapped in runCatching — falls back to SansSerif if GMS not available.
private val fontProvider: GoogleFont.Provider? = runCatching {
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )
}.getOrNull()

val RajdhaniFamily: FontFamily = fontProvider?.let { p ->
    FontFamily(
        Font(googleFont = GoogleFont("Rajdhani"), fontProvider = p, weight = FontWeight.SemiBold),
        Font(googleFont = GoogleFont("Rajdhani"), fontProvider = p, weight = FontWeight.Bold),
    )
} ?: FontFamily.SansSerif

val InterFamily: FontFamily = fontProvider?.let { p ->
    FontFamily(
        Font(googleFont = GoogleFont("Inter"), fontProvider = p, weight = FontWeight.Normal),
        Font(googleFont = GoogleFont("Inter"), fontProvider = p, weight = FontWeight.Medium),
        Font(googleFont = GoogleFont("Inter"), fontProvider = p, weight = FontWeight.SemiBold),
        Font(googleFont = GoogleFont("Inter"), fontProvider = p, weight = FontWeight.Bold),
    )
} ?: FontFamily.SansSerif

// System Leveling Typography
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = RajdhaniFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = 2.4.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = RajdhaniFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.48.sp
    ),
    titleLarge = TextStyle(
        fontFamily = RajdhaniFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.4.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = RajdhaniFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.2.sp
    )
)
