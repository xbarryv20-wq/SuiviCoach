package com.example

import com.example.data.remote.SupabaseSqlScript
import com.example.domain.model.Subscription
import com.example.domain.model.SubscriptionStatus
import com.example.i18n.AppLanguage
import com.example.i18n.formatDA
import com.example.i18n.stringsFor
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuiviCoachDomainTest {
  @Test
  fun subscriptionStatusThresholds_matchSpecification() {
    val today = DateUtils.todayIso()
    val activeSub =
      Subscription(
        id = "1",
        coachId = "c1",
        clientId = "cl1",
        planName = "1 Mois",
        priceDa = 5000,
        durationDays = 30,
        startDate = today,
        endDate = DateUtils.addDaysToIso(today, 10),
      )
    val expiringSub = activeSub.copy(endDate = DateUtils.addDaysToIso(today, 2))
    val expiredTodaySub = activeSub.copy(endDate = today)
    val expiredPastSub = activeSub.copy(endDate = DateUtils.addDaysToIso(today, -3))

    assertEquals(SubscriptionStatus.ACTIVE, activeSub.status)
    assertEquals(SubscriptionStatus.EXPIRING_SOON, expiringSub.status)
    assertEquals(SubscriptionStatus.EXPIRED, expiredTodaySub.status)
    assertEquals(SubscriptionStatus.EXPIRED, expiredPastSub.status)
  }

  @Test
  fun currencyFormatting_formatsAlgerianDinar() {
    val formatted = formatDA(5000).replace('\u00A0', ' ').replace('\u202F', ' ')
    assertEquals("5 000 DA", formatted)
  }

  @Test
  fun localization_setsRtlOnlyForArabic() {
    assertEquals(false, AppLanguage.FR.isRtl)
    assertEquals(true, AppLanguage.AR.isRtl)
    assertEquals(false, AppLanguage.EN.isRtl)
    assertEquals("SUIVICOACH", stringsFor(AppLanguage.FR).appName)
  }

  @Test
  fun sqlScript_containsAllTablesAndRls() {
    assertTrue(SupabaseSqlScript.SQL.contains("create table if not exists public.coach_profiles"))
    assertTrue(SupabaseSqlScript.SQL.contains("create table if not exists public.clients"))
    assertTrue(SupabaseSqlScript.SQL.contains("create table if not exists public.subscriptions"))
    assertTrue(SupabaseSqlScript.SQL.contains("create table if not exists public.payments"))
    assertTrue(SupabaseSqlScript.SQL.contains("enable row level security"))
  }
}
