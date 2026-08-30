package dev.unbreakableleads.compat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class LeashProtectionCompat {
    public static boolean protects(final Leashable leashable) {
        Entity holder = leashable.getLeashHolder();
        return holder instanceof PlayerEntity || holder instanceof LeashKnotEntity;
    }

    public static boolean protects(final Entity entity) {
        return entity instanceof Leashable leashable && protects(leashable);
    }

    public static boolean hasProtectedConnections(final Entity holder) {
        return Leashable.collectLeashablesHeldBy(holder).stream().anyMatch(LeashProtectionCompat::protects);
    }

    private LeashProtectionCompat() {
    }
}
