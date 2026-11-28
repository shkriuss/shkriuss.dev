# ADR 0002: Local Storage & Cryptography Architecture

- **Status:** Accepted
- **Date:** 2026-11-24

## Context

Personal data across Budget, Notes, Calendar, Trades, Repairs, Drive, and Photos must be encrypted at rest with hardware-backed keys, exportable as end-to-end encrypted (E2EE) backups via SAF, and compatible with GrapheneOS's strictest memory-safety mitigations (`hardened_malloc`, ARM MTE) without introducing unmaintained or vulnerable C/C++ binaries into APKs.

## Decision

1. **Zero Bundled C/C++ Databases in Core:** Use **Jetpack Room (`2.8.5`)** backed by GrapheneOS's own hardened system SQLite rather than bundling SQLCipher's native C library into every APK.
2. **Application-Layer Authenticated Encryption:**
   - Encrypt sensitive record payloads and settings using **Google Tink (`1.24.0`, `tink-android`)** with `AES-256-GCM` AEAD.
   - Always bind explicit **Associated Authenticated Data (AAD)** containing app ID, entity/domain name, record ID, and schema version to prevent ciphertext substitution across records or tables.
   - Wrap Tink keysets using an **Android Keystore** Key Encryption Key (KEK) backed by **StrongBox** (`setIsStrongBoxBacked(true)`) where hardware supports it, with graceful fallback to hardware TEE.
   - **Never** use deprecated `androidx.security:security-crypto`.
3. **Encrypted Preferences:** Store app preferences and lightweight metadata in **Jetpack DataStore (`1.3.0`)** encrypted through a Tink AEAD serializer in `:core:storage`.
4. **Streaming Encryption for Large Files & Backups:** Use Tink Streaming AEAD (`AES256_GCM_HKDF_4KB` / `1MB` segments) so large files (`Drive`, `Photos`, `.shkriuss` backup archives) stream directly through SAF without buffering plaintext in RAM.
5. **Passphrase Key Derivation:** Use pure-JVM **Argon2id** via Bouncy Castle (`org.bouncycastle:bcprov-jdk18on:1.87`, shrunk by R8) for user-passphrase backups and future zero-knowledge self-hosted sync key derivation. Store KDF parameters (version, salt, memory KiB, iterations, parallelism) in the authenticated header so parameters can be upgraded over time.
6. **Optional App Lock:** Support optional biometric / device-credential lock (`androidx.biometric`) and optional `FLAG_SECURE` screen protection per app.

## Rejected Alternatives

- **SQLCipher (`net.zetetic:sqlcipher-android`):** Rejected as default across all 13 apps because it bundles ~4–6 MB of C/C++ OpenSSL + SQLite native `.so` binaries per APK, enlarges the MTE/memory-safety attack surface, and complicates reproducible builds.
- **`androidx.security:security-crypto`:** Rejected because Google officially deprecated the library.
- **Plain unencrypted Room relying solely on Android File-Based Encryption (FBE):** Rejected because application-layer AEAD + StrongBox KEK provides defense-in-depth aligned with OWASP MASVS-STORAGE and enables clean ciphertext reuse for E2EE backups and zero-knowledge sync.

## Consequences

- Every core app ships zero custom C/C++ `.so` binaries, staying naturally safe under ARM MTE and `hardened_malloc`.
- Non-sensitive sort/filter indices (such as timestamps or opaque bucket IDs) must be consciously reviewed per app to avoid leaking sensitive metadata in SQLite columns; full-text search across encrypted notes is handled in memory or via an encrypted local search index.
- Room database schemas must be exported to JSON and covered by `MigrationTestHelper` tests; `fallbackToDestructiveMigration()` is forbidden in release builds.
