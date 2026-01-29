package com.example.appaddictioncontroller.utils

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.util.Calendar

class UsageStatsHelper(private val context: Context) {
    
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager
    
    /**
     * Get the current foreground app package name
     */
    fun getCurrentForegroundApp(): String? {
        val currentTime = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            currentTime - 1000 * 10, // Last 10 seconds
            currentTime
        )
        
        return stats?.maxByOrNull { it.lastTimeUsed }?.packageName
    }
    
    /**
     * Get total usage time for a specific app today
     */
    fun getTodayUsageForApp(packageName: String): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = System.currentTimeMillis()
        
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            endOfDay
        )
        
        return stats?.find { it.packageName == packageName }?.totalTimeInForeground ?: 0L
    }
    
    /**
     * Get all launchable apps (apps that appear in the launcher)
     */
    fun getLaunchableApps(): List<AppInfo> {
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null)
        intent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        
        val apps = packageManager.queryIntentActivities(intent, 0)
        val appList = mutableListOf<AppInfo>()
        
        for (resolveInfo in apps) {
            val packageName = resolveInfo.activityInfo.packageName
            
            // Skip system apps and our own app
            if (packageName == context.packageName) continue
            
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val icon = packageManager.getApplicationIcon(packageName)
                
                appList.add(AppInfo(packageName, appName, icon))
            } catch (e: PackageManager.NameNotFoundException) {
                // Skip if app not found
            }
        }
        
        return appList.sortedBy { it.appName }
    }
    
    /**
     * Check if we have usage stats permission
     */
    fun hasUsageStatsPermission(): Boolean {
        val currentTime = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            currentTime - 1000 * 60,
            currentTime
        )
        return stats != null && stats.isNotEmpty()
    }
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable
)
