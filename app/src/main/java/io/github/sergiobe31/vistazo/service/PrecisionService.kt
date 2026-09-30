package io.github.sergiobe31.vistazo.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.github.sergiobe31.vistazo.R
import io.github.sergiobe31.vistazo.VistazoApp
import io.github.sergiobe31.vistazo.data.TodayStats
import io.github.sergiobe31.vistazo.receiver.ScreenEventMonitor
import io.github.sergiobe31.vistazo.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Servicio de la barra de estado (desactivado por defecto). Hace dos cosas:
 *
 * 1. Muestra una notificación persistente con el conteo del día
 *    ("ver sin entrar"): Room emite tras cada evento y la notificación se
 *    actualiza sola al colectar el Flow.
 * 2. Al vivir como proceso en primer plano, los receivers dinámicos sobreviven
 *    a las capas de OEM (Xiaomi/Huawei/Oppo) que matan procesos en segundo
 *    plano: mejora la precisión del conteo.
 */
class PrecisionService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        ScreenEventMonitor.register(this)
        val repository = (application as VistazoApp).repository
        serviceScope.launch {
            repository.observeToday().collect { stats ->
                startForegroundWithNotification(stats)
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        ScreenEventMonitor.unregister(this)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundWithNotification(stats: TodayStats) {
        val notification = buildNotification(stats)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(stats: TodayStats): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, VistazoApp.CHANNEL_ID_PRECISION)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(
                if (stats.unlocks == 0 && stats.vistazos == 0) {
                    getString(R.string.precision_notification_fallback)
                } else {
                    getString(R.string.precision_notification_text, stats.unlocks, stats.vistazos)
                }
            )
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setContentIntent(contentIntent)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1

        fun isEnabled(context: Context): Boolean =
            (context.applicationContext as VistazoApp).settings.statusBarEnabled

        /** Activa o desactiva el modo y arranca/detiene el servicio en consecuencia. */
        fun setEnabled(context: Context, enabled: Boolean) {
            (context.applicationContext as VistazoApp).settings.statusBarEnabled = enabled
            if (enabled) start(context) else context.stopService(Intent(context, PrecisionService::class.java))
        }

        fun start(context: Context) {
            val intent = Intent(context, PrecisionService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }
    }
}
