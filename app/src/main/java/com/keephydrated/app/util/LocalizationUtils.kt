package com.keephydrated.app.util

import android.content.Context
import android.content.res.Configuration
import java.text.NumberFormat
import java.util.Locale

object LocalizationUtils {

    fun getAppLocale(languageCode: String): Locale {
        return if (languageCode == "ar") Locale("ar") else Locale.ENGLISH
    }

    fun getLocalizedContext(context: Context, languageCode: String): Context {
        val locale = getAppLocale(languageCode)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    /**
     * Formats integer/floating numbers using the appropriate locale numerals (Arabic-Indic vs Western).
     */
    fun formatNumber(number: Number, isArabic: Boolean): String {
        val locale = if (isArabic) Locale("ar") else Locale.ENGLISH
        val nf = NumberFormat.getInstance(locale)
        nf.isGroupingUsed = false
        return nf.format(number)
    }

    /**
     * Formats hour into localized 12-hour format with AM/PM (ص / م).
     */
    fun formatHour(hour: Int, isArabic: Boolean): String {
        val localizedHour = when {
            hour == 0 || hour == 12 -> 12
            hour < 12 -> hour
            else -> hour - 12
        }
        val formattedNumber = formatNumber(localizedHour, isArabic)
        val period = if (hour < 12) {
            if (isArabic) "ص" else "AM"
        } else {
            if (isArabic) "م" else "PM"
        }
        return "$formattedNumber $period"
    }

    /**
     * Formats hour and minute into localized 12-hour format with AM/PM (ص / م).
     */
    fun formatTime(hour: Int, minute: Int, isArabic: Boolean): String {
        val localizedHour = when {
            hour == 0 || hour == 12 -> 12
            hour < 12 -> hour
            else -> hour - 12
        }
        val formattedHour = formatNumber(localizedHour, isArabic)
        val clampedMinute = minute.coerceIn(0, 59)
        val formattedMinute = if (isArabic) {
            if (clampedMinute < 10) "٠${formatNumber(clampedMinute, isArabic)}" else formatNumber(clampedMinute, isArabic)
        } else {
            if (clampedMinute < 10) "0$clampedMinute" else "$clampedMinute"
        }
        val period = if (hour < 12) {
            if (isArabic) "ص" else "AM"
        } else {
            if (isArabic) "م" else "PM"
        }
        return "$formattedHour:$formattedMinute $period"
    }
}
