package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.domain.model.AuthSession
import com.example.domain.model.CoachProfile
import com.example.domain.model.PlanTemplate
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.formatDA
import com.example.ui.components.AvatarImage
import com.example.ui.components.LanguageSwitcherPill
import com.example.ui.theme.SuiviTheme
import com.example.ui.theme.tabular
import java.io.ByteArrayOutputStream

private const val GITHUB_PROFILE_URL = "https://github.com/xbarryv20-wq"

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
  onUpdateProfile: (String, String, String, String) -> Unit,
  onOpenAddTemplate: () -> Unit,
  onDeleteTemplate: (String) -> Unit,
  onSignOut: () -> Unit,
) {
  val colors = SuiviTheme.colors
  val context = LocalContext.current

  var fullName by remember(profile?.fullName, session?.fullName) {
    mutableStateOf(profile?.fullName?.ifBlank { session?.fullName.orEmpty() } ?: "")
  }
  var businessName by remember(profile?.businessName) {
    mutableStateOf(profile?.businessName ?: "SUIVICOACH Studio")
  }
  var phone by remember(profile?.phone) { mutableStateOf(profile?.phone ?: "") }
  var avatarUrl by remember(profile?.avatarUrl) { mutableStateOf(profile?.avatarUrl ?: "") }

  val pickImageLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
      if (uri != null) {
        try {
          val inputStream = context.contentResolver.openInputStream(uri)
          val bitmap = BitmapFactory.decodeStream(inputStream)
          inputStream?.close()
          if (bitmap != null) {
            val maxSize = 512f
            var width = bitmap.width.toFloat()
            var height = bitmap.height.toFloat()
            val ratio = minOf(maxSize / width, maxSize / height, 1f)
            val scaled =
              if (ratio < 1f) {
                Bitmap.createScaledBitmap(bitmap, (width * ratio).toInt(), (height * ratio).toInt(), true)
              } else {
                bitmap
              }
            val outputStream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            avatarUrl = "data:image/jpeg;base64,$base64"
          }
        } catch (_: Exception) {
          // Failed to process image
        }
      }
    }

  fun openGithubProfile() {
    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_PROFILE_URL))
      context.startActivity(intent)
    } catch (_: Exception) {
    }
  }

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
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Text(
              text = strings.coachProfileTitle,
              style = MaterialTheme.typography.titleMedium,
              color = colors.textPrimary,
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(16.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Box(
                modifier =
                  Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(colors.softSurface)
                    .clickable {
                      pickImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                      )
                    },
                contentAlignment = Alignment.Center,
              ) {
                if (avatarUrl.isNotBlank()) {
                  AvatarImage(
                    avatarUrl = avatarUrl,
                    contentDescription = strings.profilePicture,
                    size = 80.dp,
                  )
                } else {
                  Icon(
                    Icons.Filled.PhotoCamera,
                    contentDescription = strings.changeProfilePicture,
                    tint = colors.accentInk,
                    modifier = Modifier.size(28.dp),
                  )
                }
              }
              Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                  text = strings.profilePicture,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = colors.textPrimary,
                )
                if (!session?.email.isNullOrBlank()) {
                  Text(
                    text = session!!.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.accentInk,
                    maxLines = 1,
                  )
                }
                Text(
                  text = strings.changeProfilePicture,
                  style = MaterialTheme.typography.labelSmall,
                  color = colors.accentInk,
                  textDecoration = TextDecoration.Underline,
                  modifier =
                    Modifier.clickable {
                      pickImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                      )
                    },
                )
              }
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
              onClick = { onUpdateProfile(fullName, businessName, phone, avatarUrl) },
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
                Icon(Icons.Filled.Add, contentDescription = strings.addPlanTemplate, tint = colors.accentInk)
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

      // About Section
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          color = colors.cardSurface,
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(Icons.Filled.Info, contentDescription = null, tint = colors.accentBlue, modifier = Modifier.size(22.dp))
              Text(
                text = strings.aboutTitle,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
              )
            }
            Text(
              text = strings.developedBy,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.textMuted,
            )
            Text(
              text = "Aoussedj Abdelbari",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = colors.textPrimary,
            )
            Text(
              text = strings.openGitHubProfile,
              style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
              color = colors.accentInk,
              modifier = Modifier.clickable { openGithubProfile() },
            )
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
              contentColor = colors.statusExpiredInk,
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
