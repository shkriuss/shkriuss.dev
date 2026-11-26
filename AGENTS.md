# AGENTS.md — shkriuss.dev Workspace Contract

This file defines mandatory engineering, security, and workflow rules for every coding agent operating in `shkriuss/shkriuss.dev`.

## 1. Strict Platform Boundary (Non-Negotiable)

- **Android apps (`apps/*`, `core/*`):** Pure native Android ONLY — Kotlin, Jetpack Compose, and standard Android SDK / open-source AndroidX libraries.
- **NEVER** introduce WebView, `androidx.webkit`, PWAs, React Native, Flutter, Capacitor, Cordova, Ionic, or any embedded browser UI into any Android module.
- **Static site (`site/`):** Plain static HTML5 and CSS3 ONLY for documentation, APK downloads, and fingerprint verification. Zero JavaScript frameworks, zero external CDNs/fonts, zero web-app functionality.

## 2. GrapheneOS-First Hardening & Zero Google Runtime

- Primary target: **GrapheneOS** on Android 17+ (`minSdk = 33`, `compileSdk = 37.1`, `targetSdk = 37`) with Exploit Protection Compatibility Mode **OFF**.
- **NEVER** add Google Play services (`com.google.android.gms`), Firebase (`com.google.firebase`), Play Integrity, Play Core, Crashlytics, ads, trackers, or analytics.
- **NEVER** bundle third-party C/C++ native libraries (`.so`) without an explicit accepted ADR documenting memory-safety trade-offs (e.g., future Rust/Harper NLP for Grammar).
- **NEVER** use deprecated `androidx.security:security-crypto` (`EncryptedSharedPreferences` / `EncryptedFile`). Use `:core:crypto` (Google Tink AEAD / Streaming AEAD + Android Keystore/StrongBox KEK + pure-JVM Bouncy Castle Argon2id).
- **NEVER** request broad storage permissions (`READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE`, `READ_MEDIA_*`). Use Storage Access Framework (SAF) and the system Photo Picker.
- **NEVER** declare `INTERNET` in apps that do not explicitly implement optional self-hosted sync, and ensure every app functions cleanly with `Network` and `Sensors` revoked, inside secondary user profiles and Private Space.
- **NEVER** use dynamic code loading (`DexClassLoader`, `InMemoryDexClassLoader`, `PathClassLoader`, `Runtime.exec`).

## 3. Build, Toolchain & Dependency Hygiene

- **AGP 9.5+ Built-In Kotlin:** Do **NOT** apply `org.jetbrains.kotlin.android` in any Android module, and do **NOT** use `android.kotlinOptions {}` or `kapt`. Use AGP built-in Kotlin and KSP (`2.3.14+`).
- **Manual Constructor DI:** Use explicit `AppContainer` constructor wiring. Do **NOT** add Hilt, Dagger, or Koin.
- **Exact Pinned Versions Only:** All dependencies in `gradle/libs.versions.toml` and all GitHub Actions in `.github/workflows/` MUST be exact pinned versions (full 40-char commit SHAs for GitHub Actions). Zero dynamic `+` or range versions.
- **Reproducible Builds:** Keep `dependenciesInfo { includeInApk = false; includeInBundle = false }`, deterministic packaging, and `extractNativeLibs = false` in every application module.

## 4. Design System & Accessibility

- Use `:core:designsystem` (`ShkriussTheme` — monochrome stone/ink surfaces + Slate Blue accent, spacious minimal layout, system sans-serif font).
- Meet WCAG 2.2 AA contrast (≥4.5:1 normal text, ≥3:1 large text and UI components) in both Light and Dark themes.
- Enforce ≥48dp minimum touch targets, TalkBack semantics (`contentDescription`, `heading()`, `stateDescription`), localization-ready `strings.xml` (no hardcoded user-facing strings), and dynamic font scaling tested at 200%.
- Support both Phone (compact) and Tablet (expanded) adaptive layouts via Material 3 Adaptive.

## 5. Git & Mobile-Browser-First Workflow

- **NEVER** push directly to `main`, force-push, or bypass branch protection. Always work on a dedicated feature branch and open a Pull Request.
- **NEVER** commit signing keys, keystores, passwords, or `.env` files.
- **NEVER** claim a build or test passed unless observed passing in GitHub Actions CI logs.
- Write concise, plain-English code comments, commit messages, PR descriptions, and documentation legible in Vanadium on a mobile screen.
- Follow the detailed workspace rules in `.agents/rules/` and reusable workflows in `.agents/skills/`.
