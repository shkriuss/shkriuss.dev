---
trigger: glob
globs:
  - ".github/**"
  - "gradle/**"
  - "build-logic/**"
  - "**/build.gradle.kts"
description: Enforce GitHub Actions SHA pinning, least privilege, and reproducible Gradle builds.
---

# Rule: CI & Supply-Chain Security

1. Pin every third-party GitHub Action to a full 40-character commit SHA with a trailing version comment (e.g., `# v7.0.1`). Never pin only by mutable tag (`@v4`, `@main`).
2. Declare top-level `permissions: {}` in every workflow and grant minimal job-level permissions explicitly.
3. Never interpolate untrusted PR inputs (`github.event.pull_request.title`, branch names, commit messages) directly inside `run:` shell scripts. Pass values via `env:` variables.
4. Run `gradle/actions/wrapper-validation` on every workflow that invokes `./gradlew`, and maintain `distributionSha256Sum` + `validateDistributionUrl=true` in `gradle/wrapper/gradle-wrapper.properties`.
5. Pin exact library and plugin versions in `gradle/libs.versions.toml` (zero `+` ranges or snapshots).
6. Never commit release keystores, key passwords, or Cloudflare tokens to git. Gate release signing and site deployments behind protected GitHub Environments (`release`, `site-deploy`).
7. Keep APK builds reproducible: `dependenciesInfo { includeInApk = false; includeInBundle = false }`, deterministic ZIP/archive ordering, and SLSA build provenance attestation (`actions/attest-build-provenance`) on release builds.
