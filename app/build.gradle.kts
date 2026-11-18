plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.starfall.berdaoverlay"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.starfall.berdaoverlay"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
