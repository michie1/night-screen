package nl.msvos.nightscreen.overlay

object BrightnessMapper {
    fun adjustByPercentagePoints(brightnessTenths: Int, percentagePoints: Int): Int =
        (brightnessTenths + percentagePoints * 10).coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS)

    fun toWindowAlpha(brightnessTenths: Int): Float {
        val brightness = brightnessTenths.coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS)

        return (MAX_BRIGHTNESS - brightness).toFloat() / MAX_BRIGHTNESS
    }

    fun toScreenBrightnessOverride(brightnessTenths: Int): Float =
        if (brightnessTenths.coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS) < MAX_BRIGHTNESS) {
            SCREEN_BRIGHTNESS_MINIMUM
        } else {
            SCREEN_BRIGHTNESS_UNCHANGED
        }

    fun formatPercent(brightnessTenths: Int): String {
        val brightness = brightnessTenths.coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS)
        return if (brightness % 10 == 0) {
            "${brightness / 10}%"
        } else {
            "${brightness / 10}.${brightness % 10}%"
        }
    }

    const val MIN_BRIGHTNESS = 1
    const val MAX_BRIGHTNESS = 1_000

    private const val SCREEN_BRIGHTNESS_MINIMUM = 0f
    private const val SCREEN_BRIGHTNESS_UNCHANGED = -1f
}
