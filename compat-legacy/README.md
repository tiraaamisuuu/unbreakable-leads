# Unbreakable Leads legacy compatibility builds

This project builds the legacy Yarn-mapped JAR for Minecraft 1.20.6. It protects the vanilla `PathAwareEntity` leash snap path without requiring a client mod. Build with `..\gradlew.bat build`; output is `build/libs/unbreakable-leads-1.20.6-1.0.0.jar`.

The 1.20.6 API stores leash behavior directly on `MobEntity`, unlike 1.21.11's `Leashable` API. This build is intentionally separate from both the 26.2 and 1.21.11 JARs.
