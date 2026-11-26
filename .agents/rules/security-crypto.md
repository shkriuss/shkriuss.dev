---
trigger: always_on
description: Enforce OWASP MASVS v2.1.0 storage, cryptography, and key-handling standards.
---

# Rule: Storage & Cryptography Standards

1. NEVER use deprecated `androidx.security:security-crypto` (`EncryptedSharedPreferences`, `EncryptedFile`, `MasterKey`).
2. Use `:core:crypto` for all cryptographic operations:
   - Authenticated encryption: Google Tink AEAD (`AES256_GCM`) with explicit Associated Authenticated Data (AAD) binding record type, app ID, and schema version.
   - Large files & backups: Google Tink Streaming AEAD (`AES256_GCM_HKDF_4KB` / `AES256_GCM_HKDF_1MB`) so plaintext is never loaded wholly into RAM.
   - Device key wrapping: Android Keystore KEK backed by StrongBox (`setIsStrongBoxBacked(true)`) with clean fallback to hardware TEE when StrongBox is unavailable.
   - Passphrase key derivation (backups & optional sync): pure-JVM Argon2id via Bouncy Castle (`Argon2BytesGenerator`) with OWASP-compliant parameters (at least 64 MiB RAM, 3 iterations, parallelism 1 for interactive mobile unlock, parameterized in the versioned header).
3. Never bundle C/C++ SQLCipher or other native `.so` libraries without a reviewed ADR. Store sensitive record payloads as AEAD-encrypted blobs inside Room (backed by GrapheneOS's hardened OS SQLite) and store preferences in Tink-encrypted DataStore.
4. Zeroize temporary raw key material and passphrase byte/char arrays immediately after use (`SecureByteArray` / `Arrays.fill`).
5. Never log plaintext user content, encryption keys, passphrases, sync tokens, or PII (even in debug builds).
6. For any screen showing financial, journal, or private vault data, support optional `FLAG_SECURE` and optional biometric/device-credential app lock.
