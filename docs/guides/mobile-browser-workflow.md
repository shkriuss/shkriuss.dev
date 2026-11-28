# Mobile-Browser-First Workflow Guide

Everything in `shkriuss/shkriuss.dev` is designed to be operated entirely from **Vanadium** on a GrapheneOS phone or tablet — with no laptop, desktop computer, local terminal, or Android Studio.

## Daily Development Loop

1. **Plan & Implement (Antigravity Remote Agents):**
   - Open `antigravity.google.com` in Vanadium.
   - Start complex tasks with `/plan` (or `/grill-me` for design alignment) before requesting code changes.
   - Antigravity writes code on a feature branch (never `main`) and opens a **Draft Pull Request**.
2. **Review Automated CI & UI Screenshots (GitHub Web):**
   - Open the Pull Request on `github.com/shkriuss/shkriuss.dev/pulls` in Vanadium.
   - Wait for the **`PR Verify`** workflow (`pr-verify.yml`) to turn green.
   - Tap the PR summary comment posted by CI or open the **Checks -> PR Verify -> Artifacts** section to download:
     - `roborazzi-screenshots`: Rendered PNG screenshots of Phone (compact), Pixel Tablet (expanded), Light, Dark, and 200% font scale screens.
     - `debug-apks` (once app modules are added): Installable debug APKs you can sideload directly onto your GrapheneOS phone/tablet for hands-on testing.
3. **Security Audit & Merge:**
   - Before marking a sensitive PR ready for review, ask Antigravity (using **Gemini 4 Carbon**) to run a targeted audit against `.agents/skills/grapheneos-audit/SKILL.md`.
   - Tap **Ready for review** and **Squash and merge** on GitHub once all required status checks pass.

## Vanadium Tips & Honest Caveats

- **JIT Restrictions:** Vanadium disables JavaScript JIT by default on GrapheneOS. `antigravity.google.com`, `github.com`, and `dash.cloudflare.com` all work without enabling JIT, though very large GitHub diff pages may scroll slower — collapse individual file diffs or review commit-by-commit on mobile.
- **Desktop Site Toggle:** Some Cloudflare dashboard pages (such as API token creation or DNS records) and GitHub Actions manual dispatch dialogs are easier to navigate if you rotate to landscape or enable **Desktop site** in Vanadium's menu (or use a tablet).
- **Artifact ZIP Extraction:** GitHub Actions downloads artifacts as `.zip` archives. GrapheneOS's built-in **Files** app (or a trusted FOSS archive viewer) can extract PNG screenshots and APKs directly on-device.
- **Branch Protection:** Because `antigravity.google.com` mobile web UI does not expose Desktop-only `Plan Review Policy` or fine-grained `Deny` rules, branch safety is enforced directly by GitHub's **`main` Branch Ruleset**, `AGENTS.md`, `.agents/rules/`, and `.agents/hooks.json`.

## Updating Dependency Verification Metadata (No PC Needed)

Whenever dependencies in `gradle/libs.versions.toml` change:
1. Open **Actions -> Update Verification Metadata** (`update-verification-metadata.yml`) on GitHub.
2. Tap **Run workflow** on your feature branch.
3. Download the generated `verification-metadata.xml` artifact (or let Antigravity fetch it via `gh run download`) and commit it to the PR.
