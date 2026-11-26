package com.example.data.remote

import com.example.domain.model.AuthSession
import com.example.domain.model.Client
import com.example.domain.model.CoachProfile
import com.example.domain.model.Payment
import com.example.domain.model.Subscription
import com.example.domain.model.SupabaseConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SupabaseApiException(
  val statusCode: Int,
  override val message: String,
) : Exception(message)

class SupabaseClient {
  private val httpClient =
    OkHttpClient.Builder()
      .connectTimeout(20, TimeUnit.SECONDS)
      .readTimeout(25, TimeUnit.SECONDS)
      .writeTimeout(25, TimeUnit.SECONDS)
      .build()

  private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  private val errorAdapter = moshi.adapter(SupabaseErrorDto::class.java)
  private val sessionAdapter = moshi.adapter(AuthSessionResponseDto::class.java)
  private val signInAdapter = moshi.adapter(AuthSignInRequest::class.java)
  private val signUpAdapter = moshi.adapter(AuthSignUpRequest::class.java)
  private val recoverAdapter = moshi.adapter(AuthRecoverRequest::class.java)
  private val refreshAdapter = moshi.adapter(AuthRefreshRequest::class.java)

  private val profileListAdapter =
    moshi.adapter<List<CoachProfileDto>>(
      Types.newParameterizedType(List::class.java, CoachProfileDto::class.java)
    )
  private val profileAdapter = moshi.adapter(CoachProfileDto::class.java)

  private val clientListAdapter =
    moshi.adapter<List<ClientDto>>(
      Types.newParameterizedType(List::class.java, ClientDto::class.java)
    )
  private val clientAdapter = moshi.adapter(ClientDto::class.java)

  private val subscriptionListAdapter =
    moshi.adapter<List<SubscriptionDto>>(
      Types.newParameterizedType(List::class.java, SubscriptionDto::class.java)
    )
  private val subscriptionAdapter = moshi.adapter(SubscriptionDto::class.java)

  private val paymentListAdapter =
    moshi.adapter<List<PaymentDto>>(
      Types.newParameterizedType(List::class.java, PaymentDto::class.java)
    )
  private val paymentAdapter = moshi.adapter(PaymentDto::class.java)

  private fun normalizeBaseUrl(url: String): String {
    var clean = url.trim().trimEnd('/')
    listOf("/rest/v1", "/auth/v1", "/storage/v1", "/functions/v1").forEach { suffix ->
      if (clean.endsWith(suffix, ignoreCase = true)) {
        clean = clean.dropLast(suffix.length).trimEnd('/')
      }
    }
    return clean
  }

  suspend fun testConnection(config: SupabaseConfig): Boolean =
    withContext(Dispatchers.IO) {
      if (!config.isConfigured) return@withContext false
      val base = normalizeBaseUrl(config.url)
      val request =
        Request.Builder()
          .url("$base/auth/v1/settings")
          .addHeader("apikey", config.anonKey.trim())
          .get()
          .build()
      try {
        httpClient.newCall(request).execute().use { response -> response.isSuccessful }
      } catch (_: Exception) {
        false
      }
    }

  suspend fun signIn(config: SupabaseConfig, email: String, password: String): AuthSession =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val body = signInAdapter.toJson(AuthSignInRequest(email.trim(), password)).toRequestBody(jsonMediaType)
      val request =
        Request.Builder()
          .url("$base/auth/v1/token?grant_type=password")
          .addHeader("apikey", config.anonKey.trim())
          .addHeader("Content-Type", "application/json")
          .post(body)
          .build()
      val dto = executeAuthCall(request)
      dto.toAuthSession()
    }

  suspend fun signUp(
    config: SupabaseConfig,
    email: String,
    password: String,
    fullName: String,
  ): AuthSession? =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val body =
        signUpAdapter
          .toJson(
            AuthSignUpRequest(
              email = email.trim(),
              password = password,
              data = mapOf("full_name" to fullName.trim()),
            )
          )
          .toRequestBody(jsonMediaType)
      val request =
        Request.Builder()
          .url("$base/auth/v1/signup")
          .addHeader("apikey", config.anonKey.trim())
          .addHeader("Content-Type", "application/json")
          .post(body)
          .build()
      val dto = executeAuthCall(request)
      if (!dto.accessToken.isNullOrBlank() && dto.user != null) {
        dto.toAuthSession()
      } else {
        // Email confirmation enabled on Supabase project
        null
      }
    }

  suspend fun resetPassword(config: SupabaseConfig, email: String) =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val body = recoverAdapter.toJson(AuthRecoverRequest(email.trim())).toRequestBody(jsonMediaType)
      val request =
        Request.Builder()
          .url("$base/auth/v1/recover")
          .addHeader("apikey", config.anonKey.trim())
          .addHeader("Content-Type", "application/json")
          .post(body)
          .build()
      httpClient.newCall(request).execute().use { response ->
        val text = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
          throw SupabaseApiException(response.code, extractError(text, response.code))
        }
      }
    }

  suspend fun refreshSession(config: SupabaseConfig, refreshToken: String): AuthSession =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val body = refreshAdapter.toJson(AuthRefreshRequest(refreshToken)).toRequestBody(jsonMediaType)
      val request =
        Request.Builder()
          .url("$base/auth/v1/token?grant_type=refresh_token")
          .addHeader("apikey", config.anonKey.trim())
          .addHeader("Content-Type", "application/json")
          .post(body)
          .build()
      executeAuthCall(request).toAuthSession()
    }

  suspend fun signOut(config: SupabaseConfig, accessToken: String) =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val request =
        Request.Builder()
          .url("$base/auth/v1/logout")
          .addHeader("apikey", config.anonKey.trim())
          .addHeader("Authorization", "Bearer $accessToken")
          .post("".toRequestBody(jsonMediaType))
          .build()
      try {
        httpClient.newCall(request).execute().close()
      } catch (_: Exception) {
        // Ignore network errors on sign out
      }
    }

  suspend fun fetchCoachProfile(
    config: SupabaseConfig,
    accessToken: String,
    coachId: String,
  ): CoachProfile? =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val request =
        authorizedRequest(config, accessToken, "$base/rest/v1/coach_profiles?id=eq.$coachId&select=*")
          .get()
          .build()
      val body = executeRestCall(request)
      profileListAdapter.fromJson(body)?.firstOrNull()?.toDomain()
    }

  suspend fun upsertCoachProfile(
    config: SupabaseConfig,
    accessToken: String,
    profile: CoachProfile,
  ): CoachProfile =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val dto =
        CoachProfileDto(
          id = profile.id,
          fullName = profile.fullName,
          businessName = profile.businessName,
          phone = profile.phone,
          email = profile.email,
          avatarUrl = profile.avatarUrl,
        )
      val request =
        authorizedRequest(config, accessToken, "$base/rest/v1/coach_profiles")
          .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
          .post(profileAdapter.toJson(dto).toRequestBody(jsonMediaType))
          .build()
      val body = executeRestCall(request)
      profileListAdapter.fromJson(body)?.firstOrNull()?.toDomain() ?: profile
    }

  suspend fun fetchClients(config: SupabaseConfig, accessToken: String): List<Client> =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val request =
        authorizedRequest(config, accessToken, "$base/rest/v1/clients?select=*&order=created_at.desc")
          .get()
          .build()
      val body = executeRestCall(request)
      (clientListAdapter.fromJson(body) ?: emptyList()).mapNotNull { it.toDomain() }
    }

  suspend fun createClient(config: SupabaseConfig, accessToken: String, client: Client): Client =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val dto =
        ClientDto(
          coachId = client.coachId,
          fullName = client.fullName,
          phone = client.phone,
          email = client.email,
          goal = client.goal,
          notes = client.notes,
          isArchived = client.isArchived,
        )
      val request =
        authorizedRequest(config, accessToken, "$base/rest/v1/clients")
          .addHeader("Prefer", "return=representation")
          .post(clientAdapter.toJson(dto).toRequestBody(jsonMediaType))
          .build()
      val body = executeRestCall(request)
      clientListAdapter.fromJson(body)?.firstOrNull()?.toDomain()
        ?: throw SupabaseApiException(500, "Empty response from Supabase")
    }

  suspend fun updateClient(config: SupabaseConfig, accessToken: String, client: Client): Client =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val dto =
        ClientDto(
          id = client.id,
          coachId = client.coachId,
          fullName = client.fullName,
          phone = client.phone,
          email = client.email,
          goal = client.goal,
          notes = client.notes,
          isArchived = client.isArchived,
        )
      val request =
        authorizedRequest(config, accessToken, "$base/rest/v1/clients?id=eq.${client.id}")
          .addHeader("Prefer", "return=representation")
          .patch(clientAdapter.toJson(dto).toRequestBody(jsonMediaType))
          .build()
      val body = executeRestCall(request)
      clientListAdapter.fromJson(body)?.firstOrNull()?.toDomain() ?: client
    }

  suspend fun deleteClient(config: SupabaseConfig, accessToken: String, clientId: String) =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val request =
        authorizedRequest(config, accessToken, "$base/rest/v1/clients?id=eq.$clientId")
          .delete()
          .build()
      executeRestCall(request)
    }

  suspend fun fetchSubscriptions(config: SupabaseConfig, accessToken: String): List<Subscription> =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val request =
        authorizedRequest(
          config,
          accessToken,
          "$base/rest/v1/subscriptions?select=*&order=end_date.desc,created_at.desc",
        )
          .get()
          .build()
      val body = executeRestCall(request)
      (subscriptionListAdapter.fromJson(body) ?: emptyList()).mapNotNull { it.toDomain() }
    }

  suspend fun createSubscription(
    config: SupabaseConfig,
    accessToken: String,
    sub: Subscription,
  ): Subscription =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val dto =
        SubscriptionDto(
          coachId = sub.coachId,
          clientId = sub.clientId,
          planName = sub.planName,
          priceDa = sub.priceDa,
          durationDays = sub.durationDays,
          startDate = sub.startDate,
          endDate = sub.endDate,
          sessionsTotal = sub.sessionsTotal,
          sessionsUsed = sub.sessionsUsed,
          amountPaidDa = sub.amountPaidDa,
          notes = sub.notes,
        )
      val request =
        authorizedRequest(config, accessToken, "$base/rest/v1/subscriptions")
          .addHeader("Prefer", "return=representation")
          .post(subscriptionAdapter.toJson(dto).toRequestBody(jsonMediaType))
          .build()
      val body = executeRestCall(request)
      subscriptionListAdapter.fromJson(body)?.firstOrNull()?.toDomain()
        ?: throw SupabaseApiException(500, "Empty response from Supabase")
    }

  suspend fun updateSubscription(
    config: SupabaseConfig,
    accessToken: String,
    sub: Subscription,
  ): Subscription =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val dto =
        SubscriptionDto(
          id = sub.id,
          coachId = sub.coachId,
          clientId = sub.clientId,
          planName = sub.planName,
          priceDa = sub.priceDa,
          durationDays = sub.durationDays,
          startDate = sub.startDate,
          endDate = sub.endDate,
          sessionsTotal = sub.sessionsTotal,
          sessionsUsed = sub.sessionsUsed,
          amountPaidDa = sub.amountPaidDa,
          notes = sub.notes,
        )
      val request =
        authorizedRequest(config, accessToken, "$base/rest/v1/subscriptions?id=eq.${sub.id}")
          .addHeader("Prefer", "return=representation")
          .patch(subscriptionAdapter.toJson(dto).toRequestBody(jsonMediaType))
          .build()
      val body = executeRestCall(request)
      subscriptionListAdapter.fromJson(body)?.firstOrNull()?.toDomain() ?: sub
    }

  suspend fun fetchPayments(config: SupabaseConfig, accessToken: String): List<Payment> =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val request =
        authorizedRequest(config, accessToken, "$base/rest/v1/payments?select=*&order=paid_at.desc")
          .get()
          .build()
      val body = executeRestCall(request)
      (paymentListAdapter.fromJson(body) ?: emptyList()).mapNotNull { it.toDomain() }
    }

  suspend fun createPayment(
    config: SupabaseConfig,
    accessToken: String,
    payment: Payment,
  ): Payment =
    withContext(Dispatchers.IO) {
      val base = normalizeBaseUrl(config.url)
      val dto =
        PaymentDto(
          coachId = payment.coachId,
          clientId = payment.clientId,
          subscriptionId = payment.subscriptionId,
          amountDa = payment.amountDa,
          paidAt = payment.paidAt,
          method = payment.methodWire,
          note = payment.note,
        )
      val request =
        authorizedRequest(config, accessToken, "$base/rest/v1/payments")
          .addHeader("Prefer", "return=representation")
          .post(paymentAdapter.toJson(dto).toRequestBody(jsonMediaType))
          .build()
      val body = executeRestCall(request)
      paymentListAdapter.fromJson(body)?.firstOrNull()?.toDomain()
        ?: throw SupabaseApiException(500, "Empty response from Supabase")
    }

  private fun authorizedRequest(
    config: SupabaseConfig,
    accessToken: String,
    url: String,
  ): Request.Builder =
    Request.Builder()
      .url(url)
      .addHeader("apikey", config.anonKey.trim())
      .addHeader("Authorization", "Bearer $accessToken")
      .addHeader("Content-Type", "application/json")

  private fun executeAuthCall(request: Request): AuthSessionResponseDto {
    httpClient.newCall(request).execute().use { response ->
      val text = response.body?.string().orEmpty()
      if (!response.isSuccessful) {
        throw SupabaseApiException(response.code, extractError(text, response.code))
      }
      return sessionAdapter.fromJson(text)
        ?: throw SupabaseApiException(response.code, "Invalid auth response")
    }
  }

  private fun executeRestCall(request: Request): String {
    httpClient.newCall(request).execute().use { response ->
      val text = response.body?.string().orEmpty()
      if (!response.isSuccessful) {
        throw SupabaseApiException(response.code, extractError(text, response.code))
      }
      return text
    }
  }

  private fun extractError(body: String, code: Int): String {
    val parsed =
      try {
        errorAdapter.fromJson(body)
      } catch (_: Exception) {
        null
      }
    return parsed?.errorDescription
      ?: parsed?.message
      ?: parsed?.msg
      ?: parsed?.error
      ?: "HTTP $code"
  }

  private fun AuthSessionResponseDto.toAuthSession(): AuthSession {
    val token = accessToken ?: throw SupabaseApiException(401, "Missing access token")
    val refresh = refreshToken.orEmpty()
    val usr = user ?: throw SupabaseApiException(401, "Missing user in auth response")
    val fullName = (usr.userMetadata?.get("full_name") as? String).orEmpty()
    val exp = expiresAt ?: (System.currentTimeMillis() / 1000L + (expiresIn ?: 3600L))
    return AuthSession(
      accessToken = token,
      refreshToken = refresh,
      userId = usr.id,
      email = usr.email.orEmpty(),
      fullName = fullName,
      expiresAtEpochSec = exp,
    )
  }

  private fun CoachProfileDto.toDomain() =
    CoachProfile(
      id = id,
      fullName = fullName,
      businessName = businessName,
      phone = phone,
      email = email,
      avatarUrl = avatarUrl,
    )

  private fun ClientDto.toDomain(): Client? {
    val nonNullId = id ?: return null
    return Client(
      id = nonNullId,
      coachId = coachId,
      fullName = fullName,
      phone = phone,
      email = email,
      goal = goal,
      notes = notes,
      isArchived = isArchived,
      createdAt = createdAt.orEmpty(),
    )
  }

  private fun SubscriptionDto.toDomain(): Subscription? {
    val nonNullId = id ?: return null
    return Subscription(
      id = nonNullId,
      coachId = coachId,
      clientId = clientId,
      planName = planName,
      priceDa = priceDa,
      durationDays = durationDays,
      startDate = startDate,
      endDate = endDate,
      sessionsTotal = sessionsTotal,
      sessionsUsed = sessionsUsed,
      amountPaidDa = amountPaidDa,
      notes = notes,
      createdAt = createdAt.orEmpty(),
    )
  }

  private fun PaymentDto.toDomain(): Payment? {
    val nonNullId = id ?: return null
    return Payment(
      id = nonNullId,
      coachId = coachId,
      clientId = clientId,
      subscriptionId = subscriptionId,
      amountDa = amountDa,
      paidAt = paidAt.orEmpty(),
      methodWire = method,
      note = note,
    )
  }
}
