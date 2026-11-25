package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

object IntentHelpers {
  fun dialPhone(context: Context, phone: String) {
    val clean = phone.replace(" ", "").trim()
    if (clean.isEmpty()) return
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
    context.startActivity(intent)
  }

  fun openWhatsApp(context: Context, phone: String, message: String) {
    val digits = phone.filter { it.isDigit() || it == '+' }
    val algerianNormalized =
      when {
        digits.startsWith("+") -> digits.drop(1)
        digits.startsWith("00") -> digits.drop(2)
        digits.startsWith("0") && digits.length >= 9 -> "213${digits.drop(1)}"
        else -> digits
      }
    val encoded = URLEncoder.encode(message, "UTF-8")
    val url =
      if (algerianNormalized.isNotBlank()) {
        "https://wa.me/$algerianNormalized?text=$encoded"
      } else {
        "https://wa.me/?text=$encoded"
      }
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
  }

  fun shareTextReport(context: Context, subject: String, content: String) {
    val intent =
      Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, content)
      }
    context.startActivity(Intent.createChooser(intent, subject))
  }
}
