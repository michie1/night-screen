package nl.msvos.nightscreen.overlay

import kotlin.math.roundToInt
import nl.msvos.nightscreen.settings.DimPreferences

data class OverlayStyle(
    val alpha: Float,
    val red: Int,
    val green: Int,
    val blue: Int,
)

object OverlayStyleMapper {
    fun map(
        brightnessTenths: Int,
        blueLightFilterEnabled: Boolean,
        blueLightFilterStrength: Int,
    ): OverlayStyle {
        val dimAlpha = BrightnessMapper.toWindowAlpha(brightnessTenths)
        val strength = blueLightFilterStrength.coerceIn(
            DimPreferences.MIN_BLUE_LIGHT_FILTER_STRENGTH,
            DimPreferences.MAX_BLUE_LIGHT_FILTER_STRENGTH,
        )
        val filterAlpha = if (blueLightFilterEnabled) {
            strength / 100f * MAX_FILTER_ALPHA
        } else {
            0f
        }
        val combinedAlpha = 1f - (1f - dimAlpha) * (1f - filterAlpha)
        if (filterAlpha == 0f || combinedAlpha == 0f) {
            return OverlayStyle(combinedAlpha, 0, 0, 0)
        }

        return OverlayStyle(
            alpha = combinedAlpha,
            red = (AMBER_RED * filterAlpha / combinedAlpha).roundToInt(),
            green = (AMBER_GREEN * filterAlpha / combinedAlpha).roundToInt(),
            blue = 0,
        )
    }

    private const val MAX_FILTER_ALPHA = 0.6f
    private const val AMBER_RED = 255
    private const val AMBER_GREEN = 152
}
