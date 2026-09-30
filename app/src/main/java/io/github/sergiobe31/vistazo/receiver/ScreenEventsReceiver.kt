package io.github.sergiobe31.vistazo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.sergiobe31.vistazo.VistazoApp
import io.github.sergiobe31.vistazo.data.Session
import io.github.sergiobe31.vistazo.data.VistazoClassifier
import io.github.sergiobe31.vistazo.widget.CounterWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Recibe SCREEN_ON / USER_PRESENT / SCREEN_OFF (registrado en runtime por
 * ScreenEventMonitor). Las escrituras en Room van con goAsync(): onReceive
 * debe devolver en segundos y el sistema da hasta ~10 s extra al PendingResult.
 */
class ScreenEventsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val dao = (context.applicationContext as VistazoApp).database.sessionDao()
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> Unit // robustez: la presencia del filtro ayuda a algunos OEM a no dormir el proceso

            Intent.ACTION_USER_PRESENT -> {
                val result = goAsync()
                // El timestamp de desbloqueo vive en memoria (esta corrutina) y
                // en la fila insertada; al SCREEN_OFF se compara con lockTimestamp.
                val unlockAt = System.currentTimeMillis()
                scope.launch {
                    try {
                        dao.insert(Session(unlockTimestamp = unlockAt, lockTimestamp = null, esVistazo = false))
                        CounterWidget.updateAll(context)
                    } finally {
                        result.finish()
                    }
                }
            }

            Intent.ACTION_SCREEN_OFF -> {
                val result = goAsync()
                val lockAt = System.currentTimeMillis()
                scope.launch {
                    try {
                        dao.getOpenSession()?.let { open ->
                            dao.update(
                                open.copy(
                                    lockTimestamp = lockAt,
                                    esVistazo = VistazoClassifier.esVistazo(open.unlockTimestamp, lockAt),
                                )
                            )
                        }
                        CounterWidget.updateAll(context)
                    } finally {
                        result.finish()
                    }
                }
            }
        }
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)
    }
}
