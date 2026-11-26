package com.example.domain.model

import com.example.util.DateUtils
import com.squareup.moshi.JsonClass

enum class SubscriptionStatus {
  ACTIVE,
  EXPIRING_SOON,
  EXPIRED,
}

enum class PaymentMethod(val wireValue: String) {
  CASH("cash"),
  BARIDIMOB("baridimob"),
  CCP("ccp"),
  TRANSFER("transfer");

  companion object {
    fun fromWire(value: String?): PaymentMethod =
      entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) } ?: CASH
  }
}

enum class ClientFilter {
  ALL,
  ACTIVE,
  EXPIRING_SOON,
  EXPIRED,
  UNPAID,
}

data class SupabaseConfig(
  val url: String = "",
  val anonKey: String = "",
) {
  val isConfigured: Boolean
    get() {
      val u = url.trim()
      val k = anonKey.trim()
      return u.startsWith("http") &&
        !u.contains("YOUR_SUPABASE") &&
        k.length > 20 &&
        !k.contains("YOUR_SUPABASE")
    }
}

data class AuthSession(
  val accessToken: String,
  val refreshToken: String,
  val userId: String,
  val email: String,
  val fullName: String,
  val expiresAtEpochSec: Long,
)

data class CoachProfile(
  val id: String,
  val fullName: String,
  val businessName: String,
  val phone: String,
  val email: String,
  val avatarUrl: String = "",
)

@JsonClass(generateAdapter = true)
data class Client(
  val id: String,
  val coachId: String,
  val fullName: String,
  val phone: String = "",
  val email: String = "",
  val goal: String = "",
  val notes: String = "",
  val isArchived: Boolean = false,
  val createdAt: String = "",
)

@JsonClass(generateAdapter = true)
data class Subscription(
  val id: String,
  val coachId: String,
  val clientId: String,
  val planName: String,
  val priceDa: Int,
  val durationDays: Int,
  val startDate: String,
  val endDate: String,
  val sessionsTotal: Int? = null,
  val sessionsUsed: Int = 0,
  val amountPaidDa: Int = 0,
  val notes: String = "",
  val createdAt: String = "",
) {
  val daysRemaining: Int
    get() = DateUtils.daysRemainingFromToday(endDate)

  val status: SubscriptionStatus
    get() {
      val left = daysRemaining
      return when {
        left <= 0 -> SubscriptionStatus.EXPIRED
        left <= 3 -> SubscriptionStatus.EXPIRING_SOON
        else -> SubscriptionStatus.ACTIVE
      }
    }

  val progressFraction: Float
    get() {
      val total = durationDays.coerceAtLeast(1)
      val left = daysRemaining.coerceIn(0, total)
      return (left.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }

  val remainingBalanceDa: Int
    get() = (priceDa - amountPaidDa).coerceAtLeast(0)

  val isFullyPaid: Boolean
    get() = remainingBalanceDa == 0
}

@JsonClass(generateAdapter = true)
data class Payment(
  val id: String,
  val coachId: String,
  val clientId: String,
  val subscriptionId: String? = null,
  val amountDa: Int,
  val paidAt: String,
  val methodWire: String = PaymentMethod.CASH.wireValue,
  val note: String = "",
) {
  val method: PaymentMethod
    get() = PaymentMethod.fromWire(methodWire)
}

@JsonClass(generateAdapter = true)
data class PlanTemplate(
  val id: String,
  val name: String,
  val durationDays: Int,
  val priceDa: Int,
  val sessionsTotal: Int? = null,
)

data class ClientDossier(
  val client: Client,
  val latestSubscription: Subscription?,
  val subscriptions: List<Subscription>,
  val payments: List<Payment>,
) {
  val totalDueDa: Int
    get() = subscriptions.sumOf { it.remainingBalanceDa }

  val totalPaidDa: Int
    get() = payments.sumOf { it.amountDa }
}
