package io.github.sergiobe31.vistazo.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

data class TodayStats(val unlocks: Int, val vistazos: Int)

data class DayStats(
    val startOfDayMillis: Long,
    val unlocks: Int,
    val vistazos: Int,
)

class VistazoRepository(
    private val dao: SessionDao,
    private val estimateDao: EstimateDao,
) {

    /** Emite el inicio (en millis) del día local vigente y vuelve a emitir al
     * cambiar de día, para que los contadores "de hoy" no se queden
     * congelados si la app lleva horas abierta.
     */
    private fun todayStartFlow(): Flow<Long> = flow {
        while (true) {
            val today = LocalDate.now()
            val zone = ZoneId.systemDefault()
            emit(today.atStartOfDay(zone).toInstant().toEpochMilli())
            val nextMidnight = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            delay(nextMidnight - System.currentTimeMillis())
        }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeToday(): Flow<TodayStats> = todayStartFlow().flatMapLatest { start ->
        combine(
            dao.observeUnlockCount(start),
            dao.observeVistazoCount(start),
        ) { unlocks, vistazos -> TodayStats(unlocks, vistazos) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeLastDays(days: Int = 7): Flow<List<DayStats>> =
        todayStartFlow().flatMapLatest { start ->
            // 86_400_000 ms; se ignora el DST a propósito: es solo una ventana de consulta.
            dao.observeSessionsSince(start - (days - 1) * 86_400_000L).map { sessions ->
                buildDayStats(sessions, start, days)
            }
        }

    /** Cierra sesiones huérfanas (p. ej. abiertas antes de un reinicio). Sin vistazo. */
    suspend fun closeOpenSessions(at: Long) {
        dao.getOpenSessions().forEach { dao.update(it.copy(lockTimestamp = at)) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTodayEstimate(): Flow<DailyEstimate?> =
        todayStartFlow().flatMapLatest { start -> estimateDao.observeForDay(start) }

    suspend fun saveEstimateForToday(estimated: Int) {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
        estimateDao.upsert(DailyEstimate(dayStartMillis = start, estimatedUnlocks = estimated))
    }

    private fun buildDayStats(sessions: List<Session>, todayStart: Long, days: Int): List<DayStats> {
        val zone = ZoneId.systemDefault()
        val byDay = sessions.groupBy {
            LocalDate.ofInstant(Instant.ofEpochMilli(it.unlockTimestamp), zone)
        }
        val today = LocalDate.ofInstant(Instant.ofEpochMilli(todayStart), zone)
        return (days - 1 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val daySessions = byDay[date].orEmpty()
            DayStats(
                startOfDayMillis = date.atStartOfDay(zone).toInstant().toEpochMilli(),
                unlocks = daySessions.size,
                vistazos = daySessions.count { it.esVistazo },
            )
        }
    }
}
