package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.ClientDossier
import com.example.domain.model.Subscription
import com.example.i18n.AppStrings
import com.example.i18n.formatDA
import com.example.i18n.paymentMethodLabel
import com.example.ui.components.ClientMonogramAvatar
import com.example.ui.components.DaysRemainingPill
import com.example.ui.components.StatusBadge
import com.example.ui.components.SubscriptionProgressBar
import com.example.ui.theme.SuiviTheme
import com.example.ui.theme.tabular
import com.example.util.DateUtils
import com.example.util.IntentHelpers

@Composable
fun ClientDetailScreen(
  dossier: ClientDossier,
  strings: AppStrings,
  onBack: () -> Unit,
  onEditClient: () -> Unit,
  onDeleteClient: () -> Unit,
  onRenewSubscription: () -> Unit,
  onAddPayment: (Subscription?) -> Unit,
  onAdjustSession: (Subscription, Int) -> Unit,
) {
  BackHandler(onBack = onBack)

  val colors = SuiviTheme.colors
  val context = LocalContext.current
  val sub = dossier.latestSubscription
  var showDeleteConfirm by remember { mutableStateOf(false) }

  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      title = { Text(strings.deleteClientConfirmTitle) },
      text = { Text(strings.deleteClientConfirmBody) },
      confirmButton = {
        TextButton(
          onClick = {
            showDeleteConfirm = false
            onDeleteClient()
          }
        ) {
          Text(strings.delete, color = colors.statusExpiredInk)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = false }) { Text(strings.cancel) }
      },
    )
  }

  Box(
    modifier = Modifier.fillMaxSize().background(colors.background),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Top Navigation Bar
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp).testTag("detail_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = strings.close,
              tint = colors.textPrimary,
            )
          }
          Row {
            IconButton(
              onClick = onEditClient,
              modifier = Modifier.size(48.dp).testTag("detail_edit_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = strings.editClient,
                tint = colors.accentBlue,
              )
            }
            IconButton(
              onClick = { showDeleteConfirm = true },
              modifier = Modifier.size(48.dp).testTag("detail_delete_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.DeleteOutline,
                contentDescription = strings.delete,
                tint = colors.statusExpiredInk,
              )
            }
          }
        }
      }

      // Client Profile Header Card
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          color = colors.cardSurface,
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
              ) {
                ClientMonogramAvatar(
                  fullName = dossier.client.fullName,
                  status = sub?.status,
                  size = 54.dp,
                )
                Column {
                  Text(
                    text = dossier.client.fullName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary,
                  )
                  if (dossier.client.goal.isNotBlank()) {
                    Text(
                      text = dossier.client.goal,
                      style = MaterialTheme.typography.bodyMedium,
                      color = colors.accentInk,
                    )
                  }
                  if (dossier.client.phone.isNotBlank()) {
                    Text(
                      text = dossier.client.phone,
                      style = MaterialTheme.typography.bodyMedium.tabular(),
                      color = colors.textMuted,
                    )
                  }
                }
              }
              StatusBadge(status = sub?.status, strings = strings)
            }

            if (dossier.client.notes.isNotBlank()) {
              Text(
                text = dossier.client.notes,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.softSurface)
                    .padding(12.dp),
              )
            }

            // Quick Contact Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              OutlinedButton(
                onClick = { IntentHelpers.dialPhone(context, dossier.client.phone) },
                enabled = dossier.client.phone.isNotBlank(),
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(14.dp),
              ) {
                Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(strings.callAction)
              }
              Button(
                onClick = {
                  val msg =
                    if (sub != null) {
                      strings.whatsappReminderMessage(
                        dossier.client.fullName,
                        sub.planName,
                        DateUtils.formatDisplay(sub.endDate),
                        sub.daysRemaining,
                        if (dossier.totalDueDa > 0) formatDA(dossier.totalDueDa) else "",
                      )
                    } else if (dossier.totalDueDa > 0) {
                      "Salam ${dossier.client.fullName} ! Il te reste ${formatDA(dossier.totalDueDa)} à régler."
                    } else {
                      "Salam ${dossier.client.fullName}!"
                    }
                  IntentHelpers.openWhatsApp(context, dossier.client.phone, msg)
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.statusActive),
              ) {
                Icon(
                  Icons.Filled.Chat,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(strings.whatsappAction, color = Color.White)
              }
            }
          }
        }
      }

      // Active / Latest Subscription Card
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          color = colors.cardSurface,
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = strings.currentPlanTitle,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
              )
              if (sub != null) {
                DaysRemainingPill(
                  daysRemaining = sub.daysRemaining,
                  status = sub.status,
                  strings = strings,
                )
              }
            }

            if (sub != null) {
              Text(
                text = "${sub.planName} — ${formatDA(sub.priceDa)}",
                style = MaterialTheme.typography.titleLarge.tabular(),
                fontWeight = FontWeight.Bold,
                color = colors.accentInk,
              )

              SubscriptionProgressBar(progress = sub.progressFraction, status = sub.status)

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = "${DateUtils.formatDisplay(sub.startDate)} → ${DateUtils.formatDisplay(sub.endDate)}",
                  style = MaterialTheme.typography.bodyMedium.tabular(),
                  color = colors.textMuted,
                )
                val rem = dossier.getRemainingBalanceFor(sub.id)
                val fullyPaid = rem == 0
                Text(
                  text =
                    if (fullyPaid) strings.paidBadge
                    else strings.remainingToPayFormat(formatDA(rem)),
                  style = MaterialTheme.typography.labelLarge.tabular(),
                  color = if (fullyPaid) colors.statusActiveInk else colors.statusExpiringInk,
                )
              }

              // Session Tracker if session-based
              if (sub.sessionsTotal != null) {
                Row(
                  modifier =
                    Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(14.dp))
                      .background(colors.softSurface)
                      .padding(horizontal = 14.dp, vertical = 10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = strings.sessionsLabel(sub.sessionsUsed, sub.sessionsTotal),
                    style = MaterialTheme.typography.titleSmall.tabular(),
                    color = colors.textPrimary,
                  )
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                      onClick = { onAdjustSession(sub, -1) },
                      enabled = sub.sessionsUsed > 0,
                      modifier = Modifier.size(48.dp),
                    ) {
                      Icon(Icons.Filled.Remove, contentDescription = "-1", tint = colors.textMuted)
                    }
                    IconButton(
                      onClick = { onAdjustSession(sub, +1) },
                      enabled = sub.sessionsUsed < sub.sessionsTotal,
                      modifier = Modifier.size(48.dp),
                    ) {
                      Icon(Icons.Filled.Add, contentDescription = "+1", tint = colors.accentInk)
                    }
                  }
                }
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Button(
                onClick = onRenewSubscription,
                modifier = Modifier.weight(1f).height(48.dp).testTag("detail_renew_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentBlue),
              ) {
                Icon(
                  Icons.Filled.Autorenew,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(strings.renewAction, color = Color.White)
              }
              OutlinedButton(
                onClick = { onAddPayment(sub) },
                modifier = Modifier.weight(1f).height(48.dp).testTag("detail_add_payment_button"),
                shape = RoundedCornerShape(14.dp),
              ) {
                Icon(
                  Icons.Filled.Payments,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(strings.addPaymentAction)
              }
            }
          }
        }
      }

      // Payment History
      item {
        Text(
          text = strings.paymentHistoryTitle,
          style = MaterialTheme.typography.titleMedium,
          color = colors.textPrimary,
        )
      }

      if (dossier.payments.isEmpty()) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = colors.cardSurface,
          ) {
            Text(
              text = strings.noPaymentsYet,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.textMuted,
              modifier = Modifier.padding(16.dp),
            )
          }
        }
      } else {
        items(dossier.payments, key = { it.id }) { pay ->
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
              Column {
                Text(
                  text = strings.paymentMethodLabel(pay.method),
                  style = MaterialTheme.typography.titleSmall,
                  color = colors.textPrimary,
                )
                Text(
                  text =
                    listOf(DateUtils.formatDisplay(pay.paidAt), pay.note)
                      .filter { it.isNotBlank() }
                      .joinToString(", "),
                  style = MaterialTheme.typography.bodyMedium.tabular(),
                  color = colors.textMuted,
                )
              }
              Text(
                text = "+${formatDA(pay.amountDa)}",
                style = MaterialTheme.typography.titleSmall.tabular(),
                fontWeight = FontWeight.Bold,
                color = colors.statusActiveInk,
              )
            }
          }
        }
      }

      // Subscription History
      if (dossier.subscriptions.size > 1) {
        item {
          Text(
            text = strings.subscriptionHistoryTitle,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
          )
        }
        items(dossier.subscriptions.drop(1), key = { it.id }) { oldSub ->
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
              Column {
                Text(
                  text = oldSub.planName,
                  style = MaterialTheme.typography.titleSmall,
                  color = colors.textPrimary,
                )
                Text(
                  text = "${DateUtils.formatDisplay(oldSub.startDate)} → ${DateUtils.formatDisplay(oldSub.endDate)}",
                  style = MaterialTheme.typography.bodyMedium.tabular(),
                  color = colors.textMuted,
                )
              }
              Text(
                text = formatDA(oldSub.priceDa),
                style = MaterialTheme.typography.titleSmall.tabular(),
                color = colors.textPrimary,
              )
            }
          }
        }
      }

      item { Spacer(Modifier.height(32.dp)) }
    }
  }
}
