package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import com.example.domain.model.AuthSession
import com.example.domain.model.Client
import com.example.domain.model.CoachProfile
import com.example.domain.model.Payment
import com.example.domain.model.PlanTemplate
import com.example.domain.model.Subscription
import com.example.domain.model.SupabaseConfig
import com.example.i18n.AppLanguage
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.suiviDataStore: DataStore<Preferences> by preferencesDataStore(name = "suivicoach_prefs")

class PreferencesStore(private val context: Context) {
  private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

  private val planListAdapter =
    moshi.adapter<List<PlanTemplate>>(
      Types.newParameterizedType(List::class.java, PlanTemplate::class.java)
    )
  private val clientListAdapter =
    moshi.adapter<List<Client>>(Types.newParameterizedType(List::class.java, Client::class.java))
  private val subscriptionListAdapter =
    moshi.adapter<List<Subscription>>(
      Types.newParameterizedType(List::class.java, Subscription::class.java)
    )
  private val paymentListAdapter =
    moshi.adapter<List<Payment>>(Types.newParameterizedType(List::class.java, Payment::class.java))
  private val coachProfileAdapter = moshi.adapter(CoachProfile::class.java)

  private object Keys {
    val SUPABASE_URL = stringPreferencesKey("supabase_url")
    val SUPABASE_ANON_KEY = stringPreferencesKey("supabase_anon_key")
    val ACCESS_TOKEN = stringPreferencesKey("auth_access_token")
    val REFRESH_TOKEN = stringPreferencesKey("auth_refresh_token")
    val USER_ID = stringPreferencesKey("auth_user_id")
    val USER_EMAIL = stringPreferencesKey("auth_email")
    val USER_FULL_NAME = stringPreferencesKey("auth_full_name")
    val EXPIRES_AT = longPreferencesKey("auth_expires_at")
    val LANGUAGE = stringPreferencesKey("app_language")
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val PLAN_TEMPLATES = stringPreferencesKey("plan_templates_json")
    val CACHED_CLIENTS = stringPreferencesKey("cached_clients_json")
    val CACHED_SUBSCRIPTIONS = stringPreferencesKey("cached_subscriptions_json")
    val CACHED_PAYMENTS = stringPreferencesKey("cached_payments_json")
    val COACH_PROFILE = stringPreferencesKey("coach_profile_json")
  }

  val defaultTemplates: List<PlanTemplate> =
    listOf(
      PlanTemplate(id = "tpl_1m", name = "Coaching 1-on-1 (1 Mois)", durationDays = 30, priceDa = 5000, sessionsTotal = 12),
      PlanTemplate(id = "tpl_3m", name = "Pack Transformation (3 Mois)", durationDays = 90, priceDa = 13500, sessionsTotal = 36),
      PlanTemplate(id = "tpl_12s", name = "Pack 12 Séances", durationDays = 45, priceDa = 6000, sessionsTotal = 12),
      PlanTemplate(id = "tpl_online", name = "Suivi Online + Nutrition", durationDays = 30, priceDa = 3500, sessionsTotal = null),
    )

  val preferencesFlow: Flow<PersistedAppState> =
    context.suiviDataStore.data.map { prefs ->
      val lang =
        AppLanguage.entries.firstOrNull { it.code == prefs[Keys.LANGUAGE] } ?: AppLanguage.FR
      val isDark = prefs[Keys.DARK_MODE] ?: true
      val buildUrl =
        BuildConfig.SUPABASE_URL
          .takeIf { it.isNotBlank() && !it.contains("YOUR_SUPABASE") }
          ?: "https://pcyowtmdwzptcbwbddpr.supabase.co"
      val buildKey =
        BuildConfig.SUPABASE_ANON_KEY
          .takeIf { it.isNotBlank() && !it.contains("YOUR_SUPABASE") }
          .orEmpty()
      val config = SupabaseConfig(url = buildUrl, anonKey = buildKey)
      val token = prefs[Keys.ACCESS_TOKEN]
      val uid = prefs[Keys.USER_ID]
      val session =
        if (!token.isNullOrBlank() && !uid.isNullOrBlank()) {
          AuthSession(
            accessToken = token,
            refreshToken = prefs[Keys.REFRESH_TOKEN].orEmpty(),
            userId = uid,
            email = prefs[Keys.USER_EMAIL].orEmpty(),
            fullName = prefs[Keys.USER_FULL_NAME].orEmpty(),
            expiresAtEpochSec = prefs[Keys.EXPIRES_AT] ?: 0L,
          )
        } else {
          null
        }
      val templates =
        prefs[Keys.PLAN_TEMPLATES]?.let {
          try {
            planListAdapter.fromJson(it)
          } catch (_: Exception) {
            null
          }
        } ?: defaultTemplates

      val clients =
        prefs[Keys.CACHED_CLIENTS]?.let {
          try {
            clientListAdapter.fromJson(it)
          } catch (_: Exception) {
            null
          }
        } ?: emptyList()

      val subscriptions =
        prefs[Keys.CACHED_SUBSCRIPTIONS]?.let {
          try {
            subscriptionListAdapter.fromJson(it)
          } catch (_: Exception) {
            null
          }
        } ?: emptyList()

      val payments =
        prefs[Keys.CACHED_PAYMENTS]?.let {
          try {
            paymentListAdapter.fromJson(it)
          } catch (_: Exception) {
            null
          }
        } ?: emptyList()

      val profile =
        prefs[Keys.COACH_PROFILE]?.let {
          try {
            coachProfileAdapter.fromJson(it)
          } catch (_: Exception) {
            null
          }
        }

      PersistedAppState(
        language = lang,
        isDarkMode = isDark,
        supabaseConfig = config,
        session = session,
        planTemplates = templates,
        cachedClients = clients,
        cachedSubscriptions = subscriptions,
        cachedPayments = payments,
        coachProfile = profile,
      )
    }

  suspend fun saveSupabaseConfig(config: SupabaseConfig) {
    context.suiviDataStore.edit { prefs ->
      prefs[Keys.SUPABASE_URL] = config.url.trim()
      prefs[Keys.SUPABASE_ANON_KEY] = config.anonKey.trim()
    }
  }

  suspend fun saveSession(session: AuthSession?) {
    context.suiviDataStore.edit { prefs ->
      if (session == null) {
        prefs.remove(Keys.ACCESS_TOKEN)
        prefs.remove(Keys.REFRESH_TOKEN)
        prefs.remove(Keys.USER_ID)
        prefs.remove(Keys.USER_EMAIL)
        prefs.remove(Keys.USER_FULL_NAME)
        prefs.remove(Keys.EXPIRES_AT)
        prefs.remove(Keys.CACHED_CLIENTS)
        prefs.remove(Keys.CACHED_SUBSCRIPTIONS)
        prefs.remove(Keys.CACHED_PAYMENTS)
        prefs.remove(Keys.COACH_PROFILE)
      } else {
        prefs[Keys.ACCESS_TOKEN] = session.accessToken
        prefs[Keys.REFRESH_TOKEN] = session.refreshToken
        prefs[Keys.USER_ID] = session.userId
        prefs[Keys.USER_EMAIL] = session.email
        prefs[Keys.USER_FULL_NAME] = session.fullName
        prefs[Keys.EXPIRES_AT] = session.expiresAtEpochSec
      }
    }
  }

  suspend fun setLanguage(language: AppLanguage) {
    context.suiviDataStore.edit { prefs -> prefs[Keys.LANGUAGE] = language.code }
  }

  suspend fun setDarkMode(isDark: Boolean) {
    context.suiviDataStore.edit { prefs -> prefs[Keys.DARK_MODE] = isDark }
  }

  suspend fun saveCoachProfile(profile: CoachProfile?) {
    context.suiviDataStore.edit { prefs ->
      if (profile == null) {
        prefs.remove(Keys.COACH_PROFILE)
      } else {
        prefs[Keys.COACH_PROFILE] = coachProfileAdapter.toJson(profile)
      }
    }
  }

  suspend fun savePlanTemplates(templates: List<PlanTemplate>) {
    context.suiviDataStore.edit { prefs ->
      prefs[Keys.PLAN_TEMPLATES] = planListAdapter.toJson(templates)
    }
  }

  suspend fun saveSyncedData(
    clients: List<Client>,
    subscriptions: List<Subscription>,
    payments: List<Payment>,
  ) {
    context.suiviDataStore.edit { prefs ->
      prefs[Keys.CACHED_CLIENTS] = clientListAdapter.toJson(clients)
      prefs[Keys.CACHED_SUBSCRIPTIONS] = subscriptionListAdapter.toJson(subscriptions)
      prefs[Keys.CACHED_PAYMENTS] = paymentListAdapter.toJson(payments)
    }
  }
}

data class PersistedAppState(
  val language: AppLanguage,
  val isDarkMode: Boolean,
  val supabaseConfig: SupabaseConfig,
  val session: AuthSession?,
  val planTemplates: List<PlanTemplate>,
  val cachedClients: List<Client>,
  val cachedSubscriptions: List<Subscription>,
  val cachedPayments: List<Payment>,
  val coachProfile: CoachProfile? = null,
)
