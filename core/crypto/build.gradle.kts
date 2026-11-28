plugins {
    alias(libs.plugins.shkriuss.android.library)
}

android {
    namespace = "dev.shkriuss.core.crypto"
}

dependencies {
    api(projects.core.common)
    implementation(libs.tink.android)
    implementation(libs.bouncycastle.bcprov)

    testImplementation(libs.junit)
}
