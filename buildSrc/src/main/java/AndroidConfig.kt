object AndroidConfig{
    val compileSdkVersion = 30
    val buildToolsVersion = "30.0.3"
    val defaultConfig = DefaultConfig()

    class DefaultConfig {
        val applicationId = "com.lsy.matisse_kotlin"
        val minSdkVersion = 21
        val targetSdkVersion = 30
        val versionCode = 1
        val versionName = "1.0"
    }
}