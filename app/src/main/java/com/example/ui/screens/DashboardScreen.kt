package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.ClientDossier
import com.example.domain.model.Payment
import com.example.domain.model.SubscriptionStatus
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.formatDA
import com.example.i18n.paymentMethodLabel
import com.example.ui.components.AvatarImage
import com.example.ui.components.ClientMonogramAvatar
import com.example.ui.components.DaysRemainingPill
import com.example.ui.components.QuickSettingsMenu
import com.example.ui.components.StatusBadge
import com.example.ui.components.SubscriptionProgressBar
import com.example.ui.components.SuiviCoachLogo
import com.example.ui.components.QuickSettingsMenu
import com.example.ui.theme.SuiviTheme
import com.example.ui.theme.tabular
import com.example.ui.viewmodel.DashboardMetrics
import com.example.util.DateUtils
import com.example.util.IntentHelpers

@Composable
fun DashboardScreen(
  coachName: String,
  coachAvatarUrl: String = "",
  language: AppLanguage,
  isDarkMode: Boolean,
  isSyncing: Boolean,
  metrics: DashboardMetrics,
  urgentDossiers: List<ClientDossier>,
  recentPayments: List<Payment>,
  clientsById: Map<String, String>,
  strings: AppStrings,
  onSelectLanguage: (AppLanguage) -> Unit,
  onToggleTheme: () -> Unit,
  onSync: () -> Unit,
  onOpenClientDetail: (String) -> Unit,
  onQuickRenew: (ClientDossier) -> Unit,
) {
  val colors = SuiviTheme.colors
  val context = LocalContext.current

  Box(
    modifier = Modifier.fillMaxSize().background(colors.background),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Top Brand Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
          ) {
            if (coachAvatarUrl.isNotBlank()) {
              AvatarImage(
                avatarUrl = coachAvatarUrl,
                contentDescription = null,
                size = 42.dp,
              )
            } else {
              SuiviCoachLogo(size = 42.dp)
            }
            Column {
              Text(
                text = strings.appName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
              )
              Text(
                text = "${strings.welcomeCoach} ${coachName.ifBlank { "Coach" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            QuickSettingsMenu(
              language = language,
              isDarkMode = isDarkMode,
              onSelectLanguage = onSelectLanguage,
              onToggleTheme = onToggleTheme,
              contentDescription = strings.languageLabel,
            )
            IconButton(
              onClick = onSync,
              enabled = !isSyncing,
              modifier = Modifier.size(48.dp).testTag("dashboard_sync_button"),
            ) {
              if (isSyncing) {
                CircularProgressIndicator(
                  modifier = Modifier.size(18.dp),
                  color = colors.accentInk,
                  strokeWidth = 2.dp,
                )
              } else {
                Icon(
                  imageVector = Icons.Filled.Sync,
                  contentDescription = strings.syncNow,
                  tint = colors.accentBlue,
                )
              }
            }
          }
        }
      }

      // Monthly Revenue Hero Bento Card
      item {
        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(22.dp))
              .background(colors.accentBlue)
              .padding(20.dp),
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = strings.monthlyRevenue,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.88f),
              )
              Icon(
                imageVector = Icons.Filled.Payments,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
              )
            }
            Text(
              text = formatDA(metrics.monthlyRevenueDa),
              style = MaterialTheme.typography.displaySmall.tabular(),
              fontWeight = FontWeight.Bold,
              color = Color.White,
            )
            if (metrics.totalPendingDa > 0) {
              Box(
                modifier =
                  Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.22f))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
              ) {
                Text(
                  text = "${strings.pendingBalance}: ${formatDA(metrics.totalPendingDa)}",
                  style = MaterialTheme.typography.labelLarge.tabular(),
                  color = Color.White,
                )
              }
            }
          }
        }
      }

      // 3-Column Status KPI Bento Row
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          KpiMiniCard(
            title = strings.activeClients,
            value = metrics.activeCount.toString(),
            accent = colors.statusActiveInk,
            icon = Icons.Filled.CheckCircle,
            modifier = Modifier.weight(1f),
          )
          KpiMiniCard(
            title = strings.expiringSoon,
            value = metrics.expiringSoonCount.toString(),
            accent = colors.statusExpiringInk,
            icon = Icons.Filled.Schedule,
            modifier = Modifier.weight(1f),
          )
          KpiMiniCard(
            title = strings.expiredClients,
            value = metrics.expiredCount.toString(),
            accent = colors.statusExpiredInk,
            icon = Icons.Filled.ErrorOutline,
            modifier = Modifier.weight(1f),
          )
        }
      }

      // Urgent Attention Section (Expiring 1-3d / Expired)
      item {
        Text(
          text = strings.urgentAttentionTitle,
          style = MaterialTheme.typography.titleMedium,
          color = colors.textPrimary,
          modifier = Modifier.padding(top = 4.dp),
        )
      }

      if (urgentDossiers.isEmpty()) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = colors.cardSurface,
          ) {
            Row(
              modifier = Modifier.padding(18.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = colors.statusActiveInk,
              )
              Text(
                text = strings.noUrgentClients,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
              )
            }
          }
        }
      } else {
        items(urgentDossiers, key = { it.client.id }) { dossier ->
          val sub = dossier.latestSubscription ?: return@items
          UrgentRenewalCard(
            dossier = dossier,
            strings = strings,
            onOpen = { onOpenClientDetail(dossier.client.id) },
            onRenew = { onQuickRenew(dossier) },
            onCall = { IntentHelpers.dialPhone(context, dossier.client.phone) },
            onWhatsApp = {
              val msg =
                strings.whatsappReminderMessage(
                  dossier.client.fullName,
                  sub.planName,
                  DateUtils.formatDisplay(sub.endDate),
                  sub.daysRemaining,
                  if (dossier.totalDueDa > 0) formatDA(dossier.totalDueDa) else "",
                )
              IntentHelpers.openWhatsApp(context, dossier.client.phone, msg)
            },
          )
        }
      }

      // Recent Payments Section
      item {
        Text(
          text = strings.recentPaymentsTitle,
          style = MaterialTheme.typography.titleMedium,
          color = colors.textPrimary,
          modifier = Modifier.padding(top = 4.dp),
        )
      }

      if (recentPayments.isEmpty()) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = colors.cardSurface,
          ) {
            Text(
              text = strings.noPaymentsYet,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.textMuted,
              modifier = Modifier.padding(18.dp),
            )
          }
        }
      } else {
        items(recentPayments.take(5), key = { it.id }) { payment ->
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = colors.cardSurface,
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = clientsById[payment.clientId] ?: "Client",
                  style = MaterialTheme.typography.titleSmall,
                  color = colors.textPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text =
                    "${DateUtils.formatDisplay(payment.paidAt)}, ${strings.paymentMethodLabel(payment.method)}",
                  style = MaterialTheme.typography.bodyMedium.tabular(),
                  color = colors.textMuted,
                )
              }
              Text(
                text = "+${formatDA(payment.amountDa)}",
                style = MaterialTheme.typography.titleSmall.tabular(),
                fontWeight = FontWeight.Bold,
                color = colors.statusActiveInk,
              )
            }
          }
        }
      }

      item { Spacer(Modifier.height(72.dp)) }
    }
  }
}

@Composable
private fun KpiMiniCard(
  title: String,
  value: String,
  accent: Color,
  icon: ImageVector,
  modifier: Modifier = Modifier,
) {
  val colors = SuiviTheme.colors
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(18.dp),
    color = colors.cardSurface,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = accent,
        modifier = Modifier.size(20.dp),
      )
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium.tabular(),
        fontWeight = FontWeight.Bold,
        color = colors.textPrimary,
      )
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = colors.textMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun UrgentRenewalCard(
  dossier: ClientDossier,
  strings: AppStrings,
  onOpen: () -> Unit,
  onRenew: () -> Unit,
  onCall: () -> Unit,
  onWhatsApp: () -> Unit,
) {
  val colors = SuiviTheme.colors
  val sub = dossier.latestSubscription ?: return

  Surface(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .clickable(onClick = onOpen),
    shape = RoundedCornerShape(20.dp),
    color = colors.cardSurface,
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f),
        ) {
          ClientMonogramAvatar(fullName = dossier.client.fullName, status = sub.status)
          Column {
            Text(
              text = dossier.client.fullName,
              style = MaterialTheme.typography.titleMedium,
              color = colors.textPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = "${sub.planName}, ${DateUtils.formatDisplay(sub.endDate)}",
              style = MaterialTheme.typography.bodyMedium.tabular(),
              color = colors.textMuted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
        StatusBadge(status = sub.status, strings = strings)
      }

      SubscriptionProgressBar(progress = sub.progressFraction, status = sub.status)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        DaysRemainingPill(daysRemaining = sub.daysRemaining, status = sub.status, strings = strings)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          if (dossier.client.phone.isNotBlank()) {
            IconButton(
              onClick = onCall,
              modifier = Modifier.size(48.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = strings.callAction,
                tint = colors.textMuted,
              )
            }
          }
          IconButton(
            onClick = onWhatsApp,
            modifier = Modifier.size(48.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Chat,
              contentDescription = strings.whatsappAction,
              tint = colors.statusActiveInk,
            )
          }
          Button(
            onClick = onRenew,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor =
                  if (sub.status == SubscriptionStatus.EXPIRED) colors.statusExpired
                  else colors.accentBlue
              ),
          ) {
            Icon(
              imageVector = Icons.Filled.Autorenew,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(strings.renewAction, color = Color.White)
          }
        }
      }
    }
  }
}
