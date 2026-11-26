package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.Payment
import com.example.domain.model.PaymentMethod
import com.example.i18n.AppStrings
import com.example.i18n.formatDA
import com.example.i18n.paymentMethodLabel
import com.example.ui.theme.SuiviTheme
import com.example.ui.theme.tabular
import com.example.ui.viewmodel.DashboardMetrics
import com.example.util.DateUtils
import com.example.util.IntentHelpers

@Composable
fun FinancesScreen(
  metrics: DashboardMetrics,
  payments: List<Payment>,
  clientsById: Map<String, String>,
  strings: AppStrings,
  buildCsv: () -> String,
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
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = strings.navFinances,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
          )
          OutlinedButton(
            onClick = {
              IntentHelpers.shareTextReport(
                context = context,
                subject = "SUIVICOACH_Payments.csv",
                content = buildCsv(),
              )
            },
            enabled = payments.isNotEmpty(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(44.dp).testTag("export_csv_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.IosShare,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(strings.shareCsvReport)
          }
        }
      }

      // Collected vs Pending Cards
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = colors.cardSurface,
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = strings.totalCollected,
                style = MaterialTheme.typography.labelLarge,
                color = colors.textMuted,
              )
              Text(
                text = formatDA(metrics.totalCollectedDa),
                style = MaterialTheme.typography.headlineSmall.tabular(),
                fontWeight = FontWeight.Bold,
                color = colors.statusActiveInk,
              )
            }
          }

          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = colors.cardSurface,
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = strings.totalPending,
                style = MaterialTheme.typography.labelLarge,
                color = colors.textMuted,
              )
              Text(
                text = formatDA(metrics.totalPendingDa),
                style = MaterialTheme.typography.headlineSmall.tabular(),
                fontWeight = FontWeight.Bold,
                color = if (metrics.totalPendingDa > 0) colors.statusExpiringInk else colors.textMuted,
              )
            }
          }
        }
      }

      // Payment Methods Breakdown
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          color = colors.cardSurface,
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              text = strings.revenueByMethod,
              style = MaterialTheme.typography.titleMedium,
              color = colors.textPrimary,
            )
            val total = metrics.totalCollectedDa.coerceAtLeast(1)
            PaymentMethod.entries.forEach { method ->
              val amount = metrics.revenueByMethod[method] ?: 0
              val ratio = (amount.toFloat() / total.toFloat()).coerceIn(0f, 1f)
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(
                    text = strings.paymentMethodLabel(method),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                  )
                  Text(
                    text = formatDA(amount),
                    style = MaterialTheme.typography.labelLarge.tabular(),
                    color = colors.accentInk,
                  )
                }
                Box(
                  modifier =
                    Modifier
                      .fillMaxWidth()
                      .height(7.dp)
                      .clip(RoundedCornerShape(50))
                      .background(colors.softSurface),
                ) {
                  if (amount > 0) {
                    Box(
                      modifier =
                        Modifier
                          .fillMaxWidth(ratio.coerceAtLeast(0.03f))
                          .fillMaxHeight()
                          .clip(RoundedCornerShape(50))
                          .background(colors.accentBlue),
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Payments Ledger
      item {
        Text(
          text = strings.allPaymentsLedger,
          style = MaterialTheme.typography.titleMedium,
          color = colors.textPrimary,
        )
      }

      if (payments.isEmpty()) {
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
        items(payments, key = { it.id }) { payment ->
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
                    listOf(
                      DateUtils.formatDisplay(payment.paidAt),
                      strings.paymentMethodLabel(payment.method),
                      payment.note,
                    )
                      .filter { it.isNotBlank() }
                      .joinToString(", "),
                  style = MaterialTheme.typography.bodyMedium.tabular(),
                  color = colors.textMuted,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
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
