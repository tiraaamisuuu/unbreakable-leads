package dev.unbreakableleads.compat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

public final class LeashProtectionCompat {
    public static boolean protects(final MobEntity mob) {
        Entity holder = mob.getHoldingEntity();
        return holder instanceof PlayerEntity || holder instanceof net.minecraft.entity.decoration.LeashKnotEntity;
    }

    public static boolean hasProtectedConnections(final Entity holder) {
        Box box = holder.getBoundingBox().expand(32.0);
        return !holder.getWorld().getOtherEntities(holder, box,
            entity -> entity instanceof MobEntity mob && mob.getHoldingEntity() == holder && protects(mob)).isEmpty();
    }

    private LeashProtectionCompat() {
    }
}
