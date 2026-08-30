package dev.unbreakableleads.compat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class LeashProtectionCompat {
    private LeashProtectionCompat() {
    }

    public static boolean protects(final Leashable leashable) {
        final Entity holder = leashable.getLeashHolder();
        return holder instanceof PlayerEntity || holder instanceof LeashKnotEntity;
    }
}
