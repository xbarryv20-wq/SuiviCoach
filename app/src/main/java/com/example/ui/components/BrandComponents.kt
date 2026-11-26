package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.domain.model.SubscriptionStatus
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.formatDaysRemaining
import com.example.i18n.statusLabel
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.SuiviTheme
import com.example.ui.theme.VibrantCyan
import com.example.ui.theme.tabular

/**
 * Sharp, geometric athletic "S" integrated with a pulse/check line in Electric Blue (#3B82F6)
 * & Cyan (#06B6D4) inside a rounded dark badge (#090D16).
 */
@Composable
fun SuiviCoachLogo(
  size: Dp = 44.dp,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier.size(size)) {
    val w = this.size.width
    val h = this.size.height
    val corner = w * 0.26f

    // Rounded Midnight Navy badge (#090D16)
    drawRoundRect(
      color = MidnightNavy,
      cornerRadius = CornerRadius(corner, corner),
    )

    // Subtle glowing border highlight
    drawRoundRect(
      brush =
        Brush.linearGradient(
          colors = listOf(ElectricBlue.copy(alpha = 0.55f), VibrantCyan.copy(alpha = 0.35f)),
          start = Offset.Zero,
          end = Offset(w, h),
        ),
      cornerRadius = CornerRadius(corner, corner),
      style = Stroke(width = w * 0.035f),
    )

    // Geometric athletic "S"
    val sPath =
      Path().apply {
        moveTo(w * 0.68f, h * 0.26f)
        lineTo(w * 0.36f, h * 0.26f)
        lineTo(w * 0.26f, h * 0.38f)
        lineTo(w * 0.34f, h * 0.49f)
        lineTo(w * 0.64f, h * 0.49f)
        lineTo(w * 0.72f, h * 0.61f)
        lineTo(w * 0.60f, h * 0.74f)
        lineTo(w * 0.28f, h * 0.74f)
      }

    drawPath(
      path = sPath,
      brush =
        Brush.linearGradient(
          colors = listOf(ElectricBlue, VibrantCyan),
          start = Offset(w * 0.2f, h * 0.8f),
          end = Offset(w * 0.8f, h * 0.2f),
        ),
      style = Stroke(width = w * 0.115f, cap = StrokeCap.Square, join = StrokeJoin.Miter),
    )

    // Integrated athletic pulse / check accent cutting across top-right
    val pulsePath =
      Path().apply {
        moveTo(w * 0.48f, h * 0.36f)
        lineTo(w * 0.58f, h * 0.36f)
        lineTo(w * 0.65f, h * 0.20f)
        lineTo(w * 0.73f, h * 0.43f)
        lineTo(w * 0.82f, h * 0.28f)
      }

    drawPath(
      path = pulsePath,
      color = VibrantCyan,
      style = Stroke(width = w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
  }
}

@Composable
fun LanguageSwitcherPill(
  currentLanguage: AppLanguage,
  onSelect: (AppLanguage) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = SuiviTheme.colors
  Row(
    modifier =
      modifier
        .clip(RoundedCornerShape(50))
        .background(colors.softSurface)
        .padding(3.dp),
    horizontalArrangement = Arrangement.spacedBy(2.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    AppLanguage.entries.forEach { lang ->
      val selected = lang == currentLanguage
      Box(
        modifier =
          Modifier
            .clip(RoundedCornerShape(50))
            .background(
              if (selected) {
                Brush.horizontalGradient(listOf(colors.accentBlue, colors.accentCyan))
              } else {
                Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
              }
            )
            .clickable { onSelect(lang) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("lang_${lang.code}"),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = lang.label,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = if (selected) Color.White else colors.textMuted,
        )
      }
    }
  }
}

@Composable
fun ThemeToggleButton(
  isDarkMode: Boolean,
  onToggle: () -> Unit,
  contentDescription: String,
  modifier: Modifier = Modifier,
) {
  val colors = SuiviTheme.colors
  IconButton(
    onClick = onToggle,
    modifier =
      modifier
        .size(48.dp)
        .clip(CircleShape)
        .background(colors.softSurface)
        .testTag("theme_toggle_button"),
  ) {
    Icon(
      imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
      contentDescription = contentDescription,
      tint = if (isDarkMode) VibrantCyan else ElectricBlue,
      modifier = Modifier.size(20.dp),
    )
  }
}

@Composable
fun StatusBadge(
  status: SubscriptionStatus?,
  strings: AppStrings,
  modifier: Modifier = Modifier,
) {
  val colors = SuiviTheme.colors
  val tint =
    when (status) {
      SubscriptionStatus.ACTIVE -> colors.statusActive
      SubscriptionStatus.EXPIRING_SOON -> colors.statusExpiring
      SubscriptionStatus.EXPIRED -> colors.statusExpired
      null -> colors.textMuted
    }
  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(50))
        .background(tint.copy(alpha = 0.14f))
        .padding(horizontal = 10.dp, vertical = 4.dp),
  ) {
    Text(
      text = strings.statusLabel(status),
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.SemiBold,
      color = tint,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
fun DaysRemainingPill(
  daysRemaining: Int,
  status: SubscriptionStatus,
  strings: AppStrings,
  modifier: Modifier = Modifier,
) {
  val colors = SuiviTheme.colors
  val tint =
    when (status) {
      SubscriptionStatus.ACTIVE -> colors.statusActive
      SubscriptionStatus.EXPIRING_SOON -> colors.statusExpiring
      SubscriptionStatus.EXPIRED -> colors.statusExpired
    }
  Text(
    text = strings.formatDaysRemaining(daysRemaining),
    style = MaterialTheme.typography.labelLarge.tabular(),
    color = tint,
    modifier = modifier,
    maxLines = 1,
  )
}

@Composable
fun SubscriptionProgressBar(
  progress: Float,
  status: SubscriptionStatus,
  modifier: Modifier = Modifier,
) {
  val colors = SuiviTheme.colors
  val barBrush =
    when (status) {
      SubscriptionStatus.ACTIVE ->
        Brush.horizontalGradient(listOf(colors.accentBlue, colors.accentCyan))
      SubscriptionStatus.EXPIRING_SOON ->
        Brush.horizontalGradient(listOf(colors.statusExpiring, Color(0xFFFBBF24)))
      SubscriptionStatus.EXPIRED ->
        Brush.horizontalGradient(listOf(colors.statusExpired, Color(0xFFF87171)))
    }
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(50))
        .background(colors.softSurface),
  ) {
    val fill = if (status == SubscriptionStatus.EXPIRED) 1f else progress.coerceIn(0.04f, 1f)
    Box(
      modifier =
        Modifier
          .fillMaxWidth(fill)
          .fillMaxHeight()
          .clip(RoundedCornerShape(50))
          .background(barBrush),
    )
  }
}

/**
 * Avatar that decodes local base64 data URIs (Coil cannot load "data:" URIs natively)
 * and falls back to Coil for http(s) URLs.
 */
@Composable
fun AvatarImage(
  avatarUrl: String,
  contentDescription: String?,
  size: Dp = 80.dp,
  modifier: Modifier = Modifier,
) {
  val bitmap: ImageBitmap? =
    remember(avatarUrl) {
      try {
        val b64 = avatarUrl.substringAfter("base64,", "")
        if (b64.isEmpty()) {
          null
        } else {
          val bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
          android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
      } catch (_: Exception) {
        null
      }
    }
  Box(
    modifier = modifier.size(size).clip(CircleShape),
    contentAlignment = Alignment.Center,
  ) {
    when {
      bitmap != null ->
        Image(
          bitmap = bitmap,
          contentDescription = contentDescription,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
        )
      avatarUrl.startsWith("http") ->
        Image(
          painter = rememberAsyncImagePainter(avatarUrl),
          contentDescription = contentDescription,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
        )
      else ->
        Icon(
          Icons.Filled.Person,
          contentDescription = contentDescription,
          tint = VibrantCyan,
          modifier = Modifier.size((size.value * 0.35f).dp),
        )
    }
  }
}

/**
 * "III"-style overflow menu button that opens a quick-settings panel
 * containing the language switcher and the dark/light theme toggle.
 */
@Composable
fun QuickSettingsMenu(
  language: AppLanguage,
  isDarkMode: Boolean,
  onSelectLanguage: (AppLanguage) -> Unit,
  onToggleTheme: () -> Unit,
  contentDescription: String,
  modifier: Modifier = Modifier,
) {
  val colors = SuiviTheme.colors
  var expanded by remember { mutableStateOf(false) }
  Box(modifier = modifier) {
    IconButton(
      onClick = { expanded = true },
      modifier =
        Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(colors.softSurface)
          .testTag("quick_settings_button"),
    ) {
      Icon(
        Icons.Filled.MoreVert,
        contentDescription = contentDescription,
        tint = colors.accentCyan,
      )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      Column(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "Langue",
            style = MaterialTheme.typography.labelMedium,
            color = colors.textMuted,
            modifier = Modifier.padding(end = 10.dp),
          )
          LanguageSwitcherPill(
            currentLanguage = language,
            onSelect = {
              onSelectLanguage(it)
              expanded = false
            },
          )
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "Thème",
            style = MaterialTheme.typography.labelMedium,
            color = colors.textMuted,
            modifier = Modifier.padding(end = 10.dp),
          )
          ThemeToggleButton(
            isDarkMode = isDarkMode,
            onToggle = {
              onToggleTheme()
              expanded = false
            },
            contentDescription = contentDescription,
          )
        }
      }
    }
  }
}

@Composable
fun ClientMonogramAvatar(
  fullName: String,
  status: SubscriptionStatus?,
  size: Dp = 44.dp,
) {
  val colors = SuiviTheme.colors
  val initials =
    fullName
      .trim()
      .split(" ")
      .filter { it.isNotBlank() }
      .take(2)
      .joinToString("") { it.first().uppercase() }
      .ifEmpty { "C" }
  val accent =
    when (status) {
      SubscriptionStatus.ACTIVE -> colors.accentBlue
      SubscriptionStatus.EXPIRING_SOON -> colors.statusExpiring
      SubscriptionStatus.EXPIRED -> colors.statusExpired
      null -> colors.textMuted
    }
  Box(
    modifier =
      Modifier
        .size(size)
        .clip(RoundedCornerShape(14.dp))
        .background(accent.copy(alpha = 0.16f)),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = initials,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = accent,
    )
  }
}
