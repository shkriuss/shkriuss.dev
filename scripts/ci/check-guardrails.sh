#!/usr/bin/env bash
# Enforces non-negotiable shkriuss.dev platform, GrapheneOS hardening, and supply-chain guardrails.
set -euo pipefail

fail() {
  echo "::error::$1"
  exit 1
}

existing_dirs() {
  local result=()
  for dir in "$@"; do
    if [ -d "$dir" ]; then
      result+=("$dir")
    fi
  done
  printf '%s\n' "${result[@]}"
}

mapfile -t ANDROID_BUILD_DIRS < <(existing_dirs gradle build-logic core apps)
mapfile -t ANDROID_SRC_DIRS < <(existing_dirs core apps)

echo "Checking for forbidden proprietary Google / WebView / deprecated crypto dependencies..."
FORBIDDEN_DEPS='com\.google\.android\.gms|com\.google\.firebase|com\.google\.android\.play|androidx\.security:security-crypto|androidx\.webkit|androidx\.web:|net\.zetetic:sqlcipher|com\.google\.dagger:hilt|io\.insert-koin'
if [ "${#ANDROID_BUILD_DIRS[@]}" -gt 0 ] && grep -rnE "$FORBIDDEN_DEPS" "${ANDROID_BUILD_DIRS[@]}"; then
  fail "Forbidden dependency detected (GMS, Firebase, Play, WebView, deprecated security-crypto, SQLCipher, or DI framework)."
fi

echo "Checking for forbidden org.jetbrains.kotlin.android plugin (AGP 9.5+ built-in Kotlin required)..."
PLUGIN_CHECK_PATHS=("build.gradle.kts" "gradle/libs.versions.toml" "${ANDROID_BUILD_DIRS[@]}")
if grep -rnE 'org\.jetbrains\.kotlin\.android|kotlin\("android"\)' "${PLUGIN_CHECK_PATHS[@]}"; then
  fail "Forbidden kotlin-android plugin detected; AGP 9.5+ built-in Kotlin must be used."
fi

echo "Checking for unapproved native C/C++ (.so / NDK) artifacts..."
if [ "${#ANDROID_SRC_DIRS[@]}" -gt 0 ]; then
  if find "${ANDROID_SRC_DIRS[@]}" -type f \( -name '*.so' -o -name 'CMakeLists.txt' -o -name 'Android.mk' \) | grep -q .; then
    fail "Unapproved native binary (.so) or C/C++ build file detected. Native code requires an accepted ADR."
  fi
fi
if [ "${#ANDROID_BUILD_DIRS[@]}" -gt 0 ] && grep -rnE 'useLegacyPackaging[[:space:]]*=[[:space:]]*true' "${ANDROID_BUILD_DIRS[@]}"; then
  fail "useLegacyPackaging = true is forbidden; keep extractNativeLibs = false."
fi

echo "Checking Android source code for WebView, dynamic code loading, subprocess execution, or deprecated crypto..."
FORBIDDEN_CODE='android\.webkit\.WebView|DexClassLoader|BaseDexClassLoader|DelegateLastClassLoader|InMemoryDexClassLoader|PathClassLoader|System\.load\(|System\.loadLibrary\(|ProcessBuilder|Runtime\.getRuntime\(\)\.exec|EncryptedSharedPreferences|EncryptedFile|androidx\.security\.crypto\.MasterKey'
if [ "${#ANDROID_SRC_DIRS[@]}" -gt 0 ] && grep -rnE "$FORBIDDEN_CODE" "${ANDROID_SRC_DIRS[@]}"; then
  fail "Forbidden API usage detected in Android source (WebView, DCL, native load, ProcessBuilder/exec, or deprecated security-crypto)."
fi

echo "Checking AndroidManifest files for broad storage permissions and insecure flags..."
FORBIDDEN_MANIFEST='READ_EXTERNAL_STORAGE|WRITE_EXTERNAL_STORAGE|MANAGE_EXTERNAL_STORAGE|READ_MEDIA_IMAGES|READ_MEDIA_VIDEO|READ_MEDIA_AUDIO|allowBackup="true"|usesCleartextTraffic="true"|debuggable="true"|extractNativeLibs="true"'
if [ "${#ANDROID_SRC_DIRS[@]}" -gt 0 ]; then
  while IFS= read -r manifest; do
    if grep -nE "$FORBIDDEN_MANIFEST" "$manifest"; then
      fail "Forbidden permission or insecure manifest flag detected in $manifest."
    fi
  done < <(find "${ANDROID_SRC_DIRS[@]}" -name 'AndroidManifest.xml' -type f)
fi

echo "Checking static site (site/public/) for forbidden JavaScript..."
if find site/public -type f \( -name '*.js' -o -name '*.mjs' -o -name '*.ts' \) | grep -q .; then
  fail "JavaScript files are forbidden in site/public/. Keep shkriuss.dev plain static HTML/CSS only."
fi
if grep -rniE '<script\b|javascript:' site/public/; then
  fail "Inline or external <script> detected in site/public/."
fi

echo "Checking GitHub Actions workflows for full 40-char SHA pinning..."
while IFS= read -r line; do
  if printf '%s' "$line" | grep -Eq 'uses:[[:space:]]*\./'; then
    continue
  fi
  if ! printf '%s' "$line" | grep -Eq 'uses:[[:space:]]*[A-Za-z0-9._-]+/[A-Za-z0-9._/-]+@[0-9a-f]{40}([[:space:]]|$|#)'; then
    fail "Unpinned GitHub Action detected: $line"
  fi
done < <(grep -rnE '^[[:space:]]*-?[[:space:]]*uses:[[:space:]]*' .github/workflows/ || true)

echo "All shkriuss.dev guardrails passed."
