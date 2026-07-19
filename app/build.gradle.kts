plugins {
    alias(libs.plugins.pulsereport.android.application)
    alias(libs.plugins.pulsereport.android.compose)
    alias(libs.plugins.pulsereport.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.pulsereport.healthconnect.toolbox)
}

android {
    namespace = "dev.pulsereport.app"

    defaultConfig {
        applicationId = "dev.pulsereport"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:health"))
    implementation(project(":feature:mood"))
    implementation(project(":feature:diary"))
    implementation(project(":feature:export"))
    implementation(project(":feature:sources"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
