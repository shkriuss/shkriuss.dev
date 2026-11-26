# Threat Model — shkriuss.dev Android Suite

- **Baseline Standard:** OWASP MASVS v2.1.0
- **Primary Environment:** GrapheneOS (Android 17+) with Exploit Protection Compatibility Mode OFF
- **Last Reviewed:** 2026-11-24

## 1. Assets to Protect

1. **Personal App Data:** Financial budgets, trade logs, notes, meal plans, personal reminders/tasks, vehicle maintenance records, item inventories, private photos, and personal files.
2. **Cryptographic Keys:** Android Keystore KEKs (StrongBox/TEE), Tink DEK keysets, user backup/sync passphrases, and derived Argon2id keys.
3. **User Privacy & Metadata:** Absence of network telemetry, device identifiers, behavioral analytics, or unrequested sensor/storage access.
4. **Release Integrity:** Android app signing keys and reproducible APK artifacts.

## 2. Trust Boundaries

- **Trusted:** User's unlocked GrapheneOS device hardware (Titan M2 / StrongBox / TEE), GrapheneOS OS & sandbox, user's explicitly chosen offline storage locations.
- **Untrusted / Zero-Knowledge:** Optional self-hosted Cloudflare sync backend (must see ciphertext and minimal opaque sync metadata only), public Wi-Fi / cellular networks, other apps installed on the device, third-party GitHub / Cloudflare / Antigravity infrastructure (used strictly for development, CI, and static hosting — never runtime trust for user plaintext).

## 3. Threat Analysis & Mitigations

| ID | Threat | Attack Vector | Mitigation |
| --- | --- | --- | --- |
| T1 | **Local Data Exposure on Compromised Storage / Backup** | Physical device theft, accidental cloud backup, or ADB/file extraction | Application-layer Tink `AES-256-GCM` AEAD with StrongBox-backed KEK; `allowBackup="false"` and strict `dataExtractionRules`; optional biometric/PIN app lock and `FLAG_SECURE`. |
| T2 | **Ciphertext Substitution / Cross-Record Tampering** | Swapping encrypted blobs between rows, tables, or apps | Mandatory AAD binding `appId`, `entityType`, `recordId`, and `schemaVersion` on every AEAD encrypt/decrypt call. |
| T3 | **Memory Corruption / Native Code Exploits** | Crafted files, SQLite queries, or native parser vulnerabilities | Pure Kotlin/JVM core with zero bundled C/C++ `.so` libraries; OS hardened SQLite; runs with GrapheneOS `hardened_malloc`, ARM MTE, and DCL restrictions enabled. |
| T4 | **Silent Telemetry or Network Exfiltration** | Third-party SDK phone-home or accidental cleartext traffic | Zero GMS/Firebase/analytics dependencies; `INTERNET` omitted from offline-only apps; `usesCleartextTraffic="false"`; static CI check fails build if forbidden deps or permissions appear. |
| T5 | **Broad Storage or Sensor Over-Reach** | Malicious or buggy access to unrelated photos/files or sensors | Zero `READ_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE` / `READ_MEDIA_*` permissions; SAF + Photo Picker only; tested with `Network` and `Sensors` revoked. |
| T6 | **Weak Backup Encryption / Offline Brute Force** | Stolen `.shkriuss` backup archive attacked with GPUs | Pure-JVM Argon2id KDF (≥ 64 MiB memory, ≥ 3 iterations, 128-bit random salt) + Tink Streaming AEAD (`AES-256-GCM-HKDF`) with authenticated header. |
| T7 | **CI / Supply-Chain Compromise** | Poisoned GitHub Action, tampered Gradle wrapper, or compromised dependency | Full 40-char SHA pinning for all GitHub Actions; `permissions: {}` default; `gradle/actions/wrapper-validation` + `distributionSha256Sum`; exact pinned versions + verification metadata workflow; protected `release` Environment with manual approval; SLSA Level 3 provenance attestations; reproducible APKs. |
| T8 | **Signing Key Theft or Loss** | Leaked keystore in git or lost key preventing updates | `.gitignore` blocks keystore extensions; keys generated in `/dev/shm` RAM, symmetrically encrypted before artifact upload, backed up offline by maintainer, stored only in protected `release` Environment secrets; one unique key per app with APK Signature Scheme v3.1 rotation support. |
| T9 | **Malicious Browser/Web Attack Surface** | WebView RCE or JS exploit inside native apps or static site | Zero WebView in any Android app; `site/` is plain static HTML5/CSS3 with zero JavaScript and strict Cloudflare security headers. |

## 4. Residual Risks & Honest Limits

- **Unlocked Device Compromise:** If an attacker has full interactive access to an already unlocked device and knows the optional app lock credential, they can view decrypted data in the UI.
- **Ephemeral CI Signing & Keygen:** Because the maintainer has no desktop computer, key generation (`keygen-signing.yml`) and release signing (`release-app.yml`) execute inside GitHub-hosted ephemeral Linux runners. Users who want zero CI trust can compile the GPL-3.0-or-later source code themselves and sign with their own key.
- **Future On-Device Grammar NLP:** When `apps/grammar` introduces an on-device grammar checker, any native Rust/WASM/ML engine must undergo a dedicated ADR and ARM MTE verification on GrapheneOS before release.
