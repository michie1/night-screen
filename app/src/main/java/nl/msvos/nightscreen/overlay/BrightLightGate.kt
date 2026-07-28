package nl.msvos.nightscreen.overlay

internal class BrightLightGate(
    thresholdLux: Float,
    private val schedule: (delayMillis: Long, action: () -> Unit) -> Unit,
    private val cancelScheduled: () -> Unit,
    private val onSustainedBrightLight: () -> Unit,
) {
    private var thresholdLux = thresholdLux
    private var latestLux: Float? = null
    private var isBright = false
    private var countdownActive = false

    fun onLuxChanged(lux: Float) {
        latestLux = lux
        isBright = lux >= thresholdLux

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

    fun setThreshold(thresholdLux: Float) {
        if (this.thresholdLux == thresholdLux) {
            return
        }
        this.thresholdLux = thresholdLux
        cancel()
        latestLux?.let(::onLuxChanged)
    }

    fun cancel() {
        countdownActive = false
        cancelScheduled()
    }

    companion object {
        const val HOLD_MILLIS = 10_000L
    }
}
