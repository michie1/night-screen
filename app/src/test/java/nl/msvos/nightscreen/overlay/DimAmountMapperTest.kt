package nl.msvos.nightscreen.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class DimAmountMapperTest {
    @Test
    fun zeroPercentIsTransparent() {
        assertEquals(0f, DimAmountMapper.toWindowAlpha(0, 0.8f), TOLERANCE)
    }

    @Test
    fun fiftyPercentUsesHalfTheSafeMaximum() {
        assertEquals(0.4f, DimAmountMapper.toWindowAlpha(50, 0.8f), TOLERANCE)
    }

    @Test
    fun oneHundredPercentUsesTheSafeMaximum() {
        assertEquals(0.8f, DimAmountMapper.toWindowAlpha(100, 0.8f), TOLERANCE)
    }

    @Test
    fun percentIsClamped() {
        assertEquals(0f, DimAmountMapper.toWindowAlpha(-1, 0.8f), TOLERANCE)
        assertEquals(0.8f, DimAmountMapper.toWindowAlpha(101, 0.8f), TOLERANCE)
    }

    @Test
    fun invalidMaximumFallsBackToAndroidDocumentedValue() {
        assertEquals(0.8f, DimAmountMapper.toWindowAlpha(100, Float.NaN), TOLERANCE)
        assertEquals(0.8f, DimAmountMapper.toWindowAlpha(100, 2f), TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
