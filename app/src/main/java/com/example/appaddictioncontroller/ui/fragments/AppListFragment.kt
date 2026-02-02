package com.example.appaddictioncontroller.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appaddictioncontroller.R
import com.example.appaddictioncontroller.data.AppDatabase
import com.example.appaddictioncontroller.data.AppRepository
import com.example.appaddictioncontroller.data.AppSelection
import com.example.appaddictioncontroller.databinding.FragmentAppListBinding
import com.example.appaddictioncontroller.ui.AppAdapter
import com.example.appaddictioncontroller.utils.AppInfo
import com.example.appaddictioncontroller.utils.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AppListFragment : Fragment() {
    
    private var _binding: FragmentAppListBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: AppRepository
    private lateinit var usageStatsHelper: UsageStatsHelper
    private lateinit var adapter: AppAdapter
    
    private var allApps: List<AppInfo> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAppListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val database = AppDatabase.getDatabase(requireContext())
        repository = AppRepository(database.appSelectionDao())
        usageStatsHelper = UsageStatsHelper(requireContext())
        
        setupRecyclerView()
        setupSearch()
        loadApps()
    }
    
    private fun setupRecyclerView() {
        adapter = AppAdapter(
            onAppToggled = { appInfo, isEnabled ->
                // Fix: Run DB operations on IO dispatcher to prevent crashes
                lifecycleScope.launch(Dispatchers.IO) {
                    if (isEnabled) {
                         // Check if already exists to avoid conflict
                        if (repository.getAppByPackage(appInfo.packageName) == null) {
                            val selection = AppSelection(
                                packageName = appInfo.packageName,
                                appName = appInfo.appName,
                                timeLimitMinutes = 60, // Default 1 hour
                                isEnabled = true
                            )
                            repository.insert(selection)
                        } else {
                            // Update existing
                            val existing = repository.getAppByPackage(appInfo.packageName)!!
                            repository.update(existing.copy(isEnabled = true))
                        }
                    } else {
                        val existing = repository.getAppByPackage(appInfo.packageName)
                        if (existing != null) {
                            repository.delete(existing)
                        }
                    }
                }
            },
            onItemClick = { appInfo ->
                showTimeLimitDialog(appInfo)
            }
        )
        binding.appsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.appsRecyclerView.adapter = adapter
    }

    private fun showTimeLimitDialog(appInfo: AppInfo) {
        if (!appInfo.isEnabled) {
            // Optional: Auto-enable if clicked? Or just show toast.
            // For now, let's auto-enable then show dialog
             lifecycleScope.launch(Dispatchers.IO) {
                 if (repository.getAppByPackage(appInfo.packageName) == null) {
                     val selection = AppSelection(
                         packageName = appInfo.packageName,
                         appName = appInfo.appName,
                         timeLimitMinutes = 60,
                         isEnabled = true
                     )
                     repository.insert(selection)
                     // Update UI list to reflect change
                     withContext(Dispatchers.Main) {
                        appInfo.isEnabled = true
                        adapter.notifyDataSetChanged() // Quick UI update
                        showDialogForApp(appInfo)
                     }
                 } else {
                      withContext(Dispatchers.Main) {
                        showDialogForApp(appInfo)
                     }
                 }
             }
        } else {
            showDialogForApp(appInfo)
        }
    }

    private fun showDialogForApp(appInfo: AppInfo) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_time_limit, null)
        val slider = dialogView.findViewById<com.google.android.material.slider.Slider>(R.id.time_limit_slider)
        val valueText = dialogView.findViewById<android.widget.TextView>(R.id.limit_value_text)
        
        // Get current limit if possible (async) or default
        lifecycleScope.launch {
            val currentLimit = withContext(Dispatchers.IO) {
                repository.getAppByPackage(appInfo.packageName)?.timeLimitMinutes ?: 60
            }
            
            slider.value = currentLimit.toFloat()
            val hours = currentLimit / 60
            val mins = currentLimit % 60
            valueText.text = String.format("%dh %02dm", hours, mins)
        }

        slider.addOnChangeListener { _, value, _ ->
            val minutes = value.toInt()
            val hours = minutes / 60
            val mins = minutes % 60
            valueText.text = String.format("%dh %02dm", hours, mins)
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Set Limit for ${appInfo.appName}")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newLimit = slider.value.toInt()
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val existing = repository.getAppByPackage(appInfo.packageName)
                        if (existing != null) {
                            repository.update(existing.copy(timeLimitMinutes = newLimit))
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Error saving limit: ${e.message}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterApps(newText)
                return true
            }
        })
    }
    
    private fun filterApps(query: String?) {
        val filtered = if (query.isNullOrBlank()) {
            allApps
        } else {
            allApps.filter { it.appName.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT)) }
        }
        adapter.submitList(filtered)
    }

    private fun loadApps() {
        lifecycleScope.launch {
            // Get enabled apps from DB map to easily check state
            val enabledAppsList = repository.enabledApps.first()
            val enabledPackageNames = enabledAppsList.map { it.packageName }.toSet()
            
            // Get installed apps (expensive op, do in IO)
             val installedApps = withContext(Dispatchers.IO) {
                usageStatsHelper.getLaunchableApps().map { 
                    it.copy(isEnabled = enabledPackageNames.contains(it.packageName)) 
                }
            }
            
            allApps = installedApps.sortedBy { !it.isEnabled } // Enabled first
            adapter.submitList(allApps)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
