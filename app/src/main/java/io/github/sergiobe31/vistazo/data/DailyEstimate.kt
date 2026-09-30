package io.github.sergiobe31.vistazo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Estimación del usuario de cuántas veces desbloqueó el móvil en un día. */
@Entity(tableName = "daily_estimates")
data class DailyEstimate(
    @PrimaryKey val dayStartMillis: Long,
    val estimatedUnlocks: Int,
)
