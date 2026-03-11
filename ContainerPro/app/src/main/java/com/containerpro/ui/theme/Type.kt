package com.containerpro.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Google Sans — download from fonts.google.com (or use bundled in res/font/)
// JetBrains Mono — download from jetbrains.com/lp/mono/
// Fallback to system sans-serif if fonts not present in res/font/
val GoogleSans = FontFamily.Default   // replace with FontFamily(Font(R.font.google_sans_regular), ...) once fonts added
val JetBrainsMono = FontFamily.Monospace

val containerNumberStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Bold,
    fontSize   = 24.sp,
    letterSpacing = 2.sp,
)

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Bold,   fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium= TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Bold,   fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Bold,   fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge= TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Bold,   fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium=TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold,fontSize= 28.sp, lineHeight = 36.sp),
    headlineSmall= TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold,fontSize= 24.sp, lineHeight = 32.sp),
    titleLarge   = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Bold,   fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold,fontSize= 16.sp, lineHeight = 24.sp),
    titleSmall   = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold,fontSize= 14.sp, lineHeight = 20.sp),
    bodyLarge    = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium   = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall    = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge   = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Bold,   fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold,fontSize= 12.sp, lineHeight = 16.sp),
    labelSmall   = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold,fontSize= 11.sp, lineHeight = 16.sp,letterSpacing = 0.5.sp),
)
