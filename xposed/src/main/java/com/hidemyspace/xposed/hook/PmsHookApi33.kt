package com.hidemyspace.xposed.hook

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.*
import com.hidemyspace.xposed.HMSService
import com.hidemyspace.xposed.Utils
import de.robv.android.xposed.XC_MethodHook

/**
 * Hook cho Android 13+ (API 33+)
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class PmsHookApi33(private val service: HMSService) : IFrameworkHook {
    
    private val TAG = "HMS-PmsHookApi33"
    private val hooks = mutableListOf<XC_MethodHook.Unhook>()
    
    override fun load() {
        logI(TAG, "Loading hooks for Android 13+")
        
        // Hook vào AppsFilterImpl.shouldFilterApplication
        try {
            val hook = findMethod("com.android.server.pm.AppsFilterImpl") {
                name == "shouldFilterApplication"
            }.hookAfter { param ->
                // Nếu đã quyết định lọc, không cần kiểm tra thêm
                if (param.result == true) return@hookAfter
                
                // Lấy thông tin caller và target
                val callingUid = param.args[1] as Int
                val targetPackage = getPackageNameFromParam(param.args[3])
                
                if (targetPackage != null) {
                    val callingPackage = Utils.getPackageNameForUid(service.pms, callingUid)
                    
                    if (callingPackage != null && service.shouldHide(callingPackage, targetPackage)) {
                        param.result = true
                        service.incrementFilterCount()
                        logD(TAG, "Filtered $targetPackage from $callingPackage")
                    }
                }
            }
            
            hooks.add(hook)
        } catch (e: Throwable) {
            logE(TAG, "Failed to hook AppsFilterImpl.shouldFilterApplication", e)
        }
    }
    
    override fun unload() {
        hooks.forEach { it.unhook() }
        hooks.clear()
    }
    
    override fun onConfigChanged() {
        // Không cần làm gì đặc biệt khi cấu hình thay đổi
    }
    
    private fun getPackageNameFromParam(param: Any?): String? {
        return try {
            val packageSettingClass = param.javaClass
            val packageNameField = packageSettingClass.getDeclaredField("name")
            packageNameField.isAccessible = true
            packageNameField.get(param) as String
        } catch (e: Exception) {
            null
        }
    }
}