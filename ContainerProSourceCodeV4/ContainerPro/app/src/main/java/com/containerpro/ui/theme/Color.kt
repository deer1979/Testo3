package com.containerpro.ui.theme

import androidx.compose.ui.graphics.Color

// ── Emerald ──────────────────────────────────────────────
val Emerald10 = Color(0xFF002114)
val Emerald20 = Color(0xFF003823)
val Emerald30 = Color(0xFF005234)
val Emerald40 = Color(0xFF1B7A52)
val Emerald60 = Color(0xFF4EC48C)
val Emerald80 = Color(0xFF93F0C3)
val Emerald90 = Color(0xFFBEF8DC)
val Emerald99 = Color(0xFFF2FFF8)

// ── Amber ─────────────────────────────────────────────────
val Amber20 = Color(0xFF3D2000)
val Amber30 = Color(0xFF683C00)
val Amber40 = Color(0xFF8B5300)
val Amber70 = Color(0xFFF59C1A)
val Amber80 = Color(0xFFFFB84D)
val Amber90 = Color(0xFFFFD899)

// ── Coral / Error ─────────────────────────────────────────
val Coral10 = Color(0xFF1A0100)
val Coral30 = Color(0xFF5C1410)
val Coral40 = Color(0xFFB53A2F)
val Coral80 = Color(0xFFFF8D80)
val Coral90 = Color(0xFFFFDAD6)

// ── Violet ────────────────────────────────────────────────
val Violet20 = Color(0xFF150B2B)
val Violet30 = Color(0xFF2D1F45)
val Violet40 = Color(0xFF7C43BD)
val Violet80 = Color(0xFFD0BCFF)
val Violet90 = Color(0xFFEADDFF)

// ── Teal ──────────────────────────────────────────────────
val Teal20 = Color(0xFF001F20)
val Teal40 = Color(0xFF00696B)
val Teal80 = Color(0xFF80D4D6)
val Teal90 = Color(0xFFB2EBEC)

// ── Neutral (dark green-tinted) ───────────────────────────
val Neutral10 = Color(0xFF0D1511)
val Neutral15 = Color(0xFF131C17)
val Neutral20 = Color(0xFF1A241E)
val Neutral90 = Color(0xFFBDD3CA)
val Neutral99 = Color(0xFFF2FFF8)

// ── NeutralVariant ────────────────────────────────────────
val NeutralVar20 = Color(0xFF1C2620)
val NeutralVar30 = Color(0xFF2B3630)
val NeutralVar40 = Color(0xFF3E4F48)
val NeutralVar80 = Color(0xFFA4B6AF)

// ── Status ────────────────────────────────────────────────
val SuccessGreen = Color(0xFF4CAF50)
val WarningAmber = Color(0xFFFFB300)
val ErrorCoral   = Color(0xFFEF5350)

// ── Card backgrounds — DARK theme ─────────────────────────
val CardEmeraldBgDark = Color(0xFF002E1B); val CardEmeraldBdDark = Color(0x404EC48C)
val CardAmberBgDark   = Color(0xFF2B1600); val CardAmberBdDark   = Color(0x40F59C1A)
val CardCoralBgDark   = Color(0xFF2B0D0B); val CardCoralBdDark   = Color(0x33FF8D80)
val CardVioletBgDark  = Color(0xFF1E1030); val CardVioletBdDark  = Color(0x33D0BCFF)
val CardTealBgDark    = Color(0xFF001F20); val CardTealBdDark    = Color(0x3380D4D6)

// ── Card backgrounds — LIGHT theme ────────────────────────
val CardEmeraldBgLight = Color(0xFFBEF8DC); val CardEmeraldBdLight = Color(0x4D1B7A52)
val CardAmberBgLight   = Color(0xFFFFD899); val CardAmberBdLight   = Color(0x408B5300)
val CardCoralBgLight   = Color(0xFFFFDAD6); val CardCoralBdLight   = Color(0x33B53A2F)
val CardVioletBgLight  = Color(0xFFEADDFF); val CardVioletBdLight  = Color(0x337C43BD)
val CardTealBgLight    = Color(0xFFB2EBEC); val CardTealBdLight    = Color(0x3300696B)

/** Convenience class passed to themed card components. */
data class CardPalette(
    val bg: Color,
    val border: Color,
    val title: Color,
    val sub: Color,
    val iconBg: Color,
)
