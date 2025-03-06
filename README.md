# Privacy Space (Modernized)

[![Stars](https://img.shields.io/github/stars/GeekTR/PrivacySpace?label=Stars)](https://github.com/GeekTR/PrivacySpace)
[![Release](https://img.shields.io/github/v/release/Xposed-Modules-Repo/cn.geektang.privacyspace?label=Release)](https://github.com/Xposed-Modules-Repo/cn.geektang.privacyspace/releases/latest)
[![Download](https://img.shields.io/github/downloads/Xposed-Modules-Repo/cn.geektang.privacyspace/total)](https://github.com/Xposed-Modules-Repo/cn.geektang.privacyspace/releases/latest)
[![Channel](https://img.shields.io/badge/Follow-Telegram-blue.svg?logo=telegram)](https://t.me/PrivacySpaceAlpha)
[![GitHub license](https://img.shields.io/github/license/Xposed-Modules-Repo/cn.geektang.privacyspace)](https://github.com/Xposed-Modules-Repo/cn.geektang.privacyspace/blob/main/LICENSE)

[中文文档](https://github.com/Xposed-Modules-Repo/cn.geektang.privacyspace/blob/main/README_CN.md)

## Overview
Privacy Space is an Xposed module that allows you to hide apps from other apps on your device. This creates functionality similar to a "Second Space" feature found on some Android devices. The module has been updated with modern techniques inspired by HideMyAppList to improve compatibility with newer Android versions (up to Android 13/API 33).

## Features
- Hide apps from other apps (prevent apps from detecting each other)
- Bypass root/Xposed detection in banking apps by hiding Magisk/LSPosed
- Prevent automatic app updates from app stores
- Stop advertising apps from detecting and launching other apps
- Protect privacy by preventing software providers from knowing what apps you have installed
- Blind mode: prevent specified apps from seeing most other apps

## What's New in This Version
- **Support for Android 13**: Added specific hooks for Android 13 (API 33)
- **Hidden API Bypass**: Now uses org.lsposed.hiddenapibypass for better compatibility with Android 9+ restrictions
- **Modern Build System**: Converted from Groovy DSL to Kotlin DSL for better type safety and maintainability
- **Improved Hook Management**: Added proper hook cleanup logic for better stability
- **Performance Optimizations**: Added caching to reduce overhead in heavily-used methods

## Requirements
- Android 8.0+ (API 26+)
- LSPosed, EdXposed, or other Xposed framework
- Root access (for the Xposed framework)

## How to Use

### Hiding Apps
1. Enable the module in your Xposed framework manager
2. Open the Privacy Space app
3. Go to "Manage Hidden Apps" and select the apps you want to hide
4. Reboot for changes to take effect

### Setting Up Whitelist
Some apps need to see other apps to function properly. You can add these to your whitelist:
1. Go to "Set Whitelist" in the app
2. Select apps that should be able to see all other apps
3. Whitelist apps will be able to see hidden apps regardless of other settings

### Connected Apps
Connected apps can see each other even if they're hidden:
1. Go to "Set Connected Apps" in the app
2. Select apps that should be able to see each other

### Blind Mode
Blind mode prevents an app from seeing most other apps:
1. Go to "Blind Mode" in the app
2. Select apps that should be "blind"
3. These apps will only see system apps and apps in your whitelist

## Compatibility
- Android 8 (Oreo): Uses FrameworkHookerApi26Impl
- Android 9-10 (Pie/Q): Uses FrameworkHookerApi28Impl
- Android 11-12 (R/S): Uses FrameworkHookerApi30Impl
- Android 13 (T): Uses FrameworkHookerApi33Impl

## Troubleshooting
- If apps can still detect hidden apps, try clearing their cache and data
- For banking apps, you may need to add their service/dependency apps to the hidden list too
- If you experience crashes, check the Xposed logs for details

## Credits
- Original Privacy Space module
- Techniques adapted from the HideMyAppList project
- org.lsposed.hiddenapibypass for hidden API access
- EzXHelper for simplified Xposed hooking