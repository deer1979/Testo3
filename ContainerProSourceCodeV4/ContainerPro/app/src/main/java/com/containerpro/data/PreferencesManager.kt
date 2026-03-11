package com.containerpro.data

import android.content.Context
import android.content.SharedPreferences
import com.containerpro.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Theme ─────────────────────────────────────────────
    private val _theme = MutableStateFlow(loadThemePref())
    val theme: StateFlow<AppTheme> = _theme

    fun setTheme(appTheme: AppTheme) {
        prefs.edit().putString(KEY_THEME, appTheme.name).apply()
        _theme.value = appTheme
    }

    private fun loadThemePref(): AppTheme =
        runCatching {
            AppTheme.valueOf(prefs.getString(KEY_THEME, AppTheme.DARK.name) ?: AppTheme.DARK.name)
        }.getOrDefault(AppTheme.DARK)

    // ── Language ──────────────────────────────────────────
    private val _language = MutableStateFlow(loadLanguagePref())
    val language: StateFlow<String> = _language

    /** BCP-47 tag: "es", "en", "pt", "fr", "zh", "nl" */
    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANG, lang).apply()
        _language.value = lang
    }

    fun loadLanguagePref(): String =
        prefs.getString(KEY_LANG, "es") ?: "es"

    // ── Session ───────────────────────────────────────────
    fun saveTechnicianId(id: String) =
        prefs.edit().putString(KEY_TECH_ID, id).apply()

    fun loadTechnicianId(): String =
        prefs.getString(KEY_TECH_ID, "") ?: ""

    fun setLoggedIn(value: Boolean) =
        prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply()

    fun isLoggedIn(): Boolean =
        prefs.getBoolean(KEY_LOGGED_IN, false)

    companion object {
        private const val PREFS_NAME    = "containerpro_prefs"
        private const val KEY_THEME     = "theme"
        private const val KEY_LANG      = "language"
        private const val KEY_TECH_ID   = "technician_id"
        private const val KEY_LOGGED_IN = "logged_in"
    }
}
