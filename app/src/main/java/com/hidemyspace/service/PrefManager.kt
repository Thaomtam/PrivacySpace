package com.hidemyspace.service

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

/**
 * Quản lý các tùy chọn của người dùng
 */
object PrefManager {
    private lateinit var prefs: SharedPreferences
    
    /**
     * Khởi tạo PrefManager
     */
    fun init(context: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }
    
    /**
     * Chế độ giao diện tối
     */
    val darkTheme: Int
        get() = prefs.getString("dark_theme", "system")?.let {
            when (it) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        } ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    
    /**
     * Bật/tắt ghi log chi tiết
     */
    var detailLog: Boolean
        get() = prefs.getBoolean("detail_log", false)
        set(value) = prefs.edit().putBoolean("detail_log", value).apply()
    
    /**
     * Kích thước tối đa của tệp log (KB)
     */
    var maxLogSize: Int
        get() = prefs.getInt("max_log_size", 1024)
        set(value) = prefs.edit().putInt("max_log_size", value).apply()
    
    /**
     * Tự động khởi động lại sau khi thay đổi cấu hình
     */
    var autoReboot: Boolean
        get() = prefs.getBoolean("auto_reboot", false)
        set(value) = prefs.edit().putBoolean("auto_reboot", value).apply()
    
    /**
     * Hiển thị thông báo khi ứng dụng bị ẩn
     */
    var showNotification: Boolean
        get() = prefs.getBoolean("show_notification", true)
        set(value) = prefs.edit().putBoolean("show_notification", value).apply()
    
    /**
     * Lưu cấu hình mặc định
     */
    var defaultConfig: String
        get() = prefs.getString("default_config", "") ?: ""
        set(value) = prefs.edit().putString("default_config", value).apply()
} 