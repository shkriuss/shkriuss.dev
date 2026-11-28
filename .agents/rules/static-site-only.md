---
trigger: glob
globs:
  - "site/**"
description: Keep shkriuss.dev strictly a plain static HTML/CSS hub with zero web-app functionality.
---

# Rule: Static Hub Site Only (`site/**`)

1. `site/` is strictly a static documentation, app catalog, APK download, and signing-fingerprint verification hub for `shkriuss.dev`.
2. Use semantic HTML5 and plain CSS3 only.
3. NEVER add JavaScript frameworks (React, Vue, Svelte, Next.js, Vite apps), client-side application state, external CDNs, Google Fonts, trackers, analytics, or interactive web-app functionality.
4. Every page must render cleanly with JavaScript completely disabled in Vanadium on both phone and tablet viewports, and match the Slate Blue monochrome aesthetic of the Android apps.
5. Keep strict security headers configured for Cloudflare (`Content-Security-Policy`, `X-Content-Type-Options`, `Referrer-Policy`, `Permissions-Policy`).
