// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false

}

// Add the Google Services classpath
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.15") // Check for the latest version
    }
}