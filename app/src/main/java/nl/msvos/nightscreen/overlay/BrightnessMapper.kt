package nl.msvos.nightscreen.overlay

object BrightnessMapper {
    fun toWindowAlpha(brightnessPercent: Int, maximumOpacity: Float): Float {
        val safeMaximum = maximumOpacity
            .takeIf { it.isFinite() && it in 0f..1f }
            ?: DEFAULT_MAXIMUM_OPACITY
        val brightness = brightnessPercent.coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS)
        val dimFraction = (MAX_BRIGHTNESS - brightness).toFloat() /
            (MAX_BRIGHTNESS - MIN_BRIGHTNESS)

        return dimFraction * safeMaximum
    }

    const val MIN_BRIGHTNESS = 2
    const val MAX_BRIGHTNESS = 100

    private const val DEFAULT_MAXIMUM_OPACITY = 0.8f
}
