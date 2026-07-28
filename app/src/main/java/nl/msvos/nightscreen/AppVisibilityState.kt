package nl.msvos.nightscreen

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppVisibilityState {
    private val mutableVisible = MutableStateFlow(false)

    val visible: StateFlow<Boolean> = mutableVisible.asStateFlow()

    fun setVisible(visible: Boolean) {
        mutableVisible.value = visible
    }
}
