package com.hidemyspace.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hidemyspace.R
import com.hidemyspace.databinding.FragmentAppsBinding
import com.hidemyspace.model.AppConfig
import com.hidemyspace.service.ConfigManager
import com.hidemyspace.ui.adapter.AppListAdapter
import kotlinx.coroutines.launch

class AppsFragment : Fragment() {

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: AppListAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupAddButton()
        loadApps()
    }
    
    private fun setupRecyclerView() {
        adapter = AppListAdapter(
            onItemClick = { packageName ->
                showAppOptionsDialog(packageName)
            },
            onDeleteClick = { packageName ->
                ConfigManager.removeAppFromConfig(packageName)
                loadApps()
            }
        )
        
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }
    
    private fun setupAddButton() {
        binding.fabAddApp.setOnClickListener {
            showAddAppDialog()
        }
    }
    
    private fun loadApps() {
        val config = ConfigManager.getConfig()
        val apps = config.scope.keys.toList()
        
        if (apps.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            adapter.submitList(apps)
        }
    }
    
    private fun showAddAppDialog() {
        lifecycleScope.launch {
            val installedApps = ConfigManager.getInstalledApps()
            val configuredApps = ConfigManager.getConfig().scope.keys
            
            // Lọc ra các ứng dụng chưa được cấu hình
            val availableApps = installedApps.filter { 
                it.packageName !in configuredApps 
            }
            
            if (availableApps.isEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.add_app)
                    .setMessage("Tất cả ứng dụng đã được cấu hình")
                    .setPositiveButton(R.string.ok, null)
                    .show()
                return@launch
            }
            
            val appNames = availableApps.map { 
                it.loadLabel(requireContext().packageManager).toString() 
            }.toTypedArray()
            
            val packageNames = availableApps.map { it.packageName }.toTypedArray()
            
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_apps)
                .setItems(appNames) { _, which ->
                    val selectedPackage = packageNames[which]
                    showListTypeDialog(selectedPackage)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }
    
    private fun showListTypeDialog(packageName: String) {
        val options = arrayOf(
            getString(R.string.blacklist),
            getString(R.string.whitelist)
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Chọn kiểu danh sách")
            .setItems(options) { _, which ->
                val useWhitelist = which == 1
                ConfigManager.addAppToConfig(packageName, useWhitelist)
                loadApps()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showAppOptionsDialog(packageName: String) {
        val config = ConfigManager.getConfig()
        val appConfig = config.scope[packageName] ?: return
        
        val options = arrayOf(
            "Quản lý danh sách ứng dụng",
            "Đổi kiểu danh sách: ${if (appConfig.useWhitelist) "Whitelist" else "Blacklist"}",
            "Loại trừ ứng dụng hệ thống: ${if (appConfig.excludeSystemApps) "Bật" else "Tắt"}"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Tùy chọn")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showManageAppsDialog(packageName, appConfig)
                    1 -> toggleListType(packageName, appConfig)
                    2 -> toggleExcludeSystemApps(packageName, appConfig)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showManageAppsDialog(packageName: String, appConfig: AppConfig) {
        lifecycleScope.launch {
            val installedApps = ConfigManager.getInstalledApps()
            
            val appNames = installedApps.map { 
                it.loadLabel(requireContext().packageManager).toString() 
            }.toTypedArray()
            
            val targetPackageNames = installedApps.map { it.packageName }.toTypedArray()
            val checkedItems = targetPackageNames.map { 
                it in appConfig.appList 
            }.toBooleanArray()
            
            AlertDialog.Builder(requireContext())
                .setTitle("Chọn ứng dụng để ${if (appConfig.useWhitelist) "hiển thị" else "ẩn"}")
                .setMultiChoiceItems(appNames, checkedItems) { _, which, isChecked ->
                    val targetPackage = targetPackageNames[which]
                    if (isChecked) {
                        ConfigManager.addAppToHiddenList(packageName, targetPackage)
                    } else {
                        ConfigManager.removeAppFromHiddenList(packageName, targetPackage)
                    }
                }
                .setPositiveButton(R.string.ok, null)
                .show()
        }
    }
    
    private fun toggleListType(packageName: String, appConfig: AppConfig) {
        val scope = ConfigManager.getConfig().scope.toMutableMap()
        scope[packageName] = appConfig.copy(useWhitelist = !appConfig.useWhitelist)
        ConfigManager.updateConfig(ConfigManager.getConfig().copy(scope = scope))
        loadApps()
    }
    
    private fun toggleExcludeSystemApps(packageName: String, appConfig: AppConfig) {
        val scope = ConfigManager.getConfig().scope.toMutableMap()
        scope[packageName] = appConfig.copy(excludeSystemApps = !appConfig.excludeSystemApps)
        ConfigManager.updateConfig(ConfigManager.getConfig().copy(scope = scope))
        loadApps()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 