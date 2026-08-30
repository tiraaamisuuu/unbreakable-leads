# Unbreakable Leads

Unbreakable Leads is an open-source Fabric mod for Minecraft Java Edition **26.2**. It makes configured lead attachments server-authoritatively survive vanilla automatic snap conditions: long distance, fast movement, elytra firework boosts, mounting, knockback, falling, lag spikes, and ordinary chunk unload/reload cycles.

## Requirements

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3 or newer (verified with 0.19.5)
- Java 25
- Fabric API 0.150.1+26.2 or newer (release build: 0.158.0+26.2)

Install the mod on the integrated client/server for single-player. On a dedicated server it is server-authoritative; vanilla or otherwise unmodded clients can join. No client installation is required unless a future release adds client presentation features.

## Installation

1. Install Fabric Loader for 26.2 and Java 25.
2. Put `unbreakable-leads-1.0.0.jar` and the matching Fabric API jar in the server (or client) `mods` directory.
3. Start once. The configuration is created at `config/unbreakable-leads.properties`.
4. Restart after changing configuration.

Build with `./gradlew build` (Windows: `gradlew.bat build`). The distributable jar is written to `build/libs/`.

### Older Minecraft versions

The main JAR is **not** compatible with older Minecraft versions: 26.1+ uses Mojang's unobfuscated names and a different leash API. A separate compatibility project and JAR is included for **Minecraft 1.21.11** (Fabric Loader 0.18.6, Fabric API 0.141.6+1.21.11). Build it with `gradlew.bat build` from `compat-1.21.11/`; the output is `compat-1.21.11/build/libs/unbreakable-leads-1.21.11-1.0.0.jar`.

The 1.21.11 JAR protects player/fence leads from distance snaps, elytra-firework detachment, and mob mounting. Its API-era limitations are documented in that subproject's README; do not install both JARs together.

A separate **1.20.6** build is also provided in `compat-legacy/` and in the releases. Versions 1.19.4 through 1.16.5 use still different leash internals; they are not labeled compatible until their own source and runtime builds pass verification.

## Configuration

Missing files are created with documented defaults. Malformed values are logged, replaced with safe defaults, and normalized; a malformed original is retained as `unbreakable-leads.properties.malformed` when possible.

| Option | Default | Meaning |
| --- | --- | --- |
| `unbreakable_player_held` | `true` | Protect leads whose holder is a player. |
| `unbreakable_fence_attachments` | `true` | Protect leads whose holder is a fence lead-knot entity. |
| `teleport_extreme_separation` | `true` | At extreme range, safely move the leashed entity (or its vehicle) near its holder in the same dimension. |
| `catch_up_distance` | `24.0` | Catch-up threshold in blocks; valid range is 12.0–512.0. |
| `intentional_detachment_allowed` | `true` | Keep vanilla player shearing, right-click detachment, and dispenser shearing available. Set false to block those actions for protected leads. |
| `suppress_break_sound_and_item_drop` | `true` | Suppress cosmetic lead-break sound for prevented automatic breaks. A lead item is never spawned for a prevented break, even when false, so items cannot duplicate. |

The file is a Java-properties file for easy SSH/server-panel editing. Options are read on startup only.

## How it works

Minecraft 26.2 centralizes leash simulation in `Leashable.tickLeash`. Small, documented Mixins at that boundary replace the finite snap distance for configured holders, retain delayed attachments while a holder is temporarily unloaded, rebind holders after reload, and perform same-dimension catch-up. Additional redirects cover the elytra-firework and mob-mount automatic drop paths. No global entity scan or per-world attachment registry is introduced; scans occur only during a teleport, shear, or firework action.

Intentional player actions remain distinct from automatic breaks. Entity death, permanent removal, impossible dimension changes, and invalid fence knots may end a relationship. Vanilla `LeashData` remains compatible, so removing the mod leaves worlds loadable and returns affected leads to vanilla behavior.

## Commands

None. Configuration is file-based and the mod adds no permissions or network packets.

## Compatibility and limitations

- Works in integrated single-player and on dedicated Fabric servers.
- Vanilla clients can connect to a dedicated server because no custom payload or client renderer is required.
- Same-dimension command/plugin teleports are caught up safely. Cross-dimension travel is not forced because retaining a reference across dimensions can duplicate or strand entities.
- Mods that replace `Leashable.tickLeash`, directly call `dropLeash` for their own gameplay, or replace teleport/removal semantics may override this behavior. Such interactions fail safely and are logged only at debug level.
- The mod does not prevent intentional entity deletion, death, bucket capture, conversion rules that remove an entity, or destruction of a fence knot when its fence is removed.
- Catch-up chooses a nearby non-colliding position; if no candidate is available it uses the holder position.

## Verification matrix

Automated checks:

- Java 25 compilation and Gradle packaging
- JUnit validation of defaults, accepted values, malformed values, and range recovery
- Dedicated Fabric 26.2 server bootstrap with all Mixins applied

Manual release checklist:

| Scenario | Expected result |
| --- | --- |
| Player-held lead beyond 12 blocks | Attachment remains; no break sound/item. |
| Fast horse/boat, knockback, falling, lag | Attachment remains. |
| Elytra flight with firework boost | Attachment remains; no automatic drop. |
| Cross a chunk boundary; unload/reload chunk | Attachment and knot/UUID restore. |
| Reconnect or restart server | Attachment restores when holder/chunk is available. |
| Same-dimension `/tp` | Leashed entity catches up safely when configured. |
| Nether/end portal | Vanilla-safe dimension behavior; limitation above applies. |
| Entity death/removal | Relationship ends naturally; no orphan entity. |
| Right-click detach, shears, dispenser | Allowed by default; blocked when intentional detachment is false. |
| Fence knot with multiple entities | Relationships remain independent; no duplicate knot or lead. |

## Development

```text
gradlew.bat build
```

The project uses Fabric Loom 1.17.20, Gradle 9.5.1, Mojang's unobfuscated 26.2 names, and Java release 25. Generated `build/`, `run/`, and local configuration are ignored by Git.

## License

MIT. See [LICENSE](LICENSE).
