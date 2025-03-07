package com.hidemyspace.xposed.hook

import com.github.kyuubiran.ezxhelper.utils.*
import com.hidemyspace.xposed.HMSService
import com.hidemyspace.xposed.Utils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

/**
 * Hook cho Android 8 và cũ hơn (API <= 27)
 */
class PmsHookLegacy(private val service: HMSService) : IFrameworkHook {
    
    private val TAG = "HMS-PmsHookLegacy"
    private val hooks = mutableListOf<XC_MethodHook.Unhook>()
    
    override fun load() {
        logI(TAG, "Loading hooks for Android 8 and below")
        
        // Hook trực tiếp vào các phương thức của PackageManagerService
        try {
            val pmsClass = findClass("com.android.server.pm.PackageManagerService")
            
            // Tìm và hook tất cả các phương thức liên quan đến truy vấn ứng dụng
            val methodsToHook = listOf(
                "getInstalledPackages",
                "getInstalledApplications",
                "getPackageInfo",
                "getApplicationInfo",
                "queryIntentActivities",
                "queryIntentServices",
                "queryIntentReceivers",
                "queryIntentContentProviders"
            )
            
            pmsClass.declaredMethods.forEach { method ->
                if (methodsToHook.contains(method.name)) {
                    val hook = XposedBridge.hookMethod(method, object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val callingUid = getCallingUid()
                            val callingPackage = Utils.getPackageNameForUid(service.pms, callingUid)
                            
                            if (callingPackage == null) return
                            
                            when (method.name) {
                                "getInstalledPackages", "getInstalledApplications" -> {
                                    val result = param.result as? List<*> ?: return
                                    val filtered = when (method.name) {
                                        "getInstalledPackages" -> result.filterNot { pkg ->
                                            val packageName = getPackageNameFromPackageInfo(pkg)
                                            packageName != null && service.shouldHide(callingPackage, packageName)
                                        }
                                        else -> result.filterNot { app ->
                                            val packageName = getPackageNameFromAppInfo(app)
                                            packageName != null && service.shouldHide(callingPackage, packageName)
                                        }
                                    }
                                    
                                    if (filtered.size < result.size) {
                                        param.result = filtered
                                        service.incrementFilterCount()
                                        logD(TAG, "Filtered ${result.size - filtered.size} items from ${method.name}")
                                    }
                                }
                                
                                "getPackageInfo", "getApplicationInfo" -> {
                                    val packageName = param.args[0] as? String ?: return
                                    
                                    if (service.shouldHide(callingPackage, packageName)) {
                                        param.result = null
                                        service.incrementFilterCount()
                                        logD(TAG, "Filtered $packageName from $callingPackage in ${method.name}")
                                    }
                                }
                                
                                "queryIntentActivities", "queryIntentServices", 
                                "queryIntentReceivers", "queryIntentContentProviders" -> {
                                    val result = param.result as? List<*> ?: return
                                    val filtered = result.filterNot { resolveInfo ->
                                        val packageName = getPackageNameFromResolveInfo(resolveInfo)
                                        packageName != null && service.shouldHide(callingPackage, packageName)
                                    }
                                    
                                    if (filtered.size < result.size) {
                                        param.result = filtered
                                        service.incrementFilterCount()
                                        logD(TAG, "Filtered ${result.size - filtered.size} items from ${method.name}")
                                    }
                                }
                            }
                        }
                    })
                    
                    hooks.add(hook)
                }
            }
            
            // Hook filterSharedLibPackageLPr
            val hookFilterSharedLib = findMethod(pmsClass) {
                name == "filterSharedLibPackageLPr"
            }.hookAfter { param ->
                val packageName = param.args[0] as String
                val callingUid = getCallingUid()
                val callingPackage = Utils.getPackageNameForUid(service.pms, callingUid)
                
                if (callingPackage != null && service.shouldHide(callingPackage, packageName)) {
                    param.result = true
                    service.incrementFilterCount()
                    logD(TAG, "Filtered $packageName from $callingPackage in filterSharedLibPackageLPr")
                }
            }
            
            hooks.add(hookFilterSharedLib)
            
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
    
    private fun getPackageNameFromResolveInfo(resolveInfo: Any?): String? {
        return try {
            val activityInfoField = resolveInfo?.javaClass?.getDeclaredField("activityInfo")
            activityInfoField?.isAccessible = true
            val activityInfo = activityInfoField?.get(resolveInfo)
            
            val packageNameField = activityInfo?.javaClass?.getDeclaredField("packageName")
            packageNameField?.isAccessible = true
            packageNameField?.get(activityInfo) as? String
        } catch (e: Exception) {
            try {
                val serviceInfoField = resolveInfo?.javaClass?.getDeclaredField("serviceInfo")
                serviceInfoField?.isAccessible = true
                val serviceInfo = serviceInfoField?.get(resolveInfo)
                
                val packageNameField = serviceInfo?.javaClass?.getDeclaredField("packageName")
                packageNameField?.isAccessible = true
                packageNameField?.get(serviceInfo) as? String
            } catch (e: Exception) {
                try {
                    val providerInfoField = resolveInfo?.javaClass?.getDeclaredField("providerInfo")
                    providerInfoField?.isAccessible = true
                    val providerInfo = providerInfoField?.get(resolveInfo)
                    
                    val packageNameField = providerInfo?.javaClass?.getDeclaredField("packageName")
                    packageNameField?.isAccessible = true
                    packageNameField?.get(providerInfo) as? String
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}