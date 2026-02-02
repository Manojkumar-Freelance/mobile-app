package com.example.appaddictioncontroller.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.example.appaddictioncontroller.data.AppDatabase
import com.example.appaddictioncontroller.data.AppRepository
import com.example.appaddictioncontroller.data.AppSelection
import com.example.appaddictioncontroller.ui.BlockActivity
import com.example.appaddictioncontroller.utils.NotificationHelper
import com.example.appaddictioncontroller.utils.UsageStatsHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.Calendar

class MonitoringService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var repository: AppRepository
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var usageStatsHelper: UsageStatsHelper
    private var wakeLock: PowerManager.WakeLock? = null
    
    private var monitoringJob: Job? = null
    private var lastCheckedApp: String? = null
    
    // Cache enabled apps to reduce database queries
    private var cachedEnabledApps: List<AppSelection> = emptyList()
    private var lastCacheUpdate: Long = 0L
    private var errorCount = 0
    
    companion object {
        private const val TAG = "MonitoringService"
        private const val CHECK_INTERVAL = 10000L // Check every 10 seconds (optimized from 5s)
        private const val CACHE_VALIDITY_MS = 30000L // Cache enabled apps for 30 seconds
        private const val THREE_HOURS_MS = 3 * 60 * 60 * 1000L // 3 hours in milliseconds
        private const val ONE_HOUR_MS = 60 * 60 * 1000L // 1 hour in milliseconds
        private const val WARNING_INTERVAL_MS = 60 * 60 * 1000L // 1 hour between warnings
        private const val TOTAL_WARNINGS = 3
        private const val MAX_ERROR_COUNT = 5
    }
    
    override fun onCreate() {
        super.onCreate()
        
        val database = AppDatabase.getDatabase(applicationContext)
        repository = AppRepository(database.appSelectionDao())
        notificationHelper = NotificationHelper(this)
        usageStatsHelper = UsageStatsHelper(this)
        
        // Acquire WakeLock for reliable background operation
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AppAddictionController::MonitoringWakeLock"
        ).apply {
            acquire()
        }
        
        // Start as foreground service
        startForeground(
            NotificationHelper.NOTIFICATION_ID_SERVICE,
            notificationHelper.createServiceNotification()
        )
        
        Log.d(TAG, "MonitoringService created with WakeLock")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "MonitoringService started")
        
        // Start monitoring
        startMonitoring()
        
        return START_STICKY
    }
    
    private fun startMonitoring() {
        monitoringJob?.cancel()
        
        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    checkAppUsage()
                    resetDailyUsageIfNeeded()
                    errorCount = 0 // Reset error count on success
                } catch (e: Exception) {
                    errorCount++
                    Log.e(TAG, "Error in monitoring loop (count: $errorCount)", e)
                    
                    // Exponential backoff on repeated errors
                    if (errorCount >= MAX_ERROR_COUNT) {
                        Log.e(TAG, "Too many errors, stopping service")
                        stopSelf()
                        return@launch
                    }
                }
                
                // Exponential backoff delay on errors
                val delayTime = if (errorCount > 0) {
                    CHECK_INTERVAL * (1 shl errorCount) // 2^errorCount multiplier
                } else {
                    CHECK_INTERVAL
                }
                
                delay(delayTime)
            }
        }
    }
    
    private suspend fun getEnabledApps(): List<AppSelection> {
        val currentTime = System.currentTimeMillis()
        
        // Return cached apps if cache is still valid
        if (cachedEnabledApps.isNotEmpty() && (currentTime - lastCacheUpdate) < CACHE_VALIDITY_MS) {
            return cachedEnabledApps
        }
        
        // Refresh cache
        cachedEnabledApps = repository.enabledApps.first()
        lastCacheUpdate = currentTime
        return cachedEnabledApps
    }
    
    private suspend fun checkAppUsage() {
        val currentApp = usageStatsHelper.getCurrentForegroundApp()
        val enabledApps = getEnabledApps() // Use cached apps
        
        // Reset continuous usage for apps that are no longer in foreground
        if (currentApp != lastCheckedApp && lastCheckedApp != null) {
            val previousApp = enabledApps.find { it.packageName == lastCheckedApp }
            if (previousApp != null && previousApp.continuousUsageStart != 0L) {
                // User switched away from monitored app - reset continuous usage
                repository.updateContinuousUsage(previousApp.packageName, 0L, 0)
                Log.d(TAG, "Reset continuous usage for ${previousApp.appName}")
            }
        }
        
        lastCheckedApp = currentApp
        
        if (currentApp == null) return
        
        val monitoredApp = enabledApps.find { it.packageName == currentApp } ?: return

        
        // Check if app is currently blocked
        if (monitoredApp.blockUntil > System.currentTimeMillis()) {
            showBlockScreen(monitoredApp)
            return
        }
        
        // Update usage from UsageStats
        val todayUsage = usageStatsHelper.getTodayUsageForApp(currentApp)
        repository.updateUsage(currentApp, todayUsage)
        
        // Check daily limit
        val timeLimitMs = monitoredApp.timeLimitMinutes * 60 * 1000L
        if (todayUsage >= timeLimitMs) {
            blockApp(monitoredApp, "Daily time limit reached")
            return
        }
        
        // Check continuous usage
        checkContinuousUsage(monitoredApp)
    }
    
    private suspend fun checkContinuousUsage(app: AppSelection) {
        val currentTime = System.currentTimeMillis()
        
        // If continuous usage tracking hasn't started, start it
        if (app.continuousUsageStart == 0L) {
            repository.updateContinuousUsage(app.packageName, currentTime, 0)
            return
        }
        
        val continuousUsageDuration = currentTime - app.continuousUsageStart
        
        // Calculate how many warnings should have been sent based on usage duration
        // Warning 1: at 1 hour, Warning 2: at 2 hours, Warning 3: at 3 hours
        val warningsDue = when {
            continuousUsageDuration >= THREE_HOURS_MS -> 3
            continuousUsageDuration >= 2 * WARNING_INTERVAL_MS -> 2
            continuousUsageDuration >= WARNING_INTERVAL_MS -> 1
            else -> 0
        }
        
        // Send warning if we haven't sent this warning yet
        if (warningsDue > app.warningsSent && app.warningsSent < TOTAL_WARNINGS) {
            val warningNumber = app.warningsSent + 1
            notificationHelper.sendWarningNotification(app.appName, warningNumber, TOTAL_WARNINGS)
            repository.updateContinuousUsage(app.packageName, app.continuousUsageStart, warningNumber)
            Log.d(TAG, "Sent warning $warningNumber for ${app.appName}")
        }
        
        // If all 3 warnings sent and user still using, block the app
        if (app.warningsSent >= TOTAL_WARNINGS && continuousUsageDuration >= THREE_HOURS_MS) {
            blockApp(app, "Ignored all warnings after continuous usage")
        }
    }
    
    private suspend fun blockApp(app: AppSelection, reason: String) {
        Log.d(TAG, "Blocking ${app.appName}: $reason")
        
        val blockUntil = System.currentTimeMillis() + ONE_HOUR_MS
        repository.updateBlockStatus(app.packageName, blockUntil)
        
        // Reset continuous usage tracking
        repository.updateContinuousUsage(app.packageName, 0L, 0)
        
        // Send notification
        notificationHelper.sendLimitReachedNotification(app.appName)
        
        // Show block screen
        showBlockScreen(app)
    }
    
    private fun showBlockScreen(app: AppSelection) {
        val intent = Intent(this, BlockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("APP_NAME", app.appName)
            putExtra("BLOCK_UNTIL", app.blockUntil)
        }
        startActivity(intent)
    }
    
    private suspend fun resetDailyUsageIfNeeded() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis
        
        // We can check if we moved to a new day by comparing startOfToday with last cache update or query DB
        // But the DAO method `resetDailyUsage` handles the "WHERE lastResetDate < :resetDate" check.
        // We need to fetch the total usage BEFORE resetting if we are indeed crossing a day boundary.
        
        // Let's modify the repo to handle this "Save History then Reset" logic atomically or sequentially.
        try {
            repository.saveHistoryAndReset(startOfToday)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving history", e)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        monitoringJob?.cancel()
        serviceScope.cancel()
        
        // Release WakeLock
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
        
        Log.d(TAG, "MonitoringService destroyed, WakeLock released")
    }
}
