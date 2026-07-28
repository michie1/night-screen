package nl.msvos.nightscreen.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class DimPreferencesTest {
    @Test
    fun legacyNoDimBecomesFullBrightness() {
        assertEquals(100, DimPreferences.legacyDimToBrightness(0))
    }

    @Test
    fun legacyDefaultKeepsAboutTheSameVisibleLevel() {
        assertEquals(41, DimPreferences.legacyDimToBrightness(60))
    }

    @Test
    fun legacyMaximumDimBecomesDarkestBrightness() {
        assertEquals(2, DimPreferences.legacyDimToBrightness(100))
    }
}
