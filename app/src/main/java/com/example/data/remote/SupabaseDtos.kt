package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthSignInRequest(
  val email: String,
  val password: String,
)

@JsonClass(generateAdapter = true)
data class AuthSignUpRequest(
  val email: String,
  val password: String,
  val data: Map<String, String>,
)

@JsonClass(generateAdapter = true)
data class AuthRecoverRequest(
  val email: String,
)

@JsonClass(generateAdapter = true)
data class AuthRefreshRequest(
  @Json(name = "refresh_token") val refreshToken: String,
)

@JsonClass(generateAdapter = true)
data class AuthUserDto(
  val id: String,
  val email: String? = null,
  @Json(name = "user_metadata") val userMetadata: Map<String, Any?>? = null,
)

@JsonClass(generateAdapter = true)
data class AuthSessionResponseDto(
  @Json(name = "access_token") val accessToken: String? = null,
  @Json(name = "refresh_token") val refreshToken: String? = null,
  @Json(name = "expires_at") val expiresAt: Long? = null,
  @Json(name = "expires_in") val expiresIn: Long? = null,
  val user: AuthUserDto? = null,
)

@JsonClass(generateAdapter = true)
data class SupabaseErrorDto(
  val error: String? = null,
  @Json(name = "error_description") val errorDescription: String? = null,
  val msg: String? = null,
  val message: String? = null,
  val hint: String? = null,
)

@JsonClass(generateAdapter = true)
data class CoachProfileDto(
  val id: String,
  @Json(name = "full_name") val fullName: String = "",
  @Json(name = "business_name") val businessName: String = "",
  val phone: String = "",
  val email: String = "",
  @Json(name = "avatar_url") val avatarUrl: String = "",
)

@JsonClass(generateAdapter = true)
data class ClientDto(
  val id: String? = null,
  @Json(name = "coach_id") val coachId: String,
  @Json(name = "full_name") val fullName: String,
  val phone: String = "",
  val email: String = "",
  val goal: String = "",
  val notes: String = "",
  @Json(name = "is_archived") val isArchived: Boolean = false,
  @Json(name = "created_at") val createdAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class SubscriptionDto(
  val id: String? = null,
  @Json(name = "coach_id") val coachId: String,
  @Json(name = "client_id") val clientId: String,
  @Json(name = "plan_name") val planName: String,
  @Json(name = "price_da") val priceDa: Int,
  @Json(name = "duration_days") val durationDays: Int,
  @Json(name = "start_date") val startDate: String,
  @Json(name = "end_date") val endDate: String,
  @Json(name = "sessions_total") val sessionsTotal: Int? = null,
  @Json(name = "sessions_used") val sessionsUsed: Int = 0,
  @Json(name = "amount_paid_da") val amountPaidDa: Int = 0,
  val notes: String = "",
  @Json(name = "created_at") val createdAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class PaymentDto(
  val id: String? = null,
  @Json(name = "coach_id") val coachId: String,
  @Json(name = "client_id") val clientId: String,
  @Json(name = "subscription_id") val subscriptionId: String? = null,
  @Json(name = "amount_da") val amountDa: Int,
  @Json(name = "paid_at") val paidAt: String? = null,
  val method: String = "cash",
  val note: String = "",
)
