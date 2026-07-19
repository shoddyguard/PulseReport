plugins {
    alias(libs.plugins.pulsereport.android.application)
    alias(libs.plugins.pulsereport.android.compose)
}

android {
    namespace = "dev.pulsereport.tools.seeder"

    defaultConfig {
        applicationId = "dev.pulsereport.seeder"
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    // Two installable identities so the app has a real multi-source clash to test the
    // Sources feature's priority selection against: without a second writer, every metric
    // has exactly one data origin and priority never has anything to choose between.
    flavorDimensions += "origin"
    productFlavors {
        create("primary") {
            dimension = "origin"
            buildConfigField("double", "SEED_VALUE_SCALE", "1.0")
        }
        create("alt") {
            dimension = "origin"
            applicationId = "dev.pulsereport.seeder.alt"
            buildConfigField("double", "SEED_VALUE_SCALE", "0.9")
        }
    }
}

dependencies {
    implementation(libs.androidx.health.connect.client)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}

tasks.register("seedHealthConnect") {
    group = "pulsereport"
    description = "Seeds 30 days of fake health data into Health Connect as the primary origin."
    dependsOn("connectedPrimaryDebugAndroidTest")
}

tasks.register("seedHealthConnectAlt") {
    group = "pulsereport"
    description = "Seeds 30 days of fake health data into Health Connect as the alt origin, " +
        "for testing multi-source priority in the Sources feature."
    dependsOn("connectedAltDebugAndroidTest")
}
