plugins {
    alias(libs.plugins.shkriuss.android.library)
    alias(libs.plugins.shkriuss.android.compose)
    alias(libs.plugins.shkriuss.android.roborazzi)
}

android {
    namespace = "dev.shkriuss.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3.adaptive)
    api(libs.androidx.compose.material3.adaptive.layout)
    api(libs.androidx.compose.material3.adaptive.navigation)

    testImplementation(projects.core.testing)
}
