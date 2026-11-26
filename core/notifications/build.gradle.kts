plugins {
    alias(libs.plugins.shkriuss.android.library)
}

android {
    namespace = "dev.shkriuss.core.notifications"
}

dependencies {
    api(projects.core.common)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
}
