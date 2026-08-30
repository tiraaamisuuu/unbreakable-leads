package dev.unbreakableleads.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Consumer;
import org.slf4j.Logger;

public record UnbreakableLeadsConfig(
    boolean unbreakablePlayerHeld,
    boolean unbreakableFenceAttachments,
    boolean teleportExtremeSeparation,
    double catchUpDistance,
    boolean intentionalDetachmentAllowed,
    boolean suppressBreakSoundAndItemDrop
) {
    public static final double MIN_CATCH_UP_DISTANCE = 12.0;
    public static final double MAX_CATCH_UP_DISTANCE = 512.0;
    public static final double DEFAULT_CATCH_UP_DISTANCE = 24.0;

    public static UnbreakableLeadsConfig defaults() {
        return new UnbreakableLeadsConfig(true, true, true, DEFAULT_CATCH_UP_DISTANCE, true, true);
    }

    public static UnbreakableLeadsConfig load(final Path path, final Logger logger) {
        Properties properties = new Properties();
        boolean rewrite = !Files.exists(path);

        if (!rewrite) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException | IllegalArgumentException exception) {
                logger.warn("Could not read {}; using safe defaults and replacing the malformed file", path, exception);
                backupMalformedFile(path, logger);
                properties.clear();
                rewrite = true;
            }
        }

        boolean[] invalid = {false};
        UnbreakableLeadsConfig parsed = parse(properties, message -> {
            invalid[0] = true;
            logger.warn("Config: {}", message);
        });

        if (rewrite || invalid[0]) {
            write(path, parsed, logger);
        }
        return parsed;
    }

    static UnbreakableLeadsConfig parse(final Properties properties, final Consumer<String> warning) {
        UnbreakableLeadsConfig defaults = defaults();
        return new UnbreakableLeadsConfig(
            booleanValue(properties, "unbreakable_player_held", defaults.unbreakablePlayerHeld, warning),
            booleanValue(properties, "unbreakable_fence_attachments", defaults.unbreakableFenceAttachments, warning),
            booleanValue(properties, "teleport_extreme_separation", defaults.teleportExtremeSeparation, warning),
            doubleValue(properties, "catch_up_distance", defaults.catchUpDistance, MIN_CATCH_UP_DISTANCE, MAX_CATCH_UP_DISTANCE, warning),
            booleanValue(properties, "intentional_detachment_allowed", defaults.intentionalDetachmentAllowed, warning),
            booleanValue(properties, "suppress_break_sound_and_item_drop", defaults.suppressBreakSoundAndItemDrop, warning)
        );
    }

    private static boolean booleanValue(
        final Properties properties,
        final String key,
        final boolean fallback,
        final Consumer<String> warning
    ) {
        String value = properties.getProperty(key);
        if (value == null) {
            return fallback;
        }
        return switch (value.strip().toLowerCase(Locale.ROOT)) {
            case "true" -> true;
            case "false" -> false;
            default -> {
                warning.accept("'" + key + "' must be true or false; using " + fallback);
                yield fallback;
            }
        };
    }

    private static double doubleValue(
        final Properties properties,
        final String key,
        final double fallback,
        final double minimum,
        final double maximum,
        final Consumer<String> warning
    ) {
        String value = properties.getProperty(key);
        if (value == null) {
            return fallback;
        }
        try {
            double parsed = Double.parseDouble(value.strip());
            if (!Double.isFinite(parsed) || parsed < minimum || parsed > maximum) {
                throw new NumberFormatException("outside range");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            warning.accept("'" + key + "' must be a number from " + minimum + " to " + maximum + "; using " + fallback);
            return fallback;
        }
    }

    private static void backupMalformedFile(final Path path, final Logger logger) {
        try {
            Files.move(path, path.resolveSibling(path.getFileName() + ".malformed"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            logger.warn("Could not preserve malformed config {}", path, exception);
        }
    }

    private static void write(final Path path, final UnbreakableLeadsConfig config, final Logger logger) {
        String text = """
            # Unbreakable Leads configuration
            # Changes take effect after restarting Minecraft/the server.

            # Protect leads whose holder is a player.
            unbreakable_player_held=%s

            # Protect leads whose holder is a fence lead knot.
            unbreakable_fence_attachments=%s

            # Teleport the leashed entity (or its vehicle) near its holder at extreme range.
            teleport_extreme_separation=%s

            # Extreme range in blocks. Valid range: %.1f to %.1f.
            catch_up_distance=%.1f

            # Allow players and dispensers to deliberately detach or shear protected leads.
            intentional_detachment_allowed=%s

            # Suppress cosmetic break sound and lead drops for prevented automatic breaks.
            # Lead items are never duplicated even when this is false; false permits one sound on catch-up.
            suppress_break_sound_and_item_drop=%s
            """.formatted(
                config.unbreakablePlayerHeld,
                config.unbreakableFenceAttachments,
                config.teleportExtremeSeparation,
                MIN_CATCH_UP_DISTANCE,
                MAX_CATCH_UP_DISTANCE,
                config.catchUpDistance,
                config.intentionalDetachmentAllowed,
                config.suppressBreakSoundAndItemDrop
            );
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, text, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            logger.error("Could not write {}; continuing with in-memory configuration", path, exception);
        }
    }
}
