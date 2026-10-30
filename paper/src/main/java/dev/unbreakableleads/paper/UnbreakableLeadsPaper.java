package dev.unbreakableleads.paper;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public final class UnbreakableLeadsPaper extends JavaPlugin implements Listener {
    private PaperConfig config;

    @Override
    public void onEnable() {
        this.config = PaperConfig.load(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Enabled for Paper 26.3; automatic leash distance breaks are protected server-side.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityUnleash(final EntityUnleashEvent event) {
        final Entity holder = leashHolder(event.getEntity());
        if (!isProtectedHolder(holder)) {
            return;
        }

        if (event.getReason() == EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH
            && config.intentionalDetachmentAllowed()) {
            return;
        }

        if (event.getReason() == EntityUnleashEvent.UnleashReason.HOLDER_GONE
            || event.getReason() == EntityUnleashEvent.UnleashReason.LEASHED_GONE) {
            return;
        }

        event.setCancelled(true);
        if (config.suppressBreakSoundAndDrop()) {
            event.setDropLeash(false);
        }

        if (event.getReason() == EntityUnleashEvent.UnleashReason.DISTANCE) {
            catchUp(event.getEntity(), holder);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerShearEntity(final PlayerShearEntityEvent event) {
        if (config.intentionalDetachmentAllowed()) {
            return;
        }
        final Entity holder = leashHolder(event.getEntity());
        if (isProtectedHolder(holder)) {
            event.setCancelled(true);
        }
    }

    private Entity leashHolder(final Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isLeashed()) {
            return null;
        }
        try {
            return living.getLeashHolder();
        } catch (final IllegalStateException ignored) {
            return null;
        }
    }

    private boolean isProtectedHolder(final Entity holder) {
        return holder instanceof Player && config.playerHeld()
            || holder instanceof LeashHitch && config.fenceAttachments();
    }

    private void catchUp(final Entity leashed, final Entity holder) {
        if (!config.teleportExtremeSeparation()
            || !Objects.equals(leashed.getWorld(), holder.getWorld())) {
            return;
        }

        final double thresholdSquared = config.catchUpDistance() * config.catchUpDistance();
        if (leashed.getLocation().distanceSquared(holder.getLocation()) < thresholdSquared) {
            return;
        }

        Entity target = leashed;
        while (target.getVehicle() != null && target.getVehicle() != holder) {
            target = target.getVehicle();
        }

        final Location holderLocation = holder.getLocation();
        Vector offset = leashed.getLocation().toVector().subtract(holderLocation.toVector());
        offset.setY(0.0);
        if (offset.lengthSquared() < 0.0001) {
            offset = holderLocation.getDirection().multiply(-1.0).setY(0.0);
        }
        if (offset.lengthSquared() < 0.0001) {
            offset = new Vector(1.0, 0.0, 0.0);
        }
        offset.normalize().multiply(2.0);

        final Location destination = holderLocation.clone().add(offset).add(0.0, 0.25, 0.0);
        target.teleport(destination);
    }
}
