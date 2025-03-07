package com.hidemyspace.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hidemyspace.appContext
import com.hidemyspace.databinding.ItemAppBinding
import com.hidemyspace.model.AppConfig
import com.hidemyspace.service.ConfigManager

class AppListAdapter(
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : ListAdapter<String, AppListAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val packageName = getItem(position)
        holder.bind(packageName)
    }

    inner class AppViewHolder(
        private val binding: ItemAppBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(packageName: String) {
            val packageManager = appContext.packageManager
            val appInfo = try {
                packageManager.getApplicationInfo(packageName, 0)
            } catch (e: Exception) {
                null
            }
            
            // Thiết lập thông tin ứng dụng
            binding.appIcon.setImageDrawable(appInfo?.loadIcon(packageManager))
            binding.appName.text = appInfo?.loadLabel(packageManager) ?: packageName
            binding.packageName.text = packageName
            
            // Thiết lập thông tin cấu hình
            val config = ConfigManager.getConfig()
            val appConfig = config.scope[packageName]
            
            if (appConfig != null) {
                val listType = if (appConfig.useWhitelist) "Whitelist" else "Blacklist"
                val appCount = appConfig.appList.size
                binding.appConfig.text = "$listType - $appCount ứng dụng"
            } else {
                binding.appConfig.text = "Chưa cấu hình"
            }
            
            // Thiết lập sự kiện
            binding.root.setOnClickListener {
                onItemClick(packageName)
            }
            
            binding.deleteButton.setOnClickListener {
                onDeleteClick(packageName)
            }
        }
    }

    class AppDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
} 