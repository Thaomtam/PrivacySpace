package com.hidemyspace.xposed

import android.content.pm.ApplicationInfo
import android.content.pm.IPackageManager
import android.os.Build
import java.util.*

object Utils {
    
    /**
     * Tạo chuỗi ngẫu nhiên với độ dài cho trước
     */
    fun generateRandomString(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        val random = Random()
        return (1..length)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }
    
    /**
     * Lấy danh sách ứng dụng đã cài đặt
     */
    fun getInstalledApplications(pms: IPackageManager, flags: Int, userId: Int): List<ApplicationInfo> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pms.javaClass.getMethod(
                    "getInstalledApplications",
                    Long::class.java,
                    Int::class.javaPrimitiveType
                ).invoke(pms, flags.toLong(), userId) as List<ApplicationInfo>
            } else {
                pms.javaClass.getMethod(
                    "getInstalledApplications",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                ).invoke(pms, flags, userId) as List<ApplicationInfo>
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Lấy tên gói của ứng dụng từ UID
     */
    fun getPackageNameForUid(pms: IPackageManager, uid: Int): String? {
        return try {
            val names = pms.javaClass.getMethod(
                "getPackagesForUid",
                Int::class.javaPrimitiveType
            ).invoke(pms, uid) as Array<String>?
            names?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Lấy thông tin ứng dụng từ tên gói
     */
    fun getApplicationInfo(pms: IPackageManager, packageName: String, flags: Int, userId: Int): ApplicationInfo? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pms.javaClass.getMethod(
                    "getApplicationInfo",
                    String::class.java,
                    Long::class.java,
                    Int::class.javaPrimitiveType
                ).invoke(pms, packageName, flags.toLong(), userId) as ApplicationInfo
            } else {
                pms.javaClass.getMethod(
                    "getApplicationInfo",
                    String::class.java,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                ).invoke(pms, packageName, flags, userId) as ApplicationInfo
            }
        } catch (e: Exception) {
            null
        }
    }
} 