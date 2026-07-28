package nl.msvos.nightscreen.overlay

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

object DimServiceCommands {
    fun start(context: Context, percent: Int) {
        ContextCompat.startForegroundService(
            context,
            serviceIntent(context, DimService.ACTION_START, percent),
        )
    }

    fun update(context: Context, percent: Int) {
        context.startService(serviceIntent(context, DimService.ACTION_UPDATE, percent))
    }

    fun stop(context: Context) {
        context.startService(serviceIntent(context, DimService.ACTION_STOP))
    }

    private fun serviceIntent(
        context: Context,
        actionName: String,
        percent: Int? = null,
    ) = Intent(context, DimService::class.java).apply {
        action = actionName
        if (percent != null) {
            putExtra(DimService.EXTRA_DIM_PERCENT, percent.coerceIn(0, 100))
        }
    }
}
