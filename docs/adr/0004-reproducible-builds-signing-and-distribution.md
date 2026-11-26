# ADR 0004: Reproducible Builds, Browser-Only Signing Keys, and Distribution

- **Status:** Accepted
- **Date:** 2026-11-24

## Context

The project maintainer operates entirely from **Vanadium** on a GrapheneOS phone/tablet with no desktop computer, local terminal, or local Android Studio. Releases must remain reproducible, independently verifiable, free of Google identity lock-in, and straightforward to install and update on GrapheneOS.

## Decision

1. **Android Developer Verification Stance (2026–2027 Rollout):**
   - Remain **unregistered** with Google's Android Developer Verification program so the project has zero runtime or identity dependency on Google.
   - **GrapheneOS** does not enforce Play Protect / Google Developer Verification and installs signed APKs normally.
   - For **stock / certified Android** users affected by the 2026–2027 rollout, `shkriuss.dev/verify.html` clearly documents Google's official power-user Advanced Install Flow (24-hour waiting period / explicit user override) and APK verification steps.
2. **One Unique Signing Key Per App (APK Signature Scheme v3.1):**
   - Each app `dev.shkriuss.<appname>` uses its own dedicated RSA-4096 PKCS#12 keystore, preventing cross-app blast radius and enabling independent key rotation via APK Signature Scheme v3.1.
3. **Browser-Only Key Generation (`keygen-signing.yml`):**
   - Keys are generated on demand via a hardened manual `workflow_dispatch` GitHub Actions workflow running inside an ephemeral Linux runner's RAM filesystem (`/dev/shm`), encrypted symmetrically with OpenSSL AES-256-CBC + PBKDF2-HMAC-SHA256 (`600,000` iterations) using a strong passphrase supplied at dispatch time, and uploaded solely as an encrypted `.p12.enc` artifact (`retention-days: 1`).
   - Only the **public** SHA-256 certificate fingerprint is printed in logs. The maintainer downloads the encrypted backup directly in Vanadium, stores it in an offline password manager / encrypted backup, stores the base64 keystore and credentials as GitHub Secrets inside the protected `release` Environment, and immediately deletes the keygen workflow run.
   - **Honest Trade-Off:** Generating inside GitHub-hosted ephemeral CI briefly exposes key material inside GitHub's runner VM memory during that single execution (unlike an air-gapped desktop HSM). This is an intentional, documented trade-off for a zero-computer workflow; when hardware becomes available in the future, APK Signature Scheme v3.1 allows seamless key rotation to an offline-generated key.
4. **Reproducible Builds & SLSA Provenance:**
   - Every app sets `dependenciesInfo { includeInApk = false; includeInBundle = false }`, deterministic ZIP/archive ordering, pinned Gradle/AGP/library versions, and generates GitHub SLSA Build Level 3 provenance attestations (`actions/attest-build-provenance`).
5. **Distribution Strategy:**
   - **Primary:** **GitHub Releases** paired with **Obtainium** on GrapheneOS (zero extra server maintenance, direct SHA-256 + AppVerifier fingerprint checks) plus a **self-hosted F-Droid-compatible static repository** on `repo.shkriuss.dev` / `shkriuss.dev` in a later phase.
   - **Secondary (Future):** Optional submission to **Accrescent** or official **F-Droid** once core apps reach stable 1.0 releases.
   - All public signing-certificate SHA-256 fingerprints are published on `shkriuss.dev/verify.html` for verification with GrapheneOS's **AppVerifier**.

## Rejected Alternatives

- **Registering with Google Developer Verification:** Rejected because it requires submitting government ID and legal identity metadata to Google, conflicting with the project's trust boundary.
- **Sharing one signing key across all 13 apps:** Rejected because compromising or losing one key would impact every app in the suite.
- **Unprotected automatic signing on every push:** Rejected; release signing requires manual approval through the protected `release` GitHub Environment.

## Consequences

- Every release can be triggered, approved, downloaded, verified with AppVerifier, and updated from a GrapheneOS phone or tablet browser.
- Third parties can clone the repository at a release tag, build the unsigned APK reproducibly, and compare bytecode/resources against the signed release APK.
