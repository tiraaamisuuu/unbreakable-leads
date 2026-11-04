# Unbreakable Leads for Paper

This is the Paper plugin port of Unbreakable Leads. Version 1.1.2 targets Paper 26.3 build 240 and Java 25. It is separate from the Fabric mod and must be placed in the Paper server's `plugins/` directory.

The plugin cancels automatic distance and unknown leash detach events for configured player and fence holders. It preserves removal when the holder or leashed entity disappears, and deliberate player detachment remains allowed by default. It does not tick every entity.

Build from this directory with `..\gradlew.bat build` on Windows or `../gradlew build` elsewhere. The distributable JAR is written to `build/libs/unbreakable-leads-paper-26.3-1.1.2.jar`.

Version 1.1.2 was verified on a dedicated Paper 26.3-240 server through enable, the ready state, and clean shutdown.

Cross-dimension leash preservation is not promised because Paper documents that cancelling an unleash caused by a dimension change has no effect. Same-world distance and teleport separation are handled by cancelling the detach and optionally moving the leashed entity near its holder.
