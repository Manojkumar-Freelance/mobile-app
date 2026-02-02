package com.example.appaddictioncontroller.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appaddictioncontroller.R
import com.example.appaddictioncontroller.data.AppDatabase
import com.example.appaddictioncontroller.data.AppRepository
import com.example.appaddictioncontroller.databinding.FragmentHomeBinding
import com.example.appaddictioncontroller.ui.adapters.HomeAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: AppRepository
    private lateinit var adapter: HomeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val database = AppDatabase.getDatabase(requireContext())
        repository = AppRepository(database.appSelectionDao())
        
        adapter = HomeAdapter(
            packageManager = requireContext().packageManager,
            onItemClick = { app -> launchApp(app.packageName) }
        )
        binding.dashboardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.dashboardRecyclerView.adapter = adapter
        
        binding.manageAppsFab.setOnClickListener {
            findNavController().navigate(R.id.nav_apps)
        }
        
        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            repository.enabledApps.collect { apps ->
                adapter.submitList(apps)
                val totalMillis = apps.sumOf { it.totalUsageToday }
                updateTotalTime(totalMillis)
                updateMonitoredCount(apps.size)
                updateDailyGoal(totalMillis)
            }
        }
    }
    
    private fun updateTotalTime(totalMillis: Long) {
        val hours = totalMillis / (1000 * 60 * 60)
        val minutes = (totalMillis / (1000 * 60)) % 60
        binding.totalTimeText.text = "${hours}h ${minutes}m"
    }
    
    private fun updateMonitoredCount(count: Int) {
        binding.monitoredAppsCount.text = count.toString()
    }
    
    private fun updateDailyGoal(totalMillis: Long) {
        // Default daily goal: 4 hours (14400000 ms)
        val dailyGoalMs = 4 * 60 * 60 * 1000L
        val percentage = ((totalMillis.toDouble() / dailyGoalMs) * 100).toInt().coerceAtMost(100)
        binding.goalPercentage.text = "$percentage%"
    }
    
    private fun launchApp(packageName: String) {
        try {
            val intent = requireContext().packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                startActivity(intent)
            }
        } catch (e: Exception) {
            // App cannot be launched
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
