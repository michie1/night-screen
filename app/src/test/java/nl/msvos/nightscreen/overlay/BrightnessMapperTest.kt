package nl.msvos.nightscreen.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class BrightnessMapperTest {
    @Test
    fun twoPercentUsesNinetyEightPercentOpacity() {
        assertEquals(0.98f, BrightnessMapper.toWindowAlpha(2), TOLERANCE)
    }

    @Test
    fun twentyPercentUsesAndroidsUsualTouchLimit() {
        assertEquals(0.8f, BrightnessMapper.toWindowAlpha(20), TOLERANCE)
    }

    @Test
    fun fiftyOnePercentUsesFortyNinePercentOpacity() {
        assertEquals(0.49f, BrightnessMapper.toWindowAlpha(51), TOLERANCE)
    }

    @Test
    fun oneHundredPercentIsTransparent() {
        assertEquals(0f, BrightnessMapper.toWindowAlpha(100), TOLERANCE)
    }

    @Test
    fun brightnessIsClamped() {
        assertEquals(0.98f, BrightnessMapper.toWindowAlpha(0), TOLERANCE)
        assertEquals(0f, BrightnessMapper.toWindowAlpha(101), TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
