# Changelog

## 1.1.1 — 2026-11-04

- Fixes the Gradle launcher's executable flag for Linux build and release runners.
- Publishes the Minecraft 26.3 Fabric and Paper update after the 1.1.0 release workflow failed before publishing any files.

## 1.1.0 — 2026-11-04

- Ports the main Fabric release from Minecraft 26.2 to 26.3, with Fabric Loader 0.20.1 and Fabric API 0.165.2+26.3.
- Rebuilds the Paper edition against Paper 26.3 build 240.
- Verifies all six Fabric Mixins on a dedicated 26.3 server; both editions reach the ready state and shut down cleanly.
- Keeps existing configuration keys and defaults and retains separate older Fabric releases.
- Updates Gradle to 9.7.1 with distribution checksum verification, fixes project links in Fabric metadata, and adds CI/release workflows and release checksums.

## Paper 1.0.0 — 2026-10-30

- Adds a separate server-only plugin for Paper 26.3 build 231 and Java 25.
- Cancels configured automatic distance and unknown leash detach events through the supported Paper API.
- Preserves intentional player detachment and natural holder/entity removal by default.
- Adds validated Paper YAML configuration and optional extreme-distance catch-up.
- Verified on a real Paper 26.3-231 server startup through clean shutdown.

## 1.0.0 — 2026-08-30

- Initial Minecraft 26.2 Fabric release.
- Prevents configured player/fence leads from automatic distance and movement snaps.
- Covers elytra firework boosts and mob mounting.
- Preserves delayed UUID and fence-knot attachments through chunk unload/reload and restarts.
- Adds same-dimension safe catch-up, validated properties configuration, tests, and dedicated-server documentation.
