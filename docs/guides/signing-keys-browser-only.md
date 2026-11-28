# Browser-Only Android Signing Key Management

This guide explains how to generate, back up, store, verify, and rotate per-app Android signing keys strictly from **Vanadium** on GrapheneOS — without a desktop computer.

## Security Model & Honest Trade-Off

- Each app `dev.shkriuss.<appname>` gets **its own unique RSA-4096 PKCS#12 keystore** (`APK Signature Scheme v3.1`).
- Key generation runs inside `.github/workflows/keygen-signing.yml` on an ephemeral GitHub-hosted Linux runner using `/dev/shm` (RAM-backed tmpfs).
- **Zero Cleartext Dispatch Inputs:** Because `shkriuss/shkriuss.dev` is a public repository and GitHub `workflow_dispatch` inputs have no masked password type, passwords are **never** passed as workflow inputs. Instead, you place temporary secrets in the protected `release` Environment before running keygen.
- **Integrity-Protected Backup Cipher:** Keystores are encrypted in RAM with **OpenPGP SEIPD AES-256 + iterated salted SHA-512 S2K (`65,011,712` count)** (`<app>-release.p12.gpg`), verified via an in-RAM decrypt roundtrip, uploaded with `retention-days: 1`, and shredded from `/dev/shm`.
- **Trade-off to understand clearly:** While the keystore never touches git or unencrypted disk, it briefly exists in GitHub runner VM RAM during the ~15-second workflow execution. If you later obtain a trusted offline Linux machine, you can rotate to an offline-generated key seamlessly using APK Signature Scheme v3.1 (`apksigner rotate`).

## Step-by-Step: Generating a New App Signing Key

1. **Generate Two Distinct Strong Secrets (in your offline password manager, e.g., KeePassDX):**
   - `KEYSTORE_PASSWORD`: 24+ random characters (protects the PKCS#12 keystore itself).
   - `BACKUP_PASSPHRASE`: a **separate** 24+ random character passphrase (encrypts the `.p12.gpg` wrapper).
   - Save both in your offline password manager **before** proceeding.
2. **Stage Temporary Keygen Secrets in the Protected `release` Environment:**
   - In Vanadium, open **Settings -> Environments -> `release` -> Environment secrets**.
   - Add `KEYGEN_KEYSTORE_PASSWORD` and `KEYGEN_BACKUP_PASSPHRASE`.
3. **Run the Keygen Workflow in Vanadium:**
   - Open `github.com/shkriuss/shkriuss.dev/actions/workflows/keygen-signing.yml`.
   - Tap **Run workflow** on branch `main`, enter `app_name` (e.g., `notes`), tap **Run workflow**, and approve the `release` environment deployment gate.
4. **Record the Public Certificate Fingerprint & Download Backup:**
   - Copy the printed **Public SHA-256 Certificate Fingerprint** from the run summary (publish this to `site/public/verify.html` via PR).
   - Download `<app_name>-signing-backup-encrypted` and save `<app_name>-release.p12.gpg` + `<app_name>-release.p12.gpg.b64` inside your offline encrypted vault.
5. **Save Per-App Signing Secrets & Clean Up Immediately:**
   - In **Settings -> Environments -> `release`**, add the permanent per-app secrets (e.g., for `notes`):
     - `SIGNING_NOTES_GPG_B64`: paste the single-line contents of `notes-release.p12.gpg.b64` (never decrypted on your phone!).
     - `SIGNING_NOTES_GPG_PASSPHRASE`: your `BACKUP_PASSPHRASE`.
     - `SIGNING_NOTES_KEYSTORE_PASSWORD`: your `KEYSTORE_PASSWORD`.
   - Delete the temporary `KEYGEN_KEYSTORE_PASSWORD` and `KEYGEN_BACKUP_PASSPHRASE` secrets.
   - Open the `keygen-signing.yml` workflow run -> `...` -> **Delete workflow run**.

## Verifying Signed APKs on GrapheneOS

1. Install **AppVerifier** on GrapheneOS.
2. Select the downloaded APK or installed `dev.shkriuss.<appname>` app.
3. Confirm the displayed SHA-256 signing certificate digest matches `https://shkriuss.dev/verify.html` character-for-character.
