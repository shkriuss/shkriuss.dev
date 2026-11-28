package dev.shkriuss.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention plugin for pure-Kotlin Android library modules under `:core:*`.
 * Relies on AGP 9.5+ built-in Kotlin rather than the legacy standalone Android Kotlin plugin.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)

                defaultConfig {
                    if (file("consumer-rules.pro").exists()) {
                        consumerProguardFiles("consumer-rules.pro")
                    }
                }

                testOptions {
                    unitTests.isReturnDefaultValues = true
                }
            }
        }
    }
}
