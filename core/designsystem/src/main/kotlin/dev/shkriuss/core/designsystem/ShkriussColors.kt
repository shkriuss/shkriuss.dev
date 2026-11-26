package dev.shkriuss.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Monochrome stone/ink neutral palette with a single calm Slate Blue accent.
 * Every foreground/background token pair is verified against WCAG 2.2 AA (>= 4.5:1)
 * by `ShkriussContrastTest`.
 */
object ShkriussPalette {
    // Light neutral stone surfaces & ink
    val LightBackground = Color(0xFFF8F9FA)
    val LightSurfaceLowest = Color(0xFFFFFFFF)
    val LightSurfaceLow = Color(0xFFF2F4F7)
    val LightSurfaceContainer = Color(0xFFEAEEF2)
    val LightSurfaceHigh = Color(0xFFE2E7ED)
    val LightSurfaceHighest = Color(0xFFD8DFE8)
    val LightInk = Color(0xFF161B22)
    val LightInkMuted = Color(0xFF4A5568)
    val LightOutline = Color(0xFF64748B)
    val LightOutlineHairline = Color(0xFFCBD5E1)

    // Light Slate Blue accent & semantic tones
    val SlateBlueLight = Color(0xFF2C5282)
    val SlateBlueContainerLight = Color(0xFFDCE7F5)
    val OnSlateBlueContainerLight = Color(0xFF142A47)
    val SecondaryLight = Color(0xFF475569)
    val SecondaryContainerLight = Color(0xFFE2E8F0)
    val OnSecondaryContainerLight = Color(0xFF1E293B)
    val PositiveLight = Color(0xFF2F6B57)
    val PositiveContainerLight = Color(0xFFD8EFE7)
    val OnPositiveContainerLight = Color(0xFF0F3528)
    val ErrorLight = Color(0xFFB42318)
    val ErrorContainerLight = Color(0xFFFEE4E2)
    val OnErrorContainerLight = Color(0xFF55160C)

    // Dark slate-ink surfaces & light ink
    val DarkBackground = Color(0xFF0E1116)
    val DarkSurfaceLowest = Color(0xFF090C10)
    val DarkSurfaceLow = Color(0xFF151A22)
    val DarkSurfaceContainer = Color(0xFF1C232D)
    val DarkSurfaceHigh = Color(0xFF242D3A)
    val DarkSurfaceHighest = Color(0xFF2D3848)
    val DarkInk = Color(0xFFEDF2F7)
    val DarkInkMuted = Color(0xFF9AA6B8)
    val DarkOutline = Color(0xFF7C8A9E)
    val DarkOutlineHairline = Color(0xFF2D3748)

    // Dark Slate Blue accent & semantic tones
    val SlateBlueDark = Color(0xFF8AB4F8)
    val OnSlateBlueDark = Color(0xFF0A2240)
    val SlateBlueContainerDark = Color(0xFF1E3A5F)
    val OnSlateBlueContainerDark = Color(0xFFDCE9FB)
    val SecondaryDark = Color(0xFF94A3B8)
    val OnSecondaryDark = Color(0xFF0F172A)
    val SecondaryContainerDark = Color(0xFF273244)
    val OnSecondaryContainerDark = Color(0xFFE2E8F0)
    val PositiveDark = Color(0xFF7CC9AE)
    val OnPositiveDark = Color(0xFF0A2E22)
    val PositiveContainerDark = Color(0xFF1A4235)
    val OnPositiveContainerDark = Color(0xFFD8EFE7)
    val ErrorDark = Color(0xFFF8968F)
    val OnErrorDark = Color(0xFF4A0D09)
    val ErrorContainerDark = Color(0xFF6E1811)
    val OnErrorContainerDark = Color(0xFFFDE4E2)
}

val ShkriussLightColorScheme: ColorScheme = lightColorScheme(
    primary = ShkriussPalette.SlateBlueLight,
    onPrimary = Color.White,
    primaryContainer = ShkriussPalette.SlateBlueContainerLight,
    onPrimaryContainer = ShkriussPalette.OnSlateBlueContainerLight,
    secondary = ShkriussPalette.SecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = ShkriussPalette.SecondaryContainerLight,
    onSecondaryContainer = ShkriussPalette.OnSecondaryContainerLight,
    tertiary = ShkriussPalette.PositiveLight,
    onTertiary = Color.White,
    tertiaryContainer = ShkriussPalette.PositiveContainerLight,
    onTertiaryContainer = ShkriussPalette.OnPositiveContainerLight,
    error = ShkriussPalette.ErrorLight,
    onError = Color.White,
    errorContainer = ShkriussPalette.ErrorContainerLight,
    onErrorContainer = ShkriussPalette.OnErrorContainerLight,
    background = ShkriussPalette.LightBackground,
    onBackground = ShkriussPalette.LightInk,
    surface = ShkriussPalette.LightBackground,
    onSurface = ShkriussPalette.LightInk,
    surfaceVariant = ShkriussPalette.LightSurfaceContainer,
    onSurfaceVariant = ShkriussPalette.LightInkMuted,
    surfaceContainerLowest = ShkriussPalette.LightSurfaceLowest,
    surfaceContainerLow = ShkriussPalette.LightSurfaceLow,
    surfaceContainer = ShkriussPalette.LightSurfaceContainer,
    surfaceContainerHigh = ShkriussPalette.LightSurfaceHigh,
    surfaceContainerHighest = ShkriussPalette.LightSurfaceHighest,
    outline = ShkriussPalette.LightOutline,
    outlineVariant = ShkriussPalette.LightOutlineHairline,
    inverseSurface = ShkriussPalette.DarkSurfaceContainer,
    inverseOnSurface = ShkriussPalette.DarkInk,
    inversePrimary = ShkriussPalette.SlateBlueDark,
)

val ShkriussDarkColorScheme: ColorScheme = darkColorScheme(
    primary = ShkriussPalette.SlateBlueDark,
    onPrimary = ShkriussPalette.OnSlateBlueDark,
    primaryContainer = ShkriussPalette.SlateBlueContainerDark,
    onPrimaryContainer = ShkriussPalette.OnSlateBlueContainerDark,
    secondary = ShkriussPalette.SecondaryDark,
    onSecondary = ShkriussPalette.OnSecondaryDark,
    secondaryContainer = ShkriussPalette.SecondaryContainerDark,
    onSecondaryContainer = ShkriussPalette.OnSecondaryContainerDark,
    tertiary = ShkriussPalette.PositiveDark,
    onTertiary = ShkriussPalette.OnPositiveDark,
    tertiaryContainer = ShkriussPalette.PositiveContainerDark,
    onTertiaryContainer = ShkriussPalette.OnPositiveContainerDark,
    error = ShkriussPalette.ErrorDark,
    onError = ShkriussPalette.OnErrorDark,
    errorContainer = ShkriussPalette.ErrorContainerDark,
    onErrorContainer = ShkriussPalette.OnErrorContainerDark,
    background = ShkriussPalette.DarkBackground,
    onBackground = ShkriussPalette.DarkInk,
    surface = ShkriussPalette.DarkBackground,
    onSurface = ShkriussPalette.DarkInk,
    surfaceVariant = ShkriussPalette.DarkSurfaceContainer,
    onSurfaceVariant = ShkriussPalette.DarkInkMuted,
    surfaceContainerLowest = ShkriussPalette.DarkSurfaceLowest,
    surfaceContainerLow = ShkriussPalette.DarkSurfaceLow,
    surfaceContainer = ShkriussPalette.DarkSurfaceContainer,
    surfaceContainerHigh = ShkriussPalette.DarkSurfaceHigh,
    surfaceContainerHighest = ShkriussPalette.DarkSurfaceHighest,
    outline = ShkriussPalette.DarkOutline,
    outlineVariant = ShkriussPalette.DarkOutlineHairline,
    inverseSurface = ShkriussPalette.LightSurfaceContainer,
    inverseOnSurface = ShkriussPalette.LightInk,
    inversePrimary = ShkriussPalette.SlateBlueLight,
)
