package nl.msvos.nightscreen.overlay

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper

class BrightLightMonitor(
    context: Context,
    onSustainedBrightLight: () -> Unit,
) : SensorEventListener {
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val handler = Handler(Looper.getMainLooper())

    private var registered = false
    private var scheduledAction: Runnable? = null

    private val gate = BrightLightGate(
        thresholdLux = 20f,
        schedule = { delayMillis, action ->
            val runnable = Runnable(action)
            scheduledAction = runnable
            handler.postDelayed(runnable, delayMillis)
        },
        cancelScheduled = {
            scheduledAction?.let(handler::removeCallbacks)
            scheduledAction = null
        },
        onSustainedBrightLight = onSustainedBrightLight,
    )

    val isSupported: Boolean
        get() = lightSensor != null

    fun setThreshold(thresholdLux: Int) {
        gate.setThreshold(thresholdLux.toFloat())
    }

    fun setEnabled(enabled: Boolean) {
        if (enabled && !registered) {
            val sensor = lightSensor ?: return
            registered = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL,
            )
        } else if (!enabled && registered) {
            stop()
        }
    }

    fun stop() {
        gate.cancel()
        if (registered) {
            sensorManager.unregisterListener(this)
            registered = false
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        event.values.firstOrNull()?.let(gate::onLuxChanged)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
