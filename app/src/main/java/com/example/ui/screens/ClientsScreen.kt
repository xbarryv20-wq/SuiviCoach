package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.ClientDossier
import com.example.domain.model.ClientFilter
import com.example.i18n.AppStrings
import com.example.i18n.formatDA
import com.example.ui.components.ClientMonogramAvatar
import com.example.ui.components.DaysRemainingPill
import com.example.ui.components.StatusBadge
import com.example.ui.components.SubscriptionProgressBar
import com.example.ui.theme.SuiviTheme
import com.example.ui.theme.tabular
import com.example.util.DateUtils

@Composable
fun ClientsScreen(
  dossiers: List<ClientDossier>,
  searchQuery: String,
  selectedFilter: ClientFilter,
  strings: AppStrings,
  onSearchChange: (String) -> Unit,
  onFilterChange: (ClientFilter) -> Unit,
  onOpenClientDetail: (String) -> Unit,
) {
  val colors = SuiviTheme.colors

  Box(
    modifier = Modifier.fillMaxSize().background(colors.background),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
    ) {
      // Search + Filter Chips Header
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = strings.navClients,
          style = MaterialTheme.typography.headlineMedium,
          color = colors.textPrimary,
        )

        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchChange,
          placeholder = { Text(strings.searchClientsPlaceholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
          leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchChange("") }) {
                Icon(Icons.Filled.Close, contentDescription = strings.close)
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedContainerColor = colors.cardSurface,
              unfocusedContainerColor = colors.cardSurface,
              focusedBorderColor = colors.accentBlue,
              unfocusedBorderColor = Color.Transparent,
            ),
          modifier = Modifier.fillMaxWidth().testTag("clients_search_input"),
        )

        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          val filters =
            listOf(
              ClientFilter.ALL to strings.filterAll,
              ClientFilter.ACTIVE to strings.filterActive,
              ClientFilter.EXPIRING_SOON to strings.filterExpiring,
              ClientFilter.EXPIRED to strings.filterExpired,
              ClientFilter.UNPAID to strings.filterUnpaid,
            )
          filters.forEach { (filter, label) ->
            val selected = selectedFilter == filter
            FilterChip(
              selected = selected,
              onClick = { onFilterChange(filter) },
              label = { Text(label) },
              colors =
                FilterChipDefaults.filterChipColors(
                  selectedContainerColor = colors.accentBlue,
                  selectedLabelColor = Color.White,
                  containerColor = colors.cardSurface,
                  labelColor = colors.textMuted,
                ),
              border = null,
              modifier = Modifier.testTag("filter_${filter.name.lowercase()}"),
            )
          }
        }
      }

      if (dossiers.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize().padding(32.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Box(
              modifier =
                Modifier
                  .size(68.dp)
                  .clip(RoundedCornerShape(20.dp))
                  .background(colors.softSurface),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = null,
                tint = colors.accentInk,
                modifier = Modifier.size(32.dp),
              )
            }
            Text(
              text = strings.noClientsTitle,
              style = MaterialTheme.typography.titleMedium,
              color = colors.textPrimary,
            )
            Text(
              text = strings.noClientsSubtitle,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.textMuted,
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(dossiers, key = { it.client.id }) { dossier ->
            ClientListCard(
              dossier = dossier,
              strings = strings,
              onClick = { onOpenClientDetail(dossier.client.id) },
            )
          }
          item { Spacer(Modifier.height(84.dp)) }
        }
      }
    }
  }
}

@Composable
private fun ClientListCard(
  dossier: ClientDossier,
  strings: AppStrings,
  onClick: () -> Unit,
) {
  val colors = SuiviTheme.colors
  val sub = dossier.latestSubscription

  Surface(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .clickable(onClick = onClick)
        .testTag("client_card_${dossier.client.id}"),
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
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f),
        ) {
          ClientMonogramAvatar(fullName = dossier.client.fullName, status = sub?.status)
          Column {
            Text(
              text = dossier.client.fullName,
              style = MaterialTheme.typography.titleMedium,
              color = colors.textPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            val subtitle =
              listOf(dossier.client.goal, dossier.client.phone)
                .filter { it.isNotBlank() }
                .joinToString(", ")
            if (subtitle.isNotBlank()) {
              Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.tabular(),
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
        StatusBadge(status = sub?.status, strings = strings)
      }

      if (sub != null) {
        SubscriptionProgressBar(progress = sub.progressFraction, status = sub.status)

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "${sub.planName}, ${formatDA(sub.priceDa)}",
              style = MaterialTheme.typography.labelLarge.tabular(),
              color = colors.textPrimary,
            )
            Text(
              text = "${DateUtils.formatDisplay(sub.startDate)} → ${DateUtils.formatDisplay(sub.endDate)}",
              style = MaterialTheme.typography.labelMedium.tabular(),
              color = colors.textMuted,
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            DaysRemainingPill(
              daysRemaining = sub.daysRemaining,
              status = sub.status,
              strings = strings,
            )
            if (sub.sessionsTotal != null) {
              Text(
                text = strings.sessionsLabel(sub.sessionsUsed, sub.sessionsTotal),
                style = MaterialTheme.typography.labelMedium.tabular(),
                color = colors.accentInk,
              )
            }
          }
        }

        if (!sub.isFullyPaid) {
          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.statusExpiring.copy(alpha = 0.14f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
          ) {
            Text(
              text = strings.remainingToPayFormat(formatDA(sub.remainingBalanceDa)),
              style = MaterialTheme.typography.labelMedium.tabular(),
              fontWeight = FontWeight.SemiBold,
              color = colors.statusExpiringInk,
            )
          }
        }
      }
    }
  }
}
