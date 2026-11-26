package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesStore
import com.example.data.remote.SupabaseApiException
import com.example.data.remote.SupabaseClient
import com.example.domain.model.AuthSession
import com.example.domain.model.Client
import com.example.domain.model.ClientDossier
import com.example.domain.model.ClientFilter
import com.example.domain.model.CoachProfile
import com.example.domain.model.Payment
import com.example.domain.model.PaymentMethod
import com.example.domain.model.PlanTemplate
import com.example.domain.model.Subscription
import com.example.domain.model.SubscriptionStatus
import com.example.domain.model.SupabaseConfig
import com.example.i18n.AppLanguage
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardMetrics(
  val activeCount: Int = 0,
  val expiringSoonCount: Int = 0,
  val expiredCount: Int = 0,
  val monthlyRevenueDa: Int = 0,
  val totalCollectedDa: Int = 0,
  val totalPendingDa: Int = 0,
  val revenueByMethod: Map<PaymentMethod, Int> = emptyMap(),
)

data class SuiviUiState(
  val isInitialized: Boolean = false,
  val language: AppLanguage = AppLanguage.FR,
  val isDarkMode: Boolean = true,
  val supabaseConfig: SupabaseConfig = SupabaseConfig(),
  val isConnectionVerified: Boolean? = null,
  val isTestingConnection: Boolean = false,
  val session: AuthSession? = null,
  val coachProfile: CoachProfile? = null,
  val clients: List<Client> = emptyList(),
  val subscriptions: List<Subscription> = emptyList(),
  val payments: List<Payment> = emptyList(),
  val planTemplates: List<PlanTemplate> = emptyList(),
  val searchQuery: String = "",
  val selectedFilter: ClientFilter = ClientFilter.ALL,
  val isSyncing: Boolean = false,
  val isAuthLoading: Boolean = false,
  val authError: String? = null,
  val authInfoMessage: String? = null,
  val snackbarMessage: String? = null,
) {
  val dossiers: List<ClientDossier>
    get() {
      val subsByClient = subscriptions.groupBy { it.clientId }
      val paysByClient = payments.groupBy { it.clientId }
      return clients
        .filter { !it.isArchived }
        .map { client ->
          val clientSubs =
            (subsByClient[client.id] ?: emptyList()).sortedWith(
              compareByDescending<Subscription> { it.endDate }.thenByDescending { it.createdAt }
            )
          val clientPays =
            (paysByClient[client.id] ?: emptyList()).sortedByDescending { it.paidAt }
          ClientDossier(
            client = client,
            latestSubscription = clientSubs.firstOrNull(),
            subscriptions = clientSubs,
            payments = clientPays,
          )
        }
    }

  val filteredDossiers: List<ClientDossier>
    get() {
      val q = searchQuery.trim().lowercase()
      return dossiers
        .filter { dossier ->
          val matchesQuery =
            q.isEmpty() ||
              dossier.client.fullName.lowercase().contains(q) ||
              dossier.client.phone.lowercase().contains(q) ||
              dossier.client.goal.lowercase().contains(q)
          val matchesFilter =
            when (selectedFilter) {
              ClientFilter.ALL -> true
              ClientFilter.ACTIVE -> dossier.latestSubscription?.status == SubscriptionStatus.ACTIVE
              ClientFilter.EXPIRING_SOON ->
                dossier.latestSubscription?.status == SubscriptionStatus.EXPIRING_SOON
              ClientFilter.EXPIRED -> dossier.latestSubscription?.status == SubscriptionStatus.EXPIRED
              ClientFilter.UNPAID -> dossier.totalDueDa > 0
            }
          matchesQuery && matchesFilter
        }
        .sortedWith(
          compareBy<ClientDossier> {
            when (it.latestSubscription?.status) {
              SubscriptionStatus.EXPIRING_SOON -> 0
              SubscriptionStatus.EXPIRED -> 1
              SubscriptionStatus.ACTIVE -> 2
              null -> 3
            }
          }.thenBy { it.latestSubscription?.daysRemaining ?: Int.MAX_VALUE }
        )
    }

  val urgentDossiers: List<ClientDossier>
    get() =
      dossiers
        .filter {
          val st = it.latestSubscription?.status
          st == SubscriptionStatus.EXPIRING_SOON || st == SubscriptionStatus.EXPIRED
        }
        .sortedBy { it.latestSubscription?.daysRemaining ?: Int.MAX_VALUE }

  val metrics: DashboardMetrics
    get() {
      val allDossiers = dossiers
      val active = allDossiers.count { it.latestSubscription?.status == SubscriptionStatus.ACTIVE }
      val expiring =
        allDossiers.count { it.latestSubscription?.status == SubscriptionStatus.EXPIRING_SOON }
      val expired = allDossiers.count { it.latestSubscription?.status == SubscriptionStatus.EXPIRED }
      val monthPrefix = DateUtils.currentMonthPrefix()
      val monthlyRev =
        payments.filter { it.paidAt.startsWith(monthPrefix) }.sumOf { it.amountDa }
      val totalCollected = payments.sumOf { it.amountDa }
      val totalPending = subscriptions.sumOf { it.remainingBalanceDa }
      val byMethod =
        PaymentMethod.entries.associateWith { method ->
          payments.filter { it.method == method }.sumOf { it.amountDa }
        }
      return DashboardMetrics(
        activeCount = active,
        expiringSoonCount = expiring,
        expiredCount = expired,
        monthlyRevenueDa = monthlyRev,
        totalCollectedDa = totalCollected,
        totalPendingDa = totalPending,
        revenueByMethod = byMethod,
      )
    }
}

class SuiviCoachViewModel(application: Application) : AndroidViewModel(application) {
  private val store = PreferencesStore(application.applicationContext)
  private val supabase = SupabaseClient()

  private val _uiState = MutableStateFlow(SuiviUiState())
  val uiState: StateFlow<SuiviUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      val initial = store.preferencesFlow.first()
      _uiState.update {
        it.copy(
          isInitialized = true,
          language = initial.language,
          isDarkMode = initial.isDarkMode,
          supabaseConfig = initial.supabaseConfig,
          session = initial.session,
          planTemplates = initial.planTemplates,
          clients = initial.cachedClients,
          subscriptions = initial.cachedSubscriptions,
          payments = initial.cachedPayments,
          coachProfile = initial.coachProfile,
        )
      }
      if (initial.supabaseConfig.isConfigured && initial.session != null) {
        syncAllData()
      }
    }
  }

  fun setLanguage(language: AppLanguage) {
    _uiState.update { it.copy(language = language) }
    viewModelScope.launch { store.setLanguage(language) }
  }

  fun toggleDarkMode() {
    val next = !_uiState.value.isDarkMode
    _uiState.update { it.copy(isDarkMode = next) }
    viewModelScope.launch { store.setDarkMode(next) }
  }

  fun setSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun setSelectedFilter(filter: ClientFilter) {
    _uiState.update { it.copy(selectedFilter = filter) }
  }

  fun clearSnackbar() {
    _uiState.update { it.copy(snackbarMessage = null) }
  }

  fun clearAuthMessages() {
    _uiState.update { it.copy(authError = null, authInfoMessage = null) }
  }

  fun showMessage(msg: String) {
    _uiState.update { it.copy(snackbarMessage = msg) }
  }

  fun saveAndTestSupabaseConfig(url: String, anonKey: String) {
    val config = SupabaseConfig(url = url.trim(), anonKey = anonKey.trim())
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          supabaseConfig = config,
          isTestingConnection = true,
          isConnectionVerified = null,
        )
      }
      store.saveSupabaseConfig(config)
      val ok = supabase.testConnection(config)
      _uiState.update {
        it.copy(
          isTestingConnection = false,
          isConnectionVerified = ok,
        )
      }
    }
  }

  fun signIn(email: String, password: String) {
    val config = _uiState.value.supabaseConfig
    if (!config.isConfigured) {
      _uiState.update { it.copy(authError = "Configurez d'abord SUPABASE_URL et SUPABASE_ANON_KEY.") }
      return
    }
    viewModelScope.launch {
      _uiState.update { it.copy(isAuthLoading = true, authError = null, authInfoMessage = null) }
      try {
        val session = supabase.signIn(config, email, password)
        store.saveSession(session)
        _uiState.update { it.copy(isAuthLoading = false, session = session) }
        syncAllData()
      } catch (e: Exception) {
        _uiState.update { it.copy(isAuthLoading = false, authError = e.message ?: "Auth error") }
      }
    }
  }

  fun signUp(fullName: String, email: String, password: String, confirmationSentMsg: String) {
    val config = _uiState.value.supabaseConfig
    if (!config.isConfigured) {
      _uiState.update { it.copy(authError = "Configurez d'abord SUPABASE_URL et SUPABASE_ANON_KEY.") }
      return
    }
    viewModelScope.launch {
      _uiState.update { it.copy(isAuthLoading = true, authError = null, authInfoMessage = null) }
      try {
        val session = supabase.signUp(config, email, password, fullName)
        if (session != null) {
          store.saveSession(session)
          _uiState.update { it.copy(isAuthLoading = false, session = session) }
          syncAllData()
        } else {
          _uiState.update {
            it.copy(
              isAuthLoading = false,
              authInfoMessage = confirmationSentMsg,
            )
          }
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(isAuthLoading = false, authError = e.message ?: "Sign up error") }
      }
    }
  }

  fun resetPassword(email: String, sentMessage: String) {
    val config = _uiState.value.supabaseConfig
    if (!config.isConfigured) {
      _uiState.update { it.copy(authError = "Configurez d'abord SUPABASE_URL et SUPABASE_ANON_KEY.") }
      return
    }
    viewModelScope.launch {
      _uiState.update { it.copy(isAuthLoading = true, authError = null, authInfoMessage = null) }
      try {
        supabase.resetPassword(config, email)
        _uiState.update { it.copy(isAuthLoading = false, authInfoMessage = sentMessage) }
      } catch (e: Exception) {
        _uiState.update { it.copy(isAuthLoading = false, authError = e.message ?: "Reset error") }
      }
    }
  }

  fun signOut() {
    val config = _uiState.value.supabaseConfig
    val token = _uiState.value.session?.accessToken
    viewModelScope.launch {
      if (config.isConfigured && !token.isNullOrBlank()) {
        supabase.signOut(config, token)
      }
      store.saveSession(null)
      store.saveCoachProfile(null)
      _uiState.update {
        it.copy(
          session = null,
          coachProfile = null,
          clients = emptyList(),
          subscriptions = emptyList(),
          payments = emptyList(),
        )
      }
    }
  }

  fun syncAllData() {
    val config = _uiState.value.supabaseConfig
    if (!config.isConfigured || _uiState.value.session == null) return
    viewModelScope.launch {
      _uiState.update { it.copy(isSyncing = true) }
      try {
        val (profile, clients, subs, payments) =
          withValidToken { token, session ->
            val p = supabase.fetchCoachProfile(config, token, session.userId)
            val c = supabase.fetchClients(config, token)
            val s = supabase.fetchSubscriptions(config, token)
            val pay = supabase.fetchPayments(config, token)
            QuadData(p, c, s, pay)
          }
        store.saveSyncedData(clients, subs, payments)
        val currentSession = _uiState.value.session
        val resolvedProfile =
          profile
            ?: currentSession?.let {
              CoachProfile(
                id = it.userId,
                fullName = it.fullName,
                businessName = "SUIVICOACH Studio",
                phone = "",
                email = it.email,
                avatarUrl = "",
              )
            }
        store.saveCoachProfile(resolvedProfile)
        _uiState.update {
          it.copy(
            isSyncing = false,
            coachProfile = resolvedProfile,
            clients = clients,
            subscriptions = subs,
            payments = payments,
          )
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
            isSyncing = false,
            snackbarMessage = e.message ?: "Sync failed",
          )
        }
      }
    }
  }

  fun updateCoachProfile(fullName: String, businessName: String, phone: String, avatarUrl: String) {
    val config = _uiState.value.supabaseConfig
    val session = _uiState.value.session ?: return
    viewModelScope.launch {
      try {
        val updated =
          withValidToken { token, _ ->
            supabase.upsertCoachProfile(
              config,
              token,
              CoachProfile(
                id = session.userId,
                fullName = fullName.trim(),
                businessName = businessName.trim(),
                phone = phone.trim(),
                email = session.email,
                avatarUrl = avatarUrl,
              ),
            )
          }
        store.saveCoachProfile(updated)
        _uiState.update {
          it.copy(
            coachProfile = updated,
            snackbarMessage = "OK",
          )
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(snackbarMessage = e.message ?: "Error") }
      }
    }
  }

  fun createClient(
    fullName: String,
    phone: String,
    email: String,
    goal: String,
    notes: String,
    includeSubscription: Boolean,
    planName: String,
    priceDa: Int,
    durationDays: Int,
    startDateIso: String,
    sessionsTotal: Int?,
    amountPaidDa: Int,
    paymentMethod: PaymentMethod,
  ) {
    val config = _uiState.value.supabaseConfig
    val session = _uiState.value.session ?: return
    viewModelScope.launch {
      _uiState.update { it.copy(isSyncing = true) }
      try {
        val createdClient =
          withValidToken { token, validSession ->
            supabase.createClient(
              config,
              token,
              Client(
                id = "",
                coachId = validSession.userId,
                fullName = fullName.trim(),
                phone = phone.trim(),
                email = email.trim(),
                goal = goal.trim(),
                notes = notes.trim(),
              ),
            )
          }

        val createdSub =
          if (includeSubscription) {
            val endDateIso = DateUtils.addDaysToIso(startDateIso, durationDays)
            val paidClamped = amountPaidDa.coerceIn(0, priceDa.coerceAtLeast(0))
            withValidToken { token, validSession ->
              supabase.createSubscription(
                config,
                token,
                Subscription(
                  id = "",
                  coachId = validSession.userId,
                  clientId = createdClient.id,
                  planName = planName.trim().ifEmpty { "Coaching 1-on-1" },
                  priceDa = priceDa,
                  durationDays = durationDays.coerceAtLeast(1),
                  startDate = startDateIso,
                  endDate = endDateIso,
                  sessionsTotal = sessionsTotal,
                  sessionsUsed = 0,
                  amountPaidDa = paidClamped,
                ),
              )
            }
          } else {
            null
          }

        val createdPayment =
          if (createdSub != null && amountPaidDa > 0) {
            withValidToken { token, validSession ->
              supabase.createPayment(
                config,
                token,
                Payment(
                  id = "",
                  coachId = validSession.userId,
                  clientId = createdClient.id,
                  subscriptionId = createdSub.id,
                  amountDa = amountPaidDa.coerceIn(1, priceDa.coerceAtLeast(amountPaidDa)),
                  paidAt = DateUtils.nowIsoTimestamp(),
                  methodWire = paymentMethod.wireValue,
                  note = createdSub.planName,
                ),
              )
            }
          } else {
            null
          }

        val newClients = listOf(createdClient) + _uiState.value.clients
        val newSubs =
          if (createdSub != null) listOf(createdSub) + _uiState.value.subscriptions
          else _uiState.value.subscriptions
        val newPays =
          if (createdPayment != null) listOf(createdPayment) + _uiState.value.payments
          else _uiState.value.payments

        store.saveSyncedData(newClients, newSubs, newPays)
        _uiState.update {
          it.copy(
            isSyncing = false,
            clients = newClients,
            subscriptions = newSubs,
            payments = newPays,
          )
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(isSyncing = false, snackbarMessage = e.message ?: "Error") }
      }
    }
  }

  fun updateClient(client: Client) {
    val config = _uiState.value.supabaseConfig
    viewModelScope.launch {
      try {
        val updated =
          withValidToken { token, _ -> supabase.updateClient(config, token, client) }
        val newClients = _uiState.value.clients.map { if (it.id == updated.id) updated else it }
        store.saveSyncedData(newClients, _uiState.value.subscriptions, _uiState.value.payments)
        _uiState.update { it.copy(clients = newClients) }
      } catch (e: Exception) {
        _uiState.update { it.copy(snackbarMessage = e.message ?: "Error") }
      }
    }
  }

  fun deleteClient(clientId: String, onDeleted: () -> Unit = {}) {
    val config = _uiState.value.supabaseConfig
    viewModelScope.launch {
      try {
        withValidToken { token, _ -> supabase.deleteClient(config, token, clientId) }
        val newClients = _uiState.value.clients.filterNot { it.id == clientId }
        val newSubs = _uiState.value.subscriptions.filterNot { it.clientId == clientId }
        val newPays = _uiState.value.payments.filterNot { it.clientId == clientId }
        store.saveSyncedData(newClients, newSubs, newPays)
        _uiState.update {
          it.copy(clients = newClients, subscriptions = newSubs, payments = newPays)
        }
        onDeleted()
      } catch (e: Exception) {
        _uiState.update { it.copy(snackbarMessage = e.message ?: "Error") }
      }
    }
  }

  fun renewSubscription(
    clientId: String,
    planName: String,
    priceDa: Int,
    durationDays: Int,
    startDateIso: String,
    sessionsTotal: Int?,
    amountPaidDa: Int,
    paymentMethod: PaymentMethod,
    note: String,
  ) {
    val config = _uiState.value.supabaseConfig
    viewModelScope.launch {
      _uiState.update { it.copy(isSyncing = true) }
      try {
        val endDateIso = DateUtils.addDaysToIso(startDateIso, durationDays)
        val paidClamped = amountPaidDa.coerceIn(0, priceDa.coerceAtLeast(0))
        val createdSub =
          withValidToken { token, validSession ->
            supabase.createSubscription(
              config,
              token,
              Subscription(
                id = "",
                coachId = validSession.userId,
                clientId = clientId,
                planName = planName.trim().ifEmpty { "Coaching 1-on-1" },
                priceDa = priceDa,
                durationDays = durationDays.coerceAtLeast(1),
                startDate = startDateIso,
                endDate = endDateIso,
                sessionsTotal = sessionsTotal,
                sessionsUsed = 0,
                amountPaidDa = paidClamped,
                notes = note.trim(),
              ),
            )
          }

        val createdPayment =
          if (paidClamped > 0) {
            withValidToken { token, validSession ->
              supabase.createPayment(
                config,
                token,
                Payment(
                  id = "",
                  coachId = validSession.userId,
                  clientId = clientId,
                  subscriptionId = createdSub.id,
                  amountDa = paidClamped,
                  paidAt = DateUtils.nowIsoTimestamp(),
                  methodWire = paymentMethod.wireValue,
                  note = createdSub.planName,
                ),
              )
            }
          } else {
            null
          }

        val newSubs = listOf(createdSub) + _uiState.value.subscriptions
        val newPays =
          if (createdPayment != null) listOf(createdPayment) + _uiState.value.payments
          else _uiState.value.payments

        store.saveSyncedData(_uiState.value.clients, newSubs, newPays)
        _uiState.update {
          it.copy(isSyncing = false, subscriptions = newSubs, payments = newPays)
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(isSyncing = false, snackbarMessage = e.message ?: "Error") }
      }
    }
  }

  fun adjustSessionsUsed(subscription: Subscription, delta: Int) {
    val max = subscription.sessionsTotal ?: Int.MAX_VALUE
    val nextUsed = (subscription.sessionsUsed + delta).coerceIn(0, max)
    if (nextUsed == subscription.sessionsUsed) return
    val updatedLocal = subscription.copy(sessionsUsed = nextUsed)
    val config = _uiState.value.supabaseConfig
    viewModelScope.launch {
      try {
        val updated =
          withValidToken { token, _ -> supabase.updateSubscription(config, token, updatedLocal) }
        val newSubs = _uiState.value.subscriptions.map { if (it.id == updated.id) updated else it }
        store.saveSyncedData(_uiState.value.clients, newSubs, _uiState.value.payments)
        _uiState.update { it.copy(subscriptions = newSubs) }
      } catch (e: Exception) {
        _uiState.update { it.copy(snackbarMessage = e.message ?: "Error") }
      }
    }
  }

  fun recordPayment(
    clientId: String,
    subscription: Subscription?,
    amountDa: Int,
    method: PaymentMethod,
    note: String,
  ) {
    if (amountDa <= 0) return
    val config = _uiState.value.supabaseConfig
    viewModelScope.launch {
      _uiState.update { it.copy(isSyncing = true) }
      try {
        val createdPayment =
          withValidToken { token, validSession ->
            supabase.createPayment(
              config,
              token,
              Payment(
                id = "",
                coachId = validSession.userId,
                clientId = clientId,
                subscriptionId = subscription?.id,
                amountDa = amountDa,
                paidAt = DateUtils.nowIsoTimestamp(),
                methodWire = method.wireValue,
                note = note.trim(),
              ),
            )
          }

        val updatedSub =
          if (subscription != null) {
            val newPaid = (subscription.amountPaidDa + amountDa).coerceAtMost(subscription.priceDa)
            withValidToken { token, _ ->
              supabase.updateSubscription(config, token, subscription.copy(amountPaidDa = newPaid))
            }
          } else {
            null
          }

        val newPays = listOf(createdPayment) + _uiState.value.payments
        val newSubs =
          if (updatedSub != null) {
            _uiState.value.subscriptions.map { if (it.id == updatedSub.id) updatedSub else it }
          } else {
            _uiState.value.subscriptions
          }
        store.saveSyncedData(_uiState.value.clients, newSubs, newPays)
        _uiState.update {
          it.copy(isSyncing = false, subscriptions = newSubs, payments = newPays)
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(isSyncing = false, snackbarMessage = e.message ?: "Error") }
      }
    }
  }

  fun addPlanTemplate(name: String, durationDays: Int, priceDa: Int, sessionsTotal: Int?) {
    val tpl =
      PlanTemplate(
        id = "tpl_${System.currentTimeMillis()}",
        name = name.trim(),
        durationDays = durationDays.coerceAtLeast(1),
        priceDa = priceDa.coerceAtLeast(0),
        sessionsTotal = sessionsTotal,
      )
    val next = _uiState.value.planTemplates + tpl
    _uiState.update { it.copy(planTemplates = next) }
    viewModelScope.launch { store.savePlanTemplates(next) }
  }

  fun deletePlanTemplate(templateId: String) {
    val next = _uiState.value.planTemplates.filterNot { it.id == templateId }
    _uiState.update { it.copy(planTemplates = next) }
    viewModelScope.launch { store.savePlanTemplates(next) }
  }

  fun buildPaymentsCsv(): String {
    val clientsById = _uiState.value.clients.associateBy { it.id }
    val sb = StringBuilder()
    sb.appendLine("Date,Client,Method,Amount_DA,Note")
    _uiState.value.payments.forEach { p ->
      val clientName = clientsById[p.clientId]?.fullName?.replace(",", " ") ?: p.clientId
      val noteClean = p.note.replace(",", " ")
      sb.appendLine("${DateUtils.formatDisplay(p.paidAt)},$clientName,${p.methodWire},${p.amountDa},$noteClean")
    }
    return sb.toString()
  }

  private suspend fun <T> withValidToken(block: suspend (String, AuthSession) -> T): T {
    val config = _uiState.value.supabaseConfig
    var session =
      _uiState.value.session ?: throw SupabaseApiException(401, "No active session")
    val nowSec = System.currentTimeMillis() / 1000L
    if (session.expiresAtEpochSec > 0 && nowSec >= session.expiresAtEpochSec - 60L && session.refreshToken.isNotBlank()) {
      session = supabase.refreshSession(config, session.refreshToken)
      store.saveSession(session)
      _uiState.update { it.copy(session = session) }
    }
    return try {
      block(session.accessToken, session)
    } catch (e: SupabaseApiException) {
      if (e.statusCode == 401 && session.refreshToken.isNotBlank()) {
        val refreshed = supabase.refreshSession(config, session.refreshToken)
        store.saveSession(refreshed)
        _uiState.update { it.copy(session = refreshed) }
        block(refreshed.accessToken, refreshed)
      } else {
        throw e
      }
    }
  }

  private data class QuadData(
    val profile: CoachProfile?,
    val clients: List<Client>,
    val subscriptions: List<Subscription>,
    val payments: List<Payment>,
  )
}
