package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.i18n.AppLanguage

private val SuiviDarkColorScheme =
  darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = DarkSoftSurface,
    onPrimaryContainer = DarkTextPrimary,
    secondary = VibrantCyan,
    onSecondary = MidnightNavy,
    secondaryContainer = DarkSoftSurface,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = StatusActiveGreen,
    onTertiary = MidnightNavy,
    background = MidnightNavy,
    onBackground = DarkTextPrimary,
    surface = DarkCardSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSoftSurface,
    onSurfaceVariant = DarkTextMuted,
    outline = Color(0xFF283548),
    error = StatusExpiredRed,
    onError = Color.White,
  )

private val SuiviLightColorScheme =
  lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = VibrantCyan,
    onSecondary = Color.White,
    secondaryContainer = LightSoftSurface,
    onSecondaryContainer = LightTextPrimary,
    tertiary = StatusActiveGreen,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightCardSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSoftSurface,
    onSurfaceVariant = LightTextMuted,
    outline = Color(0xFFCBD5E1),
    error = RedInkLight,
    onError = Color.White,
  )

private val SuiviShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun SuiviCoachTheme(
  darkTheme: Boolean = true,
  language: AppLanguage = AppLanguage.FR,
  content: @Composable () -> Unit,
) {
  val extendedColors = if (darkTheme) DarkSuiviColors else LightSuiviColors
  val colorScheme = if (darkTheme) SuiviDarkColorScheme else SuiviLightColorScheme
  val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
  val typography = buildSuiviTypography(isArabic = language == AppLanguage.AR)

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  CompositionLocalProvider(
    LocalSuiviColors provides extendedColors,
    LocalLayoutDirection provides layoutDirection,
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = typography,
      shapes = SuiviShapes,
      content = content,
    )
  }
}

object SuiviTheme {
  val colors: SuiviExtendedColors
    @Composable get() = LocalSuiviColors.current
}
