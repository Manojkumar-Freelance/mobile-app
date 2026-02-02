package com.example.appaddictioncontroller.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appSelectionDao: AppSelectionDao) {
    
    val enabledApps: Flow<List<AppSelection>> = appSelectionDao.getEnabledApps()
    val allApps: Flow<List<AppSelection>> = appSelectionDao.getAllApps()
    
    suspend fun getAppByPackage(packageName: String): AppSelection? {
        return appSelectionDao.getAppByPackage(packageName)
    }
    
    suspend fun insert(appSelection: AppSelection) {
        appSelectionDao.insert(appSelection)
    }
    
    suspend fun update(appSelection: AppSelection) {
        appSelectionDao.update(appSelection)
    }
    
    suspend fun delete(appSelection: AppSelection) {
        appSelectionDao.delete(appSelection)
    }
    
    suspend fun updateUsage(packageName: String, usage: Long) {
        appSelectionDao.updateUsage(packageName, usage)
    }
    
    suspend fun updateContinuousUsage(packageName: String, startTime: Long, warnings: Int) {
        appSelectionDao.updateContinuousUsage(packageName, startTime, warnings)
    }
    
    suspend fun updateBlockStatus(packageName: String, blockUntil: Long) {
        appSelectionDao.updateBlockStatus(packageName, blockUntil)
    }
    
    suspend fun resetDailyUsage(resetDate: Long) {
        val apps = appSelectionDao.getEnabledApps().first()
        val lastReset = apps.firstOrNull()?.lastResetDate ?: 0L
        if (lastReset < resetDate) {
           appSelectionDao.resetDailyUsage(resetDate)
        }
    }
    
    suspend fun saveHistoryAndReset(resetDate: Long) {
        // 3. Reset
        appSelectionDao.resetDailyUsage(resetDate)
    }
    
}
