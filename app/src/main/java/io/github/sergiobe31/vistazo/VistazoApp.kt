package io.github.sergiobe31.vistazo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import io.github.sergiobe31.vistazo.data.VistazoClassifier
import io.github.sergiobe31.vistazo.data.VistazoDatabase
import io.github.sergiobe31.vistazo.data.VistazoRepository
import io.github.sergiobe31.vistazo.data.VistazoSettings
import io.github.sergiobe31.vistazo.receiver.ScreenEventMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class VistazoApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val settings by lazy { VistazoSettings(this) }

    val database by lazy { VistazoDatabase.get(this) }
    val repository by lazy { VistazoRepository(database.sessionDao(), database.estimateDao()) }

    override fun onCreate() {
        super.onCreate()
        settings.markFirstUseIfAbsent()
        VistazoClassifier.thresholdMs = settings.vistazoThresholdMs
        createNotificationChannels()
        // ACTION_USER_PRESENT y ACTION_SCREEN_OFF son broadcasts protegidos que
        // un receiver de manifiesto NO recibe: se registran aquí en runtime,
        // y BootReceiver los vuelve a registrar tras cada reinicio.
        ScreenEventMonitor.register(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_PRECISION,
                    getString(R.string.notification_channel_precision),
                    // IMPORTANCE_MIN: sin sonido ni vibración, colapsada en la
                    // bandeja, solo visible como icono en la barra de estado.
                    NotificationManager.IMPORTANCE_MIN,
                )
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_REPORT,
                    getString(R.string.notification_channel_report),
                    // Silenciable por el usuario desde los ajustes del sistema.
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            )
        }
    }

    companion object {
        const val CHANNEL_ID_PRECISION = "precision"
        const val CHANNEL_ID_REPORT = "daily_report"
    }
}
