package com.example.appaddictioncontroller.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.appaddictioncontroller.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_SERVICE = "monitoring_service_channel"
        const val CHANNEL_ID_WARNING = "usage_warning_channel"
        const val NOTIFICATION_ID_SERVICE = 1
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID_SERVICE,
                "Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
            )
            
            val warningChannel = NotificationChannel(
                CHANNEL_ID_WARNING,
                "Usage Warnings",
                NotificationManager.IMPORTANCE_DEFAULT // Lowered from HIGH for "calm"
            )

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            manager.createNotificationChannel(warningChannel)
        }
    }

    fun createServiceNotification() = NotificationCompat.Builder(context, CHANNEL_ID_SERVICE)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Digital Wellbeing Active")
        .setContentText("Monitoring your screen time...")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    fun sendWarningNotification(appName: String, warningNumber: Int, totalWarnings: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WARNING)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Gentle Reminder")
            .setContentText("You've been using $appName for a while. Consider taking a break.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2, notification)
    }

    fun sendLimitReachedNotification(appName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WARNING)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Daily Limit Reached")
            .setContentText("$appName is now paused. Resetting tomorrow.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(3, notification)
    }
}
