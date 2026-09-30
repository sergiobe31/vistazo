package io.github.sergiobe31.vistazo.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.sergiobe31.vistazo.R
import io.github.sergiobe31.vistazo.VistazoApp
import io.github.sergiobe31.vistazo.ui.MainActivity
import kotlinx.coroutines.flow.first

/**
 * Reporte diario: publica la notificación con el conteo del día. Toca la
 * notificación abre la app directamente en la pantalla de estimación
 * ("¿Cuántas crees?") para cerrar el ciclo estimación → realidad.
 */
class DailyReportWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as VistazoApp
        val stats = app.repository.observeToday().first()

        val openEstimate = Intent(applicationContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(MainActivity.EXTRA_OPEN_ESTIMATE, true)
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            REQUEST_CODE,
            openEstimate,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(applicationContext, VistazoApp.CHANNEL_ID_REPORT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.daily_report_title))
            .setContentText(
                applicationContext.getString(
                    R.string.daily_report_text, stats.unlocks, stats.vistazos,
                )
            )
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        // En Android 13+ sin permiso concedido esto no muestra nada (el propio
        // NotificationManagerCompat lo gestiona); no es un error.
        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "daily_report"
        private const val NOTIFICATION_ID = 2
        private const val REQUEST_CODE = 2
    }
}
