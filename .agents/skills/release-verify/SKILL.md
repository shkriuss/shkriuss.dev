---
name: release-verify
description: Verify reproducible release APK builds, signing fingerprints, SLSA provenance, and shkriuss.dev verification metadata.
---

# Skill: Release & Reproducibility Verification

Use this skill when preparing an app release (`<app>-vX.Y.Z`) from a mobile browser.

## Release Checklist

1. **Pre-Release Checks:**
   - Confirm `main` CI is green and `grapheneos-audit` passes with zero violations.
   - Confirm `versionCode` (strictly monotonic integer) and `versionName` (`MAJOR.MINOR.PATCH`) are updated in the app module.
   - Confirm changelog entry is written in plain English.
2. **Protected Release Build:**
   - Trigger `.github/workflows/release-app.yml` via GitHub web UI (`Actions` -> `Run workflow`), approve the protected `release` environment deployment gate.
3. **Artifact & Attestation Verification:**
   - Confirm the workflow produced:
     - Signed release APK (`APK Signature Scheme v3.1`, zero `dependenciesInfo` signing block)
     - `SHA256SUMS.txt`
     - GitHub SLSA Build Level 3 provenance attestation (`actions/attest-build-provenance`)
   - Confirm the APK signing certificate SHA-256 matches the published fingerprint on `shkriuss.dev/verify.html` (verifiable on-device with GrapheneOS's **AppVerifier** app).
4. **Catalog Update:**
   - Update `site/index.html` and `site/verify.html` via PR with the new release link and verified certificate SHA-256 fingerprint (never auto-commit from release CI without review).
