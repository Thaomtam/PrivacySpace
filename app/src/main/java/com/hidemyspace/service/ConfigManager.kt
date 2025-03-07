package com.hidemyspace.service

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.hidemyspace.appContext
import com.hidemyspace.model.AppConfig
import com.hidemyspace.model.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * Quản lý cấu hình của module
 */
object ConfigManager {
    private lateinit var context: Context
    private var config = Config()
    
    /**
     * Khởi tạo ConfigManager
     */
    fun init(context: Context) {
        this.context = context
        
        // Tải cấu hình từ SharedPreferences
        val defaultConfigJson = PrefManager.defaultConfig
        if (defaultConfigJson.isNotEmpty()) {
            try {
                config = Config.fromJson(defaultConfigJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Lấy cấu hình hiện tại
     */
    fun getConfig(): Config {
        return config
    }
    
    /**
     * Cập nhật cấu hình
     */
    fun updateConfig(newConfig: Config) {
        config = newConfig
        PrefManager.defaultConfig = newConfig.toJson()
    }
    
    /**
     * Lấy danh sách ứng dụng đã cài đặt
     */
    suspend fun getInstalledApps(): List<ApplicationInfo> = withContext(Dispatchers.IO) {
        try {
            context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.packageName != context.packageName }
                .sortedBy { it.loadLabel(context.packageManager).toString().lowercase() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Lấy danh sách ứng dụng hệ thống
     */
    suspend fun getSystemApps(): List<ApplicationInfo> = withContext(Dispatchers.IO) {
        getInstalledApps().filter { it.flags and ApplicationInfo.FLAG_SYSTEM != 0 }
    }
    
    /**
     * Lấy danh sách ứng dụng người dùng
     */
    suspend fun getUserApps(): List<ApplicationInfo> = withContext(Dispatchers.IO) {
        getInstalledApps().filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
    }
    
    /**
     * Thêm ứng dụng vào cấu hình
     */
    fun addAppToConfig(packageName: String, useWhitelist: Boolean = false, excludeSystemApps: Boolean = true) {
        val scope = config.scope.toMutableMap()
        scope[packageName] = AppConfig(useWhitelist, excludeSystemApps, emptySet())
        updateConfig(config.copy(scope = scope))
    }
    
    /**
     * Xóa ứng dụng khỏi cấu hình
     */
    fun removeAppFromConfig(packageName: String) {
        val scope = config.scope.toMutableMap()
        scope.remove(packageName)
        updateConfig(config.copy(scope = scope))
    }
    
    /**
     * Thêm ứng dụng vào danh sách ẩn
     */
    fun addAppToHiddenList(callerPackage: String, targetPackage: String) {
        val scope = config.scope.toMutableMap()
        val appConfig = scope[callerPackage] ?: return
        
        val newAppList = appConfig.appList.toMutableSet()
        newAppList.add(targetPackage)
        
        scope[callerPackage] = appConfig.copy(appList = newAppList)
        updateConfig(config.copy(scope = scope))
    }
    
    /**
     * Xóa ứng dụng khỏi danh sách ẩn
     */
    fun removeAppFromHiddenList(callerPackage: String, targetPackage: String) {
        val scope = config.scope.toMutableMap()
        val appConfig = scope[callerPackage] ?: return
        
        val newAppList = appConfig.appList.toMutableSet()
        newAppList.remove(targetPackage)
        
        scope[callerPackage] = appConfig.copy(appList = newAppList)
        updateConfig(config.copy(scope = scope))
    }
} 