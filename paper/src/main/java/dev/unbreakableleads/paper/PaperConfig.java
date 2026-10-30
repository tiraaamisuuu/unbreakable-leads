package dev.unbreakableleads.paper;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

record PaperConfig(
    boolean playerHeld,
    boolean fenceAttachments,
    boolean teleportExtremeSeparation,
    double catchUpDistance,
    boolean intentionalDetachmentAllowed,
    boolean suppressBreakSoundAndDrop
) {
    private static final double DEFAULT_CATCH_UP_DISTANCE = 24.0;
    private static final double MIN_CATCH_UP_DISTANCE = 12.0;
    private static final double MAX_CATCH_UP_DISTANCE = 512.0;

    static PaperConfig load(final JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        final FileConfiguration config = plugin.getConfig();
        double catchUpDistance = config.getDouble("catch-up-distance", DEFAULT_CATCH_UP_DISTANCE);

        if (!Double.isFinite(catchUpDistance)
            || catchUpDistance < MIN_CATCH_UP_DISTANCE
            || catchUpDistance > MAX_CATCH_UP_DISTANCE) {
            plugin.getLogger().warning("Invalid catch-up-distance; using " + DEFAULT_CATCH_UP_DISTANCE + '.');
            catchUpDistance = DEFAULT_CATCH_UP_DISTANCE;
            config.set("catch-up-distance", catchUpDistance);
            plugin.saveConfig();
        }

        return new PaperConfig(
            config.getBoolean("unbreakable-player-held", true),
            config.getBoolean("unbreakable-fence-attachments", true),
            config.getBoolean("teleport-extreme-separation", true),
            catchUpDistance,
            config.getBoolean("intentional-detachment-allowed", true),
            config.getBoolean("suppress-break-sound-and-drop", true)
        );
    }
}
