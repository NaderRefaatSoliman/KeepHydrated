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
        return NumberFormat.getInstance(locale).format(number)
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
}
