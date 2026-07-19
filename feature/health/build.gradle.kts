plugins {
    alias(libs.plugins.pulsereport.android.feature)
}

android {
    namespace = "dev.pulsereport.feature.health"
}

dependencies {
    implementation(project(":core:healthconnect"))
    implementation(project(":core:database"))
}
