# Changelog

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
