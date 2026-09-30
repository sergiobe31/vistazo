package io.github.sergiobe31.vistazo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: Session): Long

    @Update
    suspend fun update(session: Session)

    /** La sesión abierta más reciente, si existe (p. ej. pantalla apagada sin haber desbloqueado). */
    @Query("SELECT * FROM sessions WHERE lockTimestamp IS NULL ORDER BY unlockTimestamp DESC LIMIT 1")
    suspend fun getOpenSession(): Session?

    /** Todas las sesiones sin cerrar (tras un reinicio quedan huérfanas). */
    @Query("SELECT * FROM sessions WHERE lockTimestamp IS NULL")
    suspend fun getOpenSessions(): List<Session>

    @Query("SELECT COUNT(*) FROM sessions WHERE unlockTimestamp >= :since")
    fun observeUnlockCount(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM sessions WHERE unlockTimestamp >= :since AND esVistazo = 1")
    fun observeVistazoCount(since: Long): Flow<Int>

    @Query("SELECT * FROM sessions WHERE unlockTimestamp >= :since ORDER BY unlockTimestamp ASC")
    fun observeSessionsSince(since: Long): Flow<List<Session>>
}
