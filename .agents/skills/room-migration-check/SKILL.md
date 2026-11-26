---
name: room-migration-check
description: Export Room database schemas and verify every schema upgrade with MigrationTestHelper tests.
---

# Skill: Room Schema & Migration Verification

Use this skill whenever creating or modifying a Room `@Database` entity or version.

## Required Steps

1. **Schema Export:** Configure `room { schemaDirectory("$projectDir/schemas") }` using the `androidx.room` Gradle plugin and commit every exported `<version>.json` schema file to git.
2. **Explicit Migrations Only:** NEVER enable `fallbackToDestructiveMigration()` in production code. Every schema bump MUST provide a tested `Migration(from, to)` or verified `@AutoMigration`.
3. **Migration Tests:** Add a `MigrationTestHelper` test verifying:
   - Schema creation at the new version.
   - Migration from every previous released version (`1 -> 2 -> ... -> N`), asserting that existing encrypted rows remain intact and decrypt cleanly after migration.
4. **Encrypted Payload Integrity:** Remember that sensitive user fields are AEAD-encrypted blobs via `:core:crypto` (ADR 0002). Verify AAD schema-version compatibility when migrating rows.
