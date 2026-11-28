plugins {
    alias(libs.plugins.shkriuss.android.library)
}

android {
    namespace = "dev.shkriuss.core.common"
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
