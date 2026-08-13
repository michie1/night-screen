package nl.msvos.nightscreen.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import nl.msvos.nightscreen.MainActivity
import nl.msvos.nightscreen.R
import nl.msvos.nightscreen.overlay.BrightnessMapper
import nl.msvos.nightscreen.overlay.DimService

class DimNotification(private val context: Context) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            setSound(null, null)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun build(brightnessTenths: Int, paused: Boolean): Notification {
        val openIntent = PendingIntent.getActivity(
            context,
            OPEN_REQUEST_CODE,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getService(
            context,
            STOP_REQUEST_CODE,
            Intent(context, DimService::class.java).apply {
                action = DimService.ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val pauseResumeIntent = PendingIntent.getService(
            context,
            PAUSE_RESUME_REQUEST_CODE,
            Intent(context, DimService::class.java).apply {
                action = if (paused) DimService.ACTION_RESUME else DimService.ACTION_PAUSE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val decreaseIntent = servicePendingIntent(
            DECREASE_REQUEST_CODE,
            DimService.ACTION_DECREASE_BRIGHTNESS,
        )
        val increaseIntent = servicePendingIntent(
            INCREASE_REQUEST_CODE,
            DimService.ACTION_INCREASE_BRIGHTNESS,
        )
        val status = context.getString(
            if (paused) R.string.notification_paused else R.string.notification_brightness,
            BrightnessMapper.formatPercent(brightnessTenths),
        )
        val expandedView = RemoteViews(context.packageName, R.layout.notification_dim_controls)
            .apply {
                setTextViewText(R.id.notification_status, status)
                setTextViewText(
                    R.id.notification_pause_resume,
                    context.getString(
                        if (paused) R.string.notification_resume else R.string.notification_pause,
                    ),
                )
                setOnClickPendingIntent(R.id.notification_decrease, decreaseIntent)
                setOnClickPendingIntent(R.id.notification_increase, increaseIntent)
                setOnClickPendingIntent(R.id.notification_pause_resume, pauseResumeIntent)
                setOnClickPendingIntent(R.id.notification_stop, stopIntent)
                setBoolean(
                    R.id.notification_decrease,
                    "setEnabled",
                    brightnessTenths > BrightnessMapper.MIN_BRIGHTNESS,
                )
                setBoolean(
                    R.id.notification_increase,
                    "setEnabled",
                    brightnessTenths < BrightnessMapper.MAX_BRIGHTNESS,
                )
            }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_night_screen)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(status)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomBigContentView(expandedView)
            .build()
    }

    private fun servicePendingIntent(requestCode: Int, action: String): PendingIntent =
        PendingIntent.getService(
            context,
            requestCode,
            Intent(context, DimService::class.java).apply { this.action = action },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    fun update(brightnessTenths: Int, paused: Boolean) {
        if (
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID,
                build(brightnessTenths, paused),
            )
        }
    }

    companion object {
        const val CHANNEL_ID = "screen_dimming"
        const val NOTIFICATION_ID = 1001

        private const val OPEN_REQUEST_CODE = 1002
        private const val STOP_REQUEST_CODE = 1003
        private const val PAUSE_RESUME_REQUEST_CODE = 1004
        private const val DECREASE_REQUEST_CODE = 1005
        private const val INCREASE_REQUEST_CODE = 1006
    }
}
