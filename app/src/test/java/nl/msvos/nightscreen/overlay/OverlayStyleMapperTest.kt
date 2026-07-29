package nl.msvos.nightscreen.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayStyleMapperTest {
    @Test
    fun disabledFilterKeepsBlackDimOverlay() {
        val style = OverlayStyleMapper.map(
            brightnessTenths = 400,
            blueLightFilterEnabled = false,
            blueLightFilterStrength = 100,
        )

        assertEquals(0.6f, style.alpha, TOLERANCE)
        assertEquals(0, style.red)
        assertEquals(0, style.green)
        assertEquals(0, style.blue)
    }

    @Test
    fun filterWorksAtFullBrightness() {
        val style = OverlayStyleMapper.map(
            brightnessTenths = 1_000,
            blueLightFilterEnabled = true,
            blueLightFilterStrength = 100,
        )

        assertEquals(0.6f, style.alpha, TOLERANCE)
        assertEquals(255, style.red)
        assertEquals(152, style.green)
        assertEquals(0, style.blue)
    }

    @Test
    fun dimAndFilterAreCombined() {
        val style = OverlayStyleMapper.map(
            brightnessTenths = 500,
            blueLightFilterEnabled = true,
            blueLightFilterStrength = 50,
        )

        assertEquals(0.65f, style.alpha, TOLERANCE)
        assertTrue(style.red in 117..118)
        assertTrue(style.green in 70..71)
        assertEquals(0, style.blue)
    }

    @Test
    fun filterStrengthIsClamped() {
        val tooStrong = OverlayStyleMapper.map(1_000, true, 200)
        val strongest = OverlayStyleMapper.map(1_000, true, 100)

        assertEquals(strongest, tooStrong)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
