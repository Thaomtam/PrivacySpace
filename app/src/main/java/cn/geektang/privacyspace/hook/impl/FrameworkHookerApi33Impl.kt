package cn.geektang.privacyspace.hook.impl

import android.annotation.TargetApi
import android.os.Build
import cn.geektang.privacyspace.hook.Hooker
import cn.geektang.privacyspace.util.XLog
import cn.geektang.privacyspace.util.tryLoadClass
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.util.concurrent.atomic.AtomicReference

/**
 * Implementation for Android 13+ (API 33+)
 * Handles the latest changes to the package manager in Android 13
 */
@TargetApi(Build.VERSION_CODES.TIRAMISU)
object FrameworkHookerApi33Impl : Hooker {
    private lateinit var classLoader: ClassLoader
    private var unhook: XC_MethodHook.Unhook? = null
    private val lastFilteredApp = AtomicReference<String?>(null)
    
    override fun start(classLoader: ClassLoader) {
        this.classLoader = classLoader
        
        try {
            // Add all relevant package manager classes to exemption list
            HiddenApiBypass.addHiddenApiExemptions(
                "Landroid/content/pm/", 
                "Lcom/android/server/pm/",
                "Lcom/android/server/"
            )
            
            // Android 13 uses a slightly different structure for AppsFilter
            val appsFilterClass = classLoader.tryLoadClass("com.android.server.pm.AppsFilter")
            val settingBaseClass = classLoader.tryLoadClass("com.android.server.pm.SettingBase")
            val packageSettingClass = classLoader.tryLoadClass("com.android.server.pm.PackageSetting")
            
            // Create the hook callback
            val callback = object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        // Android 13 still uses the same basic parameters for shouldFilterApplication
                        val callingUid = param.args[0] as Int
                        if (callingUid == 1000) return // SYSTEM UID, don't filter
                        
                        // Use HiddenApiBypass to safely access hidden fields/methods
                        val callingSettingBase = param.args[1]
                        val targetPackageSetting = param.args[2]
                        val userId = param.args[3] as Int
                        
                        // In Android 13, we need to be more careful with hidden API access
                        val callingPackageName: String? = try {
                            HiddenApiBypass.invoke(
                                settingBaseClass, 
                                callingSettingBase, 
                                "getPackageName"
                            ) as? String
                        } catch (e: Exception) {
                            // Fallback to direct field access if method fails
                            HiddenApiBypass.getInstanceFields(settingBaseClass)
                                .find { it.name == "name" || it.name == "packageName" }
                                ?.get(callingSettingBase) as? String
                        }
                        
                        val targetPackageName: String? = try {
                            HiddenApiBypass.invoke(
                                packageSettingClass, 
                                targetPackageSetting, 
                                "getPackageName"
                            ) as? String
                        } catch (e: Exception) {
                            // Fallback to direct field access if method fails
                            HiddenApiBypass.getInstanceFields(packageSettingClass)
                                .find { it.name == "name" || it.name == "packageName" }
                                ?.get(targetPackageSetting) as? String
                        }
                        
                        if (callingPackageName == null || targetPackageName == null) {
                            return
                        }
                        
                        // Use the existing HookChecker to maintain Privacy Space's behavior
                        val shouldIntercept = HookChecker.shouldIntercept(
                            classLoader = classLoader,
                            targetPackageName = targetPackageName,
                            callingPackageName = callingPackageName,
                            userId = userId
                        )
                        
                        if (shouldIntercept) {
                            param.result = true
                            val last = lastFilteredApp.getAndSet(callingPackageName)
                            if (last != callingPackageName) {
                                XLog.d("Filtering app query from $callingPackageName")
                            }
                            XLog.v("Filtered query: $callingPackageName -> $targetPackageName (UID: $callingUid, User: $userId)")
                        }
                    } catch (t: Throwable) {
                        XLog.e(t, "Error in FrameworkHookerApi33Impl hook")
                    }
                }
            }
            
            // Hook the method
            unhook = XposedBridge.hookMethod(
                XposedHelpers.findMethodExact(
                    appsFilterClass,
                    "shouldFilterApplication",
                    Int::class.javaPrimitiveType,
                    settingBaseClass,
                    packageSettingClass,
                    Int::class.javaPrimitiveType
                ),
                callback
            )
            
            XLog.i("FrameworkHookerApi33Impl started successfully")
        } catch (e: Throwable) {
            XLog.e(e, "FrameworkHookerApi33Impl start failed.")
        }
    }
    
    override fun stop() {
        unhook?.unhook()
        unhook = null
        XLog.d("FrameworkHookerApi33Impl stopped")
    }
}