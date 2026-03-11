package com.containerpro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// ── Theme preference ──────────────────────────────────────
enum class AppTheme { DARK, LIGHT, AUTO }

val LocalAppTheme = compositionLocalOf { AppTheme.DARK }

// ── Dark color scheme ─────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = Emerald60,
    onPrimary            = Emerald10,
    primaryContainer     = Emerald30,
    onPrimaryContainer   = Emerald90,
    secondary            = Amber80,
    onSecondary          = Amber20,
    secondaryContainer   = Amber30,
    onSecondaryContainer = Amber90,
    tertiary             = Violet80,
    onTertiary           = Violet20,
    tertiaryContainer    = Violet30,
    onTertiaryContainer  = Violet90,
    error                = Coral80,
    onError              = Coral10,
    errorContainer       = Coral30,
    onErrorContainer     = Coral90,
    background           = Neutral10,
    onBackground         = Neutral90,
    surface              = Neutral15,
    onSurface            = Neutral90,
    surfaceVariant       = NeutralVar20,
    onSurfaceVariant     = NeutralVar80,
    outline              = NeutralVar30,
    outlineVariant       = NeutralVar40,
)

// ── Light color scheme ────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary              = Emerald40,
    onPrimary            = Color.White,
    primaryContainer     = Emerald90,
    onPrimaryContainer   = Emerald10,
    secondary            = Amber40,
    onSecondary          = Color.White,
    secondaryContainer   = Amber90,
    onSecondaryContainer = Amber20,
    tertiary             = Violet40,
    onTertiary           = Color.White,
    tertiaryContainer    = Violet90,
    onTertiaryContainer  = Violet20,
    error                = Coral40,
    onError              = Color.White,
    errorContainer       = Coral90,
    onErrorContainer     = Coral30,
    background           = Emerald99,
    onBackground         = Neutral20,
    surface              = Color(0xFFEBF7F0),
    onSurface            = Neutral20,
    surfaceVariant       = Color(0xFFDEFDF0),
    onSurfaceVariant     = NeutralVar40,
    outline              = Neutral90,
    outlineVariant       = Neutral90,
)

// ── Theme composable ──────────────────────────────────────
@Composable
fun ContainerProTheme(
    appTheme: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit,
) {
    val useDark = when (appTheme) {
        AppTheme.DARK  -> true
        AppTheme.LIGHT -> false
        AppTheme.AUTO  -> isSystemInDarkTheme()
    }
    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = if (useDark) DarkColorScheme else LightColorScheme,
            typography  = AppTypography,
            content     = content,
        )
    }
}

// ── Card palette helpers (Composable scope required) ──────
private val isDark @Composable get() = LocalAppTheme.current != AppTheme.LIGHT

@Composable fun emeraldPalette() = CardPalette(
    bg      = if (isDark) CardEmeraldBgDark else CardEmeraldBgLight,
    border  = if (isDark) CardEmeraldBdDark else CardEmeraldBdLight,
    title   = if (isDark) Emerald90         else Emerald10,
    sub     = if (isDark) Emerald80         else Emerald30,
    iconBg  = Emerald60.copy(alpha = 0.15f),
)

@Composable fun amberPalette() = CardPalette(
    bg      = if (isDark) CardAmberBgDark else CardAmberBgLight,
    border  = if (isDark) CardAmberBdDark else CardAmberBdLight,
    title   = if (isDark) Amber90         else Amber20,
    sub     = if (isDark) Amber80         else Amber30,
    iconBg  = Amber70.copy(alpha = 0.15f),
)

@Composable fun tealPalette() = CardPalette(
    bg      = if (isDark) CardTealBgDark else CardTealBgLight,
    border  = if (isDark) CardTealBdDark else CardTealBdLight,
    title   = if (isDark) Teal90         else Teal40,
    sub     = if (isDark) Teal80         else Teal40,
    iconBg  = Teal80.copy(alpha = 0.12f),
)

@Composable fun violetPalette() = CardPalette(
    bg      = if (isDark) CardVioletBgDark else CardVioletBgLight,
    border  = if (isDark) CardVioletBdDark else CardVioletBdLight,
    title   = if (isDark) Violet90         else Violet20,
    sub     = if (isDark) Violet80         else Violet30,
    iconBg  = Violet80.copy(alpha = 0.12f),
)

@Composable fun coralPalette() = CardPalette(
    bg      = if (isDark) CardCoralBgDark else CardCoralBgLight,
    border  = if (isDark) CardCoralBdDark else CardCoralBdLight,
    title   = if (isDark) Coral90         else Coral30,
    sub     = if (isDark) Coral80         else Coral40,
    iconBg  = Coral80.copy(alpha = 0.12f),
)
