package io.github.sergiobe31.vistazo.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Una sesión = un desbloqueo. Se inserta al recibir USER_PRESENT con
 * [lockTimestamp] a null y se completa al recibir SCREEN_OFF, momento en el
 * que se clasifica como vistazo si duró menos de [VistazoClassifier.VISTAZO_THRESHOLD_MS].
 */
@Entity(tableName = "sessions", indices = [Index("unlockTimestamp")])
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unlockTimestamp: Long,
    val lockTimestamp: Long?,
    val esVistazo: Boolean,
)
