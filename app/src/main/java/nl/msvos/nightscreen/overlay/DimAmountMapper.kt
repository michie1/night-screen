package nl.msvos.nightscreen.overlay

object DimAmountMapper {
    fun toWindowAlpha(percent: Int, maximumOpacity: Float): Float {
        val safeMaximum = maximumOpacity
            .takeIf { it.isFinite() && it in 0f..1f }
            ?: DEFAULT_MAXIMUM_OPACITY

        return percent.coerceIn(0, 100) / 100f * safeMaximum
    }

    private const val DEFAULT_MAXIMUM_OPACITY = 0.8f
}
