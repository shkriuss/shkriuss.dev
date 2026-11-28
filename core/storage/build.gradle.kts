plugins {
    alias(libs.plugins.shkriuss.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.shkriuss.core.storage"
}

dependencies {
    api(projects.core.common)
    api(projects.core.crypto)
    api(libs.androidx.datastore)
    api(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
