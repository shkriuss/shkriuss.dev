# ADR 0001: Pure Native Android & GrapheneOS-First Platform Boundary

- **Status:** Accepted
- **Date:** 2026-11-24

## Context

The shkriuss suite consists of independent personal productivity and privacy-sensitive apps (`dev.shkriuss.<appname>`). The primary target device environment is **GrapheneOS** (Android 17 / API 37+) on phones and tablets with strict exploit protections enabled, zero Google Play services, and least-privilege permissions.

## Decision

1. **Pure Native Android Only:** Every app is built exclusively with **Kotlin**, **Jetpack Compose**, and standard Android SDK / open-source AndroidX libraries.
2. **SDK Targets:**
   - `minSdk = 33` (Android 13 — ensures modern Photo Picker, runtime `POST_NOTIFICATIONS`, and clean Keystore/StrongBox APIs without legacy compatibility bloat).
   - `compileSdk = 37.1` (Android 17 QPR1 — required by Jetpack Compose BOM `2026.11.01`).
   - `targetSdk = 37` (Android 17 `CINNAMON_BUN` — enforces full adaptive large-screen resizability and strict local-network/background restrictions).
3. **GrapheneOS Hardening Compatibility:** Every app must run reliably with Exploit Protection Compatibility Mode **OFF** (`hardened_malloc`, ARM MTE, memory/storage Dynamic Code Loading restrictions, WebView JIT restrictions, native debugging disabled).
4. **Zero Proprietary Google Dependencies:** No GMS, Firebase, FCM, Play Integrity, Play Core, analytics, or ads.
5. **Least-Privilege Permissions & Profile Isolation:**
   - Use Storage Access Framework (SAF) and the system Photo Picker (`PickVisualMedia`); never request broad storage permissions.
   - Support revoked `Network` and `Sensors` permissions without crashes, hangs, or retry loops.
   - Behave cleanly in secondary user profiles and Private Space.
6. **Dependency Injection:** Use manual constructor DI (`AppContainer`) per app for transparency, zero reflection, zero code-generation overhead, and simpler auditing.
7. **Static Site Boundary:** `shkriuss.dev` (`site/`) is strictly static HTML5/CSS3 for documentation, APK downloads, and signing-fingerprint verification — never a web app or PWA.

## Rejected Alternatives

- **WebView / PWA / Capacitor / React Native / Flutter:** Rejected because embedded browser engines conflict with GrapheneOS WebView JIT restrictions, expand the native memory-corruption attack surface, break native TalkBack/adaptive semantics, and violate the pure-native requirement.
- **Hilt / Koin:** Rejected to keep compile-time complexity low, avoid reflection/service-locator ambiguity, and keep the dependency tree small and auditable.
- **Lowering `minSdk` below 33:** Rejected because GrapheneOS only supports current Pixel/modern devices running recent Android versions, and supporting legacy external storage / pre-33 notification models adds dead security-risk code.

## Consequences

- Every app starts with a minimal, auditable permission footprint and zero hidden network calls.
- Future native-code needs (such as Rust-based on-device NLP for `Grammar`) require a dedicated ADR demonstrating ARM MTE / `hardened_malloc` safety and reproducible cross-compilation.
