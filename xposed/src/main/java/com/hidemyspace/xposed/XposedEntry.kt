package com.hidemyspace.xposed

import android.content.pm.IPackageManager
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlin.concurrent.thread

private const val TAG = "HMS-XposedEntry"

class XposedEntry : IXposedHookZygoteInit, IXposedHookLoadPackage {

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == "com.hidemyspace") {
            EzXHelperInit.initHandleLoadPackage(lpparam)
            hookAllConstructorAfter("com.hidemyspace.HideMySpaceApp") {
                getFieldByDesc("Lcom/hidemyspace/HideMySpaceApp;->isHooked:Z").setBoolean(it.thisObject, true)
            }
        } else if (lpparam.packageName == "android") {
            EzXHelperInit.initHandleLoadPackage(lpparam)
            logI(TAG, "Hook entry")

            var serviceManagerHook: XC_MethodHook.Unhook? = null
            serviceManagerHook = findMethod("android.os.ServiceManager") {
                name == "addService"
            }.hookBefore { param ->
                if (param.args[0] == "package") {
                    serviceManagerHook?.unhook()
                    val pms = param.args[1] as IPackageManager
                    logD(TAG, "Got pms: $pms")
                    thread {
                        runCatching {
                            HMSService.register(pms)
                            logI(TAG, "Service started")
                        }.onFailure {
                            logE(TAG, "Service failed to start", it)
                        }
                    }
                }
            }
        }
    }
} 