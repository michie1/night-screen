package nl.msvos.nightscreen

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
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
import nl.msvos.nightscreen.overlay.DimServiceCommands
import nl.msvos.nightscreen.overlay.DimServiceState
import nl.msvos.nightscreen.settings.DimPreferences

data class MainUiState(
    val dimPercent: Int = DimPreferences.DEFAULT_DIM_PERCENT,
    val overlayPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val isRunning: Boolean = false,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val dimPreferences = DimPreferences(appContext)
    private val mutableUiState = MutableStateFlow(MainUiState())

    private var saveJob: Job? = null
    private var serviceUpdateJob: Job? = null

    val uiState: StateFlow<MainUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            dimPreferences.dimPercent.collect { percent ->
                mutableUiState.update { it.copy(dimPercent = percent) }
            }
        }
        viewModelScope.launch {
            DimServiceState.running.collect { running ->
                mutableUiState.update { it.copy(isRunning = running) }
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

    fun setDimPercent(percent: Int) {
        val clamped = percent.coerceIn(
            DimPreferences.MIN_DIM_PERCENT,
            DimPreferences.MAX_DIM_PERCENT,
        )
        mutableUiState.update { it.copy(dimPercent = clamped) }

        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(PREFERENCE_DEBOUNCE_MILLIS)
            runCatching { dimPreferences.saveDimPercent(clamped) }
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
                    DimServiceCommands.update(appContext, clamped)
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
            dimPreferences.saveDimPercent(state.dimPercent)
        }
        DimServiceCommands.start(appContext, state.dimPercent)
    }

    fun stopDimming() {
        serviceUpdateJob?.cancel()
        if (mutableUiState.value.isRunning) {
            DimServiceCommands.stop(appContext)
        }
    }

    companion object {
        private const val SERVICE_UPDATE_DEBOUNCE_MILLIS = 75L
        private const val PREFERENCE_DEBOUNCE_MILLIS = 250L
    }
}
