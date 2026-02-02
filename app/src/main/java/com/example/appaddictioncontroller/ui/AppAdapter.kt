package com.example.appaddictioncontroller.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appaddictioncontroller.databinding.ItemAppToggleBinding
import com.example.appaddictioncontroller.utils.AppInfo

class AppAdapter(
    private val onAppToggled: (AppInfo, Boolean) -> Unit,
    private val onItemClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppToggleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = getItem(position)
        holder.bind(app)
    }

    inner class AppViewHolder(private val binding: ItemAppToggleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(app: AppInfo) {
            binding.appName.text = app.appName
            binding.appIcon.setImageDrawable(app.icon)
            
            // Avoid triggering listener during binding
            binding.appSwitch.setOnCheckedChangeListener(null)
            binding.appSwitch.isChecked = app.isEnabled
            
            binding.appSwitch.setOnCheckedChangeListener { _, isChecked ->
                app.isEnabled = isChecked
                onAppToggled(app, isChecked)
            }
            
            binding.root.setOnClickListener {
                onItemClick(app)
            }
        }
    }

    class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            // Check essential fields for equality
            return oldItem.packageName == newItem.packageName && 
                   oldItem.isEnabled == newItem.isEnabled
        }
    }
}
