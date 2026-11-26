---
trigger: always_on
description: Enforce pure native Android (Kotlin + Jetpack Compose) across all app and core modules.
---

# Rule: Pure Native Android Only

1. All Android application modules (`apps/*`) and shared Android library modules (`core/*`) MUST be written in pure Kotlin using Jetpack Compose and standard Android SDK / open-source AndroidX libraries.
2. NEVER introduce WebView (`android.webkit.WebView`, `androidx.webkit:*`, `androidx.web:*`), hybrid wrappers (React Native, Capacitor, Cordova, Ionic, Flutter), or Progressive Web Apps.
3. NEVER use browser preview tools or web-app scaffolding as a substitute for native Android UI.
4. For UI verification without a desktop computer, write Roborazzi JVM screenshot tests rendering Phone (compact) and Tablet (expanded) form factors in Light, Dark, and 200% font scale modes.
5. Prefer Compose Material 3 (`1.5.0+`), Material 3 Adaptive (`1.3.0+`), Navigation 3 (`1.2.2+`), and state-based text fields (`TextFieldState`) over deprecated Material 2, `ConstraintLayout` Compose, or legacy `BasicTextField(value = ...)`.
