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
    
    // Cache for foreground app queries
    private var cachedForegroundApp: String? = null
    private var lastForegroundAppCheck: Long = 0L
    
    companion object {
        private const val FOREGROUND_APP_CACHE_MS = 3000L // Cache for 3 seconds
    }
    
    /**
     * Get the current foreground app package name (with caching)
     */
    fun getCurrentForegroundApp(): String? {
        val currentTime = System.currentTimeMillis()
        
        // Return cached result if still valid
        if (currentTime - lastForegroundAppCheck < FOREGROUND_APP_CACHE_MS) {
            return cachedForegroundApp
        }
        
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            currentTime - 1000 * 5, // Last 5 seconds (optimized from 10)
            currentTime
        )
        
        cachedForegroundApp = stats?.maxByOrNull { it.lastTimeUsed }?.packageName
        lastForegroundAppCheck = currentTime
        
        return cachedForegroundApp
    }
    
    /**
     * Get total usage time for a specific app today (with null safety)
     */
    fun getTodayUsageForApp(packageName: String): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = System.currentTimeMillis()
        
        return try {
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startOfDay,
                endOfDay
            )
            
            stats?.find { it.packageName == packageName }?.totalTimeInForeground ?: 0L
        } catch (e: Exception) {
            0L // Return 0 on any error
        }
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
    
    /**
     * Get usage data for the last 7 days (for Insights screen)
     * Returns a map of day index (0=today, 6=7 days ago) to total usage in hours
     */
    fun getLast7DaysUsage(): Map<Int, Float> {
        val calendar = Calendar.getInstance()
        val usageMap = mutableMapOf<Int, Float>()
        
        for (dayOffset in 0..6) {
            // Set to start of day
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            // Go back dayOffset days
            calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
            val startOfDay = calendar.timeInMillis
            
            // End of that day
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = calendar.timeInMillis
            
            // Reset calendar for next iteration
            calendar.add(Calendar.DAY_OF_YEAR, dayOffset)
            
            try {
                val stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startOfDay,
                    endOfDay
                )
                
                val totalMs = stats?.sumOf { it.totalTimeInForeground } ?: 0L
                val totalHours = totalMs / (1000f * 60f * 60f)
                usageMap[6 - dayOffset] = totalHours // Reverse order for chart (Mon-Sun)
            } catch (e: Exception) {
                usageMap[6 - dayOffset] = 0f
            }
        }
        
        return usageMap
    }
    
    /**
     * Get the most used app in the last 7 days
     * Returns pair of (appName, totalTimeMs)
     */
    fun getMostUsedAppLast7Days(): Pair<String, Long>? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val sevenDaysAgo = calendar.timeInMillis
        val now = System.currentTimeMillis()
        
        try {
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                sevenDaysAgo,
                now
            )
            
            val mostUsed = stats?.maxByOrNull { it.totalTimeInForeground }
            if (mostUsed != null && mostUsed.totalTimeInForeground > 0) {
                val appName = try {
                    packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(mostUsed.packageName, 0)
                    ).toString()
                } catch (e: Exception) {
                    mostUsed.packageName
                }
                return Pair(appName, mostUsed.totalTimeInForeground)
            }
        } catch (e: Exception) {
            // Return null on error
        }
        
        return null
    }
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable,
    var isEnabled: Boolean = false
)
