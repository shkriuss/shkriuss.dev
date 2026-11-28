package dev.shkriuss.core.designsystem

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import dev.shkriuss.core.testing.ShkriussDeviceSpecs
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * JVM screenshot tests rendering the shared Slate Blue design system across
 * Phone (compact), Pixel Tablet (expanded), Light, Dark, and 200% font scale.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [ShkriussDeviceSpecs.ROBOLECTRIC_SDK])
class ShkriussThemeScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = ShkriussDeviceSpecs.PHONE_LIGHT)
    fun captureShowcasePhoneLight() {
        composeRule.setContent {
            ShkriussTheme(darkTheme = false) {
                ShkriussDesignShowcase(isExpandedWidth = false)
            }
        }
        composeRule.onNodeWithText("shkriuss").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/designsystem_phone_light.png",
        )
    }

    @Test
    @Config(qualifiers = ShkriussDeviceSpecs.PHONE_DARK)
    fun captureShowcasePhoneDark() {
        composeRule.setContent {
            ShkriussTheme(darkTheme = true) {
                ShkriussDesignShowcase(isExpandedWidth = false)
            }
        }
        composeRule.onNodeWithText("shkriuss").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/designsystem_phone_dark.png",
        )
    }

    @Test
    @Config(qualifiers = ShkriussDeviceSpecs.TABLET_LIGHT)
    fun captureShowcaseTabletLight() {
        composeRule.setContent {
            ShkriussTheme(darkTheme = false) {
                ShkriussDesignShowcase(isExpandedWidth = true)
            }
        }
        composeRule.onNodeWithText("shkriuss").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/designsystem_tablet_light.png",
        )
    }

    @Test
    @Config(qualifiers = ShkriussDeviceSpecs.TABLET_DARK)
    fun captureShowcaseTabletDark() {
        composeRule.setContent {
            ShkriussTheme(darkTheme = true) {
                ShkriussDesignShowcase(isExpandedWidth = true)
            }
        }
        composeRule.onNodeWithText("shkriuss").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/designsystem_tablet_dark.png",
        )
    }

    @Test
    @Config(qualifiers = ShkriussDeviceSpecs.PHONE_LIGHT)
    fun captureShowcasePhoneLightFontScale200() {
        composeRule.setContent {
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = baseDensity.density,
                    fontScale = ShkriussDeviceSpecs.FONT_SCALE_ACCESSIBILITY,
                ),
            ) {
                ShkriussTheme(darkTheme = false) {
                    ShkriussDesignShowcase(isExpandedWidth = false)
                }
            }
        }
        composeRule.onNodeWithText("shkriuss").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/designsystem_phone_light_font200.png",
        )
    }
}
