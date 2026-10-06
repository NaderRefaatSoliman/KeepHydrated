package com.keephydrated.app.util

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context, languageCode: String) {
        val targetLocale = if (languageCode == "ar") Locale("ar") else Locale("en")
        Locale.setDefault(targetLocale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                localeManager?.applicationLocales = LocaleList(targetLocale)
            } catch (_: Exception) {
                applyLegacyLocale(context, targetLocale)
            }
        } else {
            applyLegacyLocale(context, targetLocale)
        }
    }

    private fun applyLegacyLocale(context: Context, locale: Locale) {
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun getAppLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            val locales = localeManager?.applicationLocales
            if (locales != null && !locales.isEmpty) {
                locales[0]
            } else {
                context.resources.configuration.locales[0] ?: Locale.getDefault()
            }
        } else {
            context.resources.configuration.locales[0] ?: Locale.getDefault()
        }
    }
}
