package dev.shkriuss.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Verifies WCAG 2.2 AA color contrast ratios (>= 4.5:1 for text pairs, >= 3.0:1 for outlines)
 * across both `ShkriussLightColorScheme` and `ShkriussDarkColorScheme`.
 */
class ShkriussContrastTest {
    @Test
    fun `light theme meets WCAG 2_2 AA contrast ratios`() {
        assertSchemeContrast("Light", ShkriussLightColorScheme)
    }

    @Test
    fun `dark theme meets WCAG 2_2 AA contrast ratios`() {
        assertSchemeContrast("Dark", ShkriussDarkColorScheme)
    }

    private fun assertSchemeContrast(name: String, scheme: ColorScheme) {
        val textPairs = listOf(
            "onBackground / background" to (scheme.onBackground to scheme.background),
            "onSurface / surface" to (scheme.onSurface to scheme.surface),
            "onSurface / surfaceContainerLowest" to (scheme.onSurface to scheme.surfaceContainerLowest),
            "onSurfaceVariant / surface" to (scheme.onSurfaceVariant to scheme.surface),
            "onSurfaceVariant / surfaceContainer" to (scheme.onSurfaceVariant to scheme.surfaceContainer),
            "onPrimary / primary" to (scheme.onPrimary to scheme.primary),
            "primary / background" to (scheme.primary to scheme.background),
            "onPrimaryContainer / primaryContainer" to (scheme.onPrimaryContainer to scheme.primaryContainer),
            "onSecondary / secondary" to (scheme.onSecondary to scheme.secondary),
            "onSecondaryContainer / secondaryContainer" to (scheme.onSecondaryContainer to scheme.secondaryContainer),
            "onTertiary / tertiary" to (scheme.onTertiary to scheme.tertiary),
            "onTertiaryContainer / tertiaryContainer" to (scheme.onTertiaryContainer to scheme.tertiaryContainer),
            "onError / error" to (scheme.onError to scheme.error),
            "onErrorContainer / errorContainer" to (scheme.onErrorContainer to scheme.errorContainer),
        )

        textPairs.forEach { (label, pair) ->
            val ratio = contrastRatio(pair.first, pair.second)
            assertTrue(
                "$name $label contrast ratio %.2f is below WCAG 2.2 AA 4.5:1".format(ratio),
                ratio >= 4.5,
            )
        }

        val outlineRatio = contrastRatio(scheme.outline, scheme.background)
        assertTrue(
            "$name outline/background contrast ratio %.2f is below WCAG 2.2 AA 3.0:1".format(outlineRatio),
            outlineRatio >= 3.0,
        )
    }

    private fun contrastRatio(foreground: Color, background: Color): Double {
        val l1 = relativeLuminance(foreground)
        val l2 = relativeLuminance(background)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        fun linearize(channel: Float): Double {
            val c = channel.toDouble()
            return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linearize(color.red) +
            0.7152 * linearize(color.green) +
            0.0722 * linearize(color.blue)
    }
}
