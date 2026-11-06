package app.nivvi.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.nivvi.data.AlertSettings
import app.nivvi.data.AppDatabase
import app.nivvi.data.HeartRateReading
import app.nivvi.data.SettingsRepository
import app.nivvi.service.HeartRateMonitorService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LiveState(
    val heartRate: Int? = null,
    val oxygen: Int? = null,
    val connectionState: String = "Disconnected",
    val alertActive: Boolean = false
)

class MonitorViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepo = SettingsRepository(application)
    private val dao = AppDatabase.get(application).heartRateDao()

    private val _live = MutableStateFlow(LiveState())
    val live: StateFlow<LiveState> = _live.asStateFlow()

    private val _settings = MutableStateFlow(AlertSettings())
    val settings: StateFlow<AlertSettings> = _settings.asStateFlow()

    private val _history = MutableStateFlow<List<HeartRateReading>>(emptyList())
    val history: StateFlow<List<HeartRateReading>> = _history.asStateFlow()

    private var service: HeartRateMonitorService? = null
    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as? HeartRateMonitorService.LocalBinder)?.getService()
            bound = true
            service?.updateSettings(_settings.value)
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            bound = false
        }
    }

    init {
        viewModelScope.launch {
            settingsRepo.alertSettings.collect { s ->
                _settings.value = s
                service?.updateSettings(s)
            }
        }
        viewModelScope.launch {
            while (true) {
                _live.value = LiveState(
                    heartRate = HeartRateMonitorService.latestHr,
                    oxygen = HeartRateMonitorService.latestSpo2,
                    connectionState = HeartRateMonitorService.connectionState
                )
                delay(400)
            }
        }
        viewModelScope.launch {
            val from = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            dao.readingsFrom(from).collect { _history.value = it }
        }
    }

    fun startMonitoring(context: Context) {
        val intent = Intent(context, HeartRateMonitorService::class.java)
        context.startForegroundService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        viewModelScope.launch {
            delay(600)
            service?.startScan()
        }
    }

    fun updateSettings(settings: AlertSettings) {
        viewModelScope.launch {
            settingsRepo.update(settings)
        }
    }

    override fun onCleared() {
        if (bound) {
            getApplication<Application>().unbindService(connection)
        }
        super.onCleared()
    }
}
