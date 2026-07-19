plugins {
    alias(libs.plugins.pulsereport.android.library)
    alias(libs.plugins.pulsereport.hilt)
}

android {
    namespace = "dev.pulsereport.core.database"
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.kotlinx.coroutines.android)
}
