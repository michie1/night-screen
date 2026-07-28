package nl.msvos.nightscreen.overlay

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ServiceCompat
import nl.msvos.nightscreen.AppVisibilityState
import nl.msvos.nightscreen.notification.DimNotification
import nl.msvos.nightscreen.settings.DimPreferences

class DimService : Service() {
    private lateinit var overlayController: OverlayController
    private lateinit var dimNotification: DimNotification
    private lateinit var brightLightMonitor: BrightLightMonitor
    private lateinit var brightnessPreview: BrightnessPreview
    private val previewHandler = Handler(Looper.getMainLooper())
    private var previewExpiry: Runnable? = null

    private var brightnessTenths = DimPreferences.DEFAULT_BRIGHTNESS_TENTHS
    private var autoStopInBrightLight = false
    private var isStopping = false

    override fun onCreate() {
        super.onCreate()
        overlayController = OverlayController(this)
        dimNotification = DimNotification(this)
        brightLightMonitor = BrightLightMonitor(this, ::stopDimming)
        DimServiceState.setPreviewing(false)
        brightnessPreview = BrightnessPreview(
            schedule = { delayMillis, action ->
                Runnable(action).also { runnable ->
                    previewExpiry = runnable
                    previewHandler.postDelayed(runnable, delayMillis)
                }
            },
            cancelScheduled = {
                previewExpiry?.let(previewHandler::removeCallbacks)
                previewExpiry = null
            },
            showOverlay = {
                overlayController.show(brightnessTenths)
                    .onFailure(::failSafely)
            },
            hideOverlay = {
                overlayController.hide()
                    .onFailure(::failSafely)
            },
            setPreviewing = DimServiceState::setPreviewing,
        )
        dimNotification.createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startDimming(
                brightness = intent.brightnessTenths(),
                autoStop = intent.getBooleanExtra(EXTRA_AUTO_STOP_IN_BRIGHT_LIGHT, false),
            )

            ACTION_UPDATE_BRIGHTNESS -> updateBrightness(intent.brightnessTenths())
            ACTION_UPDATE_AUTO_STOP -> updateAutoStop(
                intent.getBooleanExtra(EXTRA_AUTO_STOP_IN_BRIGHT_LIGHT, false),
            )

            ACTION_APP_VISIBLE -> pauseOverlay()
            ACTION_APP_HIDDEN -> resumeOverlay()
            ACTION_STOP -> stopDimming()
            else -> stopDimming()
        }
        return START_NOT_STICKY
    }

    private fun startDimming(brightness: Int, autoStop: Boolean) {
        brightnessTenths = brightness
        autoStopInBrightLight = autoStop && brightLightMonitor.isSupported

        runCatching {
            ServiceCompat.startForeground(
                this,
                DimNotification.NOTIFICATION_ID,
                dimNotification.build(brightnessTenths),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
            if (!AppVisibilityState.visible.value) {
                overlayController.show(brightnessTenths).getOrThrow()
            }
            DimServiceState.setRunning(true)
            brightLightMonitor.setEnabled(autoStopInBrightLight)
        }.onFailure(::failSafely)
    }

    private fun updateBrightness(brightness: Int) {
        if (!DimServiceState.running.value) {
            stopSelf()
            return
        }

        brightnessTenths = brightness
        val result = if (AppVisibilityState.visible.value) {
            brightnessPreview.start()
            Result.success(Unit)
        } else if (overlayController.isShowing) {
            overlayController.update(brightnessTenths)
        } else {
            Result.success(Unit)
        }
        result
            .onSuccess { dimNotification.update(brightnessTenths) }
            .onFailure(::failSafely)
    }

    private fun updateAutoStop(enabled: Boolean) {
        if (!DimServiceState.running.value) {
            stopSelf()
            return
        }

        autoStopInBrightLight = enabled && brightLightMonitor.isSupported
        brightLightMonitor.setEnabled(autoStopInBrightLight)
    }

    private fun pauseOverlay() {
        if (!DimServiceState.running.value) {
            stopSelf()
            return
        }
        brightnessPreview.appVisible()
    }

    private fun resumeOverlay() {
        if (!DimServiceState.running.value) {
            return
        }
        brightnessPreview.appHidden()
    }

    private fun stopDimming() {
        if (isStopping) {
            return
        }
        isStopping = true
        brightnessPreview.stop()
        brightLightMonitor.stop()
        DimServiceState.setRunning(false)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun failSafely(error: Throwable) {
        Log.e(TAG, "Dimming failed; stopping safely", error)
        if (!isStopping) {
            stopDimming()
        }
    }

    override fun onDestroy() {
        brightnessPreview.stop()
        brightLightMonitor.stop()
        DimServiceState.setRunning(false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun Intent.brightnessTenths(): Int =
        getIntExtra(
            EXTRA_BRIGHTNESS_TENTHS,
            DimPreferences.DEFAULT_BRIGHTNESS_TENTHS,
        ).coerceIn(BrightnessMapper.MIN_BRIGHTNESS, BrightnessMapper.MAX_BRIGHTNESS)

    companion object {
        const val ACTION_START = "nl.msvos.nightscreen.action.START"
        const val ACTION_UPDATE_BRIGHTNESS =
            "nl.msvos.nightscreen.action.UPDATE_BRIGHTNESS"
        const val ACTION_UPDATE_AUTO_STOP =
            "nl.msvos.nightscreen.action.UPDATE_AUTO_STOP"
        const val ACTION_APP_VISIBLE = "nl.msvos.nightscreen.action.APP_VISIBLE"
        const val ACTION_APP_HIDDEN = "nl.msvos.nightscreen.action.APP_HIDDEN"
        const val ACTION_STOP = "nl.msvos.nightscreen.action.STOP"

        const val EXTRA_BRIGHTNESS_TENTHS =
            "nl.msvos.nightscreen.extra.BRIGHTNESS_TENTHS"
        const val EXTRA_AUTO_STOP_IN_BRIGHT_LIGHT =
            "nl.msvos.nightscreen.extra.AUTO_STOP_IN_BRIGHT_LIGHT"

        private const val TAG = "NightScreen"
    }
}
