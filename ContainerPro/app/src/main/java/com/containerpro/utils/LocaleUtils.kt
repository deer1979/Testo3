package com.containerpro.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleUtils {
    fun wrap(context: Context, lang: String): Context {
        if (lang.isBlank()) return context
        val locale = if (lang == "zh") Locale.SIMPLIFIED_CHINESE else Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
