---
name: grapheneos-audit
description: Audit Android modules, dependencies, manifests, and build configs against GrapheneOS hardening and OWASP MASVS v2.1.0.
---

# Skill: GrapheneOS & OWASP MASVS Audit

Run this checklist before marking any PR ready for review or cutting a release.

## Audit Checks

1. **Zero Proprietary Google Dependencies:**
   - Verify `gradle/libs.versions.toml` and all `build.gradle.kts` files contain zero `com.google.android.gms`, `com.google.firebase`, `com.google.android.play`, or analytics/crash-reporting SDKs.
2. **Zero WebView / Hybrid Frameworks:**
   - Confirm no `android.webkit.WebView`, `androidx.webkit`, `androidx.web`, React Native, Capacitor, Cordova, or Ionic references exist in `core/` or `apps/`.
3. **Hardening & Memory Safety:**
   - Confirm zero dynamic code loading (`DexClassLoader`, `InMemoryDexClassLoader`, `PathClassLoader`, `Runtime.exec`).
   - Confirm no unexpected native `.so` libraries are bundled without an accepted ADR.
   - Confirm `extractNativeLibs = false` in application modules.
4. **Storage & Crypto:**
   - Confirm zero usages of deprecated `androidx.security:security-crypto`.
   - Confirm sensitive records are AEAD-encrypted via `:core:crypto` with explicit AAD and StrongBox/TEE-wrapped keys.
   - Confirm zero logging of keys, passphrases, or plaintext user data.
5. **Permissions & Isolation:**
   - Confirm manifests contain zero `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE`, or `READ_MEDIA_*` permissions.
   - Confirm `allowBackup="false"`, `usesCleartextTraffic="false"`, and `exported="false"` on internal components.
   - Confirm graceful offline behavior when `Network` and `Sensors` are revoked.
6. **Reproducibility:**
   - Confirm `dependenciesInfo { includeInApk = false; includeInBundle = false }` and deterministic packaging flags on every app module.
