package nl.msvos.nightscreen.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BrightnessPreviewTest {
    private var scheduledDelay = 0L
    private var scheduledAction: (() -> Unit)? = null
    private var showCount = 0
    private var hideCount = 0
    private var cancelCount = 0
    private var previewing = false

    private val preview = BrightnessPreview(
        schedule = { delay, action ->
            scheduledDelay = delay
            scheduledAction = action
        },
        cancelScheduled = {
            cancelCount += 1
            scheduledAction = null
        },
        showOverlay = { showCount += 1 },
        hideOverlay = { hideCount += 1 },
        setPreviewing = { previewing = it },
    )

    @Test
    fun sliderUpdateStartsTenSecondPreview() {
        preview.start()

        assertTrue(previewing)
        assertEquals(1, showCount)
        assertEquals(10_000L, scheduledDelay)
    }

    @Test
    fun sliderUpdateResetsPreviewTimer() {
        preview.start()
        val firstAction = scheduledAction

        preview.start()

        assertTrue(previewing)
        assertEquals(2, showCount)
        assertEquals(2, cancelCount)
        assertNotSame(firstAction, scheduledAction)
    }

    @Test
    fun expiryHidesPreview() {
        preview.start()

        scheduledAction?.invoke()

        assertFalse(previewing)
        assertEquals(1, hideCount)
    }

    @Test
    fun appBackgroundCancelsTimerAndKeepsOverlayVisible() {
        preview.start()

        preview.appHidden()

        assertFalse(previewing)
        assertEquals(null, scheduledAction)
        assertEquals(2, showCount)
        assertEquals(0, hideCount)
    }

    @Test
    fun stopCancelsTimerAndHidesOverlay() {
        preview.start()

        preview.stop()

        assertFalse(previewing)
        assertEquals(null, scheduledAction)
        assertEquals(1, hideCount)
    }

    @Test
    fun appOpenAndCleanupClearPreview() {
        preview.start()
        preview.appVisible()

        assertFalse(previewing)
        assertEquals(null, scheduledAction)
        assertEquals(1, hideCount)

        preview.start()
        preview.stop()

        assertFalse(previewing)
        assertEquals(null, scheduledAction)
        assertEquals(2, hideCount)
    }
}
