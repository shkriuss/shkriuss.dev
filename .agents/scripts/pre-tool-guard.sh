#!/usr/bin/env bash
# Deterministic safety guard for agent shell commands.
# Reads tool input JSON from stdin (if provided) and blocks destructive git/fs commands.
set -euo pipefail

INPUT="$(cat 2>/dev/null || true)"

# Match destructive patterns conservatively on command strings.
if printf '%s' "$INPUT" | grep -Eq '(rm[[:space:]]+-[a-zA-Z]*r[a-zA-Z]*f[[:space:]]+(/|\.|~|\$HOME|\*)|git[[:space:]]+push[[:space:]]+.*(--force|-f)([[:space:]]|$)|git[[:space:]]+push[[:space:]]+origin[[:space:]]+main([[:space:]]|$)|git[[:space:]]+reset[[:space:]]+--hard[[:space:]]+origin/main|sudo[[:space:]]+)'; then
  printf '{"decision":"block","reason":"Blocked by shkriuss pre-tool-guard.sh: destructive command, sudo, force-push, or direct push to main is forbidden."}\n' >&2
  exit 2
fi

exit 0
