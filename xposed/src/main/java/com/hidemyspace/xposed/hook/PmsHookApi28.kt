package com.hidemyspace.xposed.hook

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.*
import com.hidemyspace.xposed.HMSService
import com.hidemyspace.xposed.Utils
import de.robv.android.xposed.XC_MethodHook

/**
 * Hook cho Android 9-10 (API 28-29)
 */
@RequiresApi(Build.VERSION_CODES.P)
class PmsHookApi28(private val service: HMSService) : IFrameworkHook {
    
    private val TAG = "HMS-PmsHookApi28"
    private val hooks = mutableListOf<XC_MethodHook.Unhook>()
    
    override fun load() {
        logI(TAG, "Loading hooks for Android 9-10")
        
        // Hook vào ComponentResolver.shouldFilterApplication
        try {
            val hook = findMethod("com.android.server.pm.ComponentResolver") {
                name == "shouldFilterApplication"
            }.hookAfter { param ->
                // Nếu đã quyết định lọc, không cần kiểm tra thêm
                if (param.result == true) return@hookAfter
                
                // Lấy thông tin caller và target
                val callingUid = param.args[0] as Int
                val targetPackage = param.args[2] as String
                
                val callingPackage = Utils.getPackageNameForUid(service.pms, callingUid)
                
                if (callingPackage != null && service.shouldHide(callingPackage, targetPackage)) {
                    param.result = true
                    service.incrementFilterCount()
                    logD(TAG, "Filtered $targetPackage from $callingPackage")
                }
            }
            
            hooks.add(hook)
        } catch (e: Throwable) {
            logE(TAG, "Failed to hook ComponentResolver.shouldFilterApplication", e)
        }
        
        // Hook vào các phương thức của PackageManagerService
        hookPackageManagerMethods()
    }
    
    private fun hookPackageManagerMethods() {
        try {
            val pmsClass = findClass("com.android.server.pm.PackageManagerService")
            
            // Hook filterAppAccess
            val hookFilterAppAccess = findMethod(pmsClass) {
                name == "filterAppAccess"
            }.hookAfter { param ->
                val packageName = param.args[0] as String
                val callingUid = getCallingUid()
                val callingPackage = Utils.getPackageNameForUid(service.pms, callingUid)
                
                if (callingPackage != null && service.shouldHide(callingPackage, packageName)) {
                    param.result = true
                    service.incrementFilterCount()
                    logD(TAG, "Filtered $packageName from $callingPackage in filterAppAccess")
                }
            }
            
            hooks.add(hookFilterAppAccess)
            
            // Hook getInstalledApplications
            val hookGetInstalledApps = findMethodExact(
                pmsClass,
                "getInstalledApplications",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            ).hookAfter { param ->
                val callingUid = getCallingUid()
                val callingPackage = Utils.getPackageNameForUid(service.pms, callingUid)
                
                if (callingPackage != null) {
                    val result = param.result as? List<*> ?: return@hookAfter
                    val filtered = result.filterNot { app ->
                        val packageName = getPackageNameFromAppInfo(app)
                        packageName != null && service.shouldHide(callingPackage, packageName)
                    }
                    
                    if (filtered.size < result.size) {
                        param.result = filtered
                        service.incrementFilterCount()
                        logD(TAG, "Filtered ${result.size - filtered.size} apps from getInstalledApplications")
                    }
                }
            }
            
            hooks.add(hookGetInstalledApps)
            
            // Hook getInstalledPackages
            val hookGetInstalledPackages = findMethodExact(
                pmsClass,
                "getInstalledPackages",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            ).hookAfter { param ->
                val callingUid = getCallingUid()
                val callingPackage = Utils.getPackageNameForUid(service.pms, callingUid)
                
                if (callingPackage != null) {
                    val result = param.result as? List<*> ?: return@hookAfter
                    val filtered = result.filterNot { pkg ->
                        val packageName = getPackageNameFromPackageInfo(pkg)
                        packageName != null && service.shouldHide(callingPackage, packageName)
                    }
                    
                    if (filtered.size < result.size) {
                        param.result = filtered
                        service.incrementFilterCount()
                        logD(TAG, "Filtered ${result.size - filtered.size} packages from getInstalledPackages")
                    }
                }
            }
            
            hooks.add(hookGetInstalledPackages)
        } catch (e: Throwable) {
            logE(TAG, "Failed to hook PackageManagerService methods", e)
        }
    }
    
    override fun unload() {
        hooks.forEach { it.unhook() }
        hooks.clear()
    }
    
    override fun onConfigChanged() {
        // Không cần làm gì đặc biệt khi cấu hình thay đổi
    }
    
    private fun getCallingUid(): Int {
        return try {
            val binderClass = findClass("android.os.Binder")
            val getCallingUidMethod = binderClass.getDeclaredMethod("getCallingUid")
            getCallingUidMethod.invoke(null) as Int
        } catch (e: Exception) {
            1000 // System UID
        }
    }
    
    private fun getPackageNameFromAppInfo(appInfo: Any?): String? {
        return try {
            val field = appInfo?.javaClass?.getDeclaredField("packageName")
            field?.isAccessible = true
            field?.get(appInfo) as? String
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getPackageNameFromPackageInfo(packageInfo: Any?): String? {
        return try {
            val field = packageInfo?.javaClass?.getDeclaredField("packageName")
            field?.isAccessible = true
            field?.get(packageInfo) as? String
        } catch (e: Exception) {
            null
        }
    }
} 