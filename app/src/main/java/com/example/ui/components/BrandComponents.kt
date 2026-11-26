package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.layout.ContentScale
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
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SuiviTheme
import com.example.ui.theme.VibrantCyan
import com.example.ui.theme.tabular

/**
 * Astryx VIP Logo: Sharp athletic "S" with glowing gradient strokes and specular borders.
 */
@Composable
fun SuiviCoachLogo(
  size: Dp = 44.dp,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier.size(size)) {
    val w = this.size.width
    val h = this.size.height
    val corner = w * 0.28f

    // Obsidian card background
    drawRoundRect(
      color = ObsidianBlack,
      cornerRadius = CornerRadius(corner, corner),
    )

    // Specular highlight border
    drawRoundRect(
      brush =
        Brush.linearGradient(
          colors = listOf(Color.White.copy(alpha = 0.4f), ElectricBlue.copy(alpha = 0.2f)),
          start = Offset.Zero,
          end = Offset(w, h),
        ),
      cornerRadius = CornerRadius(corner, corner),
      style = Stroke(width = w * 0.04f),
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
      style = Stroke(width = w * 0.12f, cap = StrokeCap.Square, join = StrokeJoin.Miter),
    )
  }
}

/**
 * Astryx VIP Primary Button with spring scale animation and rich gradient fill.
 */
@Composable
fun VipButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: @Composable (() -> Unit)? = null,
) {
  val colors = SuiviTheme.colors
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "btnScale")

  val gradient = Brush.horizontalGradient(
    colors = listOf(colors.accentBlue, colors.accentCyan)
  )

  Box(
    modifier = modifier
      .scale(scale)
      .clip(RoundedCornerShape(16.dp))
      .background(if (enabled) gradient else Brush.linearGradient(listOf(colors.softSurface, colors.softSurface)))
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick
      )
      .padding(horizontal = 20.dp, vertical = 14.dp),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (icon != null) {
        icon()
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
      )
    }
  }
}

/**
 * Astryx VIP Glass Button with specular border and glowing effect.
 */
@Composable
fun VipGlassButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: @Composable (() -> Unit)? = null,
) {
  val colors = SuiviTheme.colors
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "glassScale")

  Box(
    modifier = modifier
      .scale(scale)
      .clip(RoundedCornerShape(16.dp))
      .background(colors.cardSurface.copy(alpha = 0.6f))
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      )
      .padding(horizontal = 18.dp, vertical = 13.dp),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (icon != null) {
        icon()
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = colors.textPrimary,
      )
    }
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
        .background(tint.copy(alpha = 0.18f))
        .padding(horizontal = 12.dp, vertical = 5.dp),
  ) {
    Text(
      text = strings.statusLabel(status),
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
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
        .clip(RoundedCornerShape(16.dp))
        .background(accent.copy(alpha = 0.18f)),
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
