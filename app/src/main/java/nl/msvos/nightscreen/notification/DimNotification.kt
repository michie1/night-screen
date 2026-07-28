package nl.msvos.nightscreen.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import nl.msvos.nightscreen.MainActivity
import nl.msvos.nightscreen.R
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

    fun build(brightnessPercent: Int): Notification {
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

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_night_screen)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("Brightness: ${brightnessPercent.coerceIn(2, 100)}%")
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                R.drawable.ic_stat_night_screen,
                context.getString(R.string.notification_stop),
                stopIntent,
            )
            .build()
    }

    fun update(brightnessPercent: Int) {
        if (
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID,
                build(brightnessPercent),
            )
        }
    }

    companion object {
        const val CHANNEL_ID = "screen_dimming"
        const val NOTIFICATION_ID = 1001

        private const val OPEN_REQUEST_CODE = 1002
        private const val STOP_REQUEST_CODE = 1003
    }
}
