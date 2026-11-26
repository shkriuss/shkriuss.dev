import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "dev.shkriuss.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.roborazzi.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "shkriuss.android.application"
            implementationClass = "dev.shkriuss.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "shkriuss.android.library"
            implementationClass = "dev.shkriuss.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "shkriuss.android.compose"
            implementationClass = "dev.shkriuss.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidRoborazzi") {
            id = "shkriuss.android.roborazzi"
            implementationClass = "dev.shkriuss.buildlogic.AndroidRoborazziConventionPlugin"
        }
    }
}
