package nl.msvos.nightscreen.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrightLightGateTest {
    private var scheduledDelay = 0L
    private var scheduledAction: (() -> Unit)? = null
    private var canceled = false
    private var triggered = false

    private val gate = BrightLightGate(
        thresholdLux = 20f,
        schedule = { delay, action ->
            scheduledDelay = delay
            scheduledAction = action
        },
        cancelScheduled = {
            canceled = true
            scheduledAction = null
        },
        onSustainedBrightLight = {
            triggered = true
        },
    )

    @Test
    fun thresholdStartsFiveSecondHold() {
        gate.onLuxChanged(20f)

        assertEquals(5_000L, scheduledDelay)
        assertFalse(triggered)

        scheduledAction?.invoke()
        assertTrue(triggered)
    }

    @Test
    fun lowerLightCancelsPendingStop() {
        gate.onLuxChanged(20f)
        gate.onLuxChanged(19f)

        assertTrue(canceled)
        assertFalse(triggered)
        assertEquals(null, scheduledAction)
    }

    @Test
    fun brightUpdatesDoNotRestartTheHold() {
        gate.onLuxChanged(30f)
        val firstAction = scheduledAction
        gate.onLuxChanged(35f)

        assertEquals(firstAction, scheduledAction)
    }

    @Test
    fun changingThresholdRestartsDecisionUsingLatestReading() {
        gate.onLuxChanged(15f)

        gate.setThreshold(10f)

        assertEquals(5_000L, scheduledDelay)
        assertFalse(triggered)
    }
}
