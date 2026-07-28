package nl.msvos.nightscreen

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import nl.msvos.nightscreen.overlay.BrightnessMapper
import nl.msvos.nightscreen.overlay.DimServiceCommands
import nl.msvos.nightscreen.overlay.DimServiceState
import nl.msvos.nightscreen.settings.DimPreferences

data class MainUiState(
    val brightnessTenths: Int = DimPreferences.DEFAULT_BRIGHTNESS_TENTHS,
    val autoStopInBrightLight: Boolean = false,
    val brightLightThresholdLux: Int = DimPreferences.DEFAULT_BRIGHT_LIGHT_THRESHOLD_LUX,
    val lightSensorAvailable: Boolean = true,
    val currentLux: Int? = null,
    val overlayPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val isRunning: Boolean = false,
    val isPreviewing: Boolean = false,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val dimPreferences = DimPreferences(appContext)
    private val sensorManager = appContext.getSystemService(SensorManager::class.java)
    private val mutableUiState = MutableStateFlow(
        MainUiState(
            lightSensorAvailable =
                sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null,
        ),
    )

    private var saveBrightnessJob: Job? = null
    private var saveLightThresholdJob: Job? = null
    private var serviceUpdateJob: Job? = null
    private var lightServiceUpdateJob: Job? = null
    private var lightSensorRegistered = false
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val lightSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val lux = event.values.firstOrNull()?.coerceAtLeast(0f)?.roundToInt() ?: return
            mutableUiState.update { it.copy(currentLux = lux) }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    val uiState: StateFlow<MainUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            dimPreferences.migrateBrightnessScale()
            dimPreferences.settings.collect { settings ->
                mutableUiState.update {
                    it.copy(
                        brightnessTenths = settings.brightnessTenths,
                        autoStopInBrightLight =
                            settings.autoStopInBrightLight && it.lightSensorAvailable,
                        brightLightThresholdLux = settings.brightLightThresholdLux,
                    )
                }
            }
        }
        viewModelScope.launch {
            DimServiceState.running.collect { running ->
                mutableUiState.update { it.copy(isRunning = running) }
            }
        }
        viewModelScope.launch {
            DimServiceState.isPreviewing.collect { previewing ->
                mutableUiState.update { it.copy(isPreviewing = previewing) }
            }
        }
        refreshPermissions()
    }

    fun refreshPermissions() {
        mutableUiState.update {
            it.copy(
                overlayPermissionGranted = Settings.canDrawOverlays(appContext),
                notificationPermissionGranted =
                    ContextCompat.checkSelfPermission(
                        appContext,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED,
            )
        }

        if (!mutableUiState.value.overlayPermissionGranted && mutableUiState.value.isRunning) {
            stopDimming()
        }
    }

    fun startLightReading() {
        if (lightSensorRegistered) {
            return
        }
        val sensor = lightSensor ?: return
        mutableUiState.update { it.copy(currentLux = null) }
        lightSensorRegistered = sensorManager.registerListener(
            lightSensorListener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL,
        )
    }

    fun stopLightReading() {
        if (lightSensorRegistered) {
            sensorManager.unregisterListener(lightSensorListener)
            lightSensorRegistered = false
        }
    }

    fun setBrightnessTenths(tenths: Int) {
        val clamped = tenths.coerceIn(
            BrightnessMapper.MIN_BRIGHTNESS,
            BrightnessMapper.MAX_BRIGHTNESS,
        )
        mutableUiState.update { it.copy(brightnessTenths = clamped) }

        saveBrightnessJob?.cancel()
        saveBrightnessJob = viewModelScope.launch {
            delay(PREFERENCE_DEBOUNCE_MILLIS)
            runCatching { dimPreferences.saveBrightnessTenths(clamped) }
                .onFailure { error ->
                    if (error !is IOException) {
                        throw error
                    }
                }
        }

        if (mutableUiState.value.isRunning) {
            serviceUpdateJob?.cancel()
            serviceUpdateJob = viewModelScope.launch {
                delay(SERVICE_UPDATE_DEBOUNCE_MILLIS)
                if (mutableUiState.value.isRunning) {
                    DimServiceCommands.updateBrightness(appContext, clamped)
                }
            }
        }
    }

    fun setAutoStopInBrightLight(enabled: Boolean) {
        val supportedValue = enabled && mutableUiState.value.lightSensorAvailable
        mutableUiState.update { it.copy(autoStopInBrightLight = supportedValue) }
        viewModelScope.launch {
            dimPreferences.saveAutoStopInBrightLight(supportedValue)
        }
        if (mutableUiState.value.isRunning) {
            DimServiceCommands.updateAutoStop(
                appContext,
                supportedValue,
                mutableUiState.value.brightLightThresholdLux,
            )
        }
    }

    fun setBrightLightThresholdLux(thresholdLux: Int) {
        val clamped = thresholdLux.coerceIn(
            DimPreferences.MIN_BRIGHT_LIGHT_THRESHOLD_LUX,
            DimPreferences.MAX_BRIGHT_LIGHT_THRESHOLD_LUX,
        )
        mutableUiState.update { it.copy(brightLightThresholdLux = clamped) }

        saveLightThresholdJob?.cancel()
        saveLightThresholdJob = viewModelScope.launch {
            delay(PREFERENCE_DEBOUNCE_MILLIS)
            dimPreferences.saveBrightLightThresholdLux(clamped)
        }

        if (mutableUiState.value.isRunning) {
            lightServiceUpdateJob?.cancel()
            lightServiceUpdateJob = viewModelScope.launch {
                delay(SERVICE_UPDATE_DEBOUNCE_MILLIS)
                if (mutableUiState.value.isRunning) {
                    DimServiceCommands.updateAutoStop(
                        appContext,
                        mutableUiState.value.autoStopInBrightLight,
                        clamped,
                    )
                }
            }
        }
    }

    fun startDimming() {
        val state = mutableUiState.value
        if (!state.overlayPermissionGranted || !state.notificationPermissionGranted) {
            refreshPermissions()
            return
        }

        viewModelScope.launch {
            dimPreferences.saveBrightnessTenths(state.brightnessTenths)
            dimPreferences.saveAutoStopInBrightLight(state.autoStopInBrightLight)
            dimPreferences.saveBrightLightThresholdLux(state.brightLightThresholdLux)
        }
        DimServiceCommands.start(
            context = appContext,
            brightnessTenths = state.brightnessTenths,
            autoStopInBrightLight = state.autoStopInBrightLight,
            brightLightThresholdLux = state.brightLightThresholdLux,
        )
    }

    fun stopDimming() {
        serviceUpdateJob?.cancel()
        lightServiceUpdateJob?.cancel()
        if (mutableUiState.value.isRunning) {
            DimServiceCommands.stop(appContext)
        }
    }

    fun showActivePanel() {
        if (mutableUiState.value.isRunning) {
            DimServiceCommands.panelVisible(appContext)
        }
    }

    fun showFullSettings() {
        if (mutableUiState.value.isRunning) {
            DimServiceCommands.settingsVisible(appContext)
        }
    }

    fun appHidden() {
        if (mutableUiState.value.isRunning) {
            DimServiceCommands.appHidden(appContext)
        }
    }

    override fun onCleared() {
        stopLightReading()
        super.onCleared()
    }

    companion object {
        private const val SERVICE_UPDATE_DEBOUNCE_MILLIS = 75L
        private const val PREFERENCE_DEBOUNCE_MILLIS = 250L
    }
}
