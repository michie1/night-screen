package nl.msvos.nightscreen.overlay

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

object DimServiceCommands {
    fun start(
        context: Context,
        brightnessTenths: Int,
        autoStopInBrightLight: Boolean,
        brightLightThresholdLux: Int,
    ) {
        ContextCompat.startForegroundService(
            context,
            serviceIntent(context, DimService.ACTION_START).apply {
                putExtra(DimService.EXTRA_BRIGHTNESS_TENTHS, brightnessTenths)
                putExtra(DimService.EXTRA_AUTO_STOP_IN_BRIGHT_LIGHT, autoStopInBrightLight)
                putExtra(DimService.EXTRA_BRIGHT_LIGHT_THRESHOLD_LUX, brightLightThresholdLux)
            },
        )
    }

    fun updateBrightness(context: Context, brightnessTenths: Int) {
        context.startService(
            serviceIntent(context, DimService.ACTION_UPDATE_BRIGHTNESS).apply {
                putExtra(DimService.EXTRA_BRIGHTNESS_TENTHS, brightnessTenths)
            },
        )
    }

    fun updateAutoStop(context: Context, enabled: Boolean, thresholdLux: Int) {
        context.startService(
            serviceIntent(context, DimService.ACTION_UPDATE_AUTO_STOP).apply {
                putExtra(DimService.EXTRA_AUTO_STOP_IN_BRIGHT_LIGHT, enabled)
                putExtra(DimService.EXTRA_BRIGHT_LIGHT_THRESHOLD_LUX, thresholdLux)
            },
        )
    }

    fun panelVisible(context: Context) {
        context.startService(serviceIntent(context, DimService.ACTION_PANEL_VISIBLE))
    }

    fun settingsVisible(context: Context) {
        context.startService(serviceIntent(context, DimService.ACTION_SETTINGS_VISIBLE))
    }

    fun appHidden(context: Context) {
        context.startService(serviceIntent(context, DimService.ACTION_APP_HIDDEN))
    }

    fun stop(context: Context) {
        context.startService(serviceIntent(context, DimService.ACTION_STOP))
    }

    private fun serviceIntent(context: Context, actionName: String) =
        Intent(context, DimService::class.java).apply {
            action = actionName
        }
}
