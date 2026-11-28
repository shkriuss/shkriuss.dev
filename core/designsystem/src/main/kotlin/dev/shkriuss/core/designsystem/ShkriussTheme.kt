package dev.shkriuss.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Root theme for every native Android app in the shkriuss suite.
 */
@Composable
fun ShkriussTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    spacing: ShkriussSpacing = ShkriussSpacing(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ShkriussDarkColorScheme else ShkriussLightColorScheme

    CompositionLocalProvider(LocalShkriussSpacing provides spacing) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ShkriussTypography,
            shapes = ShkriussShapes,
            content = content,
        )
    }
}
