---
name: compose-a11y-screenshot
description: Write WCAG 2.2 AA accessible Compose screens and Roborazzi screenshot tests for Phone, Tablet, Light, Dark, and 200% font scale.
---

# Skill: Compose Accessibility & Roborazzi Screenshots

Use this skill whenever creating or modifying Compose UI in `:core:designsystem` or `apps/*`.

## Accessibility Requirements

1. **Color Contrast:** Use `ShkriussTheme` (`MaterialTheme.colorScheme`) tokens only; never hardcode raw low-contrast hex colors in UI screens.
2. **Touch Targets:** Every interactive control MUST have a minimum 48dp × 48dp touch target (`Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)` or Material 3 defaults).
3. **TalkBack Semantics:**
   - Mark section/screen titles with `Modifier.semantics { heading() }`.
   - Supply concise `contentDescription` on every meaningful icon/image, and `null` on purely decorative icons.
   - Group related row content with `Modifier.semantics(mergeDescendants = true) {}` where appropriate.
4. **Dynamic Text & Localization:**
   - Use `stringResource(...)` for all user-visible text.
   - Never clip text at fixed pixel heights that break at 200% font scaling.

## Roborazzi Test Matrix

Every new screen or design-system showcase must render screenshots via `:core:testing` helpers across:
- Phone compact (`w411dp-h915dp`, 420dpi) — Light & Dark
- Pixel Tablet expanded (`w1280dp-h800dp`, 320dpi) — Light & Dark
- Accessibility large font (`fontScale = 2.0f`) on Phone compact

CI uploads all rendered PNGs as PR artifacts so the maintainer can visually inspect them directly in Vanadium on phone or tablet.
