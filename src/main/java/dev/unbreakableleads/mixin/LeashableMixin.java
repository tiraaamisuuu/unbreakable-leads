package dev.unbreakableleads.mixin;

import dev.unbreakableleads.LeashProtection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Alters only vanilla's centralized leash tick. Redirects preserve the original method structure while
 * bypassing its distance and temporary-unload break paths for configured attachment types.
 */
@Mixin(Leashable.class)
public interface LeashableMixin {
    @Inject(method = "tickLeash", at = @At("HEAD"))
    private static void unbreakableLeads$prepareTick(final ServerLevel level, final Entity entity, final CallbackInfo ci) {
        LeashProtection.beforeLeashTick(level, entity);
    }

    @Inject(method = "restoreLeashFromSave", at = @At("HEAD"), cancellable = true)
    private static void unbreakableLeads$restoreWithoutTimeout(
        final Entity entity,
        final Leashable.LeashData data,
        final CallbackInfo ci
    ) {
        if (entity.level() instanceof ServerLevel level && LeashProtection.restoreDelayed(level, entity, data)) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "tickLeash",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;leashSnapDistance()D")
    )
    private static double unbreakableLeads$removeSnapDistance(final Leashable leashable) {
        return LeashProtection.isProtected(leashable) ? Double.MAX_VALUE : leashable.leashSnapDistance();
    }

    @Redirect(
        method = "tickLeash",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;dropLeash()V")
    )
    private static void unbreakableLeads$keepAcrossUnloadWithDrops(final Leashable leashable) {
        if (!LeashProtection.shouldPreventUnavailableBreak(leashable)) {
            leashable.dropLeash();
        }
    }

    @Redirect(
        method = "tickLeash",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;removeLeash()V")
    )
    private static void unbreakableLeads$keepAcrossUnloadWithoutDrops(final Leashable leashable) {
        if (!LeashProtection.shouldPreventUnavailableBreak(leashable)) {
            leashable.removeLeash();
        }
    }

    @Redirect(
        method = "tickLeash",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;whenLeashedTo(Lnet/minecraft/world/entity/Entity;)V")
    )
    private static void unbreakableLeads$catchUp(final Leashable leashable, final Entity holder) {
        LeashProtection.onLeashedTick(leashable, holder);
    }
}
