package nl.msvos.nightscreen.overlay

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DimServiceState {
    private val mutableRunning = MutableStateFlow(false)

    val running: StateFlow<Boolean> = mutableRunning.asStateFlow()

    fun setRunning(running: Boolean) {
        mutableRunning.value = running
    }
}
