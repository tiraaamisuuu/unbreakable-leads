package dev.unbreakableleads.mixin;

import dev.unbreakableleads.LeashProtection;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps a successful mount operation from implicitly dropping an otherwise protected lead. */
@Mixin(Mob.class)
public abstract class MobMixin {
    @Redirect(
        method = "startRiding",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;dropLeash()V")
    )
    private void unbreakableLeads$keepLeadWhenMounting(final Mob mob) {
        if (!LeashProtection.isProtected((Leashable)mob)) {
            mob.dropLeash();
        }
    }
}
