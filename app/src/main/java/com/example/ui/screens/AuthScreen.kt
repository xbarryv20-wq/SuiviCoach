package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.components.LanguageSwitcherPill
import com.example.ui.components.SuiviCoachLogo
import com.example.ui.components.ThemeToggleButton
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.SuiviTheme

private enum class AuthTab {
  SIGN_IN,
  SIGN_UP,
  RESET,
}

@Composable
fun AuthScreen(
  language: AppLanguage,
  isDarkMode: Boolean,
  strings: AppStrings,
  isAuthLoading: Boolean,
  authError: String?,
  authInfoMessage: String?,
  onSelectLanguage: (AppLanguage) -> Unit,
  onToggleTheme: () -> Unit,
  onSignIn: (String, String) -> Unit,
  onSignUp: (String, String, String) -> Unit,
  onResetPassword: (String) -> Unit,
  onClearMessages: () -> Unit,
) {
  val colors = SuiviTheme.colors
  var activeTab by remember { mutableStateOf(AuthTab.SIGN_IN) }
  var fullName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(colors.background)
        .safeDrawingPadding(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 560.dp)
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      // Top controls: Language & Theme
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        LanguageSwitcherPill(currentLanguage = language, onSelect = onSelectLanguage)
        ThemeToggleButton(
          isDarkMode = isDarkMode,
          onToggle = onToggleTheme,
          contentDescription = strings.darkModeLabel,
        )
      }

      // Hero Card with generated dark luxury gym artwork + SUIVICOACH logo
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(176.dp)
            .clip(RoundedCornerShape(24.dp)),
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_coach_hero_1795646312749),
          contentDescription = strings.appName,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
        Box(
          modifier =
            Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors =
                    listOf(
                      MidnightNavy.copy(alpha = 0.45f),
                      MidnightNavy.copy(alpha = 0.92f),
                    )
                )
              ),
        )
        Column(
          modifier = Modifier.fillMaxSize().padding(20.dp),
          verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            SuiviCoachLogo(size = 48.dp)
            Column {
              Text(
                text = strings.appName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
              )
              Text(
                text = "Algeria • Personal Coaching",
                style = MaterialTheme.typography.labelMedium,
                color = colors.accentCyan,
              )
            }
          }

          Text(
            text = strings.tagline,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFE2E8F0),
          )
        }
      }

      // Auth Card (Sign In / Sign Up / Reset)
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.cardSurface,
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          // Segmented Tab Switcher
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.softSurface)
                .padding(4.dp),
          ) {
            listOf(AuthTab.SIGN_IN to strings.signIn, AuthTab.SIGN_UP to strings.signUp).forEach {
              (tab, label) ->
              val selected = activeTab == tab
              Box(
                modifier =
                  Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                      if (selected) {
                        Brush.horizontalGradient(listOf(colors.accentBlue, colors.accentCyan))
                      } else {
                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                      }
                    )
                    .clickable {
                      activeTab = tab
                      onClearMessages()
                    }
                    .padding(vertical = 11.dp)
                    .testTag("auth_tab_${tab.name.lowercase()}"),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelLarge,
                  color = if (selected) Color.White else colors.textMuted,
                )
              }
            }
          }

          if (activeTab == AuthTab.SIGN_UP) {
            OutlinedTextField(
              value = fullName,
              onValueChange = { fullName = it },
              label = { Text(strings.fullNameLabel) },
              leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("auth_fullname_input"),
            )
          }

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(strings.emailLabel) },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
          )

          if (activeTab != AuthTab.RESET) {
            OutlinedTextField(
              value = password,
              onValueChange = { password = it },
              label = { Text(strings.passwordLabel) },
              leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
              trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                  Icon(
                    imageVector =
                      if (passwordVisible) Icons.Filled.VisibilityOff
                      else Icons.Filled.Visibility,
                    contentDescription = null,
                  )
                }
              },
              visualTransformation =
                if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
            )
          }

          if (!authError.isNullOrBlank()) {
            Text(
              text = authError,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.statusExpired,
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(colors.statusExpired.copy(alpha = 0.12f))
                  .padding(10.dp),
            )
          }

          if (!authInfoMessage.isNullOrBlank()) {
            Text(
              text = authInfoMessage,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.statusActive,
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(colors.statusActive.copy(alpha = 0.12f))
                  .padding(10.dp),
            )
          }

          Button(
            onClick = {
              when (activeTab) {
                AuthTab.SIGN_IN -> onSignIn(email, password)
                AuthTab.SIGN_UP -> onSignUp(fullName, email, password)
                AuthTab.RESET -> onResetPassword(email)
              }
            },
            enabled =
              !isAuthLoading &&
                email.isNotBlank() &&
                (activeTab == AuthTab.RESET || password.length >= 6),
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("auth_submit_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accentBlue),
          ) {
            if (isAuthLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
              )
              Spacer(Modifier.width(8.dp))
            }
            Text(
              text =
                when (activeTab) {
                  AuthTab.SIGN_IN -> strings.signIn
                  AuthTab.SIGN_UP -> strings.signUp
                  AuthTab.RESET -> strings.sendResetEmail
                },
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White,
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
          ) {
            TextButton(
              onClick = {
                activeTab =
                  if (activeTab == AuthTab.RESET) AuthTab.SIGN_IN else AuthTab.RESET
                onClearMessages()
              },
              modifier = Modifier.testTag("auth_forgot_password_btn"),
            ) {
              Text(
                text =
                  if (activeTab == AuthTab.RESET) strings.signIn
                  else strings.forgotPasswordQuestion,
                color = colors.accentCyan,
              )
            }
          }
        }
      }

      Spacer(Modifier.height(12.dp))
    }
  }
}
