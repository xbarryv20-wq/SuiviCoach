package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.Client
import com.example.domain.model.ClientDossier
import com.example.domain.model.Subscription
import com.example.i18n.stringsFor
import com.example.ui.components.SuiviCoachLogo
import com.example.ui.screens.AddEditClientDialog
import com.example.ui.screens.AddPaymentDialog
import com.example.ui.screens.AddPlanTemplateDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ClientDetailScreen
import com.example.ui.screens.ClientsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinancesScreen
import com.example.ui.screens.RenewSubscriptionDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.SuiviCoachTheme
import com.example.ui.theme.SuiviTheme
import com.example.ui.viewmodel.SuiviCoachViewModel

private enum class MainTab {
  DASHBOARD,
  CLIENTS,
  FINANCES,
  SETTINGS,
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { SuiviCoachApp() }
  }
}

@Composable
fun SuiviCoachApp(viewModel: SuiviCoachViewModel = viewModel()) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val strings = stringsFor(state.language)

  var showAddClientDialog by remember { mutableStateOf(false) }
  var editingClient by remember { mutableStateOf<Client?>(null) }
  var renewingDossier by remember { mutableStateOf<ClientDossier?>(null) }
  var paymentTarget by remember { mutableStateOf<Pair<ClientDossier, Subscription?>?>(null) }
  var showAddTemplateDialog by remember { mutableStateOf(false) }

  var selectedTab by remember { mutableStateOf(MainTab.DASHBOARD) }
  var selectedClientId by remember { mutableStateOf<String?>(null) }

  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(state.snackbarMessage) {
    val msg = state.snackbarMessage
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearSnackbar()
    }
  }

  SuiviCoachTheme(darkTheme = state.isDarkMode, language = state.language) {
    val colors = SuiviTheme.colors

    if (!state.isInitialized) {
      Box(modifier = Modifier.fillMaxSize().background(colors.background))
      return@SuiviCoachTheme
    }

    if (state.session == null) {
      Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
      ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          AuthScreen(
            language = state.language,
            isDarkMode = state.isDarkMode,
            strings = strings,
            isAuthLoading = state.isAuthLoading,
            authError = state.authError,
            authInfoMessage = state.authInfoMessage,
            onSelectLanguage = viewModel::setLanguage,
            onToggleTheme = viewModel::toggleDarkMode,
            onSignIn = viewModel::signIn,
            onSignUp = { fullName, email, password ->
              viewModel.signUp(fullName, email, password, strings.resetEmailSent)
            },
            onResetPassword = { email ->
              viewModel.resetPassword(email, strings.resetEmailSent)
            },
            onClearMessages = viewModel::clearAuthMessages,
          )
        }
      }
    } else {
      val dossiers = state.dossiers
      val clientsById = remember(state.clients) { state.clients.associate { it.id to it.fullName } }
      val activeDossier = selectedClientId?.let { id -> dossiers.firstOrNull { it.client.id == id } }

      if (showAddClientDialog || editingClient != null) {
        AddEditClientDialog(
          existingClient = editingClient,
          templates = state.planTemplates,
          strings = strings,
          onSaveNew = {
            fullName,
            phone,
            email,
            goal,
            notes,
            includeSub,
            planName,
            priceDa,
            durationDays,
            startDate,
            sessionsTotal,
            amountPaidDa,
            method ->
            viewModel.createClient(
              fullName = fullName,
              phone = phone,
              email = email,
              goal = goal,
              notes = notes,
              includeSubscription = includeSub,
              planName = planName,
              priceDa = priceDa,
              durationDays = durationDays,
              startDateIso = startDate,
              sessionsTotal = sessionsTotal,
              amountPaidDa = amountPaidDa,
              paymentMethod = method,
            )
            showAddClientDialog = false
          },
          onSaveEdit = { updated ->
            viewModel.updateClient(updated)
            editingClient = null
          },
          onDismiss = {
            showAddClientDialog = false
            editingClient = null
          },
        )
      }

      renewingDossier?.let { dossier ->
        RenewSubscriptionDialog(
          clientName = dossier.client.fullName,
          latestSubscription = dossier.latestSubscription,
          templates = state.planTemplates,
          strings = strings,
          onConfirm = { planName, priceDa, durationDays, startDate, sessionsTotal, paidDa, method, note ->
            viewModel.renewSubscription(
              clientId = dossier.client.id,
              planName = planName,
              priceDa = priceDa,
              durationDays = durationDays,
              startDateIso = startDate,
              sessionsTotal = sessionsTotal,
              amountPaidDa = paidDa,
              paymentMethod = method,
              note = note,
            )
            renewingDossier = null
          },
          onDismiss = { renewingDossier = null },
        )
      }

      paymentTarget?.let { (dossier, sub) ->
        AddPaymentDialog(
          clientName = dossier.client.fullName,
          suggestedAmountDa = sub?.remainingBalanceDa ?: 0,
          strings = strings,
          onConfirm = { amountDa, method, note ->
            viewModel.recordPayment(
              clientId = dossier.client.id,
              subscription = sub,
              amountDa = amountDa,
              method = method,
              note = note,
            )
            paymentTarget = null
          },
          onDismiss = { paymentTarget = null },
        )
      }

      if (showAddTemplateDialog) {
        AddPlanTemplateDialog(
          strings = strings,
          onAdd = { name, days, price, sessions ->
            viewModel.addPlanTemplate(name, days, price, sessions)
            showAddTemplateDialog = false
          },
          onDismiss = { showAddTemplateDialog = false },
        )
      }

      val navItems =
        listOf(
          Triple(MainTab.DASHBOARD, strings.navDashboard, Icons.Filled.Dashboard to Icons.Outlined.Dashboard),
          Triple(MainTab.CLIENTS, strings.navClients, Icons.Filled.Group to Icons.Outlined.Group),
          Triple(
            MainTab.FINANCES,
            strings.navFinances,
            Icons.Filled.AccountBalanceWallet to Icons.Outlined.AccountBalanceWallet,
          ),
          Triple(MainTab.SETTINGS, strings.navSettings, Icons.Filled.Settings to Icons.Outlined.Settings),
        )

      BoxWithConstraints(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val useRail = maxWidth >= 840.dp

        Row(modifier = Modifier.fillMaxSize()) {
          if (useRail && activeDossier == null) {
            NavigationRail(
              containerColor = colors.cardSurface,
              modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
              header = {
                SuiviCoachLogo(size = 40.dp, modifier = Modifier.padding(vertical = 12.dp))
              },
            ) {
              navItems.forEach { (tab, label, icons) ->
                val selected = selectedTab == tab
                NavigationRailItem(
                  selected = selected,
                  onClick = { selectedTab = tab },
                  icon = {
                    Icon(
                      imageVector = if (selected) icons.first else icons.second,
                      contentDescription = label,
                    )
                  },
                  label = { Text(label) },
                  colors =
                    NavigationRailItemDefaults.colors(
                      selectedIconColor = colors.accentInk,
                      selectedTextColor = colors.accentInk,
                      unselectedIconColor = colors.textMuted,
                      unselectedTextColor = colors.textMuted,
                      indicatorColor = colors.softSurface,
                    ),
                )
              }
            }
          }

          Scaffold(
            modifier = Modifier.weight(1f).fillMaxSize(),
            containerColor = colors.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
              if (activeDossier == null && (selectedTab == MainTab.DASHBOARD || selectedTab == MainTab.CLIENTS)) {
                FloatingActionButton(
                  onClick = { showAddClientDialog = true },
                  containerColor = colors.accentBlue,
                  contentColor = Color.White,
                  shape = CircleShape,
                  modifier = Modifier.testTag("fab_add_client"),
                ) {
                  Icon(Icons.Filled.Add, contentDescription = strings.addClient)
                }
              }
            },
            bottomBar = {
              if (!useRail && activeDossier == null) {
                NavigationBar(
                  containerColor = colors.cardSurface,
                  windowInsets = WindowInsets.navigationBars,
                ) {
                  navItems.forEach { (tab, label, icons) ->
                    val selected = selectedTab == tab
                    NavigationBarItem(
                      selected = selected,
                      onClick = { selectedTab = tab },
                      icon = {
                        Icon(
                          imageVector = if (selected) icons.first else icons.second,
                          contentDescription = label,
                        )
                      },
                      label = { Text(label, maxLines = 1) },
                      colors =
                        NavigationBarItemDefaults.colors(
                          selectedIconColor = colors.accentInk,
                          selectedTextColor = colors.accentInk,
                          unselectedIconColor = colors.textMuted,
                          unselectedTextColor = colors.textMuted,
                          indicatorColor = colors.softSurface,
                        ),
                      modifier = Modifier.testTag("nav_${tab.name.lowercase()}"),
                    )
                  }
                }
              }
            },
          ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
              if (activeDossier != null) {
                ClientDetailScreen(
                  dossier = activeDossier,
                  strings = strings,
                  onBack = { selectedClientId = null },
                  onEditClient = { editingClient = activeDossier.client },
                  onDeleteClient = {
                    viewModel.deleteClient(activeDossier.client.id) { selectedClientId = null }
                  },
                  onRenewSubscription = { renewingDossier = activeDossier },
                  onAddPayment = { sub -> paymentTarget = activeDossier to sub },
                  onAdjustSession = { sub, delta -> viewModel.adjustSessionsUsed(sub, delta) },
                )
              } else {
                when (selectedTab) {
                  MainTab.DASHBOARD ->
                    DashboardScreen(
                      coachName =
                        state.coachProfile?.fullName?.ifBlank { state.session?.fullName.orEmpty() }
                          ?: state.session?.fullName.orEmpty(),
                      coachAvatarUrl = state.coachProfile?.avatarUrl.orEmpty(),
                      language = state.language,
                      isDarkMode = state.isDarkMode,
                      isSyncing = state.isSyncing,
                      metrics = state.metrics,
                      urgentDossiers = state.urgentDossiers,
                      recentPayments = state.payments,
                      clientsById = clientsById,
                      strings = strings,
                      onSelectLanguage = viewModel::setLanguage,
                      onToggleTheme = viewModel::toggleDarkMode,
                      onSync = viewModel::syncAllData,
                      onOpenClientDetail = { id -> selectedClientId = id },
                      onQuickRenew = { dossier -> renewingDossier = dossier },
                    )
                  MainTab.CLIENTS ->
                    ClientsScreen(
                      dossiers = state.filteredDossiers,
                      searchQuery = state.searchQuery,
                      selectedFilter = state.selectedFilter,
                      strings = strings,
                      onSearchChange = viewModel::setSearchQuery,
                      onFilterChange = viewModel::setSelectedFilter,
                      onOpenClientDetail = { id -> selectedClientId = id },
                    )
                  MainTab.FINANCES ->
                    FinancesScreen(
                      metrics = state.metrics,
                      payments = state.payments,
                      clientsById = clientsById,
                      strings = strings,
                      buildCsv = viewModel::buildPaymentsCsv,
                    )
                  MainTab.SETTINGS ->
                    SettingsScreen(
                      session = state.session,
                      profile = state.coachProfile,
                      language = state.language,
                      isDarkMode = state.isDarkMode,
                      templates = state.planTemplates,
                      strings = strings,
                      onSelectLanguage = viewModel::setLanguage,
                      onToggleTheme = viewModel::toggleDarkMode,
                      onUpdateProfile = viewModel::updateCoachProfile,
                      onOpenAddTemplate = { showAddTemplateDialog = true },
                      onDeleteTemplate = viewModel::deletePlanTemplate,
                      onSignOut = viewModel::signOut,
                    )
                }
              }
            }
          }
        }
      }
    }
  }
}
