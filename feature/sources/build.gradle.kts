plugins {
    alias(libs.plugins.pulsereport.android.feature)
}

android {
    namespace = "dev.pulsereport.feature.sources"
}

dependencies {
    implementation(project(":core:healthconnect"))
    implementation(project(":core:database"))
}
