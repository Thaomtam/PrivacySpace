package com.hidemyspace

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.hidemyspace.service.ConfigManager
import com.hidemyspace.service.PrefManager

lateinit var appContext: HideMySpaceApp

class HideMySpaceApp : Application() {

    var isHooked = false

    override fun onCreate() {
        super.onCreate()
        appContext = this
        
        // Khởi tạo các thành phần
        PrefManager.init(this)
        ConfigManager.init(this)
        
        // Thiết lập giao diện
        AppCompatDelegate.setDefaultNightMode(PrefManager.darkTheme)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Bỏ qua các hạn chế API ẩn
        try {
            org.lsposed.hiddenapibypass.HiddenApiBypass.addHiddenApiExemptions("")
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
} 