package com.hidemyspace.model

import org.json.JSONObject

/**
 * Cấu hình cho một ứng dụng cụ thể
 */
data class AppConfig(
    val useWhitelist: Boolean = false,
    val excludeSystemApps: Boolean = true,
    val appList: Set<String> = emptySet()
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("useWhitelist", useWhitelist)
        json.put("excludeSystemApps", excludeSystemApps)
        json.put("appList", appList.joinToString(","))
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): AppConfig {
            val useWhitelist = json.optBoolean("useWhitelist", false)
            val excludeSystemApps = json.optBoolean("excludeSystemApps", true)
            val appListStr = json.optString("appList", "")
            val appList = if (appListStr.isEmpty()) emptySet() else appListStr.split(",").toSet()
            return AppConfig(useWhitelist, excludeSystemApps, appList)
        }
    }
}

/**
 * Cấu hình tổng thể của module
 */
data class Config(
    val scope: Map<String, AppConfig> = emptyMap(),
    val detailLog: Boolean = false,
    val version: Int = 1
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("version", version)
        json.put("detailLog", detailLog)
        
        val scopeJson = JSONObject()
        scope.forEach { (packageName, config) ->
            scopeJson.put(packageName, config.toJson())
        }
        json.put("scope", scopeJson)
        
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): Config {
            val json = JSONObject(jsonStr)
            val version = json.optInt("version", 1)
            val detailLog = json.optBoolean("detailLog", false)
            
            val scopeJson = json.optJSONObject("scope") ?: JSONObject()
            val scope = mutableMapOf<String, AppConfig>()
            
            val keys = scopeJson.keys()
            while (keys.hasNext()) {
                val packageName = keys.next()
                val appConfigJson = scopeJson.getJSONObject(packageName)
                scope[packageName] = AppConfig.fromJson(appConfigJson)
            }
            
            return Config(scope, detailLog, version)
        }
    }
} 