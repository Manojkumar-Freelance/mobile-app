package com.example.appaddictioncontroller.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.appaddictioncontroller.databinding.FragmentSettingsBinding
import com.example.appaddictioncontroller.service.MonitoringService

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "AppAddictionControllerPrefs"
        private const val KEY_SERVICE_RUNNING = "service_running"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Restore dark mode state
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        binding.darkModeSwitch.isChecked = isDarkMode
        
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
            // Save preference
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
        }
        
        // Service Control which was previously in MainActivity
        val isServiceRunning = prefs.getBoolean(KEY_SERVICE_RUNNING, false)
        binding.monitoringSwitch.isChecked = isServiceRunning
        
        binding.monitoringSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startMonitoringService()
            } else {
                stopMonitoringService()
            }
        }
        
        binding.permissionsSettingsCard.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }
    
    private fun startMonitoringService() {
        val intent = Intent(requireContext(), MonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
        prefs.edit().putBoolean(KEY_SERVICE_RUNNING, true).apply()
    }

    private fun stopMonitoringService() {
        val intent = Intent(requireContext(), MonitoringService::class.java)
        requireContext().stopService(intent)
        prefs.edit().putBoolean(KEY_SERVICE_RUNNING, false).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
