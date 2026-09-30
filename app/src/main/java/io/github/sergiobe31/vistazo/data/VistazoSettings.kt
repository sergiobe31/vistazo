package io.github.sergiobe31.vistazo.data

import android.content.Context

/**
 * Preferencias de la app en un único SharedPreferences. Los valores se leen
 * en caliente desde aquí (SharedPreferences ya cachea en memoria); los que
 * afectan a la lógica de eventos (umbral) se replican además a campos
 * @Volatile para no tocar disco en cada broadcast.
 */
class VistazoSettings(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Timestamp del primer arranque; alimenta la regla de "historial tras 7 días". */
    var firstUseAt: Long
        get() = prefs.getLong(KEY_FIRST_USE, 0L)
        set(value) = prefs.edit().putLong(KEY_FIRST_USE, value).apply()

    fun markFirstUseIfAbsent(now: Long = System.currentTimeMillis()) {
        if (firstUseAt == 0L) firstUseAt = now
    }

    var statusBarEnabled: Boolean
        get() = prefs.getBoolean(KEY_STATUS_BAR, false)
        set(value) = prefs.edit().putBoolean(KEY_STATUS_BAR, value).apply()

    var reportEnabled: Boolean
        get() = prefs.getBoolean(KEY_REPORT_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_REPORT_ENABLED, value).apply()

    var reportHour: Int
        get() = prefs.getInt(KEY_REPORT_HOUR, DEFAULT_REPORT_HOUR)
        set(value) = prefs.edit().putInt(KEY_REPORT_HOUR, value).apply()

    var reportMinute: Int
        get() = prefs.getInt(KEY_REPORT_MINUTE, DEFAULT_REPORT_MINUTE)
        set(value) = prefs.edit().putInt(KEY_REPORT_MINUTE, value).apply()

    /** Umbral del vistazo en ms. */
    var vistazoThresholdMs: Long
        get() = prefs.getLong(KEY_VISTAZO_THRESHOLD, VistazoClassifier.DEFAULT_THRESHOLD_MS)
        set(value) = prefs.edit().putLong(KEY_VISTAZO_THRESHOLD, value).apply()

    companion object {
        // Mismo fichero que usaba el toggle de la barra de estado en v1.0:
        // preserva el valor existente de los usuarios.
        const val PREFS_NAME = "settings"
        const val DEFAULT_REPORT_HOUR = 22
        const val DEFAULT_REPORT_MINUTE = 0
        private const val KEY_FIRST_USE = "first_use_at"
        private const val KEY_STATUS_BAR = "precision_enabled"
        private const val KEY_REPORT_ENABLED = "report_enabled"
        private const val KEY_REPORT_HOUR = "report_hour"
        private const val KEY_REPORT_MINUTE = "report_minute"
        private const val KEY_VISTAZO_THRESHOLD = "vistazo_threshold_ms"
    }
}
