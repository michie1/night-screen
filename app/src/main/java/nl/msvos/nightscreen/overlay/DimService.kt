package nl.msvos.nightscreen.overlay

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import nl.msvos.nightscreen.notification.DimNotification
import nl.msvos.nightscreen.settings.DimPreferences

class DimService : Service() {
    private lateinit var overlayController: OverlayController
    private lateinit var dimNotification: DimNotification

    override fun onCreate() {
        super.onCreate()
        overlayController = OverlayController(this)
        dimNotification = DimNotification(this)
        dimNotification.createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startDimming(intent.dimPercent())
            ACTION_UPDATE -> updateDimming(intent.dimPercent())
            ACTION_STOP -> stopDimming()
            else -> stopDimming()
        }
        return START_NOT_STICKY
    }

    private fun startDimming(percent: Int) {
        runCatching {
            ServiceCompat.startForeground(
                this,
                DimNotification.NOTIFICATION_ID,
                dimNotification.build(percent),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
            overlayController.show(percent).getOrThrow()
            DimServiceState.setRunning(true)
        }.onFailure(::failSafely)
    }

    private fun updateDimming(percent: Int) {
        if (!overlayController.isShowing) {
            failSafely(IllegalStateException("Update received without an active overlay"))
            return
        }

        overlayController.update(percent)
            .onSuccess { dimNotification.update(percent) }
            .onFailure(::failSafely)
    }

    private fun stopDimming() {
        overlayController.hide()
            .onFailure { Log.w(TAG, "Could not remove dimming overlay", it) }
        DimServiceState.setRunning(false)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun failSafely(error: Throwable) {
        Log.e(TAG, "Dimming failed; stopping safely", error)
        stopDimming()
    }

    override fun onDestroy() {
        overlayController.hide()
            .onFailure { Log.w(TAG, "Could not remove overlay during service shutdown", it) }
        DimServiceState.setRunning(false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun Intent.dimPercent(): Int =
        getIntExtra(EXTRA_DIM_PERCENT, DimPreferences.DEFAULT_DIM_PERCENT).coerceIn(0, 100)

    companion object {
        const val ACTION_START = "nl.msvos.nightscreen.action.START"
        const val ACTION_UPDATE = "nl.msvos.nightscreen.action.UPDATE"
        const val ACTION_STOP = "nl.msvos.nightscreen.action.STOP"
        const val EXTRA_DIM_PERCENT = "nl.msvos.nightscreen.extra.DIM_PERCENT"

        private const val TAG = "NightScreen"
    }
}
