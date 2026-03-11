package com.containerpro.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.containerpro.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("containerpro_prefs", Context.MODE_PRIVATE)

    // ── Theme ─────────────────────────────────────────────
    private val _theme = MutableStateFlow(loadTheme())
    val theme: StateFlow<AppTheme> = _theme

    fun setTheme(theme: AppTheme) {
        prefs.edit { putString(KEY_THEME, theme.name) }
        _theme.value = theme
    }

    private fun loadTheme(): AppTheme =
        runCatching { AppTheme.valueOf(prefs.getString(KEY_THEME, AppTheme.DARK.name)!!) }
            .getOrDefault(AppTheme.DARK)

    // ── Language ──────────────────────────────────────────
    private val _language = MutableStateFlow(loadLanguage())
    val language: StateFlow<String> = _language

    fun setLanguage(lang: String) {
        prefs.edit { putString(KEY_LANG, lang) }
        _language.value = lang
    }

    fun loadLanguage(): String = prefs.getString(KEY_LANG, "es") ?: "es"

    // ── Session ───────────────────────────────────────────
    fun saveTechnicianId(id: String) = prefs.edit { putString(KEY_TECH_ID, id) }
    fun loadTechnicianId(): String   = prefs.getString(KEY_TECH_ID, "") ?: ""
    fun setLoggedIn(v: Boolean)      = prefs.edit { putBoolean(KEY_LOGGED_IN, v) }
    fun isLoggedIn(): Boolean        = prefs.getBoolean(KEY_LOGGED_IN, false)

    companion object {
        private const val KEY_THEME     = "theme"
        private const val KEY_LANG      = "language"
        private const val KEY_TECH_ID   = "technician_id"
        private const val KEY_LOGGED_IN = "logged_in"
    }
}
