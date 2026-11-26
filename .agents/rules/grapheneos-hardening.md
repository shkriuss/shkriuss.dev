---
trigger: always_on
description: Enforce GrapheneOS hardening, zero GMS, offline-first, and least-privilege permissions.
---

# Rule: GrapheneOS-First Hardening

1. Target `minSdk = 33`, `compileSdk = 37.1` (Android 17 QPR1), and `targetSdk = 37` (Android 17).
2. All apps MUST run cleanly with GrapheneOS Exploit Protection Compatibility Mode **OFF**:
   - Compatible with `hardened_malloc` and ARM Hardware Memory Tagging Extension (MTE).
   - Zero Dynamic Code Loading from memory or storage (`DexClassLoader`, `InMemoryDexClassLoader`, `PathClassLoader`, `System.load` of writable files).
   - Zero WebView or JIT assumptions.
   - Zero reliance on native debugging (`ptrace`).
3. Zero proprietary Google runtime dependencies: forbid `com.google.android.gms`, `com.google.firebase`, `com.google.android.play`, trackers, analytics, and crash-reporting SDKs.
4. Zero broad storage permissions: forbid `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`. Always use Storage Access Framework (SAF) and the system Photo Picker (`PickVisualMedia`), and respect Storage Scopes / Contact Scopes.
5. Offline-first by default: never crash, hang, or spin retry loops when `Network` or `Sensors` permissions are revoked, or when running inside a secondary user profile or Private Space.
6. Set `android:allowBackup="false"`, `android:dataExtractionRules` excluding all local databases/keysets, and `android:usesCleartextTraffic="false"` in every app manifest.
