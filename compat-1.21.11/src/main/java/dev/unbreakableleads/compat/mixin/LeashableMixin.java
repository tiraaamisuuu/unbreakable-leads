package dev.unbreakableleads.compat.mixin;

import dev.unbreakableleads.compat.LeashProtectionCompat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** 1.21.11 equivalent of the 26.2 centralized leash-tick snap-distance guard. */
@Mixin(Leashable.class)
public interface LeashableMixin {
    @Redirect(
        method = "tickLeash",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Leashable;getLeashSnappingDistance()D")
    )
    private static double unbreakableLeads$unlimitedDistance(final Leashable leashable) {
        return LeashProtectionCompat.protects(leashable) ? Double.MAX_VALUE : leashable.getLeashSnappingDistance();
    }

    @Redirect(
        method = "tickLeash",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Leashable;detachLeash()V")
    )
    private static void unbreakableLeads$keepUnavailable(final Leashable leashable) {
        if (!LeashProtectionCompat.protects(leashable)) {
            leashable.detachLeash();
        }
    }
}
