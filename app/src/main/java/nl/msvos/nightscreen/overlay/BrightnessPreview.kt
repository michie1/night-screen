package nl.msvos.nightscreen.overlay

internal class BrightnessPreview(
    private val schedule: (delayMillis: Long, action: () -> Unit) -> Unit,
    private val cancelScheduled: () -> Unit,
    private val showOverlay: () -> Unit,
    private val hideOverlay: () -> Unit,
    private val setPreviewing: (Boolean) -> Unit,
) {
    private var previewing = false

    fun start() {
        cancelScheduled()
        if (!previewing) {
            previewing = true
            setPreviewing(true)
        }
        showOverlay()
        schedule(PREVIEW_MILLIS) {
            if (previewing) {
                previewing = false
                setPreviewing(false)
                hideOverlay()
            }
        }
    }

    fun appVisible() {
        clear()
        hideOverlay()
    }

    fun updateDirectly() {
        clear()
        showOverlay()
    }

    fun appHidden() {
        clear()
        showOverlay()
    }

    fun stop() {
        clear()
        hideOverlay()
    }

    private fun clear() {
        cancelScheduled()
        if (previewing) {
            previewing = false
            setPreviewing(false)
        }
    }

    companion object {
        const val PREVIEW_MILLIS = 10_000L
    }
}
