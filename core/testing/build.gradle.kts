plugins {
    alias(libs.plugins.shkriuss.android.library)
}

android {
    namespace = "dev.shkriuss.core.testing"
}

dependencies {
    api(projects.core.common)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}
