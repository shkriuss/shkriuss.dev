package dev.shkriuss.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType

/**
 * Convention plugin enabling Jetpack Compose with Compose BOM 2026.11.01 and Material 3.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val commonExtension: CommonExtension =
                extensions.findByType<ApplicationExtension>()
                    ?: extensions.findByType<LibraryExtension>()
                    ?: error("shkriuss.android.compose requires shkriuss.android.application or shkriuss.android.library")

            commonExtension.buildFeatures.compose = true

            val composeBom = libs.findLibrary("androidx-compose-bom").get()
            dependencies {
                add("implementation", platform(composeBom))
                add("testImplementation", platform(composeBom))
                add("androidTestImplementation", platform(composeBom))
                add("implementation", libs.findLibrary("androidx-compose-ui").get())
                add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
                add("implementation", libs.findLibrary("androidx-compose-foundation").get())
                add("implementation", libs.findLibrary("androidx-compose-material3").get())
                add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
                add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
            }
        }
    }
}
