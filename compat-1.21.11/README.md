# Unbreakable Leads — Minecraft 1.21.11 compatibility build

This directory builds a separate JAR for the last pre-26.x Minecraft line. It uses Yarn mappings and the legacy obfuscated runtime API; it must not be combined with the main 26.2 JAR.

## Build

From this directory, run:

```text
..\gradlew.bat build
```

Output: `build/libs/unbreakable-leads-1.21.11-1.0.0.jar`.

## Supported behavior

- Player-held and fence-knot leads do not snap at vanilla's 12-block distance.
- Elytra firework boosts and successful mob mounting do not automatically detach protected leads.
- The JAR is server-authoritative and accepts vanilla clients.

## Differences from 26.2

The 1.21.11 API does not expose the 26.2 delayed-save restoration and same-dimension teleport hooks in the same form. Therefore this compatibility build does not promise the 26.2 catch-up, temporary-unload rebind, or validated configuration system. It is tested for build and dedicated-server bootstrap; verify your exact modpack's chunk/reconnect behavior before production use.

Requirements: Minecraft 1.21.11, Fabric Loader 0.18.6+, Fabric API 0.141.3+, Java 21+.
