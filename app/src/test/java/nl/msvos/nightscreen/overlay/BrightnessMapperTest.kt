package nl.msvos.nightscreen.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class BrightnessMapperTest {
    @Test
    fun oneTenthPercentUsesNinetyNinePointNinePercentOpacity() {
        assertEquals(0.999f, BrightnessMapper.toWindowAlpha(1), TOLERANCE)
    }

    @Test
    fun onePercentUsesNinetyNinePercentOpacity() {
        assertEquals(0.99f, BrightnessMapper.toWindowAlpha(10), TOLERANCE)
    }

    @Test
    fun twentyPercentUsesAndroidsUsualTouchLimit() {
        assertEquals(0.8f, BrightnessMapper.toWindowAlpha(200), TOLERANCE)
    }

    @Test
    fun oneHundredPercentIsTransparent() {
        assertEquals(0f, BrightnessMapper.toWindowAlpha(1_000), TOLERANCE)
    }

    @Test
    fun brightnessIsClamped() {
        assertEquals(0.999f, BrightnessMapper.toWindowAlpha(0), TOLERANCE)
        assertEquals(0f, BrightnessMapper.toWindowAlpha(1_001), TOLERANCE)
    }

    @Test
    fun percentFormattingUsesDecimalsOnlyWhenNeeded() {
        assertEquals("0.1%", BrightnessMapper.formatPercent(1))
        assertEquals("57%", BrightnessMapper.formatPercent(570))
        assertEquals("57.1%", BrightnessMapper.formatPercent(571))
    }

    @Test
    fun relativeChangesUsePercentagePoints() {
        assertEquals(450, BrightnessMapper.adjustByPercentagePoints(400, 5))
        assertEquals(350, BrightnessMapper.adjustByPercentagePoints(400, -5))
    }

    @Test
    fun relativeChangesClampToBrightnessRange() {
        assertEquals(1, BrightnessMapper.adjustByPercentagePoints(20, -5))
        assertEquals(1_000, BrightnessMapper.adjustByPercentagePoints(980, 5))
    }

    @Test
    fun dimmingUsesMinimumScreenBrightness() {
        assertEquals(0f, BrightnessMapper.toScreenBrightnessOverride(1), TOLERANCE)
        assertEquals(0f, BrightnessMapper.toScreenBrightnessOverride(999), TOLERANCE)
    }

    @Test
    fun fullBrightnessLeavesScreenBrightnessUnchanged() {
        assertEquals(-1f, BrightnessMapper.toScreenBrightnessOverride(1_000), TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
