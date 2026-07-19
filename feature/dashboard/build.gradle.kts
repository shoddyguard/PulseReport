plugins {
    alias(libs.plugins.pulsereport.android.feature)
}

android {
    namespace = "dev.pulsereport.feature.dashboard"
}

dependencies {
    implementation(project(":core:healthconnect"))
    implementation(project(":core:database"))
}
