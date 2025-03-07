package com.hidemyspace.xposed.hook

/**
 * Interface cho các hook framework
 */
interface IFrameworkHook {
    /**
     * Tải và cài đặt hook
     */
    fun load()
    
    /**
     * Gỡ bỏ hook
     */
    fun unload()
    
    /**
     * Xử lý khi cấu hình thay đổi
     */
    fun onConfigChanged()
}