package nl.msvos.nightscreen.overlay

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

object DimServiceCommands {
    fun start(
        context: Context,
        brightnessPercent: Int,
        autoStopInBrightLight: Boolean,
    ) {
        ContextCompat.startForegroundService(
            context,
            serviceIntent(context, DimService.ACTION_START).apply {
                putExtra(DimService.EXTRA_BRIGHTNESS_PERCENT, brightnessPercent)
                putExtra(DimService.EXTRA_AUTO_STOP_IN_BRIGHT_LIGHT, autoStopInBrightLight)
            },
        )
    }

    fun updateBrightness(context: Context, brightnessPercent: Int) {
        context.startService(
            serviceIntent(context, DimService.ACTION_UPDATE_BRIGHTNESS).apply {
                putExtra(DimService.EXTRA_BRIGHTNESS_PERCENT, brightnessPercent)
            },
        )
    }

    fun updateAutoStop(context: Context, enabled: Boolean) {
        context.startService(
            serviceIntent(context, DimService.ACTION_UPDATE_AUTO_STOP).apply {
                putExtra(DimService.EXTRA_AUTO_STOP_IN_BRIGHT_LIGHT, enabled)
            },
        )
    }

    fun appVisible(context: Context) {
        context.startService(serviceIntent(context, DimService.ACTION_APP_VISIBLE))
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
