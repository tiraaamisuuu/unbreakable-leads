# Releasing Unbreakable Leads

Use Java 25 and the Gradle wrapper to build both editions:

```sh
./gradlew build
./gradlew -p paper build
```

Update `mod_version` in `gradle.properties`, `plugin_version` in `paper/gradle.properties`, the changelog, README filenames, and `releases/<version>-mc26.3.md` before tagging.

The Fabric mod uses Minecraft internals: a successful compile is not sufficient to validate a version port. Start a disposable dedicated server with the built jar, matching Loader and Fabric API, and verify every configured Mixin loads. Smoke-test Paper separately. Do not use a production world for these checks.

`scripts/smoke_server.py` can stop a prepared disposable server once it reaches the ready state and verify the initialization marker. Set up its `eula.txt`, server properties, matching loader, dependencies, and mod/plugin directory first, then run:

```sh
python scripts/smoke_server.py --java /path/to/java --jar /path/to/fabric-server-launch.jar --directory /path/to/test-server --marker "Unbreakable Leads 1.1.1 initialized"
```

For Paper, use its server jar and the marker `Enabled for Paper 26.3`. Review `smoke-output.log` in the test directory and the manual gameplay checklist in the README.

Commit and push the verified changes, then push an annotated tag:

```sh
git tag -a v1.1.1-mc26.3 -m "Unbreakable Leads 1.1.1 for Minecraft 26.3"
git push origin v1.1.1-mc26.3
```

The release workflow builds and tests both editions, creates `SHA256SUMS.txt`, and publishes the two jars with the release notes. Verify the published downloads match their checksums. Never move a published tag to different code.

On Modrinth, upload the Fabric jar to the Fabric mod project with Minecraft **26.3**, loader **Fabric**, version **1.1.1**, and type **Release**. Add Fabric API as a required dependency. Use a separate Paper project for the Paper jar; the two files use different loaders and installation directories.
