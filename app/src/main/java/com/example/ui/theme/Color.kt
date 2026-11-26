package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Astryx VIP Brand & Obsidian Surfaces
val ObsidianBlack = Color(0xFF07090E)
val CarbonCardSurface = Color(0xFF0D1117)
val CarbonSoftSurface = Color(0xFF161F2E)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextMuted = Color(0xFF94A3B8)

val LightBackground = Color(0xFFF8FAFC)
val LightCardSurface = Color(0xFFFFFFFF)
val LightSoftSurface = Color(0xFFF1F5F9)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextMuted = Color(0xFF5B6779)

// Astryx VIP Accents & Gradients
val LiquidGold = Color(0xFFD4AF37)
val ElectricEmerald = Color(0xFF10B981)
val NeonIndigo = Color(0xFF6366F1)
val ElectricBlue = Color(0xFF3B82F6)
val VibrantCyan = Color(0xFF06B6D4)

// Status fills
val StatusActiveGreen = Color(0xFF10B981)
val StatusExpiringAmber = Color(0xFFF59E0B)
val StatusExpiredRed = Color(0xFFEF4444)

// Light-mode ink
val BlueInkLight = Color(0xFF1D4ED8)
val CyanInkLight = Color(0xFF0E7490)
val GreenInkLight = Color(0xFF047857)
val AmberInkLight = Color(0xFFB45309)
val RedInkLight = Color(0xFFB91C1C)

@Immutable
data class SuiviExtendedColors(
  val background: Color,
  val cardSurface: Color,
  val softSurface: Color,
  val textPrimary: Color,
  val textMuted: Color,
  val accentBlue: Color,
  val accentCyan: Color,
  val accentGold: Color,
  val accentInk: Color,
  val statusActive: Color,
  val statusExpiring: Color,
  val statusExpired: Color,
  val statusActiveInk: Color,
  val statusExpiringInk: Color,
  val statusExpiredInk: Color,
  val subtleBorder: Color,
  val isDark: Boolean,
)

val DarkSuiviColors =
  SuiviExtendedColors(
    background = ObsidianBlack,
    cardSurface = CarbonCardSurface,
    softSurface = CarbonSoftSurface,
    textPrimary = DarkTextPrimary,
    textMuted = DarkTextMuted,
    accentBlue = ElectricBlue,
    accentCyan = VibrantCyan,
    accentGold = LiquidGold,
    accentInk = VibrantCyan,
    statusActive = StatusActiveGreen,
    statusExpiring = StatusExpiringAmber,
    statusExpired = StatusExpiredRed,
    statusActiveInk = StatusActiveGreen,
    statusExpiringInk = StatusExpiringAmber,
    statusExpiredInk = StatusExpiredRed,
    subtleBorder = Color(0x26FFFFFF), // Specular highlight border
    isDark = true,
  )

val LightSuiviColors =
  SuiviExtendedColors(
    background = LightBackground,
    cardSurface = LightCardSurface,
    softSurface = LightSoftSurface,
    textPrimary = LightTextPrimary,
    textMuted = LightTextMuted,
    accentBlue = ElectricBlue,
    accentCyan = VibrantCyan,
    accentGold = Color(0xFFB45309),
    accentInk = CyanInkLight,
    statusActive = StatusActiveGreen,
    statusExpiring = StatusExpiringAmber,
    statusExpired = StatusExpiredRed,
    statusActiveInk = GreenInkLight,
    statusExpiringInk = AmberInkLight,
    statusExpiredInk = RedInkLight,
    subtleBorder = Color(0xFFE2E8F0),
    isDark = false,
  )

val LocalSuiviColors = staticCompositionLocalOf { DarkSuiviColors }
