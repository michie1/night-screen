package nl.msvos.nightscreen.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class DimPreferencesTest {
    @Test
    fun legacyNoDimBecomesFullBrightness() {
        assertEquals(1_000, DimPreferences.legacyDimToBrightnessTenths(0))
    }

    @Test
    fun legacyDefaultKeepsTheSameVisibleLevel() {
        assertEquals(400, DimPreferences.legacyDimToBrightnessTenths(60))
    }

    @Test
    fun legacyMaximumDimBecomesDarkestBrightness() {
        assertEquals(1, DimPreferences.legacyDimToBrightnessTenths(100))
    }

    @Test
    fun savedWholePercentConvertsExactlyToTenths() {
        assertEquals(570, DimPreferences.percentToTenths(57))
    }
}
