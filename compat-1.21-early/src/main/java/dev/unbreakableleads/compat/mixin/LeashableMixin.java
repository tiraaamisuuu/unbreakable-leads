package dev.unbreakableleads.compat.mixin;

import dev.unbreakableleads.compat.LeashProtectionCompat;
import net.minecraft.entity.Leashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Leashable.class)
public interface LeashableMixin {
    @Redirect(
        method = "tickLeash",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Leashable;detachLeash()V")
    )
    private static void unbreakableLeads$keepLongLeash(final Leashable leashable) {
        if (!LeashProtectionCompat.protects(leashable)) {
            leashable.detachLeash();
        }
    }
}
