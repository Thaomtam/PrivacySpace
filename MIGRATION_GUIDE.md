# Privacy Space Modernization Guide

This guide explains the technical improvements made to the Privacy Space module, integrating modern techniques from HideMyAppList while maintaining Privacy Space's core functionality.

## Technical Improvements

### 1. Hidden API Bypass Implementation

Privacy Space now uses the `org.lsposed.hiddenapibypass` library to access hidden Android APIs more reliably. This is especially important for Android 9 (Pie) and above, where Google introduced restrictions on accessing non-SDK interfaces.

**Before:**
```kotlin
// Direct access to hidden APIs that could fail on newer Android versions
val packageName = packageSetting?.packageName
```

**After:**
```kotlin
// Using HiddenApiBypass to safely access hidden APIs
val packageName = try {
    HiddenApiBypass.invoke(packageSettingClass, packageSetting, "getPackageName") as? String
} catch (e: Exception) {
    // Fallback to direct field access if method fails
    HiddenApiBypass.getInstanceFields(packageSettingClass)
        .find { it.name == "name" || it.name == "packageName" }
        ?.get(packageSetting) as? String
}
```

### 2. Android 13 (API 33) Support

Added a new hook implementation specifically designed for Android 13, which has changes in the package manager structure. The implementation targets the same core functionality while adapting to Android 13's specific API changes.

```kotlin
@TargetApi(Build.VERSION_CODES.TIRAMISU)
object FrameworkHookerApi33Impl : Hooker {
    // Implementation tailored for Android 13
}
```

### 3. Improved Hook Management

Added proper resource cleanup with a standardized `stop()` method in the `Hooker` interface and all implementations.

**Before:** Hooks were created but never properly cleaned up.

**After:**
```kotlin
override fun stop() {
    unhooks.forEach { it.unhook() }
    unhooks.clear()
    XLog.d("FrameworkHookerImpl stopped")
}
```

### 4. Modern Kotlin DSL Build System

Converted build files from Groovy DSL to Kotlin DSL for improved type safety and IDE support:

**Before (build.gradle):**
```groovy
dependencies {
    implementation 'androidx.core:core-ktx:1.7.0'
}
```

**After (build.gradle.kts):**
```kotlin
dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
}
```

### 5. Performance Optimizations

Added caching mechanisms to improve performance, particularly for shared user ID lookup:

```kotlin
// Cache the shared user ID map to improve performance
private val sharedUserIdMapCache = ConcurrentHashMap<Int, Map<String, List<String>>>()
```

## Using the Modernized Features

The updated Privacy Space module maintains the same user interface and functionality as before, but with improved compatibility and performance. Users will benefit from:

1. **Better compatibility with newer Android versions (up to Android 13)**
2. **Improved stability with proper hook management**
3. **Better performance with optimized code and caching**
4. **More reliable hidden app detection prevention**

## Technical Architecture

The modernized Privacy Space uses a layered architecture:

1. **UI Layer**: Remains unchanged, using Jetpack Compose
2. **Configuration Layer**: File-based JSON configuration at `/data/system/cn.geektang.privacyspace/config.json`
3. **Hook Layer**: Updated with version-specific implementations:
   - FrameworkHookerApi26Impl: Android 8
   - FrameworkHookerApi28Impl: Android 9-10
   - FrameworkHookerApi30Impl: Android 11-12
   - FrameworkHookerApi33Impl: Android 13

4. **Hidden API Access Layer**: New addition that uses org.lsposed.hiddenapibypass to safely access hidden APIs

## Migration Steps

If you're updating from an older version of Privacy Space:

1. Uninstall the previous version (your configuration will be preserved)
2. Install the new version
3. Reactivate the module in your Xposed framework
4. Reboot your device

Your existing configuration (hidden apps, whitelist, etc.) will be automatically migrated.

## Behind the Scenes: How It Works

Privacy Space hides apps by intercepting package queries through various Android system APIs. 

For example, when App A tries to detect if App B is installed, Android's `shouldFilterApplication` method is called. Privacy Space intercepts this call and returns `true` (meaning "yes, filter this app") if App B should be hidden from App A.

The main improvements in this update focus on how Privacy Space accesses these system APIs, making it more reliable on newer Android versions, especially Android 9+ where hidden API restrictions were introduced.

---

By combining the strengths of Privacy Space's functionality with HideMyAppList's modern implementation techniques, this updated module provides a more reliable and future-proof solution for app hiding on Android.