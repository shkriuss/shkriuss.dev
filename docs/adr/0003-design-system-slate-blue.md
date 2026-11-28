# ADR 0003: Shared Design System (Monochrome Stone/Ink + Slate Blue Accent)

- **Status:** Accepted
- **Date:** 2026-11-24

## Context

All 13 shkriuss apps should feel calm, cohesive, legible, and unmistakably native on both phones and tablets, while meeting WCAG 2.2 AA accessibility standards and supporting both English and Georgian text cleanly.

## Decision

1. **Color Palette (`:core:designsystem`):**
   - **Neutral Surfaces:** Calm warm stone surfaces in Light theme (`#F8F9FA` background, `#FFFFFF` elevated cards, `#EEF0F3` containers, `#161A1E` ink text) and deep quiet slate-ink surfaces in Dark theme (`#0F1216` background, `#161B22` elevated cards, `#1E252F` containers, `#EAEFF5` text).
   - **Single Accent — Slate Blue:** `#2C5282` (`primary` Light, contrast ≥ 7.4:1 against `#FFFFFF` / `#F8F9FA`) and `#8AB4F8` (`primary` Dark, contrast ≥ 8.8:1 against `#0F1216`, paired with `#0B223F` `onPrimary` at ≥ 9.5:1).
   - **Semantic Status Colors:** Muted forest green (positive/success), warm amber (warning), and calm crimson (destructive/error), all verified to meet WCAG 2.2 AA (≥ 4.5:1 normal text, ≥ 3:1 UI elements).
2. **Density & Feel:** Spacious and minimal journaling feel — generous horizontal padding (`20.dp` on compact phones, `28.dp` on tablets), clear section spacing (`24.dp`), calm `1.dp` hairline dividers over noisy drop shadows, and zero visual clutter.
3. **Typography:** Platform system sans-serif (`FontFamily.SansSerif`, rendering Android's native `Roboto` and system Georgian Noto glyphs with zero bundled font bloat), generous line heights (`LineHeightStyle.Mode.PerLine` compatibility), and strict dynamic font scaling support up to `200%`.
4. **Adaptive Layouts:** Built on Compose Material 3 (`1.5.0`) and Material 3 Adaptive (`1.3.0`) so phones use single-pane layouts with bottom navigation where needed and tablets (`≥ 840dp`) automatically adapt to navigation rails and multi-pane views.
5. **Accessibility Verification:** Every shared component and app screen enforces ≥ `48.dp` touch targets, TalkBack semantics (`heading()`, `contentDescription`, merged row semantics), localized `strings.xml`, and automated Roborazzi screenshot verification across Phone + Tablet × Light + Dark + 200% font scale.

## Rejected Alternatives

- **Material You Dynamic Color by default:** Rejected as the default palette because wallpaper-derived colors can reduce visual consistency across the suite and occasionally produce jarring accents; an opt-in user setting may be evaluated later without weakening contrast invariants.
- **Bundled custom third-party font files:** Rejected to keep APK size small, preserve native Georgian script rendering, and simplify license compliance.

## Consequences

- Every app shares `ShkriussTheme`, `ShkriussSpacing`, `ShkriussTypography`, and reusable empty/locked/section components from `:core:designsystem`.
- Reviewers can inspect visual fidelity directly from Vanadium on phone or tablet via Roborazzi PNG artifacts on every PR.
