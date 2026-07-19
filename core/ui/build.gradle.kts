plugins {
    alias(libs.plugins.pulsereport.android.library)
    alias(libs.plugins.pulsereport.android.compose)
}

android {
    namespace = "dev.pulsereport.core.ui"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
}
