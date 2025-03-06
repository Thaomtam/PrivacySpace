# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Xposed module entry points
-keep class cn.geektang.privacyspace.hook.HookMain {
    public void handleLoadPackage(de.robv.android.xposed.callbacks.XC_LoadPackage$LoadPackageParam);
    public void initZygote(de.robv.android.xposed.IXposedHookZygoteInit$StartupParam);
}

# Keep all classes in your hook implementation packages
-keep class cn.geektang.privacyspace.hook.** { *; }
-keep class cn.geektang.privacyspace.hook.impl.** { *; }

# Keep the specific hook implementations for different Android versions
-keep class cn.geektang.privacyspace.hook.impl.FrameworkHookerApi26Impl { *; }
-keep class cn.geektang.privacyspace.hook.impl.FrameworkHookerApi28Impl { *; }
-keep class cn.geektang.privacyspace.hook.impl.FrameworkHookerApi30Impl { *; }
-keep class cn.geektang.privacyspace.hook.impl.FrameworkHookerApi33Impl { *; }
-keep class cn.geektang.privacyspace.hook.impl.FrameworkHookerApi34Impl { *; }
-keep class cn.geektang.privacyspace.hook.impl.HookChecker { *; }

# Keep Xposed initialization in assets
-keep class cn.geektang.privacyspace.BuildConfig { *; }
-keepclassmembers class cn.geektang.privacyspace.BuildConfig {
   public static final boolean DEBUG;
}

# Keep model/bean classes that are used with Moshi
-keep class cn.geektang.privacyspace.bean.** { *; }
-keepclassmembers class cn.geektang.privacyspace.bean.** { *; }

# Keep utils that are used by hooks
-keep class cn.geektang.privacyspace.util.** { *; }
-keep class cn.geektang.privacyspace.constant.** { *; }

# HiddenApiBypass rules
-keep class org.lsposed.hiddenapibypass.** { *; }

# Rules for EzXHelper
-keep class com.github.kyuubiran.ezxhelper.** { *; }

# Rules for reflection-based operations
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions

# Keep Xposed API
-keep class de.robv.android.xposed.** { *; }
-keepclassmembers class de.robv.android.xposed.** { *; }

# Keep JNI methods
-keepclasseswithmembers class * {
    native <methods>;
}