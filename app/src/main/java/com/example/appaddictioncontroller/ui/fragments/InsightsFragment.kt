package com.example.appaddictioncontroller.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.appaddictioncontroller.databinding.FragmentInsightsBinding
import com.example.appaddictioncontroller.utils.UsageStatsHelper
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InsightsFragment : Fragment() {
    
    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!
    private lateinit var usageStatsHelper: UsageStatsHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        usageStatsHelper = UsageStatsHelper(requireContext())
        loadInsightsData()
    }
    
    private fun loadInsightsData() {
        lifecycleScope.launch {
            // Fetch data in background
            val insightsData = withContext(Dispatchers.IO) {
                val last7Days = usageStatsHelper.getLast7DaysUsage()
                val totalWeeklyMs = last7Days.values.sum() * 1000 * 60 * 60 // Convert hours to ms
                val avgMs = totalWeeklyMs / 7
                val mostUsedApp = usageStatsHelper.getMostUsedAppLast7Days()
                
                // Simple streak calculation: days with usage < 4 hours
                var streakDays = 0
                for (i in 0..6) {
                    if (last7Days[i] ?: 0f < 4f) {
                        streakDays++
                    } else {
                        break
                    }
                }
                
                InsightsData(last7Days, totalWeeklyMs.toLong(), avgMs.toLong(), mostUsedApp, streakDays)
            }
            
            // Update UI on main thread
            setupChart(insightsData.chartData)
            updateStatistics(insightsData.totalTime, insightsData.dailyAvg, insightsData.mostUsed, insightsData.streak)
        }
    }
    
    private fun setupChart(usageData: Map<Int, Float>) {
        val entries = usageData.map { (day, hours) ->
            BarEntry(day.toFloat(), hours)
        }
        
        val dataSet = BarDataSet(entries, "Hours Used")
        // Use vibrant colors
        dataSet.colors = listOf(
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#00BCD4"), // Teal
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#FFC107"), // Yellow
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#F44336"), // Red
            Color.parseColor("#9C27B0")  // Purple
        )
        
        // Handle Dark Mode Text Color
        val isDarkMode = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val textColor = if (isDarkMode) Color.WHITE else Color.DKGRAY
        
        dataSet.valueTextColor = textColor
        dataSet.valueTextSize = 12f
        
        val barData = BarData(dataSet)
        binding.barChart.data = barData
        binding.barChart.description.isEnabled = false
        binding.barChart.legend.isEnabled = false
        
        val xAxis = binding.barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = textColor
        xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
        
        binding.barChart.axisLeft.setDrawGridLines(false)
        binding.barChart.axisLeft.textColor = textColor
        binding.barChart.axisRight.isEnabled = false
        
        binding.barChart.animateY(1000)
        binding.barChart.invalidate()
    }
    
    private fun updateStatistics(totalMs: Long, avgMs: Long, mostUsed: Pair<String, Long>?, streak: Int) {
        // Total Time
        val totalHours = totalMs / (1000 * 60 * 60)
        binding.totalWeeklyTime.text = "${totalHours}h"
        
        // Daily Average
        val avgHours = avgMs / (1000 * 60 * 60)
        binding.dailyAverage.text = "${avgHours}h"
        
        // Most Used App
        if (mostUsed != null) {
            binding.mostUsedAppName.text = mostUsed.component1()
            val hours = mostUsed.component2() / (1000 * 60 * 60)
            val mins = (mostUsed.component2() / (1000 * 60)) % 60
            binding.mostUsedAppTime.text = "${hours}h ${mins}m"
        } else {
            binding.mostUsedAppName.text = "No data yet"
            binding.mostUsedAppTime.text = "0h 0m"
        }
        
        // Streak
        binding.streakCount.text = "$streak days"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Helper data class for insights data
private data class InsightsData(
    val chartData: Map<Int, Float>,
    val totalTime: Long,
    val dailyAvg: Long,
    val mostUsed: Pair<String, Long>?,
    val streak: Int
)
