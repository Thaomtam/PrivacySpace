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
 * Implementation for Android 14+ (API 34+)
 * Handles changes in Android 14's package manager and permission system
 */
@TargetApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
object FrameworkHookerApi34Impl : Hooker {
    private lateinit var classLoader: ClassLoader
    private var unhook: XC_MethodHook.Unhook? = null
    private val lastFilteredApp = AtomicReference<String?>(null)
    
    override fun start(classLoader: ClassLoader) {
        this.classLoader = classLoader
        
        try {
            // Add relevant Package Manager classes to Hidden API exemption list
            // Android 14 may have additional restrictions on hidden API access
            HiddenApiBypass.addHiddenApiExemptions(
                "Landroid/content/pm/", 
                "Lcom/android/server/pm/",
                "Lcom/android/server/",
                "Lcom/android/internal/pm/"
            )

            // In Android 14, some class structures might be reorganized
            // Try to find the correct AppsFilter class and methods
            val appsFilterClass = try {
                // Primary target class
                classLoader.tryLoadClass("com.android.server.pm.AppsFilter")
            } catch (e: Exception) {
                // Fallback options if class structure changes in Android 14
                try {
                    classLoader.tryLoadClass("com.android.server.pm.permission.AppsFilter")
                } catch (e2: Exception) {
                    classLoader.tryLoadClass("com.android.server.pm.pkg.AppsFilter") 
                }
            }

            val settingBaseClass = classLoader.tryLoadClass("com.android.server.pm.SettingBase")
            val packageSettingClass = classLoader.tryLoadClass("com.android.server.pm.PackageSetting")
            
            // Logging information about found classes for debugging
            XLog.d("AppsFilter class found: ${appsFilterClass?.name}")
            XLog.d("SettingBase class found: ${settingBaseClass?.name}")
            XLog.d("PackageSetting class found: ${packageSettingClass?.name}")

            // Create callback for the shouldFilterApplication method
            val callback = object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        // Get basic parameters
                        val callingUid = param.args[0] as Int
                        if (callingUid == 1000) return // SYSTEM UID, don't filter
                        
                        val callingSettingBase = param.args[1]
                        val targetPackageSetting = param.args[2]
                        val userId = param.args[3] as Int
                        
                        // Use HiddenApiBypass to safely access package names even with Android 14's restrictions
                        // Try multiple field/method names to account for potential changes
                        val callingPackageName = getPackageName(settingBaseClass, callingSettingBase)
                        val targetPackageName = getPackageName(packageSettingClass, targetPackageSetting)
                        
                        if (callingPackageName == null || targetPackageName == null) {
                            XLog.d("Could not determine package names, skipping filter check")
                            return
                        }
                        
                        // Use the existing HookChecker to maintain consistent behavior
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
                        XLog.e(t, "Error in FrameworkHookerApi34Impl hook")
                    }
                }
            }
            
            // Find and hook the shouldFilterApplication method
            // In Android 14, the method signature should remain the same but double-check with logs
            XLog.d("Attempting to find shouldFilterApplication method")
            val hookMethod = XposedHelpers.findMethodExact(
                appsFilterClass,
                "shouldFilterApplication",
                Int::class.javaPrimitiveType,
                settingBaseClass,
                packageSettingClass,
                Int::class.javaPrimitiveType
            )
            
            unhook = XposedBridge.hookMethod(hookMethod, callback)
            XLog.i("FrameworkHookerApi34Impl started successfully")
        } catch (e: Throwable) {
            XLog.e(e, "FrameworkHookerApi34Impl start failed: ${e.message}")
        }
    }
    
    override fun stop() {
        unhook?.unhook()
        unhook = null
        XLog.d("FrameworkHookerApi34Impl stopped")
    }
    
    /**
     * Helper method to get package name from an object using various possible field/method names
     * This handles potential structure changes in Android 14
     */
    private fun getPackageName(clazz: Class<*>, obj: Any?): String? {
        if (obj == null) return null
        
        return try {
            // Try method-based approach first
            val result = HiddenApiBypass.invoke(clazz, obj, "getPackageName") as? String
            if (!result.isNullOrEmpty()) {
                return result
            }
            
            // Then try various fields that might contain the package name
            val possibleFields = listOf("packageName", "name", "pkg", "pkgName")
            for (fieldName in possibleFields) {
                try {
                    val field = HiddenApiBypass.getInstanceFields(clazz)
                        .find { it.name == fieldName }
                    
                    if (field != null) {
                        val value = field.get(obj) as? String
                        if (!value.isNullOrEmpty()) {
                            return value
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next field
                }
            }
            
            // If all direct attempts fail, try to find any field that contains a package-like string
            val fields = HiddenApiBypass.getInstanceFields(clazz)
            for (field in fields) {
                try {
                    val value = field.get(obj) as? String
                    if (value != null && value.contains(".") && !value.contains("/")) {
                        return value
                    }
                } catch (e: Exception) {
                    // Continue to next field
                }
            }
            
            null
        } catch (e: Throwable) {
            XLog.e(e, "Failed to get package name: ${e.message}")
            null
        }
    }
}