package com.example.appaddictioncontroller.ui.adapters

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appaddictioncontroller.data.AppSelection
import com.example.appaddictioncontroller.databinding.ItemHomeAppCardBinding

class HomeAdapter(
    private val packageManager: PackageManager,
    private val onItemClick: (AppSelection) -> Unit = {}
) : ListAdapter<AppSelection, HomeAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemHomeAppCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = getItem(position)
        holder.bind(app, packageManager)
    }

    class AppViewHolder(
        private val binding: ItemHomeAppCardBinding,
        private val onItemClick: (AppSelection) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(app: AppSelection, pm: PackageManager) {
            binding.appName.text = app.appName
            binding.usageText.text = "${formatTime(app.totalUsageToday)} / ${app.timeLimitMinutes}m"
            
            val progress = if (app.timeLimitMinutes > 0) {
                ((app.totalUsageToday / (app.timeLimitMinutes * 60 * 1000.0)) * 100).toInt()
            } else {
                0
            }
            binding.usageProgress.progress = progress.coerceIn(0, 100)

            try {
                val icon = pm.getApplicationIcon(app.packageName)
                binding.appIcon.setImageDrawable(icon)
            } catch (e: Exception) {
                // Ignore icon load error
            }
            
            // Add click listener to launch app
            binding.root.setOnClickListener {
                onItemClick(app)
            }
        }
    }
    
    companion object {
        private fun formatTime(millis: Long): String {
            val minutes = (millis / (1000 * 60)) % 60
            val hours = (millis / (1000 * 60 * 60))
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }
    }

    class AppDiffCallback : DiffUtil.ItemCallback<AppSelection>() {
        override fun areItemsTheSame(oldItem: AppSelection, newItem: AppSelection) = oldItem.packageName == newItem.packageName
        override fun areContentsTheSame(oldItem: AppSelection, newItem: AppSelection) = oldItem == newItem
    }
}
