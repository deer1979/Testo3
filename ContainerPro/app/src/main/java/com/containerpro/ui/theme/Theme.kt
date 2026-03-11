package com.containerpro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// ── Dark color scheme ────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary            = Emerald60,
    onPrimary          = Emerald10,
    primaryContainer   = Emerald30,
    onPrimaryContainer = Emerald90,
    secondary          = Amber80,
    onSecondary        = Amber20,
    secondaryContainer = Amber30,
    onSecondaryContainer = Amber90,
    tertiary           = Violet80,
    onTertiary         = Violet20,
    tertiaryContainer  = Violet30,
    onTertiaryContainer = Violet90,
    error              = Coral80,
    onError            = Coral10,
    errorContainer     = Coral30,
    onErrorContainer   = Coral90,
    background         = Neutral10,
    onBackground       = Neutral90,
    surface            = Neutral15,
    onSurface          = Neutral90,
    surfaceVariant     = NeutralVar20,
    onSurfaceVariant   = NeutralVar80,
    outline            = NeutralVar30,
    outlineVariant     = NeutralVar40,
    inverseSurface     = Neutral90,
    inverseOnSurface   = Neutral20,
    inversePrimary     = Emerald40,
)

// ── Light color scheme ───────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary            = Emerald40,
    onPrimary          = Emerald99,
    primaryContainer   = Emerald90,
    onPrimaryContainer = Emerald10,
    secondary          = Amber40,
    onSecondary        = Color.White,
    secondaryContainer = Amber90,
    onSecondaryContainer = Amber20,
    tertiary           = Violet40,
    onTertiary         = Color.White,
    tertiaryContainer  = Violet90,
    onTertiaryContainer = Violet20,
    error              = Coral40,
    onError            = Color.White,
    errorContainer     = Coral90,
    onErrorContainer   = Coral30,
    background         = Emerald99,
    onBackground       = Neutral20,
    surface            = Color(0xFFEBF7F0),
    onSurface          = Neutral20,
    surfaceVariant     = Emerald95,
    onSurfaceVariant   = NeutralVar40,
    outline            = Neutral90,
    outlineVariant     = Neutral80,
    inverseSurface     = Neutral20,
    inverseOnSurface   = Neutral90,
    inversePrimary     = Emerald80,
)

// ── Theme preference enum ────────────────────────────────
enum class AppTheme { DARK, LIGHT, AUTO }

// ── CompositionLocal for theme state ─────────────────────
val LocalAppTheme = compositionLocalOf { AppTheme.DARK }

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
    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = AppTypography,
            content     = content,
        )
    }
}

// ── Card color helpers (theme-aware) ─────────────────────
data class CardColors(val bg: Color, val border: Color, val title: Color, val sub: Color, val icon: Color)

@Composable
fun emeraldCard(): CardColors {
    val dark = LocalAppTheme.current != AppTheme.LIGHT
    return CardColors(
        bg     = if (dark) CardEmeraldBg else CardEmeraldBgLight,
        border = if (dark) CardEmeraldBd else CardEmeraldBdLight,
        title  = if (dark) Emerald90    else Emerald10,
        sub    = if (dark) Emerald80    else Emerald30,
        icon   = Emerald60.copy(alpha = 0.15f),
    )
}

@Composable
fun amberCard(): CardColors {
    val dark = LocalAppTheme.current != AppTheme.LIGHT
    return CardColors(
        bg     = if (dark) CardAmberBg else CardAmberBgLight,
        border = if (dark) CardAmberBd else CardAmberBdLight,
        title  = if (dark) Amber90     else Amber20,
        sub    = if (dark) Amber80     else Amber30,
        icon   = Amber70.copy(alpha = 0.15f),
    )
}

@Composable
fun coralCard(): CardColors {
    val dark = LocalAppTheme.current != AppTheme.LIGHT
    return CardColors(
        bg     = if (dark) CardCoralBg else CardCoralBgLight,
        border = if (dark) CardCoralBd else CardCoralBdLight,
        title  = if (dark) Coral90     else Coral30,
        sub    = if (dark) Coral80     else Coral40,
        icon   = Coral80.copy(alpha = 0.12f),
    )
}

@Composable
fun violetCard(): CardColors {
    val dark = LocalAppTheme.current != AppTheme.LIGHT
    return CardColors(
        bg     = if (dark) CardVioletBg else CardVioletBgLight,
        border = if (dark) CardVioletBd else CardVioletBdLight,
        title  = if (dark) Violet90     else Violet20,
        sub    = if (dark) Violet80     else Violet30,
        icon   = Violet80.copy(alpha = 0.12f),
    )
}

@Composable
fun tealCard(): CardColors {
    val dark = LocalAppTheme.current != AppTheme.LIGHT
    return CardColors(
        bg     = if (dark) CardTealBg else CardTealBgLight,
        border = if (dark) CardTealBd else CardTealBdLight,
        title  = if (dark) Teal90     else Teal40,
        sub    = if (dark) Teal80     else Teal40,
        icon   = Teal60.copy(alpha = 0.12f),
    )
}
