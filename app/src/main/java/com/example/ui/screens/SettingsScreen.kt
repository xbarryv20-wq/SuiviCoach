package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.AuthSession
import com.example.domain.model.CoachProfile
import com.example.domain.model.PlanTemplate
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.formatDA
import com.example.ui.components.LanguageSwitcherPill
import com.example.ui.theme.SuiviTheme
import com.example.ui.theme.tabular

@Composable
fun SettingsScreen(
  session: AuthSession?,
  profile: CoachProfile?,
  language: AppLanguage,
  isDarkMode: Boolean,
  templates: List<PlanTemplate>,
  strings: AppStrings,
  onSelectLanguage: (AppLanguage) -> Unit,
  onToggleTheme: () -> Unit,
  onUpdateProfile: (String, String, String) -> Unit,
  onOpenAddTemplate: () -> Unit,
  onDeleteTemplate: (String) -> Unit,
  onSignOut: () -> Unit,
) {
  val colors = SuiviTheme.colors
  var fullName by remember(profile?.fullName, session?.fullName) {
    mutableStateOf(profile?.fullName?.ifBlank { session?.fullName.orEmpty() } ?: "")
  }
  var businessName by remember(profile?.businessName) {
    mutableStateOf(profile?.businessName ?: "SUIVICOACH Studio")
  }
  var phone by remember(profile?.phone) { mutableStateOf(profile?.phone ?: "") }

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
        Text(
          text = strings.navSettings,
          style = MaterialTheme.typography.headlineMedium,
          color = colors.textPrimary,
        )
      }

      // Coach Profile Card
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
            Text(
              text = strings.coachProfileTitle,
              style = MaterialTheme.typography.titleMedium,
              color = colors.textPrimary,
            )
            if (!session?.email.isNullOrBlank()) {
              Text(
                text = session!!.email,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.accentCyan,
              )
            }
            OutlinedTextField(
              value = fullName,
              onValueChange = { fullName = it },
              label = { Text(strings.fullNameLabel) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
              value = businessName,
              onValueChange = { businessName = it },
              label = { Text(strings.businessNameLabel) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
              value = phone,
              onValueChange = { phone = it },
              label = { Text(strings.clientPhoneLabel) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
            Button(
              onClick = { onUpdateProfile(fullName, businessName, phone) },
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("settings_save_profile_btn"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = colors.accentBlue),
            ) {
              Text(strings.updateProfile, color = Color.White)
            }
          }
        }
      }

      // Appearance & Language
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          color = colors.cardSurface,
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Text(
              text = strings.appearanceTitle,
              style = MaterialTheme.typography.titleMedium,
              color = colors.textPrimary,
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = strings.darkModeLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
              )
              Switch(
                checked = isDarkMode,
                onCheckedChange = { onToggleTheme() },
                modifier = Modifier.testTag("settings_darkmode_switch"),
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = strings.languageLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
              )
              LanguageSwitcherPill(currentLanguage = language, onSelect = onSelectLanguage)
            }
          }
        }
      }

      // Plan Templates Manager (DA)
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
                text = strings.planTemplatesTitle,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
              )
              IconButton(
                onClick = onOpenAddTemplate,
                modifier = Modifier.size(48.dp).testTag("settings_add_template_btn"),
              ) {
                Icon(Icons.Filled.Add, contentDescription = strings.addPlanTemplate, tint = colors.accentCyan)
              }
            }
            templates.forEach { tpl ->
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
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = tpl.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                  )
                  val sessPart = tpl.sessionsTotal?.let { " • $it séances" } ?: ""
                  Text(
                    text = "${formatDA(tpl.priceDa)} • ${tpl.durationDays} ${strings.daysUnit}$sessPart",
                    style = MaterialTheme.typography.bodyMedium.tabular(),
                    color = colors.textMuted,
                  )
                }
                IconButton(
                  onClick = { onDeleteTemplate(tpl.id) },
                  modifier = Modifier.size(48.dp),
                ) {
                  Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = strings.delete,
                    tint = colors.textMuted,
                  )
                }
              }
            }
          }
        }
      }

      // Sign Out
      item {
        Button(
          onClick = onSignOut,
          modifier = Modifier.fillMaxWidth().height(50.dp).testTag("settings_sign_out_btn"),
          shape = RoundedCornerShape(14.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = colors.statusExpired.copy(alpha = 0.16f),
              contentColor = colors.statusExpired,
            ),
        ) {
          Icon(
            Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(Modifier.width(8.dp))
          Text(strings.signOut, fontWeight = FontWeight.Bold)
        }
      }

      item { Spacer(Modifier.height(72.dp)) }
    }
  }
}
