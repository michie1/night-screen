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
    fun thresholdStartsTenSecondHold() {
        gate.onLuxChanged(5_000f)

        assertEquals(10_000L, scheduledDelay)
        assertFalse(triggered)

        scheduledAction?.invoke()
        assertTrue(triggered)
    }

    @Test
    fun lowerLightCancelsPendingStop() {
        gate.onLuxChanged(5_000f)
        gate.onLuxChanged(4_999f)

        assertTrue(canceled)
        assertFalse(triggered)
        assertEquals(null, scheduledAction)
    }

    @Test
    fun brightUpdatesDoNotRestartTheHold() {
        gate.onLuxChanged(6_000f)
        val firstAction = scheduledAction
        gate.onLuxChanged(7_000f)

        assertEquals(firstAction, scheduledAction)
    }
}
