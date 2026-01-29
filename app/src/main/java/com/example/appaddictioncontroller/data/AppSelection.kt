package com.example.appaddictioncontroller.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_selections")
data class AppSelection(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val timeLimitMinutes: Int, // Daily time limit in minutes
    val isEnabled: Boolean = true,
    val lastResetDate: Long = System.currentTimeMillis(), // For daily reset
    val totalUsageToday: Long = 0L, // Total usage in milliseconds for today
    val continuousUsageStart: Long = 0L, // When continuous usage started
    val blockUntil: Long = 0L, // Timestamp until which app is blocked
    val warningsSent: Int = 0 // Number of warnings sent during continuous usage
)
