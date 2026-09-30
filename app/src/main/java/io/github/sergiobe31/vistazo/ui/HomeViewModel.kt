package io.github.sergiobe31.vistazo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.sergiobe31.vistazo.VistazoApp
import io.github.sergiobe31.vistazo.data.DailyEstimate
import io.github.sergiobe31.vistazo.data.TodayStats
import io.github.sergiobe31.vistazo.data.VistazoClassifier
import io.github.sergiobe31.vistazo.data.VistazoRepository
import io.github.sergiobe31.vistazo.data.VistazoSettings
import io.github.sergiobe31.vistazo.service.PrecisionService
import io.github.sergiobe31.vistazo.worker.DailyReportScheduler
import java.time.LocalDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    enum class Destination { Home, History, Settings, Estimate }

    sealed interface EstimateUi {
        data object Loading : EstimateUi
        data class Ask(val today: TodayStats) : EstimateUi
        data class Revealed(val estimate: Int, val today: TodayStats) : EstimateUi
    }

    private val application = app as VistazoApp
    private val repository: VistazoRepository = application.repository
    private val settings: VistazoSettings = application.settings

    // Navegación manual: cuatro pantallas, un when en MainActivity. Sin Navigation Compose.
    private val _destination = MutableStateFlow(Destination.Home)
    val destination: StateFlow<Destination> = _destination.asStateFlow()

    fun navigateTo(destination: Destination) {
        _destination.value = destination
    }

    /** Abre la pantalla de estimación desde la notificación del reporte diario. */
    fun openEstimateFromNotification() {
        _destination.value = Destination.Estimate
    }

    val today: StateFlow<TodayStats> = repository.observeToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayStats(0, 0))

    val lastDays = repository.observeLastDays(days = 7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** El histórico solo aparece tras 7 días de uso. */
    val historyAvailable: StateFlow<Boolean> = flow {
        while (true) {
            val firstUse = settings.firstUseAt
            emit(firstUse > 0 && System.currentTimeMillis() - firstUse >= SEVEN_DAYS_MS)
            delay(60_000)
        }
    }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // --- Barra de estado (servicio) ---

    private val _statusBarEnabled = MutableStateFlow(PrecisionService.isEnabled(app))
    val statusBarEnabled: StateFlow<Boolean> = _statusBarEnabled.asStateFlow()

    fun setStatusBarEnabled(enabled: Boolean) {
        PrecisionService.setEnabled(application, enabled)
        _statusBarEnabled.value = enabled
    }

    // --- Reporte diario ---

    private val _reportEnabled = MutableStateFlow(settings.reportEnabled)
    val reportEnabled: StateFlow<Boolean> = _reportEnabled.asStateFlow()

    private val _reportTime = MutableStateFlow(settings.reportHour to settings.reportMinute)
    val reportTime: StateFlow<Pair<Int, Int>> = _reportTime.asStateFlow()

    /** La UI pide POST_NOTIFICATIONS antes de llamar aquí con enabled=true. */
    fun setReportEnabled(enabled: Boolean) {
        settings.reportEnabled = enabled
        _reportEnabled.value = enabled
        if (enabled) {
            DailyReportScheduler.schedule(application, settings.reportHour, settings.reportMinute)
        } else {
            DailyReportScheduler.cancel(application)
        }
    }

    fun setReportTime(hour: Int, minute: Int) {
        settings.reportHour = hour
        settings.reportMinute = minute
        _reportTime.value = hour to minute
        if (settings.reportEnabled) DailyReportScheduler.schedule(application, hour, minute)
    }

    // --- Umbral de vistazo ---

    private val _thresholdSeconds = MutableStateFlow((settings.vistazoThresholdMs / 1_000).toInt())
    val thresholdSeconds: StateFlow<Int> = _thresholdSeconds.asStateFlow()

    fun setVistazoThreshold(seconds: Int) {
        val ms = seconds * 1_000L
        settings.vistazoThresholdMs = ms
        VistazoClassifier.thresholdMs = ms // refresca la cache en memoria
        _thresholdSeconds.value = seconds
    }

    // --- "¿Cuántas crees?" ---

    /** Tic minuto para que el criterio "de noche" se aplique sin reiniciar. */
    private val clockTick: Flow<LocalDateTime> = flow {
        while (true) {
            emit(LocalDateTime.now())
            delay(60_000)
        }
    }

    val estimateUi: StateFlow<EstimateUi> = combine(
        repository.observeToday(),
        repository.observeTodayEstimate(),
    ) { todayStats, estimate ->
        estimate.toUi(todayStats)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstimateUi.Loading)

    /** Entrada discreta en la home: sin estimación hoy y contexto suficiente. */
    val estimateEntryVisible: StateFlow<Boolean> = combine(
        repository.observeToday(),
        repository.observeTodayEstimate(),
        clockTick,
    ) { todayStats, estimate, now ->
        estimate == null && (now.hour >= EVENING_HOUR || todayStats.unlocks >= MIN_UNLOCKS_FOR_ESTIMATE)
    }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun submitEstimate(count: Int) {
        viewModelScope.launch { repository.saveEstimateForToday(count) }
    }

    private fun DailyEstimate?.toUi(todayStats: TodayStats): EstimateUi =
        if (this == null) EstimateUi.Ask(todayStats) else EstimateUi.Revealed(estimatedUnlocks, todayStats)

    companion object {
        private const val SEVEN_DAYS_MS = 7 * 86_400_000L
        private const val EVENING_HOUR = 18
        private const val MIN_UNLOCKS_FOR_ESTIMATE = 5

        val Factory = viewModelFactory {
            initializer {
                HomeViewModel(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
            }
        }
    }
}
