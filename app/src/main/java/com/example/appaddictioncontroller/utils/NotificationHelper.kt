package com.example.appaddictioncontroller.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.appaddictioncontroller.R
import com.example.appaddictioncontroller.ui.MainActivity

class NotificationHelper(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        const val CHANNEL_ID_SERVICE = "monitoring_service_channel"
        const val CHANNEL_ID_WARNING = "warning_channel"
        const val NOTIFICATION_ID_SERVICE = 1001
        const val NOTIFICATION_ID_WARNING = 1002
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Service channel
            val serviceChannel = NotificationChannel(
                CHANNEL_ID_SERVICE,
                "Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows that the app monitoring service is running"
            }
            
            // Warning channel
            val warningChannel = NotificationChannel(
                CHANNEL_ID_WARNING,
                "Usage Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when you're approaching or exceeding usage limits"
            }
            
            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(warningChannel)
        }
    }
    
    fun createServiceNotification(): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, CHANNEL_ID_SERVICE)
            .setContentTitle("App Addiction Controller")
            .setContentText("Monitoring your app usage")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    fun sendWarningNotification(appName: String, warningNumber: Int, totalWarnings: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WARNING)
            .setContentTitle("⚠️ Usage Warning ($warningNumber/$totalWarnings)")
            .setContentText("You've been using $appName for too long. Take a break!")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_WARNING + warningNumber, notification)
    }
    
    fun sendLimitReachedNotification(appName: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WARNING)
            .setContentTitle("🚫 Time Limit Reached")
            .setContentText("$appName has been blocked for 1 hour")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_WARNING, notification)
    }
}
