plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.heesoyam.berda"
    compileSdk = 35
    defaultConfig {
        // Keep the overlay app separate from the Berda game package.
        applicationId = "com.devvolodya.berdaoverlay"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
    }
}

kotlin { jvmToolchain(17) }
