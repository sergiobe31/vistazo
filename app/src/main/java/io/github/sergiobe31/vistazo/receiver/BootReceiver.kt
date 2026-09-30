package io.github.sergiobe31.vistazo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.sergiobe31.vistazo.VistazoApp
import io.github.sergiobe31.vistazo.service.PrecisionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Único receiver de manifiesto (BOOT_COMPLETED sí puede declararse ahí).
 * Relanza el proceso para re-registrar los receivers dinámicos y, si el modo
 * precisión está activo, el foreground service (BOOT_COMPLETED está en la
 * lista de exenciones para arrancar un FGS desde segundo plano).
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as VistazoApp
        ScreenEventMonitor.register(context)
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // El reinicio deja la última sesión sin cerrar: se cierra sin marcar vistazo.
                app.repository.closeOpenSessions(System.currentTimeMillis())
            } finally {
                result.finish()
            }
        }
        if (PrecisionService.isEnabled(context)) {
            PrecisionService.start(context)
        }
    }
}
