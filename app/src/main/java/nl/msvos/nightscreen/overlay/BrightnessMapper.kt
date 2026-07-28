package nl.msvos.nightscreen.overlay

object BrightnessMapper {
    fun toWindowAlpha(brightnessPercent: Int): Float {
        val brightness = brightnessPercent.coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS)

        return (MAX_BRIGHTNESS - brightness).toFloat() / MAX_BRIGHTNESS
    }

    const val MIN_BRIGHTNESS = 2
    const val MAX_BRIGHTNESS = 100
}
