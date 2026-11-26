package dev.shkriuss.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention plugin for standalone native Android apps (`dev.shkriuss.<appname>`).
 * Enforces GrapheneOS hardening, targetSdk 37, reproducible builds, and R8 shrinking.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)

                defaultConfig {
                    targetSdk = 37
                }

                // Disable Google Play encrypted dependency metadata block for reproducible FOSS APKs.
                dependenciesInfo {
                    includeInApk = false
                    includeInBundle = false
                }

                buildTypes {
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                    }
                    debug {
                        isMinifyEnabled = false
                    }
                }

                packaging {
                    jniLibs {
                        useLegacyPackaging = false
                    }
                    resources {
                        excludes += setOf(
                            "/META-INF/{AL2.0,LGPL2.1}",
                            "/META-INF/LICENSE*",
                            "/META-INF/NOTICE*",
                        )
                    }
                }
            }
        }
    }
}
