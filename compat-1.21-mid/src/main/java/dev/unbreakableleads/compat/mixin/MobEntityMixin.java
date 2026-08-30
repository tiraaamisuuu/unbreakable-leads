package dev.unbreakableleads.compat.mixin;

import dev.unbreakableleads.compat.LeashProtectionCompat;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
    @Redirect(
        method = "startRiding",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;detachLeash()V")
    )
    private void unbreakableLeads$keepLeadWhenMounting(final MobEntity mob) {
        if (!LeashProtectionCompat.protects((Leashable) mob)) {
            mob.detachLeash();
        }
    }
}
