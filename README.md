# Unbreakable Leads

Unbreakable Leads is an open-source Fabric mod for Minecraft Java Edition **26.3**, with a separate Paper plugin for Paper 26.3 servers. It protects configured lead attachments against automatic breaking from distance and movement. The Fabric edition also covers elytra firework boosts, mounting, and ordinary chunk unload/reload cycles.

Download [version 1.1.0 for Minecraft 26.3](https://github.com/tiraaamisuuu/unbreakable-leads/releases/tag/v1.1.0-mc26.3). Choose the Fabric jar for a Fabric instance or the Paper jar for a Paper server.

## Requirements

- Minecraft Java Edition 26.3
- Fabric Loader 0.20.1 or newer
- Java 25
- Fabric API 0.165.2+26.3 or newer for Minecraft 26.3

Install the mod on the integrated client/server for single-player. On a dedicated server it is server-authoritative; vanilla or otherwise unmodded clients can join. No client installation is required unless a future release adds client presentation features.

## Installation

1. Install Fabric Loader for 26.3 and Java 25.
2. Put `unbreakable-leads-26.3-1.1.0.jar` and the matching Fabric API jar in the server (or client) `mods` directory.
3. Start once. The configuration is created at `config/unbreakable-leads.properties`.
4. Restart after changing configuration.

Build with `./gradlew build` (Windows: `gradlew.bat build`). The distributable jar is written to `build/libs/`.

### Paper 26.3

Paper servers cannot load Fabric mod JARs. A separate server-only Paper plugin is provided in `paper/`, targeting **Paper 26.3 build 240** and **Java 25**.

1. Download `unbreakable-leads-paper-26.3-1.1.0.jar` from the 1.1.0 GitHub release.
2. Put it in the Paper server's `plugins/` directory, not `mods/`.
3. Start the server once to create `plugins/UnbreakableLeads/config.yml`.

The Paper port cancels automatic distance/unknown detach events, supports player and fence-hitch holders, suppresses automatic lead drops, optionally catches up extremely separated entities, and preserves deliberate removal by default. It uses Paper's supported event/entity API and does not tick every entity globally. Paper documents that cancellation cannot preserve a leash through a dimension change, so cross-dimension attachment is not promised.

Build it with `gradlew.bat -p paper build` on Windows or `./gradlew -p paper build` elsewhere. Its JAR is written to `paper/build/libs/`.

### Compatibility JARs

The Fabric 26.3 jar is specific to Minecraft 26.3. Minecraft 26.2 still uses the [Fabric 1.0.0 release](https://github.com/tiraaamisuuu/unbreakable-leads/releases/tag/v1.0.0). Every supported 1.20.x and 1.21.x release has its own JAR and GitHub release asset, with exact Minecraft/Fabric API metadata for Modrinth:

- **1.20.0–1.20.6:** `compat-legacy/` (Java 17 for 1.20–1.20.4; Java 21 for 1.20.5–1.20.6).
- **1.21.0–1.21.3:** `compat-1.21-early/` (Java 21).
- **1.21.4–1.21.5:** `compat-1.21-mid/` (Java 21).
- **1.21.6–1.21.11:** `compat-1.21.11/` (Java 21).

Do not install more than one Unbreakable Leads JAR in the same instance. Versions 1.19.4 through 1.16.5 are not included; their leash internals require a separate port and are not being represented as compatible until independently verified.

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

Minecraft 26.3 centralizes leash simulation in `Leashable.tickLeash`. Small, documented Mixins at that boundary replace the finite snap distance for configured holders, retain delayed attachments while a holder is temporarily unloaded, rebind holders after reload, and perform same-dimension catch-up. Additional redirects cover the elytra-firework and mob-mount automatic drop paths. No global entity scan or per-world attachment registry is introduced; scans occur only during a teleport, shear, or firework action.

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
- Dedicated Fabric 26.3 server bootstrap with Loader 0.20.1, Fabric API 0.165.2+26.3, and all six Mixins applied
- Dedicated Paper 26.3 build 240 server bootstrap with the plugin enabled
- Both servers reach the ready state and shut down cleanly; gameplay scenarios below are the manual checklist

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

The project uses Fabric Loom 1.17.20, Gradle 9.7.1, Mojang's unobfuscated 26.3 names, and Java release 25. Generated `build/`, `run/`, and local configuration are ignored by Git.

GitHub Actions builds both editions on pushes and pull requests. See [RELEASING.md](RELEASING.md) for publishing and server smoke checks.

## License

MIT. See [LICENSE](LICENSE).
