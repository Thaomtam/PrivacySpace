package cn.geektang.privacyspace.hook.impl

import android.content.pm.PackageManager
import cn.geektang.privacyspace.hook.Hooker
import cn.geektang.privacyspace.util.HookUtil
import cn.geektang.privacyspace.util.XLog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.Method

object FrameworkHookerApi26Impl : Hooker {
    private lateinit var classLoader: ClassLoader
    private val unhooks = mutableListOf<XC_MethodHook.Unhook>()
    
    /**
     * Target APIs: Android 8.0 Oreo (API 26)
     */
    override fun start(classLoader: ClassLoader) {
        this.classLoader = classLoader
        try {
            // Initialize Hidden API Bypass for accessing restricted APIs
            HiddenApiBypass.addHiddenApiExemptions("Lcom/android/server/pm/", "Landroid/content/pm/")
            
            // Load the Package Manager Service class
            val pmsClass = HookUtil.loadPms(classLoader)
                ?: throw PackageManager.NameNotFoundException("Failed to load Package Manager Service")

            // Find and hook the methods related to package filtering
            pmsClass.declaredMethods.forEach { method ->
                when (method.name) {
                    "filterAppAccessLPr" -> {
                        if (method.parameterCount == 3) {
                            val callback = createFilterAppAccessCallback()
                            unhooks.add(XposedBridge.hookMethod(method, callback))
                            XLog.d("Hooked filterAppAccessLPr method")
                        }
                    }
                    "getInstalledApplications" -> {
                        val callback = createGetInstalledAppsCallback() 
                        unhooks.add(XposedBridge.hookMethod(method, callback))
                        XLog.d("Hooked getInstalledApplications method")
                    }
                    "getInstalledPackages" -> {
                        val callback = createGetInstalledPackagesCallback()
                        unhooks.add(XposedBridge.hookMethod(method, callback))
                        XLog.d("Hooked getInstalledPackages method")
                    }
                    "queryIntentActivitiesInternal" -> {
                        val callback = createQueryIntentActivitiesCallback()
                        unhooks.add(XposedBridge.hookMethod(method, callback))
                        XLog.d("Hooked queryIntentActivitiesInternal method")
                    }
                }
            }
            
            XLog.i("FrameworkHookerApi26Impl started successfully")
        } catch (e: Throwable) {
            XLog.e(e, "Failed to hook package manager")
        }
    }
    
    override fun stop() {
        unhooks.forEach { it.unhook() }
        unhooks.clear()
        XLog.d("FrameworkHookerApi26Impl stopped")
    }
    
    private fun createFilterAppAccessCallback(): XC_MethodHook {
        return object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    if (param.result == true) return
                    
                    val packageSetting = param.args[0]
                    val targetPackageName = getPackageNameSafely(packageSetting) ?: return
                    val callingUid = param.args[1] as Int
                    val userId = param.args[2] as Int
                    val callingPackageName = getCallingPackageName(callingUid, param.thisObject) ?: return
                    
                    val shouldIntercept = HookChecker.shouldIntercept(
                        classLoader,
                        userId,
                        targetPackageName,
                        callingPackageName
                    )
                    
                    if (shouldIntercept) {
                        param.result = true
                    }
                } catch (e: Throwable) {
                    XLog.e(e, "Error in filterAppAccess hook")
                }
            }
        }
    }
    
    private fun createGetInstalledAppsCallback(): XC_MethodHook {
        return object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val result = param.result as? List<*> ?: return
                    val flags = param.args[0] as Int
                    val userId = param.args[1] as Int
                    val callingUid = getCallingUid(param)
                    val callingPackageName = getCallingPackageName(callingUid, param.thisObject) ?: return
                    
                    // Process result to filter out hidden apps
                    val resultMutable = result.toMutableList()
                    val iterator = resultMutable.iterator()
                    while (iterator.hasNext()) {
                        val appInfo = iterator.next() ?: continue
                        val targetPackageName = appInfo.javaClass.getDeclaredField("packageName").get(appInfo) as? String ?: continue
                        
                        val shouldIntercept = HookChecker.shouldIntercept(
                            classLoader,
                            userId,
                            targetPackageName,
                            callingPackageName
                        )
                        
                        if (shouldIntercept) {
                            iterator.remove()
                        }
                    }
                    
                    param.result = resultMutable
                } catch (e: Throwable) {
                    XLog.e(e, "Error in getInstalledApps hook")
                }
            }
        }
    }
    
    private fun createGetInstalledPackagesCallback(): XC_MethodHook {
        return object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val result = param.result as? List<*> ?: return
                    val flags = param.args[0] as Int
                    val userId = param.args[1] as Int
                    val callingUid = getCallingUid(param)
                    val callingPackageName = getCallingPackageName(callingUid, param.thisObject) ?: return
                    
                    // Process result to filter out hidden packages
                    val resultMutable = result.toMutableList()
                    val iterator = resultMutable.iterator()
                    while (iterator.hasNext()) {
                        val pkgInfo = iterator.next() ?: continue
                        val targetPackageName = pkgInfo.javaClass.getDeclaredField("packageName").get(pkgInfo) as? String ?: continue
                        
                        val shouldIntercept = HookChecker.shouldIntercept(
                            classLoader,
                            userId,
                            targetPackageName,
                            callingPackageName
                        )
                        
                        if (shouldIntercept) {
                            iterator.remove()
                        }
                    }
                    
                    param.result = resultMutable
                } catch (e: Throwable) {
                    XLog.e(e, "Error in getInstalledPackages hook")
                }
            }
        }
    }
    
    private fun createQueryIntentActivitiesCallback(): XC_MethodHook {
        return object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val result = param.result as? List<*> ?: return
                    val callingUid = getCallingUid(param)
                    val userId = param.args[5] as? Int ?: 0
                    val callingPackageName = getCallingPackageName(callingUid, param.thisObject) ?: return
                    
                    // Filter out ResolveInfo objects for hidden apps
                    val resultMutable = result.toMutableList()
                    val iterator = resultMutable.iterator()
                    while (iterator.hasNext()) {
                        val resolveInfo = iterator.next() ?: continue
                        val activityInfo = resolveInfo.javaClass.getDeclaredField("activityInfo").get(resolveInfo)
                        val targetPackageName = activityInfo?.javaClass?.getDeclaredField("packageName")?.get(activityInfo) as? String ?: continue
                        
                        val shouldIntercept = HookChecker.shouldIntercept(
                            classLoader,
                            userId,
                            targetPackageName,
                            callingPackageName
                        )
                        
                        if (shouldIntercept) {
                            iterator.remove()
                        }
                    }
                    
                    param.result = resultMutable
                } catch (e: Throwable) {
                    XLog.e(e, "Error in queryIntentActivities hook")
                }
            }
        }
    }
    
    private fun getCallingUid(param: XC_MethodHook.MethodHookParam): Int {
        // Try to find the calling UID in the method parameters
        for ((index, arg) in param.args.withIndex()) {
            if (arg is Int && index < param.args.size - 1) {
                // Often the UID parameter is marked with a name hinting at "caller" or "uid"
                val nextParam = param.method.parameterTypes.getOrNull(index + 1)
                val paramName = param.method.parameters.getOrNull(index)?.name?.lowercase() ?: ""
                if (paramName.contains("uid") || paramName.contains("caller")) {
                    return arg
                }
            }
        }
        
        // Default to a common parameter position if we can't identify it
        return param.args.getOrNull(2) as? Int ?: 1000 // Default to system UID
    }
    
    private fun getPackageNameSafely(packageSetting: Any?): String? {
        if (packageSetting == null) return null
        
        // Try using HiddenApiBypass first
        return try {
            packageSetting.javaClass.getDeclaredField("packageName").get(packageSetting) as? String
                ?: packageSetting.javaClass.getDeclaredField("name").get(packageSetting) as? String
        } catch (e: Throwable) {
            try {
                // Fallback to using HiddenApiBypass to find all fields
                HiddenApiBypass.getInstanceFields(packageSetting.javaClass)
                    .find { it.name == "packageName" || it.name == "name" }
                    ?.get(packageSetting) as? String
            } catch (e2: Throwable) {
                XLog.e(e2, "Failed to get package name safely")
                null
            }
        }
    }
    
    private fun getCallingPackageName(uid: Int, pms: Any): String? {
        try {
            // Try to find the method to get package name from UID
            val method: Method? = pms.javaClass.declaredMethods.find { 
                (it.name == "getNameForUid" || it.name == "getPackagesForUid") && 
                it.parameterTypes.size == 1 && 
                it.parameterTypes[0] == Int::class.javaPrimitiveType 
            }
            
            val result = method?.invoke(pms, uid)
            
            return when {
                result is Array<*> && result.isNotEmpty() -> result[0] as? String
                result is String -> result
                else -> null
            }
        } catch (e: Throwable) {
            XLog.e(e, "Failed to get calling package name")
            return null
        }
    }
}