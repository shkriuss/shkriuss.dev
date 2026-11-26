# GEMINI.md

Follow all mandatory rules in [AGENTS.md](AGENTS.md) and `.agents/rules/`.

Quick reminders before making any change:
1. Pure native Android only (Kotlin + Jetpack Compose) in `core/` and `apps/`; plain static HTML/CSS only in `site/`.
2. GrapheneOS-first: zero GMS/Firebase, zero WebView, zero `androidx.security:security-crypto`, zero broad storage permissions, zero dynamic code loading.
3. AGP 9.5+ built-in Kotlin: never apply `org.jetbrains.kotlin.android`; use KSP and manual constructor DI (`AppContainer`).
4. Work only on feature branches and verify every change via GitHub Actions CI (`pr-verify.yml`) before claiming completion.
5. Remember the maintainer works strictly from a mobile/tablet browser (Vanadium on GrapheneOS) with no desktop computer.
