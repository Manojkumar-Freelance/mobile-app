package com.example.appaddictioncontroller.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSelectionDao {
    
    @Query("SELECT * FROM app_selections WHERE isEnabled = 1")
    fun getEnabledApps(): Flow<List<AppSelection>>
    
    @Query("SELECT * FROM app_selections")
    fun getAllApps(): Flow<List<AppSelection>>
    
    @Query("SELECT * FROM app_selections WHERE packageName = :packageName")
    suspend fun getAppByPackage(packageName: String): AppSelection?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appSelection: AppSelection)
    
    @Update
    suspend fun update(appSelection: AppSelection)
    
    @Delete
    suspend fun delete(appSelection: AppSelection)
    
    @Query("UPDATE app_selections SET totalUsageToday = :usage WHERE packageName = :packageName")
    suspend fun updateUsage(packageName: String, usage: Long)
    
    @Query("UPDATE app_selections SET continuousUsageStart = :startTime, warningsSent = :warnings WHERE packageName = :packageName")
    suspend fun updateContinuousUsage(packageName: String, startTime: Long, warnings: Int)
    
    @Query("UPDATE app_selections SET blockUntil = :blockUntil WHERE packageName = :packageName")
    suspend fun updateBlockStatus(packageName: String, blockUntil: Long)
    
    @Query("UPDATE app_selections SET totalUsageToday = 0, continuousUsageStart = 0, warningsSent = 0, lastResetDate = :resetDate WHERE lastResetDate < :resetDate")
    suspend fun resetDailyUsage(resetDate: Long)
}
