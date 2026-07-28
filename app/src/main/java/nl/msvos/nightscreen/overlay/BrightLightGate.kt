package nl.msvos.nightscreen.overlay

internal class BrightLightGate(
    private val schedule: (delayMillis: Long, action: () -> Unit) -> Unit,
    private val cancelScheduled: () -> Unit,
    private val onSustainedBrightLight: () -> Unit,
) {
    private var isBright = false
    private var countdownActive = false

    fun onLuxChanged(lux: Float) {
        isBright = lux >= LUX_THRESHOLD

        if (!isBright) {
            cancel()
        } else if (!countdownActive) {
            countdownActive = true
            schedule(HOLD_MILLIS) {
                val shouldTrigger = countdownActive && isBright
                countdownActive = false
                if (shouldTrigger) {
                    onSustainedBrightLight()
                }
            }
        }
    }

    fun cancel() {
        countdownActive = false
        cancelScheduled()
    }

    companion object {
        const val LUX_THRESHOLD = 250f
        const val HOLD_MILLIS = 10_000L
    }
}
