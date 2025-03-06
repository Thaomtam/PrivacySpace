package cn.geektang.privacyspace.hook.impl

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.UserHandle
import cn.geektang.privacyspace.hook.Hooker
import cn.geektang.privacyspace.util.ConfigHelper.getPackageName
import cn.geektang.privacyspace.util.HookUtil
import cn.geektang.privacyspace.util.XLog
import cn.geektang.privacyspace.util.tryLoadClass
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.Field
import java.lang.reflect.Method

object FrameworkHookerApi28Impl : Hooker {
    private lateinit var pmsClass: Class<*>
    private lateinit var settingsClass: Class<*>
    private lateinit var mSettingsField: Field
    private lateinit var getAppIdMethod: Method
    private lateinit var getSettingLPrMethod: Method
    private lateinit var classLoader: ClassLoader
    
    // Store unhook handles for proper cleanup
    private val unhooks = mutableListOf<XC_MethodHook.Unhook>()

    override fun start(classLoader: ClassLoader) {
        this.classLoader = classLoader
        try {
            // Use HiddenApiBypass to safely access hidden APIs
            HiddenApiBypass.addHiddenApiExemptions("Lcom/android/server/pm/", "Landroid/content/pm/")
            
            pmsClass = HookUtil.loadPms(classLoader) ?: throw PackageManager.NameNotFoundException()
            mSettingsField = pmsClass.getDeclaredField("mSettings")
            mSettingsField.isAccessible = true

            settingsClass = classLoader.tryLoadClass("com.android.server.pm.Settings")
            getAppIdMethod =
                UserHandle::class.java.getDeclaredMethod("getAppId", Int::class.javaPrimitiveType)
            getAppIdMethod.isAccessible = true

            for (method in settingsClass.declaredMethods) {
                if ((method.name == "getSettingLPr" || method.name == "getUserIdLPr")
                    && method.parameterCount == 1
                    && method.parameterTypes.first() == Int::class.javaPrimitiveType
                ) {
                    getSettingLPrMethod = method
                    method.isAccessible = true
                    break
                }
            }
        } catch (e: Throwable) {
            XLog.e(e, "PMS load failed.", e)
            return
        }
        
        // Create a reusable method hook callback
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                when (param.method.name) {
                    "filterAppAccessLPr" -> {
                        hookFilterAppAccess(param)
                    }
                    "applyPostResolutionFilter" -> {
                        hookApplyPostResolutionFilter(param)
                    }
                }
            }
        }
        
        // Hook methods and store unhook references
        pmsClass.declaredMethods.forEach { method ->
            when (method.name) {
                "filterAppAccessLPr" -> {
                    if (method.parameterCount == 5) {
                        unhooks.add(XposedBridge.hookMethod(method, callback))
                    }
                }
                "applyPostResolutionFilter" -> {
                    unhooks.add(XposedBridge.hookMethod(method, callback))
                }
            }
        }
        
        XLog.i("FrameworkHookerApi28Impl started successfully")
    }
    
    override fun stop() {
        // Properly unhook all methods
        unhooks.forEach { it.unhook() }
        unhooks.clear()
        XLog.d("FrameworkHookerApi28Impl stopped")
    }

    private fun hookApplyPostResolutionFilter(param: MethodHookParam) {
        try {
            val resultList = param.result as? MutableList<*> ?: return
            val callingUid = param.args[3] as? Int ?: return
            val userId = param.args[5] as? Int ?: return
            val callingPackageName = getPackageName(param.thisObject, callingUid) ?: return
            val waitRemoveList = mutableListOf<ResolveInfo>()
            
            for (resolveInfo in resultList) {
                val targetPackageName = (resolveInfo as? ResolveInfo)?.getPackageName() ?: continue
                val shouldIntercept = HookChecker.shouldIntercept(
                    classLoader,
                    userId,
                    targetPackageName,
                    callingPackageName
                )
                if (shouldIntercept) {
                    waitRemoveList.add(resolveInfo)
                }
            }

            for (resolveInfo in waitRemoveList) {
                resultList.remove(resolveInfo)
            }
            if (waitRemoveList.isNotEmpty()) {
                param.result = resultList
            }
        } catch (e: Throwable) {
            XLog.e(e, "Error in hookApplyPostResolutionFilter")
        }
    }

    private fun hookFilterAppAccess(param: MethodHookParam) {
        try {
            if (param.result == true) {
                return
            }
            
            val packageSetting = param.args.first()
            // Use HiddenApiBypass to safely access package name
            val targetPackageName = try {
                packageSetting?.packageName
            } catch (e: Throwable) {
                // Fallback to reflection if direct access fails
                HiddenApiBypass.getInstanceFields(packageSetting!!.javaClass)
                    .find { it.name == "name" || it.name == "packageName" }
                    ?.get(packageSetting) as? String
            } ?: return
            
            val callingUid = param.args[1] as Int
            val userId = param.args[4] as Int
            val callingPackageName = getPackageName(param.thisObject, callingUid) ?: return

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
            XLog.e(e, "Error in hookFilterAppAccess")
        }
    }

    private fun getPackageName(pms: Any, uid: Int): String? {
        return try {
            val callingAppId = getAppIdMethod.invoke(null, uid)
            val mSettings = mSettingsField.get(pms)
            val packageSetting = getSettingLPrMethod.invoke(mSettings, callingAppId)
            
            // Use HiddenApiBypass to get the package name
            packageSetting?.packageName
        } catch (e: Throwable) {
            XLog.e(e, "Error in getPackageName")
            null
        }
    }
}