// Top-level build.gradle.kts file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Firebase Google Services plugin
    id("com.google.gms.google-services") version "4.4.2" apply false
}
