package nl.msvos.nightscreen.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class BrightnessMapperTest {
    @Test
    fun twoPercentUsesTheSafeMaximum() {
        assertEquals(0.8f, BrightnessMapper.toWindowAlpha(2, 0.8f), TOLERANCE)
    }

    @Test
    fun fiftyOnePercentUsesHalfTheSafeMaximum() {
        assertEquals(0.4f, BrightnessMapper.toWindowAlpha(51, 0.8f), TOLERANCE)
    }

    @Test
    fun oneHundredPercentIsTransparent() {
        assertEquals(0f, BrightnessMapper.toWindowAlpha(100, 0.8f), TOLERANCE)
    }

    @Test
    fun brightnessIsClamped() {
        assertEquals(0.8f, BrightnessMapper.toWindowAlpha(0, 0.8f), TOLERANCE)
        assertEquals(0f, BrightnessMapper.toWindowAlpha(101, 0.8f), TOLERANCE)
    }

    @Test
    fun invalidMaximumFallsBackToAndroidDocumentedValue() {
        assertEquals(0.8f, BrightnessMapper.toWindowAlpha(2, Float.NaN), TOLERANCE)
        assertEquals(0.8f, BrightnessMapper.toWindowAlpha(2, 2f), TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
