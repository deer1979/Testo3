package com.containerpro

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.containerpro.data.PreferencesManager
import com.containerpro.navigation.AppNavGraph
import com.containerpro.ui.theme.AppTheme
import com.containerpro.ui.theme.ContainerProTheme
import com.containerpro.utils.LocaleUtils

class MainActivity : ComponentActivity() {

    private lateinit var prefs: PreferencesManager

    // Apply locale BEFORE layout inflation
    override fun attachBaseContext(newBase: Context) {
        val lang = PreferencesManager(newBase).loadLanguage()
        super.attachBaseContext(LocaleUtils.wrap(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = PreferencesManager(this)

        setContent {
            val appTheme by prefs.theme.collectAsState()

            ContainerProTheme(appTheme = appTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(
                        preferencesManager = prefs,
                        onThemeChange      = { prefs.setTheme(it) },
                        onLanguageChange   = { lang ->
                            prefs.setLanguage(lang)
                            // Recreate so resources reload in new locale
                            recreate()
                        },
                    )
                }
            }
        }
    }
}
