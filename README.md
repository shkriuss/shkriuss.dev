# shkriuss.dev

An open-source, self-hostable suite of personal **pure native Android apps** (`dev.shkriuss.<appname>`), built GrapheneOS-first with zero proprietary Google dependencies, plus the plain static documentation and downloads hub at [shkriuss.dev](https://shkriuss.dev).

## Non-Negotiable Principles

- **Pure Native Android Only:** Every app is written in Kotlin with Jetpack Compose and the standard Android SDK. No web apps, no PWAs, no WebView UIs, and no cross-platform web wrappers.
- **GrapheneOS-First Hardening:** All apps are designed to run reliably with Exploit Protection Compatibility Mode **OFF** (`hardened_malloc`, ARM MTE, memory/storage DCL restrictions, native debugging blocked) on Android 17+ (`minSdk = 33`, `compileSdk = 37.1`, `targetSdk = 37`).
- **Zero Proprietary Runtime Dependencies:** No Google Play services (GMS), no Firebase/FCM, no Play Integrity, no ads, no trackers, and no analytics.
- **Local-First & Offline-First:** Every app works fully with the `Network` and `Sensors` permissions revoked, inside secondary user profiles and Private Space, and using only least-privilege Storage Access Framework (SAF) and the system Photo Picker.
- **Strong Cryptography, Zero Bundled C/C++:** Room over GrapheneOS's hardened OS SQLite, Google Tink AEAD (`AES-256-GCM`) and Streaming AEAD (`AES-256-GCM-HKDF`) backed by Android Keystore / StrongBox (TEE fallback), Tink-encrypted DataStore, and pure-JVM Argon2id via Bouncy Castle for user-passphrase E2EE backups.
- **Mobile-Browser-First Workflow:** Designed so coding, code review, UI screenshot inspection, release signing, and deployment can be performed entirely from Vanadium on a GrapheneOS phone or tablet via Google Antigravity Remote Agents, GitHub, and Cloudflare.

## Planned Apps

| App | Package ID | Purpose |
| --- | --- | --- |
| Budget | `dev.shkriuss.budget` | Plan, track, and manage personal budgets with history |
| Calendar | `dev.shkriuss.calendar` | Personal calendar with Georgian Orthodox Church & public holidays of Georgia |
| Drive | `dev.shkriuss.drive` | Store and organize personal files |
| Grammar | `dev.shkriuss.grammar` | On-device English grammar, spelling, and fluency checker |
| Meals | `dev.shkriuss.meals` | Plan and organize meals, meal times, and portions |
| Notes | `dev.shkriuss.notes` | Create, store, and organize personal notes |
| Photos | `dev.shkriuss.photos` | Store and organize personal photos |
| Reminders | `dev.shkriuss.reminders` | Create and organize reminders with on-device notifications |
| Repairs | `dev.shkriuss.repairs` | Track vehicle repair and maintenance history |
| Shelf | `dev.shkriuss.shelf` | Track personal items, electronics, brands, and home accessories |
| Tasks | `dev.shkriuss.tasks` | Task lists with on-device notifications and history |
| Trades | `dev.shkriuss.trades` | Record, store, and review financial trades |
| Water | `dev.shkriuss.water` | Log water intake with on-device drinking reminders |

## Monorepo Layout

```text
├── build-logic/          # Gradle 9.9 / AGP 9.5 convention plugins (built-in Kotlin, Compose, Roborazzi)
├── core/
│   ├── common/           # AppResult, dispatchers, Clock, HLC, integer minor-unit math
│   ├── designsystem/     # Shared Slate Blue Material 3 Adaptive theme & UI components + Roborazzi tests
│   ├── crypto/           # Android Keystore/StrongBox, Tink AEAD & Streaming AEAD, pure-JVM Argon2id
│   ├── storage/          # Authenticated encrypted envelope & Tink-backed DataStore wrapper
│   ├── backup/           # Versioned E2EE .shkriuss backup format (Argon2id + Streaming AEAD)
│   ├── notifications/    # On-device alarm & boot-reschedule contracts (zero FCM)
│   └── testing/          # Shared Roborazzi phone/tablet/200%-font specs and test helpers
├── apps/                 # Independent native Android apps (added incrementally per phase)
├── site/                 # Plain static HTML/CSS hub for shkriuss.dev (zero JS, zero web-app features)
├── docs/                 # ADRs, OWASP MASVS v2.1.0 mapping, threat model, browser-only guides
├── .agents/              # Antigravity workspace rules, skills, and safety hooks
└── .github/workflows/    # SHA-pinned CI verification, site deploy, signing keygen, reproducible releases
```

## Visual Design

- **Color:** Monochrome stone/ink neutral surfaces with a single calm **Slate Blue** accent, meeting WCAG 2.2 AA contrast in both Light and Dark themes.
- **Density:** Calm, spacious, minimal journaling aesthetic with generous whitespace and 48dp minimum touch targets.
- **Typography:** Platform system sans-serif (`Roboto` / native system font with full Georgian script support), dynamic font scaling tested up to 200%, and localization-ready `strings.xml`.

## Documentation

- [ADR 0001: Pure Native Android & GrapheneOS-First](docs/adr/0001-pure-native-android-grapheneos-first.md)
- [ADR 0002: Storage & Cryptography Architecture](docs/adr/0002-storage-and-cryptography-architecture.md)
- [ADR 0003: Slate Blue Design System & Accessibility](docs/adr/0003-design-system-slate-blue.md)
- [ADR 0004: Reproducible Builds, Signing Keys & Distribution](docs/adr/0004-reproducible-builds-signing-and-distribution.md)
- [Threat Model](docs/security/threat-model.md)
- [OWASP MASVS v2.1.0 Control Matrix](docs/security/masvs-v2.1-matrix.md)
- [Mobile Browser Workflow Guide](docs/guides/mobile-browser-workflow.md)
- [Browser-Only Signing Key Management](docs/guides/signing-keys-browser-only.md)

## License

Licensed under the **GNU General Public License v3.0 or later** (`GPL-3.0-or-later`). See [LICENSE](LICENSE).
