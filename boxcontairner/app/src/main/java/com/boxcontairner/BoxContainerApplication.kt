package com.boxcontairner

import android.app.Application
import com.boxcontairner.core.logging.CrashlyticsTree
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class BoxContainerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1) Firebase (lo hace google-services automáticamente pero forzamos para que el
        //    orden quede explícito).
        FirebaseApp.initializeApp(this)

        // 2) App Check — bloquea uso del backend desde clientes no verificados.
        //    En debug: token efímero que se loguea en logcat → whitelistalo en consola.
        //    En release: Play Integrity (firma de Google Play).
        val factory = if (BuildConfig.DEBUG) {
            DebugAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(factory)

        // 3) Crashlytics — solo en release (debug no manda crashes)
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG

        // 4) Logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }

        Timber.i("BoxContainer iniciado — build=${if (BuildConfig.DEBUG) "DEBUG" else "RELEASE"}")
    }
}
