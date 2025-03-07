package com.hidemyspace.xposed

import android.content.pm.ApplicationInfo
import android.content.pm.IPackageManager
import android.os.Build
import com.github.kyuubiran.ezxhelper.utils.logD
import com.github.kyuubiran.ezxhelper.utils.logE
import com.github.kyuubiran.ezxhelper.utils.logI
import com.hidemyspace.xposed.hook.*
import java.io.File

class HMSService(val pms: IPackageManager) {

    companion object {
        private const val TAG = "HMS-Service"
        var instance: HMSService? = null

        fun register(pms: IPackageManager) {
            if (instance == null) {
                instance = HMSService(pms)
            }
        }
    }

    private lateinit var dataDir: String
    private lateinit var configFile: File
    private lateinit var logFile: File

    private val configLock = Any()
    private val systemApps = mutableSetOf<String>()
    private val frameworkHooks = mutableSetOf<IFrameworkHook>()

    var config = Config()
        private set

    var filterCount = 0
        private set

    init {
        setupDataDir()
        loadConfig()
        installHooks()
        logI(TAG, "HMS service initialized")
    }

    private fun setupDataDir() {
        dataDir = "/data/misc/hide_my_space_" + Utils.generateRandomString(8)
        File(dataDir).mkdirs()
        
        configFile = File("$dataDir/config.json")
        logFile = File("$dataDir/runtime.log")
        logFile.createNewFile()
        
        logI(TAG, "Data dir: $dataDir")
    }

    private fun loadConfig() {
        if (!configFile.exists()) {
            logI(TAG, "Config file not found, using default")
            return
        }
        
        try {
            val json = configFile.readText()
            config = Config.fromJson(json)
            logI(TAG, "Config loaded")
        } catch (e: Exception) {
            logE(TAG, "Failed to load config", e)
        }
    }

    private fun installHooks() {
        // Lấy danh sách ứng dụng hệ thống
        Utils.getInstalledApplications(pms, 0, 0).forEach {
            if (it.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                systemApps.add(it.packageName)
            }
        }

        // Cài đặt các hook dựa trên phiên bản Android
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                frameworkHooks.add(PmsHookApi33(this))
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                frameworkHooks.add(PmsHookApi30(this))
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                frameworkHooks.add(PmsHookApi28(this))
            }
            else -> {
                frameworkHooks.add(PmsHookLegacy(this))
            }
        }

        // Cài đặt các hook
        frameworkHooks.forEach { it.load() }
        logI(TAG, "Hooks installed")
    }

    fun shouldHide(caller: String?, query: String?): Boolean {
        if (caller == null || query == null) return false
        if (caller == query) return false
        
        // Kiểm tra xem caller có trong phạm vi cấu hình không
        val appConfig = config.scope[caller] ?: return false
        
        // Kiểm tra xem query có nên bị ẩn không
        if (appConfig.useWhitelist) {
            // Chế độ whitelist: ẩn tất cả trừ những ứng dụng trong danh sách
            if (appConfig.excludeSystemApps && query in systemApps) return false
            return query !in appConfig.appList
        } else {
            // Chế độ blacklist: chỉ ẩn những ứng dụng trong danh sách
            return query in appConfig.appList
        }
    }

    fun incrementFilterCount() {
        filterCount++
    }

    fun updateConfig(newConfig: Config) {
        synchronized(configLock) {
            config = newConfig
            configFile.writeText(newConfig.toJson())
            frameworkHooks.forEach { it.onConfigChanged() }
        }
        logI(TAG, "Config updated")
    }
} 