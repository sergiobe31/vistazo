package io.github.sergiobe31.vistazo.data

/** Umbral de la métrica diferencial: cerrar el móvil antes del tiempo configurado. */
object VistazoClassifier {

    const val DEFAULT_THRESHOLD_MS = 60_000L

    /** Cache en memoria: se refresca al cambiar el ajuste, no en cada evento. */
    @Volatile
    var thresholdMs: Long = DEFAULT_THRESHOLD_MS

    fun esVistazo(unlockTimestamp: Long, lockTimestamp: Long): Boolean =
        lockTimestamp - unlockTimestamp < thresholdMs
}
