package com.boxcontairner.core.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Tree de Timber que forwarea WARN+ a Crashlytics para producción.
 * En DEBUG usar Timber.DebugTree() en su lugar para ver en logcat.
 */
class CrashlyticsTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Solo reportar WARN (5), ERROR (6), ASSERT (7) — DEBUG/INFO no generan ruido
        if (priority < android.util.Log.WARN) return

        val crashlytics = FirebaseCrashlytics.getInstance()
        if (tag != null) crashlytics.setCustomKey("tag", tag)
        crashlytics.log(message)

        if (t != null) {
            crashlytics.recordException(t)
        } else if (priority >= android.util.Log.ERROR) {
            // Error sin throwable: reportarlo igual como exception sintética
            crashlytics.recordException(LoggedException(message))
        }
    }

    private class LoggedException(message: String) : RuntimeException(message)
}
