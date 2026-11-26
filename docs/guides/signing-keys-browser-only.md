# Browser-Only Android Signing Key Management

This guide explains how to generate, back up, store, verify, and rotate per-app Android signing keys strictly from **Vanadium** on GrapheneOS — without a desktop computer.

## Security Model & Honest Trade-Off

- Each app `dev.shkriuss.<appname>` gets **its own unique RSA-4096 PKCS#12 keystore** (`APK Signature Scheme v3.1`).
- Key generation runs inside `.github/workflows/keygen-signing.yml` on an ephemeral GitHub-hosted Linux runner using `/dev/shm` (RAM-backed tmpfs), encrypts the keystore with AES-256-CBC + PBKDF2-HMAC-SHA256 (`600,000` iterations) using a passphrase you enter at dispatch time, uploads **only** the encrypted `.p12.enc` artifact (`retention-days: 1`), and prints **only** the public SHA-256 certificate fingerprint.
- **Trade-off to understand clearly:** While the keystore is never written to git and never uploaded unencrypted, it briefly exists in GitHub runner VM RAM during the ~15-second workflow execution. If you ever obtain a trusted offline Linux machine later, you can rotate to an offline-generated key seamlessly using APK Signature Scheme v3.1 (`apksigner rotate`).

## Step-by-Step: Generating a New App Signing Key

1. **Prepare Strong Passwords (in your offline password manager, e.g., KeePassDX):**
   - `KEYSTORE_PASSWORD`: 24+ random characters (protects the `.p12` keystore itself).
   - `BACKUP_ENCRYPTION_PASSPHRASE`: a **separate** 24+ random character passphrase (encrypts the `.p12.enc` download artifact).
   - Store both safely in your password manager **before** running the workflow.
2. **Run the Keygen Workflow in Vanadium:**
   - Open `github.com/shkriuss/shkriuss.dev/actions/workflows/keygen-signing.yml`.
   - Tap **Run workflow**.
   - Fill in:
     - `app_name`: e.g. `notes` (lowercase letters only)
     - `keystore_password`: paste your generated `KEYSTORE_PASSWORD`
     - `backup_passphrase`: paste your generated `BACKUP_ENCRYPTION_PASSPHRASE`
   - Tap **Run workflow** and wait ~20 seconds for it to finish.
3. **Record the Public Certificate Fingerprint:**
   - Open the completed workflow run summary.
   - Copy the printed **SHA-256 Certificate Fingerprint** (uppercase colon-separated hex). You will publish this exact fingerprint on `shkriuss.dev/verify.html`.
4. **Download & Back Up the Encrypted Keystore:**
   - Under **Artifacts**, download `<app_name>-signing-backup-encrypted` (contains `<app_name>-release.p12.enc` and `<app_name>-release.base64.enc`).
   - Save the encrypted file inside your encrypted offline backup vault / password manager attachment.
5. **Delete the Workflow Run Immediately:**
   - On the workflow run page, tap the `...` menu -> **Delete workflow run** (this removes the artifact and dispatch metadata immediately rather than waiting 24 hours).
6. **Configure Protected GitHub Environment Secrets (`release`):**
   - Go to **Settings -> Environments -> `release` -> Add environment secret**.
   - Add per-app secrets (never repository-wide secrets, so PR workflows cannot read them):
     - `SIGNING_KEYSTORE_<APP>_B64` (only when you later configure release signing; keep decrypted base64 out of browser history — see note below)
     - `SIGNING_KEYSTORE_<APP>_PASSWORD`
     - `SIGNING_KEY_<APP>_ALIAS` (e.g., `shkriuss-<app>`)

> **Important Browser-Only Note on Populating `SIGNING_KEYSTORE_<APP>_B64`:**
> To decrypt `<app_name>-release.base64.enc` purely on GrapheneOS without a PC, you can either run OpenSSL inside an offline local shell app (such as Termux installed offline) OR — simpler and zero extra apps — have `release-app.yml` accept the **encrypted** `.p12.enc` base64 directly as `SIGNING_KEYSTORE_<APP>_ENC_B64` alongside `SIGNING_KEYSTORE_<APP>_ENC_PASSPHRASE` inside the protected `release` Environment! That way, you NEVER even have to decrypt the keystore on your phone to paste it into GitHub Secrets: you just upload/paste the already-encrypted base64 directly from the downloaded artifact!
>
> `release-app.yml` supports this **double-encrypted secret mode** natively (`SIGNING_KEYSTORE_ENC_B64` + `SIGNING_KEYSTORE_ENC_PASSPHRASE` + `SIGNING_KEYSTORE_PASSWORD`), making the entire lifecycle 100% Vanadium-only with zero extra phone apps!

## Verifying Signed APKs on GrapheneOS

1. Install **AppVerifier** (from Accrescent or GrapheneOS's recommended sources).
2. Share or select the installed `dev.shkriuss.<appname>` app or downloaded APK in AppVerifier.
3. Compare the displayed **SHA-256 signing certificate digest** character-for-character against `https://shkriuss.dev/verify.html`.
