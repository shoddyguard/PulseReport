plugins {
    alias(libs.plugins.pulsereport.android.library)
    alias(libs.plugins.pulsereport.hilt)
}

android {
    namespace = "dev.pulsereport.core.healthconnect"
}

dependencies {
    implementation(project(":core:model"))

    // api so feature modules can use the permission contract and record types
    api(libs.androidx.health.connect.client)

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}
