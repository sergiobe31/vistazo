package io.github.sergiobe31.vistazo.receiver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat

/**
 * ACTION_USER_PRESENT y ACTION_SCREEN_OFF son broadcasts protegidos: Android
 * los entrega solo a receivers registrados con registerReceiver() mientras el
 * proceso vive; un <receiver> en el manifiesto nunca los recibe. Por eso el
 * registro se hace aquí en runtime y BootReceiver lo repite tras cada reinicio.
 */
object ScreenEventMonitor {

    // El sistema mantiene una referencia débil al receiver registrado: hay que
    // guardar una fuerte en este singleton para que no lo reclame el GC.
    private var receiver: ScreenEventsReceiver? = null

    @Synchronized
    fun register(context: Context) {
        if (receiver != null) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        receiver = ScreenEventsReceiver().also {
            ContextCompat.registerReceiver(
                context.applicationContext, it, filter,
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
        }
    }

    @Synchronized
    fun unregister(context: Context) {
        receiver?.let {
            runCatching { context.applicationContext.unregisterReceiver(it) }
        }
        receiver = null
    }
}
