package com.example.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
  private val sarFormat = NumberFormat.getNumberInstance(Locale.US).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
  }

  private val bdtFormat = NumberFormat.getNumberInstance(Locale.US).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 0
  }

  private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
  private val shortDateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

  fun formatSar(amount: Double): String {
    return "${sarFormat.format(amount)} SAR"
  }

  fun formatBdt(amount: Double): String {
    return "${bdtFormat.format(amount)} ৳"
  }

  fun formatDate(millis: Long): String {
    return dateFormat.format(Date(millis))
  }

  fun formatShortDate(millis: Long): String {
    return shortDateFormat.format(Date(millis))
  }

  fun formatMonthLabel(yearMonth: String): String {
    if (yearMonth == "ALL") return "সকল সময় (All Time)"
    return try {
      val parts = yearMonth.split("-")
      val year = parts[0]
      val monthNum = parts[1].toInt()
      val banglaMonths = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
      )
      "${banglaMonths[monthNum - 1]} $year"
    } catch (e: Exception) {
      yearMonth
    }
  }
}
