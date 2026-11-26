---
name: scaffold-android-app
description: Scaffold a new pure-native Android app module under apps/<appname> following shkriuss GrapheneOS-first conventions.
---

# Skill: Scaffold Native Android App

Use this skill whenever adding a new app `dev.shkriuss.<appname>` to the monorepo.

## Checklist

1. **Create Module Directory:** `apps/<appname>/` and register `include(":apps:<appname>")` in `settings.gradle.kts`.
2. **Apply Convention Plugins Only:**
   - `id("shkriuss.android.application")`
   - `id("shkriuss.android.compose")`
   - `id("shkriuss.android.roborazzi")`
   - NEVER apply `org.jetbrains.kotlin.android` (AGP 9.5+ built-in Kotlin is enabled).
3. **Manifest Hardening (`src/main/AndroidManifest.xml`):**
   - `android:allowBackup="false"`
   - `android:dataExtractionRules="@xml/data_extraction_rules"` (excluding all Room databases and DataStore files)
   - `android:fullBackupContent="@xml/backup_rules"` (empty/exclude-all)
   - `android:usesCleartextTraffic="false"`
   - `android:enableOnBackInvokedCallback="true"`
   - Declare ZERO broad storage permissions and ZERO `INTERNET` permission unless optional self-hosted sync is implemented in the app.
4. **Architecture & DI:**
   - Create a manual constructor-wired `<AppName>Container` (no Hilt/Koin).
   - Wire `ShkriussTheme` from `:core:designsystem` and Material 3 Adaptive (`NavigationSuiteScaffold` / list-detail where relevant).
   - Store all user-facing strings in `res/values/strings.xml` (no hardcoded Compose strings).
5. **Testing & Screenshots:**
   - Add unit tests for domain logic and repositories.
   - Add Roborazzi screenshot tests covering Phone (compact) + Pixel Tablet (expanded) in Light, Dark, and 200% font scale.
   - Add Room migration tests if the app defines a Room database.
6. **Verify via CI:** Open a Draft PR on a feature branch and confirm `pr-verify.yml` passes completely green.
