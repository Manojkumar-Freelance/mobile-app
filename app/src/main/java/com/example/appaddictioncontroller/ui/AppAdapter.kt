package com.example.appaddictioncontroller.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appaddictioncontroller.R
import com.example.appaddictioncontroller.utils.AppInfo
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial

class AppAdapter(
    private val onAppToggled: (AppInfo, Boolean, Int) -> Unit
) : ListAdapter<AppInfo, AppAdapter.AppViewHolder>(AppDiffCallback()) {

    private val expandedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        private val appName: TextView = itemView.findViewById(R.id.appName)
        private val usageInfo: TextView = itemView.findViewById(R.id.usageInfo)
        private val enableSwitch: SwitchMaterial = itemView.findViewById(R.id.enableSwitch)
        private val expandedSection: View = itemView.findViewById(R.id.expandedSection)
        private val timeLimitSlider: Slider = itemView.findViewById(R.id.timeLimitSlider)
        private val timeLimitText: TextView = itemView.findViewById(R.id.timeLimitText)

        fun bind(appInfo: AppInfo, position: Int) {
            appIcon.setImageDrawable(appInfo.icon)
            appName.text = appInfo.appName
            usageInfo.text = appInfo.packageName

            // Show/hide expanded section
            val isExpanded = expandedPositions.contains(position)
            expandedSection.visibility = if (isExpanded) View.VISIBLE else View.GONE

            // Handle switch toggle
            enableSwitch.setOnCheckedChangeListener(null)
            enableSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    expandedPositions.add(position)
                    expandedSection.visibility = View.VISIBLE
                } else {
                    expandedPositions.remove(position)
                    expandedSection.visibility = View.GONE
                }
                
                val timeLimit = timeLimitSlider.value.toInt()
                onAppToggled(appInfo, isChecked, timeLimit)
            }

            // Handle slider changes
            timeLimitSlider.addOnChangeListener { _, value, _ ->
                timeLimitText.text = "${value.toInt()} min"
                if (enableSwitch.isChecked) {
                    onAppToggled(appInfo, true, value.toInt())
                }
            }

            // Initialize slider text
            timeLimitText.text = "${timeLimitSlider.value.toInt()} min"
        }
    }

    class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
}
