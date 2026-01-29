package com.example.appaddictioncontroller.ui

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appaddictioncontroller.data.AppDatabase
import com.example.appaddictioncontroller.data.AppRepository
import com.example.appaddictioncontroller.data.AppSelection
import com.example.appaddictioncontroller.databinding.ActivityMainBinding
import com.example.appaddictioncontroller.service.MonitoringService
import com.example.appaddictioncontroller.utils.UsageStatsHelper
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: AppRepository
    private lateinit var usageStatsHelper: UsageStatsHelper
    private lateinit var appAdapter: AppAdapter
    private var isServiceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize
        val database = AppDatabase.getDatabase(applicationContext)
        repository = AppRepository(database.appSelectionDao())
        usageStatsHelper = UsageStatsHelper(this)

        setupRecyclerView()
        setupPermissionButtons()
        setupServiceControl()
        checkPermissions()
        loadApps()
    }

    private fun setupRecyclerView() {
        appAdapter = AppAdapter { appInfo, isEnabled, timeLimit ->
            lifecycleScope.launch {
                if (isEnabled) {
                    val appSelection = AppSelection(
                        packageName = appInfo.packageName,
                        appName = appInfo.appName,
                        timeLimitMinutes = timeLimit,
                        isEnabled = true
                    )
                    repository.insert(appSelection)
                } else {
                    val existing = repository.getAppByPackage(appInfo.packageName)
                    existing?.let { repository.delete(it) }
                }
            }
        }

        binding.appsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = appAdapter
        }
    }

    private fun setupPermissionButtons() {
        binding.usageStatsButton.setOnClickListener {
            requestUsageStatsPermission()
        }

        binding.overlayButton.setOnClickListener {
            requestOverlayPermission()
        }
    }

    private fun setupServiceControl() {
        binding.toggleServiceButton.setOnClickListener {
            if (isServiceRunning) {
                stopMonitoringService()
            } else {
                if (hasAllPermissions()) {
                    startMonitoringService()
                } else {
                    Toast.makeText(this, "Please grant all permissions first", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkPermissions() {
        val hasUsageStats = usageStatsHelper.hasUsageStatsPermission()
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }

        binding.usageStatsButton.isEnabled = !hasUsageStats
        binding.overlayButton.isEnabled = !hasOverlay

        if (hasUsageStats && hasOverlay) {
            binding.permissionsCard.visibility = View.GONE
        }
    }

    private fun hasAllPermissions(): Boolean {
        val hasUsageStats = usageStatsHelper.hasUsageStatsPermission()
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
        return hasUsageStats && hasOverlay
    }

    private fun requestUsageStatsPermission() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun loadApps() {
        lifecycleScope.launch {
            val apps = usageStatsHelper.getLaunchableApps()
            appAdapter.submitList(apps)
        }
    }

    private fun startMonitoringService() {
        val intent = Intent(this, MonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        isServiceRunning = true
        updateServiceUI()
    }

    private fun stopMonitoringService() {
        val intent = Intent(this, MonitoringService::class.java)
        stopService(intent)
        isServiceRunning = false
        updateServiceUI()
    }

    private fun updateServiceUI() {
        if (isServiceRunning) {
            binding.serviceStatusText.text = "Service is running"
            binding.toggleServiceButton.text = "Stop Monitoring"
        } else {
            binding.serviceStatusText.text = "Service is stopped"
            binding.toggleServiceButton.text = "Start Monitoring"
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }
}
