package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Brand & Accent Colors
val MidnightNavy = Color(0xFF090D16)
val DarkCardSurface = Color(0xFF121826)
val DarkSoftSurface = Color(0xFF1A2234)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextMuted = Color(0xFF94A3B8)

val LightBackground = Color(0xFFF8FAFC)
val LightCardSurface = Color(0xFFFFFFFF)
val LightSoftSurface = Color(0xFFF1F5F9)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextMuted = Color(0xFF64748B)

val ElectricBlue = Color(0xFF3B82F6)
val VibrantCyan = Color(0xFF06B6D4)

// Subscription Status Colors
val StatusActiveGreen = Color(0xFF10B981)
val StatusExpiringAmber = Color(0xFFF59E0B)
val StatusExpiredRed = Color(0xFFEF4444)

@Immutable
data class SuiviExtendedColors(
  val background: Color,
  val cardSurface: Color,
  val softSurface: Color,
  val textPrimary: Color,
  val textMuted: Color,
  val accentBlue: Color,
  val accentCyan: Color,
  val statusActive: Color,
  val statusExpiring: Color,
  val statusExpired: Color,
  val subtleBorder: Color,
  val isDark: Boolean,
)

val DarkSuiviColors =
  SuiviExtendedColors(
    background = MidnightNavy,
    cardSurface = DarkCardSurface,
    softSurface = DarkSoftSurface,
    textPrimary = DarkTextPrimary,
    textMuted = DarkTextMuted,
    accentBlue = ElectricBlue,
    accentCyan = VibrantCyan,
    statusActive = StatusActiveGreen,
    statusExpiring = StatusExpiringAmber,
    statusExpired = StatusExpiredRed,
    subtleBorder = Color(0xFF1E293B),
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
    statusActive = StatusActiveGreen,
    statusExpiring = StatusExpiringAmber,
    statusExpired = StatusExpiredRed,
    subtleBorder = Color(0xFFE2E8F0),
    isDark = false,
  )

val LocalSuiviColors = staticCompositionLocalOf { DarkSuiviColors }
