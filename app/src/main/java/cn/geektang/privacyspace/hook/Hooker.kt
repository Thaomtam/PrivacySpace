package cn.geektang.privacyspace.hook

interface Hooker {
    /**
     * Start the hooking process with the given classloader
     */
    fun start(classLoader: ClassLoader)
    
    /**
     * Stop the hooking process and clean up resources
     * Default implementation is empty for backward compatibility
     */
    fun stop() {}

    val Any.packageName: String
        get() = toString().substringAfterLast(" ").substringBefore("/")
}